package converter;

import gov.nasa.jpf.jvm.MJIEnv;
import gov.nasa.jpf.nhandler.conversion.ConversionException;
import gov.nasa.jpf.nhandler.conversion.Converter;
import gov.nasa.jpf.util.test.TestJPF;

public class JPF_converter_JPF2JVMTest extends TestJPF {

  public static void convertStringTest__Ljava_lang_String_2__V (MJIEnv env, int objRef, int jpfRef) throws ConversionException{
    String s1 = new String("Hello World");
    Converter converter = new Converter(env);

    // converting JPF String to JVM string
    String s2 = (String) converter.getJVMObj(jpfRef);
    assertEquals(s1, s2);
  }

  public static void convertIntegerTest__Ljava_lang_Integer_2__V (MJIEnv env, int objRef, int jpfRef) throws ConversionException{
    Integer i1 = new Integer(100);
    Converter converter = new Converter(env);

    // converting JPF Integer to JVM Integer
    Integer i2 = (Integer) converter.getJVMObj(jpfRef);
    assertEquals(i1, i2);
  }
}
