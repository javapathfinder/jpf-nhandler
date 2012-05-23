package converter;

import gov.nasa.jpf.jvm.MJIEnv;
import gov.nasa.jpf.util.test.TestJPF;

import org.junit.Test;

public class JVM2JPFTest extends TestJPF {
  private final static String[] JPF_ARGS = {};

  private static MJIEnv env;

  public static void main (String[] args){
    runTestsOfThisClass(args);
  }

  public static void setEnv (MJIEnv env){
    JVM2JPFTest.env = env;
  }

  private native Object createJPFInt ();

  @Test
  /**
   * Testing getJVMCls(int JPFRef, MJIEnv env)
   */
  public void testNative1 (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      Integer i2 = new Integer(100);
      Integer i1 = (Integer) createJPFInt();
      assertEquals(i1, i2);
    }
  }

  private native Object concatString ();

  @Test
  /**
   * Testing getJVMCls(int JPFRef, MJIEnv env)
   */
  // should be used by the local peer. Cause jpf-core already handle
  public void testNative2 (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      String s1 = "@JPF";
      String s2 = s1.concat("@JVM");
      assertTrue(s2.contains("JVM"));
    }
  }
}
