package org.freyja.sql;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.freyja.FreyjaException;
import org.freyja.jdbc.core.FreyjaJdbcTemplate;
import org.freyja.jdbc.object.BeanInfo;
import org.freyja.jdbc.object.BeanInfoCache;
import org.freyja.jdbc.object.ColumnPropertyMapping;
import org.freyja.jdbc.object.HqlMapping;
import org.springframework.jdbc.core.JdbcTemplate;

import net.sf.ehcache.CacheManager;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.JSqlParser;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.update.Update;
/**hql -> hqlMap*/
public class SqlParser {

	public static HqlMapping hqlToSql(String hql) {
		try {
			HqlMapping hm = new HqlMapping();
			if (hql.startsWith("from") || hql.startsWith("FROM")) {
				hql = "select * " + hql;
			}
			hm.hql = hql;

			JSqlParser parserManager = new CCJSqlParserManager();
			Statement statement = parserManager.parse(new StringReader(hql));

			org.freyja.deparser.hql.StatementDeParser ddd = new org.freyja.deparser.hql.StatementDeParser();
			ddd.setHm(hm);
			statement.accept(ddd);
			return hm;
		} catch (JSQLParserException e) {
			throw new FreyjaException("not supported HQL:" + hql);
		}
	}

	public static void parserRowMapper(HqlMapping hm, int type,
			JdbcTemplate jdbcTemplate, CacheManager cacheManager) {
		if (type == FreyjaJdbcTemplate.QuertByMapRowMapper) {

			hm.rowMapperType = HqlMapping.MapRowMapper;
		} else {
			if (hm.single && hm.bi != null) {
				hm.rowMapperType = HqlMapping.BeanPropertyRowMapper;
			} else {
				hm.rowMapperType = HqlMapping.ObjectRowMapper;
			}
		}
	}

	public static String createSelectSql(HqlMapping hmp) {
		BeanInfo<?> bi = hmp.bi;
		if (hmp.update != null) {
			Update update = hmp.update;
			PlainSelect ps = new PlainSelect();
			ps.setWhere(update.getWhere());
			ps.setFromItem(update.getTable());
			SelectExpressionItem sei = new SelectExpressionItem();
			Table table = new Table(null, update.getTable().getAlias());
			sei.setExpression(new Column(table, bi.idColumn.columnName));
			List<SelectItem> seiList = new ArrayList<SelectItem>();
			seiList.add(sei);
			ps.setSelectItems(seiList);
			return ps.toString();
		} else if (hmp.delete != null) {
			Delete delete = hmp.delete;
			PlainSelect ps = new PlainSelect();
			ps.setWhere(delete.getWhere());
			ps.setFromItem(delete.getTable());
			SelectExpressionItem sei = new SelectExpressionItem();

			Table table = new Table(null, delete.getTable().getAlias());
			sei.setExpression(new Column(table, bi.idColumn.columnName));
			List<SelectItem> seiList = new ArrayList<SelectItem>();
			seiList.add(sei);
			ps.setSelectItems(seiList);
			return ps.toString();

		}
		return null;

	}

	// public static boolean parserManyToOne(Column tableColumn, Table
	// fromTable) {
	// Table columnTable = tableColumn.getTable();
	//
	// if (columnTable.getName() == null) {
	// return false;
	// }
	//
	// String tableName = fromTable.getName();
	//
	// BeanInfo<?> frombi = BeanInfoCache.get(tableName);
	//
	// ColumnPropertyMapping cpm = frombi.propertyColumnMap.get(columnTable
	// .getName().toLowerCase());
	//
	// if (cpm != null) {
	//
	// BeanInfo<?> joinbi = BeanInfoCache.get(cpm.pd.getPropertyType());
	//
	// ColumnPropertyMapping columnAPM = joinbi.propertyColumnMap
	// .get(tableColumn.getColumnName().toLowerCase());
	//
	// // Join join = new Join();
	// // join.setLeft(true);
	// // EqualsTo equalsTo = new EqualsTo();
	// //
	// // String leftAlias = fromTable.getAlias();
	// //
	// // if (leftAlias == null) {
	// // leftAlias = frombi.tableName + "_";
	// // }
	// //
	// // equalsTo.setLeftExpression(new Column(new Table(null, leftAlias),
	// // cpm.columnName));
	// //
	// // String rightAlias = joinbi.tableName + "_";
	// //
	// // equalsTo.setRightExpression(new Column(new Table(null, rightAlias),
	// // joinbi.idColumn.columnName));
	// //
	// // Table r = new Table(null, joinbi.tableName);
	// // r.setAlias(rightAlias);
	// // join.setOnExpression(equalsTo);
	// // join.setRightItem(r);
	// // hm.joinMap.put(columnTable.getName(), join);
	// tableColumn.setColumnName(columnAPM.columnName);
	// // tableColumn.setTable(new Table(null, rightAlias));
	// return true;
	// }
	// return false;
	// }

	// public static void parser(Column column, Table table ) {
	// boolean f = false;
	//
	// if (column.getTable().getSchemaName() != null) {
	// if (table.getAlias() == null) {
	// throw new FreyjaException("not support");
	// }
	// f = parserManyToOne(column, table);
	// } else {
	// f = parserManyToOne(column, table);
	// }
	// if (f) {
	// return;
	// } else {
	// BeanInfo<?> bi = BeanInfoCache.get(table.getName());
	// ColumnPropertyMapping cpm = bi.propertyColumnMap.get(column
	// .getColumnName().toLowerCase());
	// column.setColumnName(cpm.columnName);
	// }
	// }

	// public static String findTableName(Column tableColumn,
	// List<FromItem> fromItems, List<SelectItem> selectItems) {
	//
	// if (selectItems == null) {// 从joins中找出复合条件的tableName
	// for (FromItem fi : fromItems) {
	// if (tableColumn.getTable().getName() != null) {
	// if (tableColumn.getTable().getName().equals(fi.getAlias())) {
	// return findTableNameByFromItem(tableColumn, fi);
	// }
	// } else {
	// String tableName = findTableNameByFromItem(tableColumn, fi);
	// if (tableName != null) {
	// return tableName;
	// }
	// }
	// }
	// } else {// 从seletItems中找出复合条件的Column 然后根据column找到tableName
	//
	// for (SelectItem si : selectItems) {
	//
	// if (si instanceof AllColumns) {
	// for (FromItem fi : fromItems) {
	// String tableName = findTableNameByFromItem(tableColumn,
	// fi);
	// if (tableName != null) {
	// return tableName;
	// }
	// }
	// } else if (si instanceof AllTableColumns) {
	// String alias = ((AllTableColumns) si).getTable().getName();
	// for (FromItem fi : fromItems) {
	// if (alias.equals(fi.getAlias())) {
	// String tableName = findTableNameByFromItem(
	// tableColumn, fi);
	// if (tableName != null) {
	// return tableName;
	// }
	// }
	// }
	// } else if (si instanceof SelectExpressionItem) {
	// SelectExpressionItem sei = (SelectExpressionItem) si;
	// Expression ep = sei.getExpression();
	//
	// if (tableColumn.getColumnName().equals(sei.getAlias())) {
	//
	// return null;
	// } else {
	// if (ep instanceof Column) {
	// Column column = (Column) ep;
	//
	// if (tableColumn.getColumnName().equals(
	// column.getColumnName())) {
	// if (column.getTable().getName() != null) {
	// for (FromItem fi : fromItems) {
	// if (column.getTable().getName()
	// .equals(fi.getAlias())) {
	// return findTableNameByFromItem(
	// column, fi);
	// }
	// }
	// } else {
	// for (FromItem fi : fromItems) {
	// String tableName = findTableNameByFromItem(
	// column, fi);
	// if (tableName != null) {
	// return tableName;
	// }
	// }
	// }
	// }
	// } else {
	// throw new FreyjaException("not supported hql");
	// }
	// }
	// }
	// }
	// }
	//
	// return null;
	// }

	// public static String findTableNameByFromItem(Column tableColumn,
	// FromItem fromItem) {
	// if (fromItem instanceof Table) {
	// String tableName = ((Table) fromItem).getName();
	// BeanInfo<?> bi = BeanInfoCache.get(tableName);
	// ColumnPropertyMapping cpm = bi.propertyColumnMap.get(tableColumn
	// .getColumnName().toLowerCase());
	//
	// if (cpm != null) {
	// return tableName;
	// }
	// } else if (fromItem instanceof SubSelect) {
	// PlainSelect ps = (PlainSelect) ((SubSelect) fromItem)
	// .getSelectBody();
	//
	// List<FromItem> fromItems = joinsToFromItems(ps.getJoins());
	// fromItems.add(ps.getFromItem());
	// return findTableName(tableColumn, fromItems, ps.getSelectItems());
	// }
	//
	// return null;
	// }

	// public static List<FromItem> joinsToFromItems(List<Join> joins) {
	// List<FromItem> fromItems = new ArrayList<FromItem>();
	// if (joins != null) {
	// for (Join join : joins) {
	// fromItems.add(join.getRightItem());
	// }
	// }
	// return fromItems;
	// }

	public static String createSelectSql(Integer first, Integer max,
			String select) {
		if (first != null && max != null) {
			select = select + " limit " + first + " , " + max;
		}
		return select;
	}

//	public static String parserArgs(String sql, Object... args) {
//		String[] arr = sql.split("\\?");
//
//		StringBuffer newSql = new StringBuffer();
//		for (int i = 0; i < arr.length; i++) {
//			newSql.append(arr[i]).append(args[i]);
//		}
//		return newSql.toString();
//
//	}

	
	public static String findTableNameFromFromItem(Column tableColumn,
			FromItem fromItem) {
		String tableName = null;
		if (tableColumn.getTable().getName() != null) {
			if (fromItem.getAlias() == null
					|| !tableColumn.getTable().getName()
							.equals(fromItem.getAlias())) {
				return null;
			}

		} else {

		}
		if (fromItem instanceof Table) {
			Table table = (Table) fromItem;

			BeanInfo<?> bi = BeanInfoCache.get(table.getName());

			ColumnPropertyMapping cpm = bi.propertyColumnMap.get(tableColumn
					.getColumnName().toLowerCase());
			if (cpm == null) {
				return null;
			}

			return bi.tableName;
		} else if (fromItem instanceof SubSelect) {
			SelectBody sb = ((SubSelect) fromItem).getSelectBody();
			if (sb instanceof PlainSelect) {
				PlainSelect ps = (PlainSelect) sb;
				tableName = findTableNameFromPlainSelect(tableColumn,
						ps.getSelectItems(), ps.getFromItem(), ps.getJoins());

			} else {
				throw new FreyjaException("not supported hql");
			}
		} else {

		}
		return tableName;
	}

	public static String findTableNameFromPlainSelect(Column tableColumn,
			List<SelectItem> selectItems, FromItem fromItem, List<Join> joins) {
		String tableName = null;
		if (selectItems == null) {
			tableName = findTableNameFromFromItem(tableColumn, fromItem);

			if (tableName != null) {
				return tableName;
			}

			if (joins != null) {
				for (Join join : joins) {
					tableName = findTableNameFromFromItem(tableColumn,
							join.getRightItem());
					if (tableName != null) {
						return tableName;
					}
				}
			}
		} else {
			for (SelectItem si : selectItems) {
				if (si instanceof AllColumns) {
					tableName = findTableNameFromPlainSelect(tableColumn, null,
							fromItem, joins);

				} else if (si instanceof AllTableColumns) {

					String alias = ((AllTableColumns) si).getTable().getName();

					if (alias.equals(fromItem.getAlias())) {
						tableName = findTableNameFromFromItem(tableColumn,
								fromItem);
						if (tableName != null) {
							return tableName;
						} else {
							throw new FreyjaException("not supported hql");
						}
					}
					if (joins != null) {
						for (Join join : joins) {
							if (alias.equals(join.getRightItem())) {
								tableName = findTableNameFromFromItem(
										tableColumn, join.getRightItem());
								if (tableName != null) {
									return tableName;
								} else {
									throw new FreyjaException(
											"not supported hql");
								}
							}
						}
					}
				} else if (si instanceof SelectExpressionItem) {
					SelectExpressionItem sei = (SelectExpressionItem) si;
					if (sei.getExpression() instanceof Column) {
						Column column = (Column) sei.getExpression();
						if (tableColumn.getColumnName().equals(sei.getAlias())) {

							tableName = findTableNameFromPlainSelect(column,
									null, fromItem, joins);
							if (tableName != null) {
								return tableName;
							}

						} else if (tableColumn.getColumnName().equals(
								column.getColumnName())) {
							tableName = findTableNameFromPlainSelect(column,
									null, fromItem, joins);
							if (tableName != null) {
								return tableName;
							}
						}
					} else {
						return null;
					}
				}
			}
		}
		return tableName;
	}
}
