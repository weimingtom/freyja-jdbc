package org.freyja.deparser.eql;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.ConvertUtils;
import org.freyja.FreyjaException;
import org.freyja.jdbc.object.BeanInfo;
import org.freyja.jdbc.object.BeanInfoCache;
import org.freyja.jdbc.object.ColumnPropertyMapping;
import org.freyja.jdbc.object.HqlMapping;
import org.freyja.sql.SqlParser;

import net.sf.ehcache.Cache;
import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.Query;
import net.sf.ehcache.search.expression.And;
import net.sf.ehcache.search.expression.Criteria;
import net.sf.ehcache.search.expression.EqualTo;
import net.sf.ehcache.search.expression.Or;
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
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SubSelect;

public class EQLExpressionDeParser implements ExpressionVisitor,
		ItemsListVisitor {

	protected boolean useBracketsInExprList = true;

	private Cache cache;
	private BeanInfo<?> bi;
	private Object[] args;
	private Criteria criteria;
	private int count = -1;

	public Criteria getCriteria() {
		return criteria;
	}

	public void setCriteria(Criteria criteria) {
		this.criteria = criteria;
	}

	public EQLExpressionDeParser() {
	}

	public EQLExpressionDeParser(Cache cache) {
		this.cache = cache;
	}

	public EQLExpressionDeParser(BeanInfo<?> bi, Cache cache, Object[] args) {
		this.bi = bi;
		this.cache = cache;
		this.args = args;
	}

	public void visit(Addition addition) {
		visitBinaryExpression(addition, " + ");
	}

	public void visit(AndExpression andExpression) {

		andExpression.getLeftExpression().accept(this);
		Criteria leftCriteria = criteria;

		andExpression.getRightExpression().accept(this);
		Criteria rightCriteria = criteria;
		criteria = new And(leftCriteria, rightCriteria);

		// visitBinaryExpression(andExpression, " and ");
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
		Expression leftExpression = equalsTo.getLeftExpression();
		Expression rightExpression = equalsTo.getRightExpression();
		if (leftExpression instanceof Column) {
			Object[] arr = getAttribute(leftExpression);
			Attribute attribute = (Attribute) arr[0];
			criteria = attribute.eq(parser(rightExpression, (Class) arr[1]));
		} else {
			if (rightExpression instanceof Column) {
				Object[] arr = getAttribute(rightExpression);
				Attribute attribute = (Attribute) arr[0];
				criteria = attribute.eq(parser(leftExpression, (Class) arr[1]));
			}
		}
		visitBinaryExpression(equalsTo, " = ");
	}

	private Object parser(Expression rightExpression, Class clazz) {

		if (rightExpression instanceof JdbcParameter) {

			count++;
			Object obj = ConvertUtils.convert(args[count].toString(), clazz);
			return obj;
		} else if (rightExpression instanceof StringValue) {

			return ConvertUtils.convert(
					((StringValue) rightExpression).getValue(), clazz);
		} else {
			return ConvertUtils.convert(rightExpression.toString(), clazz);

		}

	}

	private Object[] getAttribute(Expression leftExpression) {

		String cname = ((Column) leftExpression).getColumnName();
		ColumnPropertyMapping cpm = bi.columnPropertyMap.get(cname
				.toLowerCase());
		String propertyName = cpm.propertyName;
		return new Object[] { cache.getSearchAttribute(propertyName),
				cpm.pd.getPropertyType() };
	}

	public void visit(GreaterThan greaterThan) {
		Expression leftExpression = greaterThan.getLeftExpression();
		Expression rightExpression = greaterThan.getRightExpression();

		if (leftExpression instanceof Column) {
			Object[] arr = getAttribute(leftExpression);
			Attribute attribute = (Attribute) arr[0];
			criteria = attribute.gt(parser(rightExpression, (Class) arr[1]));
		} else {
			if (rightExpression instanceof Column) {
				Object[] arr = getAttribute(rightExpression);
				Attribute attribute = (Attribute) arr[0];
				criteria = attribute.gt(parser(leftExpression, (Class) arr[1]));
			}
		}
		// visitBinaryExpression(greaterThan, " > ");
	}

	public void visit(GreaterThanEquals greaterThanEquals) {
		Expression leftExpression = greaterThanEquals.getLeftExpression();
		Expression rightExpression = greaterThanEquals.getRightExpression();

		if (leftExpression instanceof Column) {
			Object[] arr = getAttribute(leftExpression);
			Attribute attribute = (Attribute) arr[0];
			criteria = attribute.ge(parser(rightExpression, (Class) arr[1]));
		} else {
			if (rightExpression instanceof Column) {
				Object[] arr = getAttribute(rightExpression);
				Attribute attribute = (Attribute) arr[0];
				criteria = attribute.ge(parser(leftExpression, (Class) arr[1]));
			}
		}

		visitBinaryExpression(greaterThanEquals, " >= ");

	}

	public void visit(InExpression inExpression) {

		Expression leftExpression = inExpression.getLeftExpression();
		if (leftExpression instanceof Column) {
			Object[] arr = getAttribute(leftExpression);
			Attribute attribute = (Attribute) arr[0];

			if (inExpression.getItemsList() instanceof ExpressionList) {
				ExpressionList el = (ExpressionList) inExpression
						.getItemsList();
				List list = new ArrayList(el.getExpressions().size());
				for (Object obj : el.getExpressions()) {
					Expression e = (Expression) obj;
					list.add(parser(e, (Class) arr[1]));
				}
				criteria = attribute.in(list);
			}

		}

		// inExpression.getLeftExpression().accept(this);
		//
		// inExpression.getItemsList().accept(this);
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

		Expression leftExpression = minorThan.getLeftExpression();
		Expression rightExpression = minorThan.getRightExpression();
		if (leftExpression instanceof Column) {
			Object[] arr = getAttribute(leftExpression);
			Attribute attribute = (Attribute) arr[0];
			criteria = attribute.lt(parser(rightExpression, (Class) arr[1]));
		} else {
			if (rightExpression instanceof Column) {
				Object[] arr = getAttribute(rightExpression);
				Attribute attribute = (Attribute) arr[0];
				criteria = attribute.lt(parser(leftExpression, (Class) arr[1]));
			}

		}
		// visitBinaryExpression(minorThan, " < ");

	}

	public void visit(MinorThanEquals minorThanEquals) {
		Expression leftExpression = minorThanEquals.getLeftExpression();
		Expression rightExpression = minorThanEquals.getRightExpression();
		if (leftExpression instanceof Column) {
			Object[] arr = getAttribute(leftExpression);
			Attribute attribute = (Attribute) arr[0];
			criteria = attribute.le(parser(rightExpression, (Class) arr[1]));
		} else {
			if (rightExpression instanceof Column) {
				Object[] arr = getAttribute(rightExpression);
				Attribute attribute = (Attribute) arr[0];
				criteria = attribute.le(parser(leftExpression, (Class) arr[1]));
			}
		}

		visitBinaryExpression(minorThanEquals, " <= ");

	}

	public void visit(Multiplication multiplication) {
		visitBinaryExpression(multiplication, " * ");

	}

	public void visit(NotEqualsTo notEqualsTo) {
		Expression leftExpression = notEqualsTo.getLeftExpression();
		Expression rightExpression = notEqualsTo.getRightExpression();
		if (leftExpression instanceof Column) {
			Object[] arr = getAttribute(leftExpression);
			Attribute attribute = (Attribute) arr[0];
			criteria = attribute.ne(parser(rightExpression, (Class) arr[1]));
		} else {
			if (rightExpression instanceof Column) {
				Object[] arr = getAttribute(rightExpression);
				Attribute attribute = (Attribute) arr[0];
				criteria = attribute.ne(parser(leftExpression, (Class) arr[1]));
			}
		}

		// visitBinaryExpression(notEqualsTo, " <> ");

	}

	public void visit(NullValue nullValue) {

	}

	public void visit(OrExpression orExpression) {

		orExpression.getLeftExpression().accept(this);
		Criteria leftCriteria = criteria;

		orExpression.getRightExpression().accept(this);
		Criteria rightCriteria = criteria;
		criteria = new Or(leftCriteria, rightCriteria);

		// visitBinaryExpression(orExpression, " or ");

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
	}

	public void visit(Column tableColumn) {
	}

	public void visit(Function function) {
	}

	public void visit(ExpressionList expressionList) {
	}

	public void visit(DateValue dateValue) {
	}

	public void visit(TimestampValue timestampValue) {
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