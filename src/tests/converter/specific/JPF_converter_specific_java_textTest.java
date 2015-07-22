package converter.specific;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import nhandler.conversion.ConversionException;
import nhandler.conversion.ConverterBase;
import nhandler.conversion.jpf2jvm.JPF2JVMConverter;
import nhandler.conversion.jvm2jpf.JVM2JPFConverter;
import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;

public class JPF_converter_specific_java_textTest extends NativePeer{

  public static final String DF_PATTERN1 = "0,0000.000", DF_PATTERN2 = "#,#.000";
  public static final String DF_RESULT1 = "0,4242.420", DF_RESULT2 = "4,2,4,2.420";
  public static final double DF_INPUT = 4242.42;

  @MJI
  public static int convertDecimalFormatTest__Ljava_text_DecimalFormat_2___3Ljava_text_DecimalFormat_2
  (MJIEnv env, int objRef, int rFormat) {
    ConverterBase.reset(env);
    
    DecimalFormat format = null;
    try {
      format = (DecimalFormat) JPF2JVMConverter.obtainJVMObj(rFormat, env);
    } catch (ConversionException e) {
      e.printStackTrace();
    }
    
    DecimalFormat jvmFormat = new DecimalFormat(java_textTest.DF_PATTERN);
    TestJPF.assertEquals(jvmFormat, format);
    TestJPF.assertEquals(jvmFormat.toPattern(), format.toPattern());
    
    /*
     * We have to test two things:
     * - returning the same object that was passed to us, after modifying it
     * - returning a newly created object
     */
    format.applyPattern(DF_PATTERN1);
    DecimalFormat[] ret = new DecimalFormat[2];
    ret[0] = format;
    ret[1] = new DecimalFormat(DF_PATTERN2);
    
    int JPFret = MJIEnv.NULL;
    try {
      JPFret = JVM2JPFConverter.obtainJPFObj(ret, env);
    } catch (ConversionException e) {
      e.printStackTrace();
    }
    return JPFret;
  }
  
  public static final String SDF_PATTERN1 = "YYYY-'W'ww", SDF_PATTERN2 = "yyyy.MM.dd G 'at' HH:mm:ss";
  public static final String SDF_RESULT1 = "1983-W25", SDF_RESULT2= "1983.06.12 AD at 10:37:04";
  public static final long SDF_INPUT = 424242424242L;
  
  /*
   * This works the same as the above tests for DecimalFormat
   */
  @MJI
  public static int convertSimpleDateFormatTest__Ljava_text_SimpleDateFormat_2___3Ljava_text_SimpleDateFormat_2
  (MJIEnv env, int objRef, int rFormat) {
    ConverterBase.reset(env);
    
    SimpleDateFormat format = null;
    try {
      format = (SimpleDateFormat) JPF2JVMConverter.obtainJVMObj(rFormat, env);
    } catch (ConversionException e) {
      e.printStackTrace();
    }
    
    SimpleDateFormat jvmFormat = new SimpleDateFormat(java_textTest.SDF_PATTERN);
    
    TestJPF.assertEquals(jvmFormat, format);
    
    format.applyPattern(SDF_PATTERN1);
    SimpleDateFormat[] ret = new SimpleDateFormat[2];
    ret[0] = format;
    ret[1] = new SimpleDateFormat(SDF_PATTERN2);
    
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(SDF_INPUT);
    
    int JPFret = MJIEnv.NULL;
    try {
      JPFret = JVM2JPFConverter.obtainJPFObj(ret, env);
    } catch (ConversionException e) {
      e.printStackTrace();
    }
    return JPFret;
  }
}