package converter.specific;

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.MJIEnv;

import java.util.Random;

import org.junit.Test;

public class JPF2JVMjava_utilTest extends TestJPF {
  private final static String[] JPF_ARGS = {};

  private static MJIEnv env;

  public static void main (String[] args) {
    runTestsOfThisClass(args);
  }

  public static void setEnv (MJIEnv env) {
    JPF2JVMjava_utilTest.env = env;
  }
  
  public static final int SEED = 42;
  
  private native void convertRandomTest (Random rand, int[] num);
  
  @Test
  public void convertRandomTest() {
    if(verifyNoPropertyViolation(JPF_ARGS)) {
      Random rand = new Random(SEED);
      int[] num = new int[10];
      for(int i = 0; i < 5; i++)
        num[i] = rand.nextInt();
      convertRandomTest(rand, num);
    }
  }
}
