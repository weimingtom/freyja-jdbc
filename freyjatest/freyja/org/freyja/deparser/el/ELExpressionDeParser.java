package org.freyja.deparser.el;

import java.util.Iterator;
import java.util.List;

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
import net.sf.jsqlparser.statement.select.SubSelect;

public class ELExpressionDeParser implements ExpressionVisitor,
		ItemsListVisitor {

	protected StringBuffer buffer;
	protected boolean useBracketsInExprList = true;
	private int count = 0;
	private Object[] args;

	public ELExpressionDeParser() {
	}

	public ELExpressionDeParser(Object[] args) {
		this.args = args;
	}

	public StringBuffer getBuffer() {
		return buffer;
	}

	public void setBuffer(StringBuffer buffer) {
		this.buffer = buffer;
	}

	public void visit(Addition addition) {
		visitBinaryExpression(addition, " + ");
	}

	public void visit(AndExpression andExpression) {
		visitBinaryExpression(andExpression, " AND ");
	}

	public void visit(Between between) {
		between.getLeftExpression().accept(this);
		if (between.isNot())
			buffer.append(" NOT");

		buffer.append(" BETWEEN ");
		between.getBetweenExpressionStart().accept(this);
		buffer.append(" AND ");
		between.getBetweenExpressionEnd().accept(this);

	}

	public void visit(Division division) {
		visitBinaryExpression(division, " / ");

	}

	public void visit(DoubleValue doubleValue) {
		buffer.append(doubleValue.getValue());

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
		if (inExpression.isNot())
			buffer.append(" NOT");
		buffer.append(" IN ");

		inExpression.getItemsList().accept(this);
	}

	public void visit(InverseExpression inverseExpression) {
		buffer.append("-");
		inverseExpression.getExpression().accept(this);
	}

	public void visit(IsNullExpression isNullExpression) {
		isNullExpression.getLeftExpression().accept(this);
		if (isNullExpression.isNot()) {
			buffer.append(" IS NOT NULL");
		} else {
			buffer.append(" IS NULL");
		}
	}

	public void visit(JdbcParameter jdbcParameter) {
		// buffer.append("?");
		buffer.append(args[count]);
		count++;
	}

	public void visit(LikeExpression likeExpression) {
		visitBinaryExpression(likeExpression, " LIKE ");

	}

	public void visit(ExistsExpression existsExpression) {
		if (existsExpression.isNot()) {
			buffer.append(" NOT EXISTS ");
		} else {
			buffer.append(" EXISTS ");
		}
		existsExpression.getRightExpression().accept(this);
	}

	public void visit(LongValue longValue) {
		buffer.append(longValue.getStringValue());

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
		buffer.append("NULL");

	}

	public void visit(OrExpression orExpression) {
		visitBinaryExpression(orExpression, " OR ");

	}

	public void visit(Parenthesis parenthesis) {
		if (parenthesis.isNot())
			buffer.append(" NOT ");

		buffer.append("(");
		parenthesis.getExpression().accept(this);
		buffer.append(")");

	}

	public void visit(StringValue stringValue) {
		buffer.append("'" + stringValue.getValue() + "'");

	}

	public void visit(Subtraction subtraction) {
		visitBinaryExpression(subtraction, "-");

	}

	private void visitBinaryExpression(BinaryExpression binaryExpression,
			String operator) {
		if (binaryExpression.isNot())
			buffer.append(" NOT ");
		binaryExpression.getLeftExpression().accept(this);
		buffer.append(operator);
		binaryExpression.getRightExpression().accept(this);

	}

	public void visit(SubSelect subSelect) {
	}

	/** el表达式里面不能有table */
	public void visit(Column tableColumn) {
		buffer.append(tableColumn.getColumnName());
	}

	public void visit(Function function) {
		if (function.isEscaped()) {
			buffer.append("{fn ");
		}

		buffer.append(function.getName());
		if (function.isAllColumns()) {
			buffer.append("(*)");
		} else if (function.getParameters() == null) {
			buffer.append("()");
		} else {
			boolean oldUseBracketsInExprList = useBracketsInExprList;
			if (function.isDistinct()) {
				useBracketsInExprList = false;
				buffer.append("(DISTINCT ");
			}
			visit(function.getParameters());
			useBracketsInExprList = oldUseBracketsInExprList;
			if (function.isDistinct()) {
				buffer.append(")");
			}
		}

		if (function.isEscaped()) {
			buffer.append("}");
		}

	}

	public void visit(ExpressionList expressionList) {
		if (useBracketsInExprList)
			buffer.append("(");
		for (Iterator iter = expressionList.getExpressions().iterator(); iter
				.hasNext();) {
			Expression expression = (Expression) iter.next();
			expression.accept(this);
			if (iter.hasNext())
				buffer.append(", ");
		}
		if (useBracketsInExprList)
			buffer.append(")");
	}

	public void visit(DateValue dateValue) {
		buffer.append("{d '" + dateValue.getValue().toString() + "'}");
	}

	public void visit(TimestampValue timestampValue) {
		buffer.append("{ts '" + timestampValue.getValue().toString() + "'}");
	}

	public void visit(TimeValue timeValue) {
		buffer.append("{t '" + timeValue.getValue().toString() + "'}");
	}

	public void visit(CaseExpression caseExpression) {
		buffer.append("CASE ");
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

		buffer.append(" END");
	}

	public void visit(WhenClause whenClause) {
		buffer.append(" WHEN ");
		whenClause.getWhenExpression().accept(this);
		buffer.append(" THEN ");
		whenClause.getThenExpression().accept(this);
	}

	public void visit(AllComparisonExpression allComparisonExpression) {
		buffer.append(" ALL ");
		allComparisonExpression.GetSubSelect().accept((ExpressionVisitor) this);
	}

	public void visit(AnyComparisonExpression anyComparisonExpression) {
		buffer.append(" ANY ");
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