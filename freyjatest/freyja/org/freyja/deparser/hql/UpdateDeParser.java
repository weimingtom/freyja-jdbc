package org.freyja.deparser.hql;


import org.freyja.jdbc.object.BeanInfo;
import org.freyja.jdbc.object.BeanInfoCache;
import org.freyja.jdbc.object.HqlMapping;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.update.Update;

public class UpdateDeParser {
	protected ExpressionDeParser expressionVisitor;
	private HqlMapping hm;

	public HqlMapping getHm() {
		return hm;
	}

	public void setHm(HqlMapping hm) {
		this.hm = hm;
	}

	public UpdateDeParser() {
	}

	public UpdateDeParser(ExpressionDeParser expressionVisitor) {
		this.expressionVisitor = expressionVisitor;
	}
	public void deParse(Update update) {
		Table table = update.getTable();

	

		StringBuffer sb1 = new StringBuffer();
		for (int i = 0; i < update.getColumns().size(); i++) {
			Column column = (Column) update.getColumns().get(i);			
			column.accept(expressionVisitor);			
			sb1.append(column.getWholeColumnName() + "=");
			Expression expression = (Expression) update.getExpressions().get(i);
			if (expression instanceof JdbcParameter) {
				hm.jdbcParameterNumber++;
			}
			expression.accept(expressionVisitor);
			sb1.append(expression);
			if (i < update.getColumns().size() - 1) {
				sb1.append(", ");
			}
		}

		StringBuffer sb3 = new StringBuffer();
		if (update.getWhere() != null) {
			sb3.append(" WHERE ");
			update.getWhere().accept(expressionVisitor);

			sb3.append(update.getWhere());
		}

		StringBuffer sb2 = new StringBuffer();

		BeanInfo<?> bi = BeanInfoCache.get(table.getName());
		hm.bi = bi;
		table.setName(bi.tableName);
		sb2.append("UPDATE " + table.toString());
		
		sb2.append(" set ");

		hm.sql = sb2.append(sb1).append(sb3).toString();
	}

	public ExpressionDeParser getExpressionVisitor() {
		return expressionVisitor;
	}

	public void setExpressionVisitor(ExpressionDeParser visitor) {
		expressionVisitor = visitor;
	}
}
