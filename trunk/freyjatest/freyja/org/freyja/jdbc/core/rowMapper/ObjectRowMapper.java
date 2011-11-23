package org.freyja.jdbc.core.rowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.freyja.jdbc.object.BeanInfo;
import org.freyja.jdbc.object.BeanInfoCache;
import org.freyja.jdbc.object.ColumnPropertyMapping;
import org.freyja.support.MethodUtil;
import org.springframework.jdbc.core.RowMapper;
public class ObjectRowMapper<T> implements RowMapper<Object> {

	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		Object[] arrValues = new Object[columnCount];
		for (int i = 1; i <= columnCount; i++) {
			BeanInfo<?> bi = BeanInfoCache.get(rsmd.getTableName(i));
			Object result;
			if (bi != null) {
				String columnName = rsmd.getColumnName(i);
				ColumnPropertyMapping cpm = bi.columnPropertyMap.get(columnName
						.toLowerCase());
				if (cpm == null) {
					continue;
				}
				result = MethodUtil.getColumnValue(rs, i, cpm.pd);
			} else {
				result = MethodUtil.getColumnValue(rs, i);
			}
			if (columnCount == 1) {//if columnCount = 1 return a Object
				return result;
			}
			arrValues[i - 1] = result;
		}
		return arrValues;
	}
}