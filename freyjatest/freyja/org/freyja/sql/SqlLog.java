package org.freyja.sql;

import org.apache.log4j.Logger;
/**hql sql log*/
public class SqlLog {
	public static boolean showSql = false;
	public static boolean showHql = false;
	public static Logger log = Logger.getLogger("Freyja");

	public static void showSql(String hql, String sql, Object... args) {
		String argString = "";
		if (showHql || showSql) {
			if (args != null && args.length != 0) {
				argString += " [args] ";
				for (Object obj : args) {
					if (obj != null) {
						argString += obj.toString() + " ";
					}
				}
			}
		}
		if (showHql) {
			if (hql != null) {
				String h = " [HQL] " + hql + argString;
				log.info(h);
			}
		}

		if (showSql) {
			if (sql != null) {
				String s = " [SQL] " + sql + argString;
				log.info(s);
			}
		}

		if (showHql || showSql) {
			String s = "";
			log.info(s);
		}
	}
}
