package converter;

import gov.nasa.jpf.jvm.ClassInfo;
import gov.nasa.jpf.jvm.MJIEnv;
import gov.nasa.jpf.nhandler.conversion.ConversionException;
import gov.nasa.jpf.nhandler.conversion.Converter;
import gov.nasa.jpf.util.test.TestJPF;

import java.util.HashMap;

import converter.JVM2JPFTest.JVM2JPFTestConversion;

public class JPF_converter_JVM2JPFTest extends TestJPF {

  public static int createStringTest____Ljava_lang_String_2 (MJIEnv env, int objRef) throws ConversionException{
    String s = new String("Hello World");

    Converter converter = new Converter(env);

    // converting JVM String to JPF string
    int jpfObj = converter.getJPFObj(s);

    return jpfObj;
  }

  public static int createIntegerTest____Ljava_lang_Integer_2 (MJIEnv env, int objRef) throws ConversionException{
    Integer i = new Integer(100);

    Converter converter = new Converter(env);

    // converting JVM Integer to JPF Integer
    int jpfObj = converter.getJPFObj(i);

    return jpfObj;
  }

  public static int createArrayTest_____3Ljava_lang_String_2 (MJIEnv env, int objRef) throws ConversionException{
    String[] arr = { "e1", "e2", "e3" };

    Converter converter = new Converter(env);

    // converting JVM String array to JPF String array
    int jpfObj = converter.getJPFObj(arr);

    return jpfObj;
  }

  public static int createHashMapTest____Ljava_util_HashMap_2 (MJIEnv env, int objRef) throws ConversionException{
    HashMap<Integer, String> map = new HashMap<Integer, String>();
    map.put(0, "zero");
    map.put(1, "one");
    map.put(2, "two");

    Converter converter = new Converter(env);

    // converting JVM map to JPF map
    int jpfObj = converter.getJPFObj(map);

    return jpfObj;
  }

  public static int createClassTest____Ljava_lang_Class_2 (MJIEnv env, int objRef) throws ConversionException{
    JVM2JPFTestConversion.inc(10);
    Class<?> cls = JVM2JPFTestConversion.class;

    assertEquals(JVM2JPFTestConversion.i, 10);
    Converter converter = new Converter(env);

    // converting JVM class to JPF class
    ClassInfo ci = converter.getJPFCls(cls);

    return ci.getClassObjectRef();
  }
}
