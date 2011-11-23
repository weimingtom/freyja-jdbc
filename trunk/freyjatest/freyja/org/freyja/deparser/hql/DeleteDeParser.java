package org.freyja.deparser.hql;

import java.util.Map.Entry;

import org.freyja.jdbc.object.BeanInfo;
import org.freyja.jdbc.object.BeanInfoCache;
import org.freyja.jdbc.object.HqlMapping;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.Join;

public class DeleteDeParser {
	protected ExpressionDeParser expressionVisitor;
	private HqlMapping hm;

	public HqlMapping getHm() {
		return hm;
	}

	public void setHm(HqlMapping hm) {
		this.hm = hm;
	}

	public DeleteDeParser() {
	}

	public DeleteDeParser(ExpressionDeParser expressionVisitor) {

		this.expressionVisitor = expressionVisitor;
	}

	public void deParse(Delete delete) {

		StringBuffer sb2 = new StringBuffer();
		if (delete.getWhere() != null) {
			sb2.append(" WHERE ");
			delete.getWhere().accept(expressionVisitor);
			sb2.append(delete.getWhere());
		}

		StringBuffer sb1 = new StringBuffer();

		Table table = delete.getTable();
		BeanInfo<?> bi = BeanInfoCache.get(table.getName());
		hm.bi = bi;
		table.setName(bi.tableName);

//		if (hm.joinMap.size() == 0) {
			sb1.append("DELETE FROM " + delete.getTable().getWholeTableName());
//		} else {
//			sb1.append("DELETE " + table.getAlias() + " FROM "
//					+ table.getName() + " AS " + table.getAlias());
//			for (Entry<String, Join> entry : hm.joinMap.entrySet()) {
//				Join join = entry.getValue();
//				if (join.isSimple()) {
//					sb1.append(", " + join);
//				} else {
//					sb1.append(" " + join);
//				}
//			}
//		}

		hm.sql = sb1.append(sb2).toString();
	}

	public ExpressionDeParser getExpressionVisitor() {
		return expressionVisitor;
	}

	public void setExpressionVisitor(ExpressionDeParser visitor) {
		expressionVisitor = visitor;
	}

}
