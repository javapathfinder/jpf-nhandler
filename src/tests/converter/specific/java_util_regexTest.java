package converter.specific;

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.MJIEnv;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

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
  
  public static final String SEQUENCE1 = "Bugblatter Beast",
      SEQUENCE2 = "Traal";
  
  private native Matcher convertMatcherTest(Matcher matcher);
  
  @Test
  public void convertMatcherTest() {
    if (verifyNoPropertyViolation(JPF_ARGS)) {
      Matcher matcher = Pattern.compile(PATTERN1).matcher(SEQUENCE1);
      
      matcher = convertMatcherTest(matcher);
      
      assertTrue(verifyMatcher(matcher, PATTERN2, "input", SEQUENCE2));
    }
  }
  
  static boolean verifyMatcher(Matcher matcher, String expectedPattern, String inputFieldName, String expectedInput) {
    System.out.println(matcher);
    System.out.println(matcher.pattern());
    System.out.println(expectedPattern);
    if (!matcher.pattern().toString().equals(expectedPattern))
      return false;
    
    Field inputField = null;
    try {
      inputField = Matcher.class.getDeclaredField(inputFieldName);
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }
    
    inputField.setAccessible(true);
    String input = null;
    try {
      input = (String) inputField.get(matcher);
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    
    if (!input.equals(expectedInput))
      return false;
    
    return true;
  }
}
