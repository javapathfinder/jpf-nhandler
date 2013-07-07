package converter.specific;

import java.text.DecimalFormat;

import org.junit.Test;

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.MJIEnv;

public class java_textTest extends TestJPF {
  private final static String[] JPF_ARGS = {};

  private static MJIEnv env;

  public static void main (String[] args) {
    runTestsOfThisClass(args);
  }

  public static void setEnv (MJIEnv env) {
    java_textTest.env = env;
  }

  public static final String PATTERN = "#,#####.#### ; $#";

  private native DecimalFormat[] convertDecimalFormatTest (DecimalFormat df);

  @Test
  public void convertDecimalFormatTest () {
    if (verifyNoPropertyViolation(JPF_ARGS)) {
      DecimalFormat format = new DecimalFormat(PATTERN);
      DecimalFormat[] retFormats = convertDecimalFormatTest(format);

      /*
       * The object that was passed was also returned as retFormats[0],
       * so they should be equal
       */
      assertTrue(retFormats[0] == format);

      assertEquals(JPF_converter_specific_java_textTest.RESULT1,
                   retFormats[0].format(JPF_converter_specific_java_textTest.INPUT));
      assertEquals(JPF_converter_specific_java_textTest.RESULT2,
                   retFormats[1].format(JPF_converter_specific_java_textTest.INPUT));
    }
  }
}
