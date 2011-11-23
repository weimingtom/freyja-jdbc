package org.freyja.deparser.hql;

import org.freyja.FreyjaException;
import org.freyja.jdbc.object.BeanInfo;
import org.freyja.jdbc.object.BeanInfoCache;
import org.freyja.jdbc.object.HqlMapping;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;

public class StatementDeParser implements StatementVisitor {

	private HqlMapping hm;

	public void visit(CreateTable createTable) {
	}

	public void visit(Delete delete) {
		SelectDeParser selectDeParser = new SelectDeParser();
		ExpressionDeParser expressionDeParser = new ExpressionDeParser(
				selectDeParser);
		expressionDeParser.setTable(delete.getTable());
		expressionDeParser.setHmp(hm);
		selectDeParser.setExpressionVisitor(expressionDeParser);
		DeleteDeParser deleteDeParser = new DeleteDeParser(expressionDeParser);
		deleteDeParser.setHm(hm);
		deleteDeParser.deParse(delete);
		hm.delete=delete;
	}

	public void visit(Drop drop) {

	}

	public void visit(Insert insert) {

	}

	public void visit(Replace replace) {
	}

	public void visit(Select select) {
		SelectBody sb = select.getSelectBody();
		if (sb instanceof PlainSelect) {
			SelectDeParser selectDeParser = new SelectDeParser();


			ExpressionDeParser expressionDeParser = new ExpressionDeParser(
					selectDeParser);
			
			expressionDeParser.setHmp(hm);
			selectDeParser.setExpressionVisitor(expressionDeParser);

			selectDeParser.setHmp(hm);
			
			select.getSelectBody().accept(selectDeParser);

//			PlainSelect ps = (PlainSelect) sb;
//			FromItem fi = ps.getFromItem();
//			if (fi instanceof Table) {
//				Table table = (Table) fi;
//				if (ps.getSelectItems().size() == 1) {
//					SelectItem si = (SelectItem) ps.getSelectItems().get(0);
//					if (si instanceof AllColumns) {
//						hm.single = true;
//						hm.bi = BeanInfoCache.get(table.getName());
//					} else if (si instanceof AllTableColumns) {
//						AllTableColumns atc = (AllTableColumns) si;
//
//						if (atc.getTable().getName().equals(table.getAlias())) {
//							hm.single = true;
//							hm.bi = BeanInfoCache.get(table.getName());
//						}
//					}
//				}
//			}else{
//				hm.supportQueryCache=false;
//			}

			hm.sql = select.toString();
			hm.select=select;
		} else {
			throw new FreyjaException("not supported");
		}
	}

	public void visit(Truncate truncate) {

	}

	public void visit(Update update) {	
		
		SelectDeParser selectDeParser = new SelectDeParser();
		ExpressionDeParser expressionDeParser = new ExpressionDeParser(
				selectDeParser);
		expressionDeParser.setTable(update.getTable());
		expressionDeParser.setHmp(hm);
		selectDeParser.setExpressionVisitor(expressionDeParser);	
		UpdateDeParser updateDeParser = new UpdateDeParser(expressionDeParser);
		updateDeParser.setHm(hm);			
		updateDeParser.deParse(update);
		hm.update=update;
	}

	public HqlMapping getHm() {
		return hm;
	}

	public void setHm(HqlMapping hm) {
		this.hm = hm;
	}

}
