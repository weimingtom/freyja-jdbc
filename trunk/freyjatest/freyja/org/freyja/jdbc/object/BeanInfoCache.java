package org.freyja.jdbc.object;

import java.util.HashMap;

public class BeanInfoCache {
	public final static HashMap<String, BeanInfo> beanInfoMap = new HashMap<String, BeanInfo>();

	public static BeanInfo get(Class clazz) {
		BeanInfo bi = beanInfoMap.get(clazz.getName());
		return bi;
	}

	public static BeanInfo put(Class clazz) {
		BeanInfo bi = new BeanInfo(clazz);
		beanInfoMap.put(clazz.getName(), bi);
		beanInfoMap.put(clazz.getSimpleName(), bi);
		beanInfoMap.put(bi.tableName, bi);
		return bi;
	}

	public static BeanInfo get(String tableName) {
		BeanInfo bi = beanInfoMap.get(tableName);
		return bi;
	}
}
