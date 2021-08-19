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

public class DBAccess {
	public static void main(String[] a) throws Exception {
		DB.createConnection();
		DB.createTable();
		DB.insertAddress(1245, "Lakeside dr.");
		DB.insertAddress(300, "Bloor street East");
		DB.shutdown();
	}
}