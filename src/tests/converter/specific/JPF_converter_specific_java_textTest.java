package converter.specific;

import java.text.DecimalFormat;

import nhandler.conversion.ConversionException;
import nhandler.conversion.ConverterBase;
import nhandler.conversion.jpf2jvm.JPF2JVMConverter;
import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;

public class JPF_converter_specific_JPF2JVMjava_textTest extends NativePeer{

  @MJI
  public static void convertDecimalFormatTest__Ljava_text_DecimalFormat_2__V
  (MJIEnv env, int objRef, int rFormat) {
    ConverterBase.reset(env);
    
    DecimalFormat format = null;
    try {
      format = (DecimalFormat) JPF2JVMConverter.obtainJVMObj(rFormat, env);
    } catch (ConversionException e) {
      e.printStackTrace();
    }
    
    DecimalFormat jvmFormat = new DecimalFormat(JPF2JVMjava_textTest.PATTERN);
    TestJPF.assertEquals(jvmFormat, format);
    TestJPF.assertEquals(jvmFormat.toPattern(), format.toPattern());
  }
}