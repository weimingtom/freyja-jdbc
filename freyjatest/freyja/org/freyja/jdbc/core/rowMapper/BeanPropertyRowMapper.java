package org.freyja.jdbc.core.rowMapper;

import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.freyja.cache.CacheOperations;
import org.freyja.jdbc.core.FreyjaJdbcTemplate;
import org.freyja.jdbc.object.BeanInfo;
import org.freyja.jdbc.object.ColumnPropertyMapping;
import org.freyja.support.MethodUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.jdbc.core.RowMapper;

public class BeanPropertyRowMapper<T> implements RowMapper<T> {

	public BeanPropertyRowMapper() {
	}

	private BeanInfo<T> bi;

	private CacheOperations cacheOperation;
	private FreyjaJdbcTemplate freyjaJdbcTemplate;

	public BeanPropertyRowMapper(BeanInfo<T> bi,
			FreyjaJdbcTemplate freyjaJdbcTemplate) {
		this.cacheOperation = freyjaJdbcTemplate.getCacheOperation();
		this.freyjaJdbcTemplate = freyjaJdbcTemplate;
		this.bi = bi;
	}

	public T mapRow(ResultSet rs, int rowNumber) throws SQLException {
		T mappedObject = BeanUtils.instantiate(bi.clazz);
		BeanWrapper bw = PropertyAccessorFactory
				.forBeanPropertyAccess(mappedObject);

		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		Object key = null;

		for (int index = 1; index <= columnCount; index++) {
			if (!bi.tableName.equals(rsmd.getTableName(index))) {
				continue;
			}
			String column = rsmd.getColumnName(index);

			ColumnPropertyMapping cpmManyToOne = bi.columnPropertyManyToOneColumnMap
					.get(column.toLowerCase());

			if (cpmManyToOne != null) {// manyToOne
				PropertyDescriptor pd = cpmManyToOne.pd;
				Object value = MethodUtil.getColumnValue(rs, index, pd);
				value = freyjaJdbcTemplate.get(pd.getPropertyType(), value);
				bw.setPropertyValue(pd.getName(), value);
			}
			ColumnPropertyMapping cpm = bi.columnPropertyMap.get(column
					.toLowerCase());
			if (cpm == null) {// 有的实体和字段不对应则不处理
				continue;
			}

			PropertyDescriptor pd = cpm.pd;
			Object value = MethodUtil.getColumnValue(rs, index, pd);
			if (cpm.isPrimaryKey) {// 优先从缓存里面查找出结果
				key = value;
				T result = cacheOperation.get(bi.clazz, key);
				if (result != null) {
					return result;
				}
			}
			bw.setPropertyValue(pd.getName(), value);

		}

		cacheOperation.put(mappedObject, key);// 查找出来的值放入缓存
		return mappedObject;
	}
}
