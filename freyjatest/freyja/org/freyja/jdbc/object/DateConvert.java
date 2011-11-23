package org.freyja.jdbc.object;

import java.text.SimpleDateFormat;

import org.apache.commons.beanutils.Converter;
/**string -> date*/
public class DateConvert implements Converter {

	private static SimpleDateFormat df;

	public DateConvert(String pattern) {
		df = new SimpleDateFormat(pattern);
	}

	public Object convert(Class arg0, Object arg1) {
		if (arg1 == null) {
			return null;
		}
		String p = (String) arg1;
		if (p == null || p.trim().length() == 0) {
			return null;
		}
		try {
			return df.parse(p.trim());
		} catch (Exception e) {
			return null;
		}
	}
}
