package converter;

import gov.nasa.jpf.jvm.MJIEnv;
import gov.nasa.jpf.util.test.TestJPF;

import org.junit.Test;

public class JPF2JVMTest extends TestJPF {
  private final static String[] JPF_ARGS = {};

  private static MJIEnv env;

  public static void main (String[] args){
    runTestsOfThisClass(args);
  }

  public static void setEnv (MJIEnv env){
    JPF2JVMTest.env = env;
  }

  private native void convertStringTest (String s);

  @Test
  public void convertStringTest (){
    if (verifyNoPropertyViolation()){
      String s = new String("Hello World");
      convertStringTest(s);
    }
  }

  private native void convertIntegerTest (Integer i);

  @Test
  public void convertIntegerTest (){
    if (verifyNoPropertyViolation()){
      Integer i = new Integer(100);
      convertIntegerTest(i);
    }
  }

}
