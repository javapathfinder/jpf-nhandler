package converter.specific;

import java.util.regex.Pattern;

import org.junit.Test;

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.MJIEnv;

public class java_util_regexTest extends TestJPF {

  private final static String[] JPF_ARGS = {};

  private static MJIEnv env;

  public static void main (String[] args) {
    runTestsOfThisClass(args);
  }

  public static void setEnv (MJIEnv env) {
    java_util_regexTest.env = env;
  }
  
  public static final String PATTERN1 = "[a-z]+",
      PATTERN2 = "[0-9]+";
  
  private native Pattern convertPatternTest(Pattern pattern);
  
  @Test
  public void convertPatternTest() {
    if (verifyNoPropertyViolation(JPF_ARGS)) {
      Pattern pattern = Pattern.compile("[a-z]+");
      
      pattern = convertPatternTest(pattern);
      assertEquals(PATTERN2, pattern.toString());
    }
  }
}
