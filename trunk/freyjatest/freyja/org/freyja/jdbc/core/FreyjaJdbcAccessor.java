package org.freyja.jdbc.core;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.util.ClassLoaderUtil;

import org.apache.commons.beanutils.ConvertUtils;
import org.freyja.cache.CacheOperations;
import org.freyja.jdbc.object.BeanInfo;
import org.freyja.jdbc.object.BeanInfoCache;
import org.freyja.jdbc.object.DateConvert;
import org.freyja.jdbc.object.HqlMapping;
import org.freyja.sql.SqlLog;
import org.freyja.sql.SqlParser;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.util.ClassUtils;
import org.xidea.el.ExpressionFactory;
import org.xidea.el.impl.ExpressionFactoryImpl;

public abstract class FreyjaJdbcAccessor extends JdbcDaoSupport {
	public final static List EMPTY_LIST = new ArrayList();

	public final static int QuertByMapRowMapper = 1;
	public final static int QuertByObjectRowMapper = 0;

	public final static Map<Integer, Map<String, Object>> sysMap = new HashMap<Integer, Map<String, Object>>();

	static {
		ConvertUtils.register(new DateConvert("yyyy-MM-dd HH:mm:ss"),
				Date.class);
		ConvertUtils.register(new DateConvert("yyyy-MM-dd"), Date.class);
	}

	protected final static ExpressionFactory factory = ExpressionFactoryImpl
			.getInstance();
	protected CacheManager cacheManager;
	protected CacheOperations cacheOperation;

	protected boolean useQueryCache;

	// protected FreyjaTransactionManager transactionManager;

	// protected void add(String entityName, Object id, Object value) {
	//
	// JdbcTransactionObjectSupport dsto = (JdbcTransactionObjectSupport)
	// transactionManager
	// .doGetTransaction();
	// boolean f = transactionManager.isExistingTransaction(dsto);
	//
	// if (f) {
	// int s = dsto.getConnectionHolder().hashCode();
	// Map<String, Object> m = sysMap.get(s);
	// if (m == null) {
	// m = new HashMap<String, Object>();
	// }
	// try {
	// m.put(entityName + "#" + id, BeanUtils.cloneBean(value));
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// sysMap.put(s, m);
	//
	// }
	// }

	public CacheOperations getCacheOperation() {
		return cacheOperation;
	}

	public void setCacheOperation(CacheOperations cacheOperation) {
		this.cacheOperation = cacheOperation;
	}

	private String[] packagesToScan;
	private Properties freyjaProperties;

	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
	public static final String NET_SF_EHCACHE_CONFIGURATION_RESOURCE_NAME = "net.sf.ehcache.configurationResourceName";
	private static final String RESOURCE_PATTERN = "/**/*.class";

	private void packagesToScan(String[] packagesToScan) throws Exception {
		if (packagesToScan != null) {
			for (String pkg : packagesToScan) {
				String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
						+ ClassUtils.convertClassNameToResourcePath(pkg)
						+ RESOURCE_PATTERN;
				Resource[] resources = this.resourcePatternResolver
						.getResources(pattern);
				MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(
						this.resourcePatternResolver);

				for (Resource resource : resources) {
					if (resource.isReadable()) {
						MetadataReader reader = readerFactory
								.getMetadataReader(resource);
						String className = reader.getClassMetadata()
								.getClassName();
						if (matchesFilter(reader, readerFactory)) {
							Class<?> clazz = this.resourcePatternResolver
									.getClassLoader().loadClass(className);
							BeanInfoCache.put(clazz);
						}
					}
				}
			}
		}
	}

	private TypeFilter[] entityTypeFilters = new TypeFilter[] {
			new AnnotationTypeFilter(Entity.class, false),
			new AnnotationTypeFilter(Embeddable.class, false),
			new AnnotationTypeFilter(MappedSuperclass.class, false) };

	private boolean matchesFilter(MetadataReader reader,
			MetadataReaderFactory readerFactory) throws IOException {
		if (this.entityTypeFilters != null) {
			for (TypeFilter filter : this.entityTypeFilters) {
				if (filter.match(reader, readerFactory)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected void initDao() throws Exception {

		useQueryCache = Boolean.parseBoolean(freyjaProperties.getProperty(
				"cache.use_query_cache", "false"));

		boolean showSql = Boolean.parseBoolean(freyjaProperties.getProperty(
				"show_sql", "false"));

		boolean showHql = Boolean.parseBoolean(freyjaProperties.getProperty(
				"show_hql", "false"));

		SqlLog.showSql = showSql;
		SqlLog.showHql = showHql;
		cacheLimit = Integer.parseInt(freyjaProperties.getProperty(
				"cacheLimit", DEFAULT_CACHE_LIMIT + ""));

		int timeToIdleSeconds = Integer.parseInt(freyjaProperties.getProperty(
				"timeToIdleSeconds", "60"));

		packagesToScan(packagesToScan);

		if (Boolean.parseBoolean(freyjaProperties.getProperty(
				"cache.use_cache", "false"))) {

			String configurationResourceName = null;
			if (freyjaProperties != null) {
				configurationResourceName = freyjaProperties
						.getProperty(NET_SF_EHCACHE_CONFIGURATION_RESOURCE_NAME);
			}
			URL url;
			try {
				url = new URL(configurationResourceName);
			} catch (MalformedURLException e) {
				url = loadResource(configurationResourceName);
			}

			Configuration configuration = ConfigurationFactory
					.parseConfiguration(url);

			for (Entry<String, CacheConfiguration> entry : configuration
					.getCacheConfigurations().entrySet()) {
				CacheConfiguration cc = entry.getValue();
				BeanInfo<?> bi = BeanInfoCache.get(entry.getKey());
				if (bi == null) {
					continue;
				}
				cc.addSearchable(bi.searchable);
			}
			cacheManager = new CacheManager(configuration);

			if (Boolean.parseBoolean(freyjaProperties.getProperty(
					"cache.cache_all", "false"))) {
				for (Entry<String, BeanInfo> entry : BeanInfoCache.beanInfoMap
						.entrySet()) {
					BeanInfo<?> bi = entry.getValue();
					String cacheName = bi.clazz.getName();
					Cache classCache = cacheManager.getCache(cacheName);
					if (classCache == null) {
						CacheConfiguration c = new CacheConfiguration(
								cacheName, 0);
						c.setEternal(false);
						c.setTimeToLiveSeconds(0);
						c.setTimeToIdleSeconds(timeToIdleSeconds);
						c.setOverflowToDisk(false);
						c.addSearchable(bi.searchable);
						classCache = new Cache(c);
						cacheManager.addCache(classCache);
					}
				}
			}
			Cache classCache = cacheManager
					.getCache(CacheOperations.QUERYCACHENAME);
			if (classCache == null) {
				CacheConfiguration c = new CacheConfiguration(
						CacheOperations.QUERYCACHENAME, 0);
				c.setEternal(false);
				c.setTimeToLiveSeconds(0);
				c.setTimeToIdleSeconds(timeToIdleSeconds);
				c.setOverflowToDisk(false);
				classCache = new Cache(c);
				cacheManager.addCache(classCache);
			}

		}

		this.cacheOperation = new CacheOperations(cacheManager);

		// this.transactionManager = new
		// FreyjaTransactionManager(getDataSource());

	}

	protected static URL loadResource(String configurationResourceName) {
		ClassLoader standardClassloader = ClassLoaderUtil
				.getStandardClassLoader();
		URL url = null;
		if (standardClassloader != null) {
			url = standardClassloader.getResource(configurationResourceName);
		}
		return url;
	}

	public Properties getFreyjaProperties() {
		return freyjaProperties;
	}

	public void setFreyjaProperties(Properties freyjaProperties) {
		this.freyjaProperties = freyjaProperties;
	}

	public String[] getPackagesToScan() {
		return packagesToScan;
	}

	public void setPackagesToScan(String[] packagesToScan) {
		this.packagesToScan = packagesToScan;
	}

	protected HqlMapping getParsedSql(String hql, int type) {
		HqlMapping hm = this.parsedSqlCache.get(hql);
		if (hm == null) {
			hm = SqlParser.hqlToSql(hql);
			if (type != 3) {
				SqlParser.parserRowMapper(hm, type, getJdbcTemplate(),
						cacheManager);
			}

			hm.selectSQL = SqlParser.createSelectSql(hm);
			this.parsedSqlCache.put(hql, hm);
		}
		return hm;
	}

	public static final int DEFAULT_CACHE_LIMIT = 10000;

	private volatile int cacheLimit = DEFAULT_CACHE_LIMIT;

	private final Map<String, HqlMapping> parsedSqlCache = new LinkedHashMap<String, HqlMapping>(
			DEFAULT_CACHE_LIMIT, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, HqlMapping> eldest) {
			return size() > getCacheLimit();
		}
	};

	public int getCacheLimit() {
		return cacheLimit;
	}

	public void setCacheLimit(int cacheLimit) {
		this.cacheLimit = cacheLimit;
	}

}
