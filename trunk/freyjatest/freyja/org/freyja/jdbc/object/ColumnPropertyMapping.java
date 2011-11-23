package org.freyja.jdbc.object;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
/**column <-> property info*/
public class ColumnPropertyMapping {
	public String columnName;
	public String propertyName;
	public boolean isPrimaryKey = false;
	public PropertyDescriptor pd;
	public Field field;
	public boolean isManyToOne = false;

	public ColumnPropertyMapping() {
	}

	public ColumnPropertyMapping(String columnName, String propertyName,
			boolean isPrimaryKey,boolean isManyToOne, PropertyDescriptor pd, Field field) {
		this.columnName = columnName;
		this.propertyName = propertyName;
		this.isManyToOne = isManyToOne;
		this.isPrimaryKey = isPrimaryKey;
		this.pd = pd;
		this.field = field;
	}

}
