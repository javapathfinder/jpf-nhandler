//
//Copyright (C) 2006 United States Government as represented by the
//Administrator of the National Aeronautics and Space Administration
//(NASA).  All Rights Reserved.
//
//This software is distributed under the NASA Open Source Agreement
//(NOSA), version 1.3.  The NOSA has been approved by the Open Source
//Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
//directory tree for the complete NOSA document.
//
//THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
//KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
//LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
//SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
//A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
//THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
//DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//
/**
 * This is a raw test class, which produces AssertionErrors for all
 * cases we want to catch. Make double-sure we don't refer to any
 * JPF class in here, or we start to check JPF recursively.
 * To turn this into a Junt test, you have to write a wrapper
 * TestCase, which just calls the testXX() methods.
 * The Junit test cases run JPF.main explicitly by means of specifying
 * which test case to run, but be aware of this requiring proper
 * state clean up in JPF !
 *
 * KEEP IT SIMPLE - it's already bad enough we have to mimic unit tests
 * by means of system tests (use whole JPF to check if it works), we don't
 * want to make the observer problem worse by means of enlarging the scope
 * JPF has to look at
 *
 * Note that we don't use assert expressions, because those would already
 * depend on working java.lang.Class APIs
 */
package reflection;

import gov.nasa.jpf.util.test.TestJPF;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.junit.Test;

/**
 * test of java.lang.Class API
 */
public class ClassTest_core extends TestJPF implements Cloneable, Serializable {

  /**************************** tests **********************************/
  static String clsName = ClassTest_core.class.getName();

  int data = 42; // that creates a default ctor for our newInstance test

  @Test
  public void testClassForName () throws ClassNotFoundException {
    if (verifyNoPropertyViolation()) {

      Class<?> clazz = Class.forName(clsName);
      System.out.println("loaded " + clazz.getName());

      if (clazz == null) { throw new RuntimeException("Class.forName() returned null object"); }

      if (!clsName.equals(clazz.getName())) { throw new RuntimeException("getName() wrong for Class.forName() acquired class"); }
    }
  }

  @Test
  public void testClassForNameException () throws ClassNotFoundException {
    if (verifyUnhandledException("java.lang.ClassNotFoundException")) {
      Class<?> clazz = Class.forName("x.y.NonExisting");
    }
  }

  @Test
  public void testGetClass () {
    if (verifyNoPropertyViolation()) {
      Class<?> clazz = this.getClass();

      if (clazz == null) { throw new RuntimeException("Object.getClass() failed"); }

      if (!clsName.equals(clazz.getName())) { throw new RuntimeException("getName() wrong for getClass() acquired class"); }
    }
  }

  @Test
  public void testIdentity () {
    if (verifyNoPropertyViolation()) {
      Class<?> clazz1 = null;
      Class<?> clazz2 = ClassTest_core.class;
      Class<?> clazz3 = this.getClass();

      try {
        clazz1 = Class.forName(clsName);
      } catch (Throwable x) {
        x = null; // Get rid of IDE warning
      }

      if (clazz1 != clazz2) { throw new RuntimeException("Class.forName() and class field not identical"); }

      if (clazz2 != clazz3) { throw new RuntimeException("Object.getClass() and class field not identical"); }
    }
  }

  @Test
  public void testNewInstance () throws InstantiationException, IllegalAccessException {
    if (verifyNoPropertyViolation()) {
      Class<?> clazz = ClassTest_core.class;
      ClassTest_core o = (ClassTest_core) clazz.newInstance();

      System.out.println("new instance: " + o);

      if (o.data != 42) { throw new RuntimeException("Class.newInstance() failed to call default ctor"); }
    }
  }

  static class InAccessible {
    private InAccessible () {
    }
  }

  @Test
  public void testNewInstanceFailAccess () throws IllegalAccessException, InstantiationException {
    if (verifyUnhandledException("java.lang.IllegalAccessException")) {
      Class<?> clazz = InAccessible.class;
      clazz.newInstance();
    }
  }

  static abstract class AbstractClass {
  }

  @Test
  public void testNewInstanceFailAbstract () throws IllegalAccessException, InstantiationException {
    if (verifyUnhandledException("java.lang.InstantiationException")) {
      Class<?> clazz = AbstractClass.class;
      clazz.newInstance();
    }
  }

  @Test
  public void testIsAssignableFrom () {
    if (verifyNoPropertyViolation()) {
      Class<?> clazz1 = Integer.class;
      Class<?> clazz2 = Object.class;

      assert clazz2.isAssignableFrom(clazz1);

      assert !clazz1.isAssignableFrom(clazz2);
    }
  }

  @Test
  public void testInstanceOf () {
    if (verifyNoPropertyViolation()) {
      assert this instanceof Cloneable;
      assert this instanceof TestJPF;
      assert this instanceof Object;

      if (this instanceof Runnable) {
        assert false : "negative instanceof test failed";
      }
    }
  }

  @Test
  public void testAsSubclass () {
    if (verifyNoPropertyViolation()) {
      Class<?> clazz1 = Float.class;

      Class<? extends Number> clazz2 = clazz1.asSubclass(Number.class);
      assert clazz2 != null;

      try {
        clazz1.asSubclass(String.class);
        assert false : "testAsSubclass() failed to throw ClassCastException";
      } catch (ClassCastException ccx) {
        ccx = null; // Get rid of IDE warning
      }
    }
  }

  @SuppressWarnings("null")
  @Test
  public void testClassField () {
    if (verifyNoPropertyViolation()) {

      Class<?> clazz = ClassTest_core.class;

      if (clazz == null) { throw new RuntimeException("class field not set"); }

      if (!clsName.equals(clazz.getName())) { throw new RuntimeException("getName() wrong for class field"); }
    }
  }

  @Test
  public void testInterfaces () {
    if (verifyNoPropertyViolation()) {
      Class<?>[] ifc = ClassTest_core.class.getInterfaces();
      if (ifc.length != 2) { throw new RuntimeException("wrong number of interfaces: " + ifc.length); }

      int n = ifc.length;
      String[] ifcs = { Cloneable.class.getName(), Serializable.class.getName() };
      for (int i = 0; i < ifcs.length; i++) {
        for (int j = 0; j < ifc.length; j++) {
          if (ifc[j].getName().equals(ifcs[i])) {
            n--;
            break;
          }
        }
      }

      if (n != 0) { throw new RuntimeException("wrong interface types: " + ifc[0].getName() + ',' + ifc[1].getName()); }
    }
  }

  static class TestClassBase {
    protected TestClassBase () {
    }

    public void foo () {
    }
  }

  interface TestIfc {
    void boo (); // 4

    void foo ();
  }

  static abstract class TestClass extends TestClassBase implements TestIfc {
    static {
      System.out.println("why is TestClass.<clinit>() executed?");
    }

    public TestClass () {
    }

    public TestClass (int a) {
      a = 0;
    }

    public void foo () {
    } // 1

    void bar () {
    } // 2

    public static void baz () {
    } // 3

  }

  @Test
  public void testMethods () {
    if (verifyNoPropertyViolation()) {

      Class<?> cls = TestClass.class;
      Method[] methods = cls.getMethods();

      boolean fooSeen = false, bazSeen = false, booSeen = false;

      for (int i = 0; i < methods.length; i++) {
        Method m = methods[i];
        Class<?> declCls = m.getDeclaringClass();
        String mname = m.getName();

        // we don't care about the Object methods
        if (declCls == Object.class) {
          methods[i] = null;
          continue;
        }

        // non-publics, <clinit> and <init> are filtered out

        if (declCls == TestClass.class) {
          if (mname.equals("foo")) {
            methods[i] = null;
            fooSeen = true;
            continue;
          }
          if (mname.equals("baz")) {
            methods[i] = null;
            bazSeen = true;
            continue;
          }
        }

        // TestClass is abstract and doesn't implement TestIfc.boo()
        if (declCls == TestIfc.class) {
          if (mname.equals("boo")) {
            methods[i] = null;
            booSeen = true;
            continue;
          }
        }
      }

      assert fooSeen : "no TestClass.foo() seen";
      assert bazSeen : "no TestClass.baz() seen";
      assert booSeen : "no TestIfc.boo() seen";

      for (int i = 0; i < methods.length; i++) {
        assert (methods[i] == null) : ("unexpected method in getMethods(): " + methods[i].getDeclaringClass().getName() + " : " + methods[i]);
      }
    }
  }

  private static class NestedClass {
  }

  @Test
  public void testGetEnclosingClassExist () {
    if (verifyNoPropertyViolation()) {
      Class<?> clz = NestedClass.class;
      Class<?> enclosingClass = clz.getEnclosingClass();
      assert enclosingClass == ClassTest_core.class;
    }
  }

  @Test
  public void testGetEnclosingClassNotExist () {
    if (verifyNoPropertyViolation()) {
      Class<?> clz = this.getClass();
      Class<?> enclosingClass = clz.getEnclosingClass();
      assert enclosingClass == null;
    }
  }
}
