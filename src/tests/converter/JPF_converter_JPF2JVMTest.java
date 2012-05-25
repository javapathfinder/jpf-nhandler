package converter;

import gov.nasa.jpf.jvm.MJIEnv;
import gov.nasa.jpf.nhandler.conversion.ConversionException;
import gov.nasa.jpf.nhandler.conversion.Converter;
import gov.nasa.jpf.util.test.TestJPF;

import java.util.HashMap;

import converter.JPF2JVMTest.JPF2JVMTestConversion;

/**
 * This is a native peer class which is used to test the conversion from JPF to
 * JVM. It works along with the test class JPF2JVMTest.
 * 
 * @author Nastaran Shafiei
 * @author Franck van Breugel
 */
public class JPF_converter_JPF2JVMTest extends TestJPF {

  public static void convertStringTest__Ljava_lang_String_2__V (MJIEnv env, int objRef, int jpfRef) throws ConversionException{
    Converter converter = new Converter(env);

    // converting JPF String to JVM string
    String s1 = (String) converter.getJVMObj(jpfRef);

    String s2 = new String("Hello World");

    assertEquals(s1, s2);
  }

  public static void convertIntegerTest__Ljava_lang_Integer_2__V (MJIEnv env, int objRef, int jpfRef) throws ConversionException{
    Converter converter = new Converter(env);

    // converting JPF Integer to JVM Integer
    Integer i1 = (Integer) converter.getJVMObj(jpfRef);

    Integer i2 = new Integer(100);

    assertEquals(i1, i2);
  }

  public static void convertArrayTest___3Ljava_lang_String_2__V (MJIEnv env, int objRef, int jpfArr) throws ConversionException{
    Converter converter = new Converter(env);

    // converting JPF String array to JVM String array
    String[] arr1 = (String[]) converter.getJVMObj(jpfArr);

    String[] arr2 = { "e1", "e2", "e3" };

    assertEquals(arr1.length, arr2.length);
    assertEquals(arr1[0], arr2[0]);
    assertEquals(arr1[1], arr2[1]);
    assertEquals(arr1[2], arr2[2]);
  }

  public static void convertHashMapTest__Ljava_util_HashMap_2__V (MJIEnv env, int objRef, int jpfMap) throws ConversionException{
    Converter converter = new Converter(env);

    // converting JPF HashMap to JVM HashMap
    @SuppressWarnings("unchecked")
	HashMap<Integer, String> map1 = (HashMap<Integer, String>) converter.getJVMObj(jpfMap);

    HashMap<Integer, String> map2 = new HashMap<Integer, String>();
    map2.put(0, "zero");
    map2.put(1, "one");
    map2.put(2, "two");

    assertEquals(map1, map2);
    assertEquals(map1.get(0), map2.get(0));
    assertEquals(map1.get(1), map2.get(1));
    assertEquals(map1.get(2), map2.get(2));
  }

  public static void convertClassTest__Ljava_lang_Class_2__V (MJIEnv env, int objRef, int jpfCls) throws ConversionException, IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException{
    Converter converter = new Converter(env);

    // converting JPF Class to JVM Class
    Class<?> cls1 = (Class<?>) converter.getJVMCls(jpfCls);

    Class<?> cls2 = JPF2JVMTestConversion.class;

    assertEquals(cls1, cls2);
    assertEquals(JPF2JVMTestConversion.i, 10);
    assertEquals(cls1.getDeclaredFields()[0].getInt(cls1), 10);
  }
}
