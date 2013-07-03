package converter.specific;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import converter.specific.JPF2JVMjava_lang_reflectTest.TestClass;

import nhandler.conversion.ConversionException;
import nhandler.conversion.ConverterBase;
import nhandler.conversion.jpf2jvm.JPF2JVMConverter;
import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;

public class JPF_converter_specific_JPF2JVMjava_lang_reflectTest extends NativePeer {

  @MJI
  public static void convertMethodTest__Ljava_lang_reflect_Method_2Ljava_lang_reflect_Method_2__V
  (MJIEnv env, int objRef, int rMeth1, int rMeth2) {
    ConverterBase.reset(env);

    Method meth1 = null, meth2 = null;
    try {
      meth1 = (Method) JPF2JVMConverter.obtainJVMObj(rMeth1, env);
      meth2 = (Method) JPF2JVMConverter.obtainJVMObj(rMeth2, env);
    } catch (ConversionException e) {
      TestJPF.fail("Problem occured during conversion");
      e.printStackTrace();
    }

    Method jvmMeth1 = null, jvmMeth2 = null;
    try {
      jvmMeth1 = JPF2JVMjava_lang_reflectTest.TestClass.class.getDeclaredMethod("meth",
                                                                                String.class,
                                                                                int.class,
                                                                                int[][][].class,
                                                                                String[][][][].class);
      jvmMeth2 = JPF2JVMjava_lang_reflectTest.TestClass.class.getDeclaredMethod("meth",
                                                                                String.class,
                                                                                int.class,
                                                                                String.class,
                                                                                String.class);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }

    TestJPF.assertEquals(jvmMeth1, meth1);
    TestJPF.assertEquals(jvmMeth2, meth2);
    TestJPF.assertTrue(meth1.isAccessible());
    TestJPF.assertFalse(meth2.isAccessible());
    Integer i1 = null, i2 = null;
    try {
      i1 = (Integer) meth1.invoke(null, null, 0, null, null);
      i2 = (Integer) meth2.invoke(new JPF2JVMjava_lang_reflectTest.TestClass(), null, 0, null, null);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    TestJPF.assertEquals((int) i1, 1);
    TestJPF.assertEquals((int) i2, 2);
  }
  
  
  @MJI
  public static void convertConstructorTest__Ljava_lang_reflect_Constructor_2Ljava_lang_reflect_Constructor_2__V
  (MJIEnv env, int objRef, int rCtor1, int rCtor2) {
    ConverterBase.reset(env);
    
    Constructor<?> ctor1 = null, ctor2 = null;
    try {
      ctor1 = (Constructor<?>) JPF2JVMConverter.obtainJVMObj(rCtor1, env);
      ctor2 = (Constructor<?>) JPF2JVMConverter.obtainJVMObj(rCtor2, env);
    } catch (ConversionException e) {
      e.printStackTrace();
      TestJPF.fail("Problem occured during conversion");
    }
    
    Constructor<?> jvmCtor1 = null, jvmCtor2 = null;
    try {
      jvmCtor1 = JPF2JVMjava_lang_reflectTest.TestClass.class.getConstructor(String.class);
      jvmCtor2 = JPF2JVMjava_lang_reflectTest.TestClass.class.getConstructor(Integer.class);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }
    
    TestJPF.assertEquals(jvmCtor1, ctor1);
    TestJPF.assertEquals(jvmCtor2, ctor2);
    TestJPF.assertTrue(ctor1.isAccessible());
    TestJPF.assertFalse(ctor2.isAccessible());
    JPF2JVMjava_lang_reflectTest.TestClass obj1 = null, obj2 = null;
    try {
      obj1 = (TestClass) ctor1.newInstance((Object) null);
      obj2 = (TestClass) ctor2.newInstance((Object) null);
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    
    TestJPF.assertEquals(obj1.f1, 1);
    TestJPF.assertEquals(obj2.f1, 2);
  }
  
  
}
