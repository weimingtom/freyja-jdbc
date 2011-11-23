package org.freyja.support;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtils;
import org.freyja.jdbc.object.ColumnPropertyMapping;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.NumberUtils;
/**invoke helper*/
public class MethodUtil {

	public static void invokeSet(ColumnPropertyMapping cpm, Object o,
			Object... value) {
		try {
			Method method = cpm.pd.getWriteMethod();
			method.setAccessible(true);
			method.invoke(o, value);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public static Object invokeGet(ColumnPropertyMapping cpm, Object o) {
		try {
			Method method = cpm.pd.getReadMethod();
			method.setAccessible(true);
			return method.invoke(o);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Map<Class<? extends Annotation>, Annotation> arrToMap(
			Annotation[] arr) {
		final Map<Class<? extends Annotation>, Annotation> map = new HashMap<Class<? extends Annotation>, Annotation>(
				(int) (arr.length * 1.5));
		for (Annotation an : arr) {
			map.put(an.annotationType(), an);
		}
		return map;
	}

	public static Object convertValueToRequiredType(Object value,
			Class requiredType) {
		if (String.class.equals(requiredType)) {
			return value.toString();
		} else if (Number.class.isAssignableFrom(requiredType)) {
			if (value instanceof Number) {
				// Convert original Number to target Number class.
				return NumberUtils.convertNumberToTargetClass(((Number) value),
						requiredType);
			} else {
				// Convert stringified value to target Number class.
				return NumberUtils.parseNumber(value.toString(), requiredType);
			}
		} else {
			throw new IllegalArgumentException("Value [" + value
					+ "] is of type [" + value.getClass().getName()
					+ "] and cannot be converted to required type ["
					+ requiredType.getName() + "]");
		}
	}

	public static Object convert(Object key) {
		if (key instanceof Integer) {
			return ConvertUtils.convert(key.toString(), Long.class);
		} else if (key instanceof Float) {
			return ConvertUtils.convert(key.toString(), Double.class);
		}
		return key;
	}


	public static Object getColumnValue(ResultSet rs, int index,
			PropertyDescriptor pd) throws SQLException {

		Class requiredType = pd.getPropertyType();
		
		return JdbcUtil.getResultSetValue(rs, index, requiredType);
		
//		if (Integer[][].class.equals(requiredType)) {
//			return (Integer[][]) SerializationHelper.deserialize(rs.getBlob(
//					index).getBinaryStream());
//		} else if (Integer[].class.equals(requiredType)) {
//			return (Integer[]) SerializationHelper.deserialize(rs
//					.getBlob(index).getBinaryStream());
//		} // ehcache不支持timestamp比较
//		else if (java.util.Date.class.equals(requiredType)) {
//			Timestamp t = rs.getTimestamp(index);
//			if (t == null) {
//				return null;
//			}
//			return new Date(t.getTime());
//		}
//
//		return JdbcUtil.getResultSetValue(rs, index, requiredType);
	}

	public static Object getColumnValue(ResultSet rs, int index)
			throws SQLException {
		Object obj = JdbcUtils.getResultSetValue(rs, index);

		if (obj instanceof BigDecimal) {
			return ((BigDecimal) obj).longValue();
		} else if (obj instanceof BigInteger) {
			return ((BigInteger) obj).longValue();
		}
		return obj;
	}

//	public static boolean beanMapping(Object oldObj, Object newObj) {
//
//		Class clazz = oldObj.getClass();
//		Field[] fields = clazz.getDeclaredFields();
//		for (Field field : fields) {
//			PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(clazz,
//					field.getName());
//			if (pd == null || pd.getWriteMethod() == null) {
//				continue;
//			}
//
//			Method readMethod = pd.getReadMethod();
//			readMethod.setAccessible(true);
//			try {
//				Object oldValue = readMethod.invoke(oldObj);
//				Object newValue = readMethod.invoke(newObj);
//				if (oldValue != null && !oldValue.equals(newValue)) {
//					return false;
//				}
//			} catch (IllegalArgumentException e) {
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				e.printStackTrace();
//			} catch (InvocationTargetException e) {
//				e.printStackTrace();
//			}
//		}
//		return true;
//	}
	
	
}
