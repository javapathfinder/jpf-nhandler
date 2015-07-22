package nhandler.conversion.jpf2jvm;

import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.JPF_java_lang_reflect_Method;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.StaticElementInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import nhandler.conversion.ConversionException;


public class JPF2JVMjava_lang_reflect_MethodConverter extends JPF2JVMConverter {

  /**
   * No static fields to set
   */
  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {

  }

  /**
   * Most of the work is done in instantiateFrom, here we only
   * call setAccessible, if needed
   */
  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {
    assert JVMObj instanceof Method : "Not the correct converter!";
    int JPFRef = dei.getObjectRef();
    boolean isAccessible = env.getBooleanField(JPFRef, "isAccessible");
    ((Method) JVMObj).setAccessible(isAccessible);
  }

  /**
   * We need to get the following: Class<?> declaringClass, String name,
   * Class<?>[] parameterTypes, Class<?> returnType, Class<?>[]
   * checkedExceptions, int modifiers, int slot, String signature
   */
  @Override
  protected Object instantiateFrom (Class<?> cl, int JPFRef, MJIEnv env) {
    assert cl == Method.class;
    Object JVMObj = null;
    Constructor<?> ctor = null;
    try {
      ctor = cl.getDeclaredConstructor(Class.class, String.class, Class[].class, Class.class, Class[].class, int.class, int.class, String.class, byte[].class, byte[].class, byte[].class);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }
    assert ctor != null;
    ctor.setAccessible(true);
    
    MethodInfo mi = JPF_java_lang_reflect_Method.getMethodInfo(env, JPFRef);
    
    ClassInfo methDeclCi = mi.getClassInfo();
    int JPFCls = methDeclCi.getClassObjectRef();
    Class<?> clazz = null;
    try {
      clazz = obtainJVMCls(JPFCls, env);
    } catch (ConversionException e) {
      e.printStackTrace();
    }
    
    String name = mi.getName();
    
    String[] parameterTypeNames = mi.getArgumentTypeNames();
    Class<?>[] parameterTypes = JPF2JVMUtilities.getClassesFromNames(parameterTypeNames);
    
    try {
      JVMObj = clazz.getDeclaredMethod(name, parameterTypes);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }
    
    return JVMObj;
  }

}
