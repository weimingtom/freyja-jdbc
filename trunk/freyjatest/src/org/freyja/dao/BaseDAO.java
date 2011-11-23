package org.freyja.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.freyja.pojo.Page;
import org.springframework.stereotype.Repository;
public interface BaseDAO {
	Object findObjectByHql(String hql);

	List findByHql(String hql);

	List<Map<String, Object>> findMapByHql(String hql, Object... args);

	<T> Object save(T entity);

	<T> void update(T entity);

	<T> void saveOrUpdate(T entity);

	<T> void delete(T entity);

	long getCount(String hql);

	long getCount(String hql, Object... args);

	List getByPage(Page page, String hql);

	List getByPage(Page page, String hql, Object... args);

	List getBySqlPage(Page page, String hql);

	List find(String hql, Object... values);

	List find(String hql);

	List findTOP(String hql, int first, int max);

	<T> T get(Class<T> clazz, Serializable id);

	Object get2(Class clazz, Serializable id);

	<T> List<T> find(Class<T> clazz);

	<T> List<T> findByNamedParam(Class<T> clazz, String paramName, Object value);

	<T> T findObjectByHql(Class<T> clazz, String where, Object... values);

	<T> T findObjectByHql(Class<T> clazz, String where);

	<T> T get(Class<T> clazz, String where);

	<T> List<T> find(Class<T> clazz, String where, Object... values);

	<T> List<T> find(Class<T> clazz, String where);

	void saveOrUpdateAll(List list);

	int count(String hql);

	int count(String hql, Object... values);

	<T> long getCount(Class<T> clazz, String where, Object... values);

	List createQueryBySql(String hql);

	int updateByHql(String hql);

	public <T> T get(Class<T> clazz, String where, Object... values);

	public void executeUpdate(String hql);

	public void executeUpdate(String hql, Object... args);

	List getMapByPage(Page page, String hql);

	void executeUpdateBySQL(String sql);

	List findNoCache(String hql);

	List findBySQL(String sql);
}
