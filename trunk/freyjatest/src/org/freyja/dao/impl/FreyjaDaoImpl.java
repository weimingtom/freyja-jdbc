package org.freyja.dao.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


import org.freyja.dao.BaseDAO;
import org.freyja.jdbc.core.FreyjaJdbcTemplate;
import org.freyja.pojo.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("freyja")
public class FreyjaDaoImpl implements BaseDAO {

	@Autowired
	FreyjaJdbcTemplate hibernateTemplate;

	@Override
	public Object findObjectByHql(String hql) {
		List list = hibernateTemplate.find(hql);
		if (list.size() == 0) {
			return null;
		} else {
			return list.get(0);
		}
	}

	@Override
	public List findByHql(String hql) {
		return hibernateTemplate.find(hql);
	}

	@Override
	public <T> Object save(T entity) {
		return hibernateTemplate.save(entity);
	}

	@Override
	public <T> void saveOrUpdate(T entity) {
		hibernateTemplate.saveOrUpdate(entity);
	}

	@Override
	public <T> void update(T entity) {
		hibernateTemplate.update(entity);
	}

	@Override
	public void delete(Object entity) {
		hibernateTemplate.delete(entity);

	}

	@Override
	public long getCount(String hql) {

		List<Long> list = hibernateTemplate.find(hql);

		if (list.size() == 0) {
			return 0l;
		} else {
			if (list.get(0) != null) {
				return list.get(0);
			} else {
				return 0l;
			}

		}

	}

	@Override
	public List getByPage(final Page page, final String hql) {
		return null;
	}

	@Override
	public List getBySqlPage(final Page page, final String sql)
			{
		return null;
	}

	@Override
	public List find(String hql, Object... values) {
		return hibernateTemplate.find(hql, values);
	}

	@Override
	public List findTOP(final String hql, final int first, final int max) {
		return null;
	}

	@Override
	public <T> List<T> find(Class<T> clazz) {
		return hibernateTemplate.find("from " + clazz.getName());
	}

	@Override
	public <T> List<T> findByNamedParam(Class<T> clazz, String paramName,
			Object value) {
		return null;
	}

	@Override
	public <T> T get(Class<T> clazz, Serializable id) {
		return hibernateTemplate.get(clazz, id);
	}

	@Override
	public Object get2(final Class clazz, final Serializable id) {

		return null;
	}

	@Override
	public <T> T findObjectByHql(Class<T> clazz, String where, Object... values) {
		List list = hibernateTemplate.find("from " + clazz.getName()
				+ " where " + where, values);
		if (list.size() == 0) {
			return null;
		}
		return (T) list.get(0);
	}

	@Override
	public <T> List<T> find(Class<T> clazz, String where, Object... values) {
		List<T> list = hibernateTemplate.find("from " + clazz.getSimpleName()
				+ " where " + where, values);
		return list;
	}

	public void saveOrUpdateAll2(List list) {
		hibernateTemplate.saveOrUpdateAll(list);
	}

	@Override
	public void saveOrUpdateAll(final List list) {
	}

	@Override
	public int count(String hql) {
		return Integer.parseInt(hibernateTemplate.find(hql).get(0).toString());
	}

	@Override
	public List find(String hql) {
		return find(hql, null);
	}

	@Override
	public <T> List<T> find(Class<T> clazz, String where) {
		return find(clazz, where, null);
	}

	@Override
	public <T> T findObjectByHql(Class<T> clazz, String where) {
		return findObjectByHql(clazz, where, null);
	}

	@Override
	public List createQueryBySql(final String hql) {
		return null;
	}

	@Override
	public int updateByHql(final String hql) {
		return 0;
	}

	@Override
	public <T> long getCount(Class<T> clazz, String where, Object... values) {
		List<Long> list = hibernateTemplate.find("select count(*) from "
				+ clazz.getName() + " where " + where, values);

		if (list.size() == 0) {
			return 0l;
		} else {
			if (list.get(0) != null) {
				return list.get(0);
			} else {
				return 0l;
			}
		}
	}

	@Override
	public <T> T get(Class<T> clazz, String where) {
		// hibernateTemplate.setCacheQueries(true);
		List<T> list = hibernateTemplate.find("from " + clazz.getName()
				+ " where " + where);
		// hibernateTemplate.setCacheQueries(false);
		if (list.size() == 0) {
			return null;
		}
		return list.get(0);
	}

	@Override
	public <T> T get(Class<T> clazz, String where, Object... values) {

		// Iterator<T> it = hibernateTemplate.iterate("from " + clazz.getName()
		// + " where " + where, values);
		//
		// while (it.hasNext()) {
		// return it.next();
		// }
		// return null;

		// hibernateTemplate.setCacheQueries(true);
		List list = hibernateTemplate.find("from " + clazz.getName()
				+ " where " + where, values);
		// hibernateTemplate.setCacheQueries(false);
		if (list.size() == 0) {
			return null;
		}
		return (T) list.get(0);
	}

	@Override
	public void executeUpdate(final String hql) {
	}

	// @Override
	// public void commit() {
	// Session session = hibernateTemplate.getSessionFactory()
	// .getCurrentSession();
	// Transaction transaction = session.beginTransaction();
	// transaction.commit();
	//
	// }

	@Override
	public void executeUpdateBySQL(final String sql) {
	}

	@Override
	public List findNoCache(String hql) {
		List list = hibernateTemplate.find(hql);
		return list;
	}

	@Override
	public List findBySQL(final String sql) {
		return null;
	}

	@Override
	public List<Map<String, Object>> findMapByHql(String hql, Object... args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getCount(String hql, Object... args) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List getByPage(Page page, String hql, Object... args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int count(String hql, Object... values) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void executeUpdate(String hql, Object... args) {
		// TODO Auto-generated method stub

	}

	@Override
	public List getMapByPage(Page page, String hql) {
		// TODO Auto-generated method stub
		return null;
	}
}