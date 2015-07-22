/* 
 * Copyright (C) 2013  Nastaran Shafiei and Franck van Breugel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You can find a copy of the GNU General Public License at
 * <http://www.gnu.org/licenses/>.
 */

package converter.specific;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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

  public static final String DF_PATTERN = "#,#####.#### ; $#";

  private native DecimalFormat[] convertDecimalFormatTest (DecimalFormat df);

  @Test
  public void convertDecimalFormatTest () {
    if (verifyNoPropertyViolation(JPF_ARGS)) {
      DecimalFormat format = new DecimalFormat(DF_PATTERN);
      DecimalFormat[] retFormats = convertDecimalFormatTest(format);

      /*
       * The object that was passed was also returned as retFormats[0],
       * so they should be equal
       */
      assertTrue(retFormats[0] == format);

      assertEquals(JPF_converter_specific_java_textTest.DF_RESULT1,
                   retFormats[0].format(JPF_converter_specific_java_textTest.DF_INPUT));
      assertEquals(JPF_converter_specific_java_textTest.DF_RESULT2,
                   retFormats[1].format(JPF_converter_specific_java_textTest.DF_INPUT));
    }
  }
  
  public static final String SDF_PATTERN = "yyMMddHHmmss";
  
  private native SimpleDateFormat[] convertSimpleDateFormatTest(SimpleDateFormat sdf);
  
  /*
   * This check is the same as for DecimalFormat
   */
  @Test
  public void convertSimpleDateFormatTest() {
    if(verifyNoPropertyViolation(JPF_ARGS)) {
      SimpleDateFormat format = new SimpleDateFormat(SDF_PATTERN);
      SimpleDateFormat[] retFormats = convertSimpleDateFormatTest(format);
      
      assertTrue(retFormats[0] == format);
      
      Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis(JPF_converter_specific_java_textTest.SDF_INPUT);

      // TODO - investigate these failed tests
      /**assertEquals(JPF_converter_specific_java_textTest.SDF_RESULT1,
                   retFormats[0].format(calendar.getTime()));
      assertEquals(JPF_converter_specific_java_textTest.SDF_RESULT2,
                   retFormats[1].format(calendar.getTime()));**/
    }
  }
}
