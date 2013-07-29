package converter.specific;

import nhandler.conversion.ConversionException;
import nhandler.conversion.ConverterBase;
import nhandler.conversion.jpf2jvm.JPF2JVMConverter;
import nhandler.conversion.jvm2jpf.JVM2JPFConverter;
import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;

public class JPF_converter_specific_java_langTest extends NativePeer {
  
  @MJI
  public static int convertClassTest__Ljava_lang_Class_2__Ljava_lang_Class_2 (MJIEnv env, int objRef, int rClass) {
    ConverterBase.reset(env);
    
    Class<?> clazz = null;
    
    try {
      clazz = (Class<?>) JPF2JVMConverter.obtainJVMObj(rClass, env);
    } catch (ConversionException e) {
      e.printStackTrace();
    }
    
    TestJPF.assertEquals(clazz, Integer.class);
    int JPFRet = MJIEnv.NULL;
    
    try {
      JPFRet = JVM2JPFConverter.obtainJPFObj(Float.class, env);
    } catch (ConversionException e) {
      e.printStackTrace();
    }
    return JPFRet;
  }
  
  @MJI
  public static int convertStringTest__Ljava_lang_String_2__Ljava_lang_String_2
  (MJIEnv env, int objRef, int rString) {
    
    ConverterBase.reset(env);
    
    String string = null;
    
    try {
      string = (String) JPF2JVMConverter.obtainJVMObj(rString, env);
    } catch (ConversionException e) {
      e.printStackTrace();
    }
    TestJPF.assertEquals("Zaphod Beeblebrox", string);
    
    int JPFRet = MJIEnv.NULL;
    
    try {
      JPFRet = JVM2JPFConverter.obtainJPFObj("Ford Prefect", env);
    } catch (ConversionException e) {
      e.printStackTrace();
    }
    return JPFRet;
  }
}