package org.freyja.jdbc.core;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.search.Query;
import net.sf.ehcache.search.Result;
import net.sf.ehcache.search.Results;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.update.Update;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.log4j.Logger;
import org.freyja.cache.CacheOperations;
import org.freyja.deparser.el.ELExpressionDeParser;
import org.freyja.deparser.eql.EQLExpressionDeParser;
import org.freyja.jdbc.core.rowMapper.BeanPropertyRowMapper;
import org.freyja.jdbc.core.rowMapper.MapRowMapper;
import org.freyja.jdbc.core.rowMapper.ObjectRowMapper;
import org.freyja.jdbc.object.BeanInfo;
import org.freyja.jdbc.object.BeanInfoCache;
import org.freyja.jdbc.object.ColumnPropertyMapping;
import org.freyja.jdbc.object.HqlMapping;
import org.freyja.sql.SqlLog;
import org.freyja.sql.SqlParser;
import org.freyja.support.MethodUtil;
import org.freyja.transaction.FreyjaTransactionManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

public class FreyjaJdbcTemplate extends FreyjaJdbcAccessor implements
		FreyjaJdbcOperations {

	protected static Logger log = Logger.getLogger(FreyjaJdbcTemplate.class);

	public FreyjaJdbcTemplate() {
	}

	public FreyjaJdbcTemplate(JdbcTemplate jdbcTemplate,
			CacheManager cacheManager) {
		setJdbcTemplate(jdbcTemplate);
		this.cacheOperation = new CacheOperations(cacheManager);
		// this.transactionManager = new
		// FreyjaTransactionManager(getDataSource());
	}

	// private void update(String entityName, Object id, Object value) {
	// JdbcTransactionObjectSupport dsto = (JdbcTransactionObjectSupport)
	// transactionManager
	// .doGetTransaction();
	// boolean f = transactionManager.isExistingTransaction(dsto);
	//
	// if (f) {
	// int s = dsto.getConnectionHolder().hashCode();
	// Map<String, Object> m = sysMap.get(s);
	// if (m == null) {
	// return;
	// }
	//
	// Object oldValue = m.get(entityName + "#" + id);
	//
	// if (oldValue == null) {
	// return;
	// }
	// if (!MethodUtil.beanMapping(oldValue, value)) {
	// update(value);
	// }
	// }
	// }

	@Override
	public List find(String hql, Object... args) {
		List list = find(null, null, hql, QuertByObjectRowMapper, args);
		if (list == null) {
			return EMPTY_LIST;
		}
		return list;
	}

	@Override
	public List find(Integer first, Integer max, String hql, Object... args) {
		List list = find(first, max, hql, QuertByObjectRowMapper, args);
		if (list == null) {
			return EMPTY_LIST;
		}
		return list;
	}

	@Override
	public List findByMap(Integer first, Integer max, String hql,
			Object... args) {
		List list = find(first, max, hql, QuertByMapRowMapper, args);
		if (list == null) {
			return EMPTY_LIST;
		}
		return list;
	}

	private String createKey(Object... args) {

		if (args == null)
			return null;
		String key = "";
		for (Object arg : args) {

			key += arg.toString();
		}

		return key;
	}

	@Override
	public List find(Integer first, Integer max, String hql, int type,
			Object... args) {

		String key = hql + "#" + first + "#" + max + "#" + type + "#"
				+ createKey(args);

		HqlMapping hm = super.getParsedSql(hql, type);

		List list;

		if (useQueryCache && hm.supportQueryCache) {
			Object obj = cacheOperation.getFormQueryCache(key);
			if (obj != null) {
				return (List) obj;
			}
		}

		String selectSql = SqlParser.createSelectSql(first, max, hm.sql);
		SqlLog.showSql(hql, selectSql, args);

		RowMapper rm = null;
		if (hm.rowMapperType == HqlMapping.BeanPropertyRowMapper) {
			rm = new BeanPropertyRowMapper(hm.bi, this);
		} else if (hm.rowMapperType == HqlMapping.MapRowMapper) {
			rm = new MapRowMapper();
		} else if (hm.rowMapperType == HqlMapping.ObjectRowMapper) {
			rm = new ObjectRowMapper();
		}

		list = getJdbcTemplate().query(selectSql, args, rm);
		if (useQueryCache && hm.supportQueryCache) {
			cacheOperation.putIntoQueryCache(key, list);

			Object obj = cacheOperation
					.getFormQueryCache(hm.bi.clazz.getName());
			Map<String, List<String>> map;
			if (obj == null) {
				map = new HashMap<String, List<String>>();

			} else {
				map = (Map<String, List<String>>) obj;
			}
			for (String c : hm.queryCacheKeys) {
				List<String> l = map.get(c);
				if (l == null) {
					l = new ArrayList<String>();
					map.put(c, l);
				}
				l.add(key);
			}
			cacheOperation.putIntoQueryCache(hm.bi.clazz.getName(), map);

			// Cache cache = cacheOperation.getCache(hm.bi.clazz.getName());

			// if (cache != null) {
			// CacheSelectDeParser csdp = new CacheSelectDeParser();
			// CacheExpressionDeParser cedp = new CacheExpressionDeParser();
			// csdp.setExpressionVisitor(cedp);
			// cedp.setCache(cache);
			// cedp.setKey(key);
			// hm.select.getSelectBody().accept(csdp);
			// }
		}
		return list;
	}

	@Override
	public void executeUpdate(String hql, Object... args) {
		HqlMapping hmp = getParsedSql(hql, 3);
		SqlLog.showSql(hql, hmp.sql, args);
		getJdbcTemplate().update(hmp.sql, args);
		updateCaches(hmp, args);
	}

	private Results findCachesByCache(BeanInfo<?> bi, Expression where,
			Object[] args, boolean values, Cache cache) {

		Query querty = cache.createQuery();
		if (values) {
			querty.includeValues();
		} else {
			querty.includeKeys();
		}
		if (where != null) {
			EQLExpressionDeParser eql = new EQLExpressionDeParser(bi, cache,
					args);
			where.accept(eql);
			querty.addCriteria(eql.getCriteria());
		}

		Results results = querty.execute();
		return results;
	}

	private List findCachesBySql(String selectSql, Table table, BeanInfo<?> bi,
			Object... args) {
		List caches = new ArrayList();

		SqlLog.showSql(null, selectSql, args);

		List<Map<String, Object>> list = getJdbcTemplate().queryForList(
				selectSql, args);

		for (Map<String, Object> map : list) {
			Serializable id = (Serializable) map.get(bi.idColumn.columnName);

			Object obj = cacheOperation.get(bi.clazz, id);
			if (obj == null) {
				continue;
			}
			caches.add(obj);
		}

		return caches;
	}

	private void updateCaches(HqlMapping hmp, Object... args) {
		if (cacheManager == null) {
			return;
		}
		Cache cache = cacheManager.getCache(hmp.bi.clazz.getName());
		if (cache == null) {
			return;
		}
		if (hmp.update != null) {
			Update update = hmp.update;

			Object[] newArgs = args;
			if (hmp.jdbcParameterNumber > 0) {
				newArgs = Arrays.copyOfRange(args, hmp.jdbcParameterNumber,
						args.length);
			}

			if (hmp.supportEhcacheSearch && hmp.bi.supportEhcacheSearch) {
				Results results = findCachesByCache(hmp.bi, update.getWhere(),
						newArgs, true, cache);
				if (results != null) {
					for (Result result : results.all()) {
						update(result.getValue(), hmp.bi, update, args);
					}
				}
			} else {
				String selectSql = hmp.selectSQL;

				List caches = findCachesBySql(selectSql, update.getTable(),
						hmp.bi, newArgs);
				for (Object value : caches) {
					update(value, hmp.bi, update, args);
				}
			}

		} else if (hmp.delete != null) {
			Delete delete = hmp.delete;

			if (hmp.supportEhcacheSearch && hmp.bi.supportEhcacheSearch) {
				Results results = findCachesByCache(hmp.bi, delete.getWhere(),
						args, false, cache);
				if (results != null) {
					for (Result result : results.all()) {
						cacheOperation.remove(hmp.bi.clazz, result.getKey());
					}
				}
			} else {
				String selectSQL = hmp.selectSQL;
				SqlLog.showSql(null, selectSQL, args);
				List<Map<String, Object>> list = getJdbcTemplate()
						.queryForList(selectSQL, args);
				for (Map<String, Object> map : list) {
					Serializable id = (Serializable) map
							.get(hmp.bi.idColumn.columnName);
					cacheOperation.remove(hmp.bi.clazz, id);
				}
			}
		}
	}

	private <T> void update(T t, BeanInfo<?> bi, Update update, Object[] args) {
		ELExpressionDeParser eledp = new ELExpressionDeParser(args);
		int length = update.getColumns().size();
		for (int i = 0; i < length; i++) {
			String column = ((Column) update.getColumns().get(i))
					.getColumnName();
			ColumnPropertyMapping cpm = bi.columnPropertyMap.get(column
					.toLowerCase());
			PropertyDescriptor pd = cpm.pd;
			if (pd == null) {
				continue;
			}
			// 修改查询缓存

			removeQueryCache(bi, cpm);

			Expression expression = (Expression) update.getExpressions().get(i);
			StringBuffer buffer = new StringBuffer();
			eledp.setBuffer(buffer);
			expression.accept(eledp);
			Object value = null;
			if (expression instanceof Column) {
				Column column2 = (Column) expression;

				ColumnPropertyMapping cpm2 = bi.columnPropertyMap.get(column2
						.getColumnName().toLowerCase());

				value = MethodUtil.invokeGet(cpm2, t);
			} else {
				value = MethodUtil.invokeGet(cpm, t);
				if (expression instanceof LongValue
						|| expression instanceof DoubleValue) {
					value = expression.toString();
				} else {
					org.xidea.el.Expression el = factory.create(buffer
							.toString());
					value = el.evaluate(column, value);
				}
			}
			if (value == null) {
				MethodUtil.invokeSet(cpm, t, new Object[] { null });
			} else {

				Object o = ConvertUtils.convert(value.toString(),
						pd.getPropertyType());
				MethodUtil.invokeSet(cpm, t, o);
			}
		}
	}

	@Override
	public <T> T get(Class<T> clazz, Object id) {

		return get(clazz.getName(), id);
	}

	@Override
	public <T> T get(String entityName, Object id) {
		T entity = cacheOperation.get(entityName, id);
		if (entity != null) {

			// 清除transient字段
			// BeanInfo<T> bi = BeanInfoCache.get(entityName);
			// for (Map.Entry<String, ColumnPropertyMapping> m : bi.transientMap
			// .entrySet()) {
			// ColumnPropertyMapping cpm = m.getValue();
			// PropertyDescriptor pd = cpm.pd;
			// Method method = pd.getWriteMethod();
			// if (method != null && pd.getReadMethod() != null) {
			//
			// MethodUtil.invokeSet(cpm, entity, MethodUtil
			// .instantiate(method.getParameterTypes()[0]));
			// }
			// }

			// add(entityName, id, entity);

			// JdbcTransactionObjectSupport dsto =
			// (JdbcTransactionObjectSupport) transactionManager
			// .doGetTransaction();
			//
			// boolean f = transactionManager.isExistingTransaction(dsto);

			// if (f) {
			// try {
			// return (T) BeanUtils.cloneBean(entity);
			// } catch (IllegalAccessException e) {
			// e.printStackTrace();
			// } catch (InstantiationException e) {
			// e.printStackTrace();
			// } catch (InvocationTargetException e) {
			// e.printStackTrace();
			// } catch (NoSuchMethodException e) {
			// e.printStackTrace();
			// }
			//
			// }

			return entity;
		}

		BeanInfo<T> bi = BeanInfoCache.get(entityName);
		RowMapper<T> rm = new BeanPropertyRowMapper(bi, this);

		SqlLog.showSql(null, bi.select, id);

		List<T> list = getJdbcTemplate().query(bi.select, new Object[] { id },
				rm);
		if (list.size() == 1) {
			entity = list.get(0);
			cacheOperation.put(entity, id);

			// add(entityName, id, entity);
			return entity;
		}
		return null;
	}

	public <T> Object save(final T t) {
		if (t == null) {
			return null;
		}
		final BeanInfo<T> bi = BeanInfoCache.get(t.getClass());

		KeyHolder keyHolder = new GeneratedKeyHolder();
		PreparedStatementCreator psc = new PreparedStatementCreator() {
			public PreparedStatement createPreparedStatement(Connection con)
					throws SQLException {
				String sql = bi.insert;
				PreparedStatement pstmt = con.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);
				int colIndex = 0;

				List<Object> values = new ArrayList<Object>();
				for (String columnName : bi.insertColumns) {
					colIndex++;
					ColumnPropertyMapping cpm = bi.columnPropertyMap
							.get(columnName.toLowerCase());
					Object value = MethodUtil.invokeGet(cpm, t);
					if (cpm.isManyToOne) {
						BeanInfo<T> bi2 = BeanInfoCache.get(cpm.pd
								.getPropertyType());
						ColumnPropertyMapping idCpm = bi2.idColumn;
						if (value != null) {
							Object idValue = MethodUtil.invokeGet(idCpm, value);
							if (idValue == null) {
								value = save(value);
							} else {
								value = idValue;
							}
						}
					}
					StatementCreatorUtils.setParameterValue(pstmt, colIndex,
							bi.insertArgTypes.get(colIndex - 1), value);

					values.add(value);
				}

				SqlLog.showSql(null, sql, values);
				return pstmt;
			}
		};
		
		removeAllQueryCache(bi);
		getJdbcTemplate().update(psc, keyHolder);

		ColumnPropertyMapping idCpm = bi.columnPropertyMap
				.get(bi.idColumn.columnName.toLowerCase());

		Object idValue = (Serializable) MethodUtil.invokeGet(idCpm, t);

		if (idValue == null) {
			Number pk = keyHolder.getKey();
			PropertyDescriptor idpd = idCpm.pd;
			idValue = MethodUtil.convertValueToRequiredType(pk, idpd
					.getReadMethod().getReturnType());
			MethodUtil.invokeSet(idCpm, t, idValue);
		}
		return idValue;

	}

	@Override
	public <T> void update(T t) {
		if (t == null) {
			return;
		}
		BeanInfo<T> bi = BeanInfoCache.get(t.getClass());

		Object[] values = new Object[bi.updateColumns.size() + 1];

		int i = 0;
		for (String columnName : bi.updateColumns) {
			ColumnPropertyMapping cpm = bi.columnPropertyMap.get(columnName
					.toLowerCase());
			Object value = MethodUtil.invokeGet(cpm, t);
			if (cpm.isManyToOne) {
				BeanInfo<T> bi2 = BeanInfoCache.get(cpm.pd.getPropertyType());
				ColumnPropertyMapping idCpm = bi2.idColumn;
				Object result = value;
				if (value != null) {
					value = MethodUtil.invokeGet(idCpm, value);

					// update(bi2.clazz.getName(), value, result);
				}
			}

			removeQueryCache(bi, cpm);

			values[i] = value;
			i++;
		}

		ColumnPropertyMapping idCpm = bi.columnPropertyMap
				.get(bi.idColumn.columnName.toLowerCase());
		Serializable idValue = (Serializable) MethodUtil.invokeGet(idCpm, t);
		values[i] = idValue;

		SqlLog.showSql(null, bi.update, values);
		getJdbcTemplate().update(bi.update, values, bi.updateArgTypesArray);
	}

	private void removeQueryCache(BeanInfo<?> bi, ColumnPropertyMapping cpm) {
		Object obj = cacheOperation.getFormQueryCache(bi.clazz.getName());

		if (obj != null) {
			Map<String, List<String>> map = (Map<String, List<String>>) obj;
			if (map != null) {
				List<String> l = map.get(cpm.propertyName.toLowerCase());

				if (l != null) {
					for (String key : l) {
						cacheOperation.removeQueryCache(key);
					}
					map.remove(cpm.propertyName.toLowerCase());
				}
			}
		}
	}

	private void removeAllQueryCache(BeanInfo<?> bi) {

		Object obj = cacheOperation.getFormQueryCache(bi.clazz.getName());

		if (obj != null) {
			Map<String, List<String>> map = (Map<String, List<String>>) obj;
			if (map != null) {
				for (Entry<String, ColumnPropertyMapping> entry : bi.columnPropertyMap
						.entrySet()) {
					ColumnPropertyMapping cpm = entry.getValue();
					List<String> l = map.get(cpm.propertyName.toLowerCase());
					if (l != null) {
						for (String key : l) {
							cacheOperation.removeQueryCache(key);
						}
						map.remove(cpm.propertyName.toLowerCase());
					}
				}
			}
		}
	}

	@Override
	public <T> Serializable delete(T t) {
		if (t == null) {
			return null;
		}
		BeanInfo<T> bi = BeanInfoCache.get(t.getClass());

		Serializable id = (Serializable) MethodUtil.invokeGet(bi.idColumn, t);

		SqlLog.showSql(null, bi.delete, id);
		getJdbcTemplate().update(bi.delete, id);
		cacheOperation.remove(t.getClass(), id);

		removeAllQueryCache(bi);

		return id;
	}

	@Override
	public <T> void saveOrUpdate(T t) {
		if (t == null) {
			return;
		}

		BeanInfo<T> bi = BeanInfoCache.get(t.getClass());
		Object v = MethodUtil.invokeGet(bi.idColumn, t);
		if (v == null) {
			save(t);
		} else {
			update(t);
		}
	}

	@Override
	public <T> void saveOrUpdateAll(List<T> list) {
		if (list.size() == 0) {
			return;
		}
		for (T t : list) {
			saveOrUpdate(t);
		}
	}
}
