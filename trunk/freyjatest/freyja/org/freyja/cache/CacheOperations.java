package org.freyja.cache;

import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;
import org.freyja.support.MethodUtil;

public class CacheOperations {

	public static final String QUERYCACHENAME = "freyja_query_cache";

	protected static Logger log = Logger.getLogger(CacheOperations.class);

	private CacheManager cacheManager;

	public CacheOperations(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public <T> Cache getCache(Class<T> clazz) {
		if (cacheManager == null) {
			return null;
		}
		Cache clssCache = cacheManager.getCache(clazz.getName());
		return clssCache;
	}

	public <T> Cache getCache(String entryName) {
		if (cacheManager == null) {
			return null;
		}
		Cache clssCache = cacheManager.getCache(entryName);
		return clssCache;
	}

	public <T> void remove(Class<T> clazz, Object id) {
		Cache clssCache = getCache(clazz);
		if (clssCache == null) {
			return;
		}
		clssCache.remove(MethodUtil.convert(id));
	}

	public <T> void update(T t, Object id) {

		Cache cache = getCache(t.getClass().getName());
		if (cache == null) {
			return;
		}

		cache.replace(cache.getQuiet(id), new Element(id, t));
	}

	public <T> void put(T t, Object id) {
		Cache clssCache = getCache(t.getClass().getName());
		if (clssCache == null) {
			return;
		}

		clssCache.put(new Element(MethodUtil.convert(id), t));

	}

	public <T> T get(Class<T> clazz, Object id) {
		return get(clazz.getName(), id);
	}

	public <T> T get(String entryName, Object id) {

		Cache clssCache = getCache(entryName);
		if (clssCache == null) {
			return null;
		}
		Element entity = clssCache.get(MethodUtil.convert(id));
		if (entity != null) {
			return (T) entity.getObjectValue();
		}
		return null;
	}

	public Object getFormQueryCache(String key) {
		Cache clssCache = getCache(QUERYCACHENAME);
		if (clssCache == null) {
			return null;
		}

		Element entity = clssCache.get(key);
		if (entity == null) {
			return null;
		}
		return entity.getObjectValue();
	}

	public void removeQueryCache(String key) {
		Cache clssCache = getCache(QUERYCACHENAME);
		if (clssCache == null) {
			return;
		}
		clssCache.remove(key);
	}

	public void putIntoQueryCache(String key, Object obj) {
		Cache clssCache = getCache(QUERYCACHENAME);
		if (clssCache == null) {
			return;
		}
		clssCache.put(new Element(key, obj));
	}

}
