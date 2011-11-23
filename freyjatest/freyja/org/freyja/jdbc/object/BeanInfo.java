package org.freyja.jdbc.object;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.sf.ehcache.config.SearchAttribute;
import net.sf.ehcache.config.Searchable;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.StatementCreatorUtils;

/** bean <-> table info */
public class BeanInfo<T> {

	public Class<T> clazz;

	public ColumnPropertyMapping idColumn;

	public String tableName;
	public List<String> insertColumns = new ArrayList<String>();

	public List<String> updateColumns = new ArrayList<String>();
	public Searchable searchable = new Searchable();
	public String insert;

	public String update;

	public String delete;

	public String select;

	public boolean supportEhcacheSearch = true;

	public int[] updateArgTypesArray;

	public List<Integer> insertArgTypes = new ArrayList<Integer>();;

	public Map<String, ColumnPropertyMapping> propertyColumnMap = new HashMap<String, ColumnPropertyMapping>();

	public Map<String, ColumnPropertyMapping> columnPropertyMap = new HashMap<String, ColumnPropertyMapping>();

	public Map<String, ColumnPropertyMapping> columnPropertyManyToOneColumnMap = new HashMap<String, ColumnPropertyMapping>();

	public Map<String, ColumnPropertyMapping> transientMap = new HashMap<String, ColumnPropertyMapping>();

	public BeanInfo(Class<T> clazz) {
		this.clazz = clazz;
		Table table = clazz.getAnnotation(Table.class);
		if (table != null) {
			tableName = table.name();
		} else {
			tableName = clazz.getSimpleName();
		}

		Field[] fields = clazz.getDeclaredFields();
		List<String> placeholders = new ArrayList<String>();
		List<String> updateColumnsHolders = new ArrayList<String>();

		List<Integer> updateArgTypes = new ArrayList<Integer>();

		for (int i = 0; i < fields.length; i++) {
			Field f = fields[i];
			PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(clazz,
					f.getName());
			if (pd == null || pd.getWriteMethod() == null) {
				continue;
			}

			Method readMethod = pd.getReadMethod();

			String c = "";

			Transient transientObj = f.getAnnotation(Transient.class);
			ColumnPropertyMapping cpm = new ColumnPropertyMapping(null,
					f.getName(), false, false, pd, f);
			if (transientObj != null) {
				if (pd.getWriteMethod() != null) {
					transientMap.put(f.getName(), cpm);
				}
				continue;
			} else {
				transientObj = readMethod.getAnnotation(Transient.class);
				if (transientObj != null) {
					if (pd.getWriteMethod() != null) {
						transientMap.put(f.getName(), cpm);
					}
					continue;
				}
			}

			Column column = f.getAnnotation(Column.class);

			if (column != null) {
				c = column.name();
			} else {
				column = readMethod.getAnnotation(Column.class);
				if (column != null) {
					c = column.name();
				} else {
					c = f.getName();
				}
			}

			boolean isId = false;
			Id id = f.getAnnotation(Id.class);
			if (id != null) {
				isId = true;
			} else {
				id = readMethod.getAnnotation(Id.class);
				if (id != null) {
					isId = true;
				}
			}
			boolean isManyToOne = false;
			ManyToOne manyToOne = f.getAnnotation(ManyToOne.class);
			if (manyToOne != null) {
				JoinColumn jc = f.getAnnotation(JoinColumn.class);
				c = jc.name();
				isManyToOne = true;
			} else {
				manyToOne = readMethod.getAnnotation(ManyToOne.class);
				if (manyToOne != null) {
					JoinColumn jc = readMethod.getAnnotation(JoinColumn.class);
					c = jc.name();
					isManyToOne = true;
				}
			}

			cpm.isPrimaryKey = isId;
			cpm.columnName = c;
			String lowerCaseColumnName = c.toLowerCase();
			if (isManyToOne) {
				columnPropertyManyToOneColumnMap.put(lowerCaseColumnName, cpm);
				continue;
			}
			if (isId) {
				idColumn = cpm;
			} else {
				updateArgTypes.add(StatementCreatorUtils
						.javaTypeToSqlParameterType(f.getType()));
				updateColumns.add(lowerCaseColumnName);
				updateColumnsHolders.add(c + " = ?");
			}

			if (supportEhcacheSearch
					&& !AttributeType.isSupportedType(f.getType())) {

				supportEhcacheSearch = false;

			}
			insertColumns.add(c);
			insertArgTypes.add(StatementCreatorUtils
					.javaTypeToSqlParameterType(f.getType()));

			searchable.addSearchAttribute(new SearchAttribute().name(
					pd.getName()).expression(
					"value." + readMethod.getName() + "()"));
			propertyColumnMap.put(pd.getName().toLowerCase(), cpm);
			columnPropertyMap.put(lowerCaseColumnName, cpm);

			placeholders.add("?");
		}

		updateArgTypes.add(StatementCreatorUtils
				.javaTypeToSqlParameterType(idColumn.field.getType()));

		updateArgTypesArray = new int[updateArgTypes.size()];
		for (int j = 0; j < updateArgTypes.size(); j++) {
			updateArgTypesArray[j] = updateArgTypes.get(j).intValue();
		}

		StringBuilder insertBuilder = new StringBuilder();
		insertBuilder.append("insert into ").append(tableName);
		insertBuilder.append(" (")
				.append(StringUtils.join(insertColumns.toArray(), ","))
				.append(") ");
		insertBuilder.append(" values (")
				.append(StringUtils.join(placeholders.toArray(), ","))
				.append(")");

		insert = insertBuilder.toString();

		StringBuilder updateBuilder = new StringBuilder();
		updateBuilder.append("update ").append(tableName);
		updateBuilder.append(" set ");
		updateBuilder.append(StringUtils.join(updateColumnsHolders.toArray(),
				","));
		updateBuilder.append(" where ").append(idColumn.columnName)
				.append(" = ?");
		update = updateBuilder.toString();

		delete = "delete from " + tableName + " where " + idColumn.columnName
				+ " = ?";
		select = "select * from " + tableName + " where " + idColumn.columnName
				+ " = ?";
	}
}
