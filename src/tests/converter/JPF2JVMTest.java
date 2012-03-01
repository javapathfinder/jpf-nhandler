package converter;

import gov.nasa.jpf.jvm.MJIEnv;
import gov.nasa.jpf.util.test.TestJPF;

import org.junit.Test;

public class JPF2JVMTest extends TestJPF {
  private final static String[] JPF_ARGS = {};

  private static MJIEnv env;

  public static void main (String[] args) {
    runTestsOfThisClass(args);
  }

  public static void setEnv (MJIEnv env) {
    JPF2JVMTest.env = env;
  }

  private native boolean nativeGetJVMClsTest (Object o);

  private native boolean nativeGetJVMClsTest2 (Object o);

  @Test
  /**
   * Testing getJVMCls(int JPFRef, MJIEnv env)
   */
  public void getJVMClsTest () {
    if (verifyNoPropertyViolation(JPF_ARGS)) {
      String s = new String("Hello World");
      nativeGetJVMClsTest(s);

      Integer i = new Integer(100);
      nativeGetJVMClsTest2(i);
    }
  }
}
