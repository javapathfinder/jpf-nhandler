package converter.specific;

import java.text.DecimalFormat;

import org.junit.Test;

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.MJIEnv;

public class JPF2JVMjava_textTest extends TestJPF {
  private final static String[] JPF_ARGS = {};

  private static MJIEnv env;

  public static void main (String[] args) {
    runTestsOfThisClass(args);
  }

  public static void setEnv (MJIEnv env) {
    JPF2JVMjava_textTest.env = env;
  }
  
  public static final String PATTERN = "#,#####.#### ; $#";

  private native void convertDecimalFormatTest (DecimalFormat df);

  @Test
  public void convertDecimalFormatTest () {
    if (verifyNoPropertyViolation(JPF_ARGS)) {
      DecimalFormat format = new DecimalFormat(PATTERN);
      convertDecimalFormatTest(format);
    }
  }
}
