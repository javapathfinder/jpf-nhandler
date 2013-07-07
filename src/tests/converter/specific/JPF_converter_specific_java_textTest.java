package converter.specific;

import java.text.DecimalFormat;

import nhandler.conversion.ConversionException;
import nhandler.conversion.ConverterBase;
import nhandler.conversion.jpf2jvm.JPF2JVMConverter;
import nhandler.conversion.jvm2jpf.JVM2JPFConverter;
import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;

public class JPF_converter_specific_java_textTest extends NativePeer{

  public static final String PATTERN1 = "0,0000.000", PATTERN2 = "#,#.000";
  public static final String RESULT1 = "0,4242.420", RESULT2 = "4,2,4,2.420";
  public static final double INPUT = 4242.42;

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
    
    DecimalFormat jvmFormat = new DecimalFormat(java_textTest.PATTERN);
    TestJPF.assertEquals(jvmFormat, format);
    TestJPF.assertEquals(jvmFormat.toPattern(), format.toPattern());
    
    /*
     * We have to test two things:
     * - returning the same object that was passed to us, after modifying it
     * - returning a newly created object
     */
    format.applyPattern(PATTERN1);
    DecimalFormat[] ret = new DecimalFormat[2];
    ret[0] = format;
    ret[1] = new DecimalFormat(PATTERN2);
    
    int JPFret = MJIEnv.NULL;
    try {
      JPFret = JVM2JPFConverter.obtainJPFObj(ret, env);
    } catch (ConversionException e) {
      e.printStackTrace();
    }
    return JPFret;
  }
}