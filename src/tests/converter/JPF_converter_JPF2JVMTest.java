package converter;

import gov.nasa.jpf.jvm.MJIEnv;
import gov.nasa.jpf.nhandler.conversion.ConversionException;
import gov.nasa.jpf.nhandler.conversion.Converter;
import gov.nasa.jpf.util.test.TestJPF;

public class JPF_converter_JPF2JVMTest extends TestJPF {

  public static boolean nativeGetJVMClsTest__Ljava_lang_Object_2__Z (MJIEnv env, int objRef, int jpfRef) throws ConversionException {
    System.out.println("peer4JPF2JVMTest");
    String s1 = new String("Hello World");
    Converter converter = new Converter(env);
    String s2 = (String) converter.getJVMObj(jpfRef);
    assertEquals(s1, s2);
    return true;
  }

  public static boolean nativeGetJVMClsTest2__Ljava_lang_Object_2__Z (MJIEnv env, int objRef, int jpfRef) throws ConversionException {
    System.out.println("peer4JPF2JVMTest-2");
    Integer i1 = new Integer(100);
    Converter converter = new Converter(env);
    Integer i2 = (Integer) converter.getJVMObj(jpfRef);

    assertEquals(i1, i2);
    System.out.println("i1: " + i1);
    System.out.println("i2: " + i2);

    return true;
  }
}
