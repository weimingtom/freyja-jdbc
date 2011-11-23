package org.freyja.jdbc.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

public class HqlMapping {

	public final static int BeanPropertyRowMapper = 1;
	public final static int MapRowMapper = 2;
	public final static int ObjectRowMapper = 3;

	public String hql;
	public String sql;
	public int jdbcParameterNumber = 0;
	public List<Join> joins = new ArrayList<Join>();

	public Select select;
	public Update update;
	public Delete delete;

	public boolean single = true;

	public BeanInfo<?> bi;
	// public RowMapper rm;
	public int rowMapperType = 0;
	public List<String> queryCacheKeys=new ArrayList<String>();
	// public Map<String, Join> joinMap = new HashMap<String, Join>();

	public String selectSQL;
	public boolean supportEhcacheSearch = true;
	public boolean supportQueryCache = true;

}
