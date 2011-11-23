package org.freyja.deparser.hql;

import java.util.Iterator;
import java.util.List;

import org.freyja.FreyjaException;
import org.freyja.jdbc.object.BeanInfo;
import org.freyja.jdbc.object.BeanInfoCache;
import org.freyja.jdbc.object.ColumnPropertyMapping;
import org.freyja.jdbc.object.HqlMapping;
import org.freyja.sql.SqlParser;
import org.springframework.jdbc.core.SqlParameter;

import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.InverseExpression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SubSelect;

public class ExpressionDeParser implements ExpressionVisitor, ItemsListVisitor {

	protected SelectDeParser selectVisitor;
	protected boolean useBracketsInExprList = true;

	private HqlMapping hmp;

	public HqlMapping getHmp() {
		return hmp;
	}

	public void setHmp(HqlMapping hmp) {
		this.hmp = hmp;
	}

	private Table table;

	public Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		this.table = table;
	}

	public ExpressionDeParser() {
	}

	public ExpressionDeParser(SelectDeParser selectVisitor) {
		this.selectVisitor = selectVisitor;
	}

	public void visit(Addition addition) {
		visitBinaryExpression(addition, " + ");
	}

	public void visit(AndExpression andExpression) {
		visitBinaryExpression(andExpression, " and ");
	}

	public void visit(Between between) {
		between.getLeftExpression().accept(this);

		between.getBetweenExpressionStart().accept(this);
		between.getBetweenExpressionEnd().accept(this);

	}

	public void visit(Division division) {
		visitBinaryExpression(division, " / ");

	}

	public void visit(DoubleValue doubleValue) {

	}

	public void visit(EqualsTo equalsTo) {
		visitBinaryExpression(equalsTo, " = ");
	}

	public void visit(GreaterThan greaterThan) {
		visitBinaryExpression(greaterThan, " > ");
	}

	public void visit(GreaterThanEquals greaterThanEquals) {
		visitBinaryExpression(greaterThanEquals, " >= ");

	}

	public void visit(InExpression inExpression) {

		inExpression.getLeftExpression().accept(this);

		inExpression.getItemsList().accept(this);
	}

	public void visit(InverseExpression inverseExpression) {
		inverseExpression.getExpression().accept(this);
	}

	public void visit(IsNullExpression isNullExpression) {
		isNullExpression.getLeftExpression().accept(this);
	}

	public void visit(JdbcParameter jdbcParameter) {

	}

	public void visit(LikeExpression likeExpression) {
		visitBinaryExpression(likeExpression, " like ");

	}

	public void visit(ExistsExpression existsExpression) {
		existsExpression.getRightExpression().accept(this);
	}

	public void visit(LongValue longValue) {

	}

	public void visit(MinorThan minorThan) {
		visitBinaryExpression(minorThan, " < ");

	}

	public void visit(MinorThanEquals minorThanEquals) {
		visitBinaryExpression(minorThanEquals, " <= ");

	}

	public void visit(Multiplication multiplication) {
		visitBinaryExpression(multiplication, " * ");

	}

	public void visit(NotEqualsTo notEqualsTo) {
		visitBinaryExpression(notEqualsTo, " <> ");

	}

	public void visit(NullValue nullValue) {

	}

	public void visit(OrExpression orExpression) {
		visitBinaryExpression(orExpression, " or ");

	}

	public void visit(Parenthesis parenthesis) {
		parenthesis.getExpression().accept(this);

	}

	public void visit(StringValue stringValue) {

	}

	public void visit(Subtraction subtraction) {
		visitBinaryExpression(subtraction, "-");

	}

	private void visitBinaryExpression(BinaryExpression binaryExpression,
			String operator) {
		binaryExpression.getLeftExpression().accept(this);
		binaryExpression.getRightExpression().accept(this);

	}

	public void visit(SubSelect subSelect) {

		hmp.supportEhcacheSearch = false;
		hmp.supportQueryCache = false;

		subSelect.getSelectBody().accept(selectVisitor);
	}

	// private String findColumnFromSelectItem(Column tableColumn,
	// List<SelectItem> selectItems) {
	//
	// return null;
	// }

	public void visit(Column tableColumn) {

		hmp.queryCacheKeys.add(tableColumn.getColumnName().toLowerCase());

		String tableName = null;
		PlainSelect ps = selectVisitor.getPlainSelect();
		if (ps == null) {
			tableName = SqlParser.findTableNameFromPlainSelect(tableColumn,
					null, table, null);
		} else {

			FromItem fromItem = ps.getFromItem();
			tableName = SqlParser.findTableNameFromPlainSelect(tableColumn,
					null, fromItem, ps.getJoins());
		}

		if (tableName == null) {
			return;
		}
		BeanInfo<?> bi = BeanInfoCache.get(tableName);

		ColumnPropertyMapping cpm = bi.propertyColumnMap.get(tableColumn
				.getColumnName().toLowerCase());
		tableColumn.setColumnName(cpm.columnName);

	}

	// public void visit(Column tableColumn) {
	// if (table != null) {
	//
	// SqlParser.parser(tableColumn, table);
	// return;
	// }
	//
	// PlainSelect ps = selectVisitor.getPlainSelect();
	// FromItem fromItem = ps.getFromItem();
	// Table columnTable = tableColumn.getTable();
	//
	// if (columnTable.getName() != null) {
	// if (fromItem instanceof Table) {
	// Table fromTable = (Table) fromItem;
	//
	// boolean f = SqlParser.parserManyToOne(tableColumn, fromTable);
	// if (f) {
	// return;
	// }
	// }
	// }
	// List<FromItem> fromItems = SqlParser.joinsToFromItems(ps.getJoins());
	// fromItems.add(fromItem);
	//
	// String tableName = SqlParser
	// .findTableName(tableColumn, fromItems, null);
	//
	// if (tableName == null) {
	// return;
	// }
	// BeanInfo<?> bi = BeanInfoCache.get(tableName);
	// ColumnPropertyMapping cpm = bi.propertyColumnMap.get(tableColumn
	// .getColumnName().toLowerCase());
	// tableColumn.setColumnName(cpm.columnName);
	// }

	public void visit(Function function) {

		hmp.supportQueryCache = false;
		hmp.single = false;
		if (function.getParameters() != null) {
			List<Expression> expressions = function.getParameters()
					.getExpressions();
			for (Expression ex : expressions) {
				ex.accept(this);
			}
		}
	}

	public void visit(ExpressionList expressionList) {
		for (Iterator iter = expressionList.getExpressions().iterator(); iter
				.hasNext();) {
			Expression expression = (Expression) iter.next();
			expression.accept(this);
		}
	}

	public SelectDeParser getSelectVisitor() {
		return selectVisitor;
	}

	public void setSelectVisitor(SelectDeParser visitor) {
		selectVisitor = visitor;
	}

	public void visit(DateValue dateValue) {
	}

	public void visit(TimestampValue timestampValue) {
		// if(hmp!=null){
		// hmp.supportEhcacheSearch=false;
		// }
	}

	public void visit(TimeValue timeValue) {
	}

	public void visit(CaseExpression caseExpression) {
		Expression switchExp = caseExpression.getSwitchExpression();
		if (switchExp != null) {
			switchExp.accept(this);
		}

		List clauses = caseExpression.getWhenClauses();
		for (Iterator iter = clauses.iterator(); iter.hasNext();) {
			Expression exp = (Expression) iter.next();
			exp.accept(this);
		}

		Expression elseExp = caseExpression.getElseExpression();
		if (elseExp != null) {
			elseExp.accept(this);
		}

	}

	public void visit(WhenClause whenClause) {
		whenClause.getWhenExpression().accept(this);
		whenClause.getThenExpression().accept(this);
	}

	public void visit(AllComparisonExpression allComparisonExpression) {
		allComparisonExpression.GetSubSelect().accept((ExpressionVisitor) this);
	}

	public void visit(AnyComparisonExpression anyComparisonExpression) {
		anyComparisonExpression.GetSubSelect().accept((ExpressionVisitor) this);
	}

	public void visit(Concat concat) {
		visitBinaryExpression(concat, " || ");
	}

	public void visit(Matches matches) {
		visitBinaryExpression(matches, " @@ ");
	}

	public void visit(BitwiseAnd bitwiseAnd) {
		visitBinaryExpression(bitwiseAnd, " & ");
	}

	public void visit(BitwiseOr bitwiseOr) {
		visitBinaryExpression(bitwiseOr, " | ");
	}

	public void visit(BitwiseXor bitwiseXor) {
		visitBinaryExpression(bitwiseXor, " ^ ");
	}

}