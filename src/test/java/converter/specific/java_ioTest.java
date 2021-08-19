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

package converter.specific;

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.MJIEnv;

import java.io.File;

import org.junit.Test;

public class java_ioTest extends TestJPF {

  private final static String[] JPF_ARGS = {};

  private static MJIEnv env;

  public static void main (String[] args) {
    runTestsOfThisClass(args);
  }

  public static void setEnv (MJIEnv env) {
    java_ioTest.env = env;
  }

  public static final String FILE_PATH1 = "42.txt",
      FILE_PATH2 = "43.txt";
  
  private native File convertFileTest(File file);
  
  @Test
  public void convertFileTest() {
    
    if (verifyNoPropertyViolation(JPF_ARGS)) {
      File file = new File(FILE_PATH1);
      
      file = convertFileTest(file);
      assertEquals(FILE_PATH2, file.getPath());
    }
  }

}
