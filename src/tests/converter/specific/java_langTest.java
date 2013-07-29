package converter.specific;

import org.junit.Test;

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.MJIEnv;

public class java_langTest extends TestJPF {
  private final static String[] JPF_ARGS = {};

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
      System.out.println("foobar");
      Class<?> clazz = Integer.class;

      clazz = convertClassTest(clazz);
      assertEquals(Float.class, clazz);
    }
  }

}
