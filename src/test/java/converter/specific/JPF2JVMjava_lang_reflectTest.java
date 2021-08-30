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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Test;

public class JPF2JVMjava_lang_reflectTest extends TestJPF {
  // override vm.class property as it may have been set by other extensions of JPF
  private final static String[] JPF_ARGS = { "+test.vm.class = gov.nasa.jpf.vm.SingleProcessVM" };

  private static MJIEnv env;

  public static void main (String[] args) {
    runTestsOfThisClass(args);
  }

  public static void setEnv (MJIEnv env) {
    JPF2JVMjava_lang_reflectTest.env = env;
  }

  private native void convertMethodTest (Method meth1, Method meth2);

  @Test
  public void convertMethodTest () {
    if (verifyNoPropertyViolation(JPF_ARGS)) {
      
      Method meth1 = null;
      Method meth2 = null;
      try {
        meth1 = JPF2JVMjava_lang_reflectTest.TestClass.class.getDeclaredMethod("meth", String.class, int.class, int[][][].class, String[][][][].class);
        meth2 = JPF2JVMjava_lang_reflectTest.TestClass.class.getDeclaredMethod("meth", String.class, int.class, String.class, String.class);
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (SecurityException e) {
        e.printStackTrace();
      }
      meth1.setAccessible(true);
      meth2.setAccessible(false);

      convertMethodTest(meth1, meth2);
    }
  }
  
  private native void convertConstructorTest (Constructor<?> ctor1, Constructor<?> ctor2);
  
  @Test
  public void convertConstructorTest() {
    if (verifyNoPropertyViolation(JPF_ARGS)) {
      
      Constructor<?> ctor1 = null, ctor2 = null;
      try {
        ctor1 = JPF2JVMjava_lang_reflectTest.TestClass.class.getConstructor(String.class);
        ctor2 = JPF2JVMjava_lang_reflectTest.TestClass.class.getConstructor(Integer.class);
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (SecurityException e) {
        e.printStackTrace();
      }
      
      ctor1.setAccessible(true);
      ctor2.setAccessible(false);
      
      convertConstructorTest(ctor1, ctor2);
    }
  }
  
  private native void convertFieldTest (Field field1, Field field2);
  
  @Test
  public void convertFieldTest() {
    if(verifyNoPropertyViolation(JPF_ARGS)) {
      
      Field field1 = null, field2 = null;
      try {
        field1 = JPF2JVMjava_lang_reflectTest.TestClass.class.getDeclaredField("field1");
        field2 = JPF2JVMjava_lang_reflectTest.TestClass.class.getDeclaredField("field2");
      } catch (NoSuchFieldException e) {
        e.printStackTrace();
      } catch (SecurityException e) {
        e.printStackTrace();
      }
      
      field1.setAccessible(true);
      field2.setAccessible(false);
      
      convertFieldTest(field1, field2);
    }
  }
  
  public static class TestClass <T> {
    
    public int f1 = 0;
    
    public int field1 = 1, field2 = 2;
    
    public T genericField;
    
    public TestClass () {
      
    }
    
    public TestClass (String s) {
      f1 = 1;
    }
    
    public static int meth (String s, int i, int[][][] i2, String[][][][] s2) {
      return 1;
    }

    /**
     * To check whether the right method is invoked using reflection when
     * arguments and name are ambiguous, but the Method object represents the
     * right method.
     * For example, meth.invoke(object, null, 0, null, null)
     * This seemed to be happening when the Method.slot field wasn't being set
     * correctly
     */
    public int meth (String s, int i, String s2, String s3) {
      return 2;
    }
    
    public TestClass (Integer i) {
      f1 = 2;
    }
  }
}
