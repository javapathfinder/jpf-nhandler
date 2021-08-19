/* 
 * Copyright (C) 2013  Nastaran Shafiei
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You can find a copy of the GNU General Public License at
 * <http://www.gnu.org/licenses/>.
 */

package example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DB {
	private static String driver = "org.apache.derby.jdbc.EmbeddedDriver";

	private static String protocol = "jdbc:derby:memory";

	private static String dbPath = "/";

	private static String url = protocol + dbPath + "MemDB;create=true;";

	private static Statement stmt = null;

	private static Connection conn = null;

	private static String table = "MemTable";

	protected static void createConnection () {
		try {
			Class.forName(driver).newInstance();
			// Get a connection
			conn = DriverManager.getConnection(url);
		} catch (Exception except) {
			except.printStackTrace();
		}
	}

	protected static void createTable () {
		try {
			stmt = conn.createStatement();

			// Lets get rid of an existing table, if any, OW if the table exist already we get exception
			stmt.execute("drop table " + table);

			// We create a table...
			stmt.execute("create table " + table + "(num int, addr varchar(40))");
		} catch (SQLException e) {

			try {
				stmt.execute("create table " + table + "(num int, addr varchar(40))");
				return;
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}

	protected static void insertAddress (int number, String street) {
		try {
			stmt = conn.createStatement();
			stmt.execute("insert into " + table + " values (" + number + ",'" + street + "')");
			stmt.close();
		} catch (SQLException sqlExcept) {
			sqlExcept.printStackTrace();
		}
	}

	protected static void shutdown () {
		try {
			if (stmt != null) {
				stmt.close();
			}
			if (conn != null) {
				DriverManager.getConnection(url + ";shutdown=true");
				conn.close();
			}
		} catch (SQLException sqlExcept) {

		}
	}
}
