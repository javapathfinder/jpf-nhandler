/* 
 * Copyright (C) 2013  Nastaran Shafiei and Franck van Breugel
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

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Scanner;

public class ScraperScanner {
  public static void main (String[] args) throws IOException {
    URL url = new URL("http://www.nastaran.ca/");
    InputStreamReader reader = new InputStreamReader(url.openStream());
    Scanner scanner = new Scanner(reader);
    while (scanner.hasNextLine()) {
      System.out.println(scanner.nextLine());
    }
    reader.close();
  }
}
