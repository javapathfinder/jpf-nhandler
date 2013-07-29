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
