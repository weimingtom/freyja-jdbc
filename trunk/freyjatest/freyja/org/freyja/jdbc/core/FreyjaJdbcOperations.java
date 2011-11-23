package org.freyja.jdbc.core;

import java.util.List;

public interface FreyjaJdbcOperations {

	<T> T get(Class<T> entityClass, Object id);

	<T> T get(String entityName, Object id);

	<T> Object save(T entity);

	<T> void update(T entity);

	<T> void saveOrUpdate(T entity);

	<T> Object delete(T entity);

	<T> void saveOrUpdateAll(List<T> list);

	List find(String queryString, Object... values);

	void executeUpdate(String hql, Object... args);

	List find(Integer first, Integer max, String hql, Object... args);

	List findByMap(Integer first, Integer max, String hql, Object... args);

	List find(Integer first, Integer max, String hql, int type, Object... args);

}
