package converter.specific;

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.MJIEnv;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.junit.Test;

public class JPF2JVMjava_lang_reflectTest extends TestJPF {
  private final static String[] JPF_ARGS = {};

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
    if (verifyNoPropertyViolation()) {
      
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

  public static class TestClass {
    
    public TestClass () {
      
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
  }
}
