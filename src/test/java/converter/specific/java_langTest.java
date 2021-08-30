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

import org.junit.Test;

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.MJIEnv;

public class java_langTest extends TestJPF {
  // override vm.class property as it may have been set by other extensions of JPF
  private final static String[] JPF_ARGS = { "+test.vm.class = gov.nasa.jpf.vm.SingleProcessVM" };

  private static MJIEnv env;

  public static void main (String[] args) {
    runTestsOfThisClass(args);
  }

  public static void setEnv (MJIEnv env) {
    java_langTest.env = env;
  }
  
  private native Class<?> convertClassTest(Class<?> clazz);
  
  @Test
  public void convertClassTest() {
    if (verifyNoPropertyViolation(JPF_ARGS)) {
      Class<?> clazz = Integer.class;

      clazz = convertClassTest(clazz);
      assertEquals(Float.class, clazz);
    }
  }
  
  private native String convertStringTest(String string);
  
  @Test
  public void convertStringTest() {
    if (verifyNoPropertyViolation(JPF_ARGS)) {
      String string = "Zaphod Beeblebrox";
      
      string = convertStringTest(string);
      
      assertEquals("Ford Prefect", string);
    }
  }

}
