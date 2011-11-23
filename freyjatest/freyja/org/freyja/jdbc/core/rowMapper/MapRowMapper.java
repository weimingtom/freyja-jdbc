package org.freyja.jdbc.core.rowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.freyja.jdbc.object.BeanInfo;
import org.freyja.jdbc.object.BeanInfoCache;
import org.freyja.support.MethodUtil;
import org.springframework.jdbc.core.RowMapper;
public class MapRowMapper<T> implements RowMapper<Map<String, Object>> {

	public Map<String, Object> mapRow(ResultSet rs, int rowNum)
			throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		Map<String, Object> map = new HashMap<String, Object>(
				columnCount);
		for (int i = 1; i <= columnCount; i++) {
			String key = rsmd.getColumnLabel(i);
			BeanInfo<?> bi = BeanInfoCache.get(rsmd.getTableName(i));
			Object result;
			if (bi != null) {
				result = MethodUtil.getColumnValue(rs, i, bi.columnPropertyMap
						.get(rsmd.getColumnName(i).toLowerCase()).pd);
			} else {
				result = MethodUtil.getColumnValue(rs, i);
			}

			map.put(key, result);
		}
		return map;
	}

}