package org.freyja.deparser.hql;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.freyja.jdbc.core.FreyjaJdbcAccessor;
import org.freyja.jdbc.object.BeanInfo;
import org.freyja.jdbc.object.BeanInfoCache;
import org.freyja.jdbc.object.HqlMapping;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.OrderByVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.Top;
import net.sf.jsqlparser.statement.select.Union;

public class SelectDeParser implements SelectVisitor, OrderByVisitor,
		SelectItemVisitor, FromItemVisitor {
	protected ExpressionDeParser expressionVisitor;

	private PlainSelect plainSelect;

	private HqlMapping hmp;

	public HqlMapping getHmp() {
		return hmp;
	}

	public void setHmp(HqlMapping hmp) {
		this.hmp = hmp;
	}

	public PlainSelect getPlainSelect() {
		return plainSelect;
	}

	public void setPlainSelect(PlainSelect plainSelect) {
		this.plainSelect = plainSelect;
	}

	public SelectDeParser() {
	}

	public SelectDeParser(ExpressionDeParser expressionVisitor,
			StringBuffer buffer) {
		this.expressionVisitor = expressionVisitor;
	}

	public void visit(PlainSelect plainSelect) {
		this.plainSelect = plainSelect;

		if (plainSelect.getDistinct() != null) {
			if (plainSelect.getDistinct().getOnSelectItems() != null) {
				for (Iterator iter = plainSelect.getDistinct()
						.getOnSelectItems().iterator(); iter.hasNext();) {
					SelectItem selectItem = (SelectItem) iter.next();
					selectItem.accept(this);
				}
			}
		}
		if (plainSelect.getSelectItems().size() != 1) {

			hmp.single = false;
		}
		for (Iterator iter = plainSelect.getSelectItems().iterator(); iter
				.hasNext();) {
			SelectItem selectItem = (SelectItem) iter.next();
			selectItem.accept(this);
		}
		if (plainSelect.getWhere() != null) {
			plainSelect.getWhere().accept(expressionVisitor);
		}

		if (plainSelect.getGroupByColumnReferences() != null) {
			for (Iterator iter = plainSelect.getGroupByColumnReferences()
					.iterator(); iter.hasNext();) {
				Expression columnReference = (Expression) iter.next();
				columnReference.accept(expressionVisitor);
			}
		}

		if (plainSelect.getHaving() != null) {
			plainSelect.getHaving().accept(expressionVisitor);
		}

		if (plainSelect.getOrderByElements() != null) {
			deparseOrderBy(plainSelect.getOrderByElements());
		}

		if (plainSelect.getLimit() != null) {
			deparseLimit(plainSelect.getLimit());
		}

		if (plainSelect.getJoins() != null) {

			for (Iterator iter = plainSelect.getJoins().iterator(); iter
					.hasNext();) {
				Join join = (Join) iter.next();
				deparseJoin(join);
			}
		}

		if (plainSelect.getFromItem() != null) {
			plainSelect.getFromItem().accept(this);
		}

		// List<Join> joins = plainSelect.getJoins();

		// if (hm.joinMap.size() != 0) {
		// if (joins == null) {
		// joins = new ArrayList<Join>();
		// }
		// for (Entry<String, Join> entry : hm.joinMap.entrySet()) {
		// joins.add(entry.getValue());
		// }
		// plainSelect.setJoins(joins);
		// }

	}

	public void visit(Union union) {
		for (Iterator iter = union.getPlainSelects().iterator(); iter.hasNext();) {
			PlainSelect plainSelect = (PlainSelect) iter.next();
			plainSelect.accept(this);

		}

		if (union.getOrderByElements() != null) {
			deparseOrderBy(union.getOrderByElements());
		}

		if (union.getLimit() != null) {
			deparseLimit(union.getLimit());
		}

	}

	public void visit(OrderByElement orderBy) {
		orderBy.getExpression().accept(expressionVisitor);
	}

	public void visit(Column column) {
	}

	public void visit(AllColumns allColumns) {

	}

	public void visit(AllTableColumns allTableColumns) {
		hmp.single = false;
	}

	public void visit(SelectExpressionItem selectExpressionItem) {
		selectExpressionItem.getExpression().accept(expressionVisitor);

	}

	public void visit(SubSelect subSelect) {

		hmp.single = false;
		subSelect.getSelectBody().accept(this);
	}

	public void visit(Table tableName) {

		BeanInfo<?> bi = BeanInfoCache.get(tableName.getName());
		hmp.bi = bi;
		tableName.setName(bi.tableName);

	}

	public void deparseOrderBy(List orderByElements) {
		for (Iterator iter = orderByElements.iterator(); iter.hasNext();) {
			OrderByElement orderByElement = (OrderByElement) iter.next();
			orderByElement.accept(this);
		}
	}

	public void deparseLimit(Limit limit) {

	}

	public ExpressionDeParser getExpressionVisitor() {
		return expressionVisitor;
	}

	public void setExpressionVisitor(ExpressionDeParser visitor) {
		expressionVisitor = visitor;
	}

	public void visit(SubJoin subjoin) {

		hmp.single = false;
		subjoin.getLeft().accept(this);
		deparseJoin(subjoin.getJoin());
	}

	public void deparseJoin(Join join) {

		hmp.single = false;
		if (join.getOnExpression() != null) {
			join.getOnExpression().accept(expressionVisitor);
		}

		FromItem fromItem = join.getRightItem();
		fromItem.accept(this);

	}

}
