package nhandler.conversion.jpf2jvm;

import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.JPF_java_lang_reflect_Constructor;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.StaticElementInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import nhandler.conversion.ConversionException;

public class JPF2JVMjava_lang_reflect_ConstructorConverter extends JPF2JVMConverter {

  /**
   * No static fields to set
   */
  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {

  }

  /**
   * Most of the work is done in instantiateFrom, here we only call
   * setAccessible(), if needed
   */
  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {
    assert JVMObj instanceof Constructor<?> : "Not the correct converter!";
    int JPFRef = dei.getObjectRef();
    boolean isAccessible = env.getBooleanField(JPFRef, "isAccessible");
    ((Constructor<?>) JVMObj).setAccessible(isAccessible);
  }

  @Override
  protected Object instantiateFrom (Class<?> cl, int jPFRef, MJIEnv env) {
    assert cl == Constructor.class;
    Object JVMObj = null;
    Constructor<?> ctor = null;
    try {
      ctor = cl.getDeclaredConstructor(Class.class, Class[].class, Class[].class, int.class, int.class, String.class, byte[].class, byte[].class);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }
    assert ctor != null;
    ctor.setAccessible(true);

    MethodInfo mi = getConstructorMethodInfo(jPFRef, env);

    ClassInfo methDeclCi = mi.getClassInfo();
    int JPFCls = methDeclCi.getClassObjectRef();
    Class<?> clazz = null;
    try {
      clazz = obtainJVMCls(JPFCls, env);
    } catch (ConversionException e) {
      e.printStackTrace();
    }

    String[] parameterTypeNames = mi.getArgumentTypeNames();
    Class<?>[] parameterTypes = JPF2JVMUtilities.getClassesFromNames(parameterTypeNames);

    try {
      JVMObj = clazz.getDeclaredConstructor(parameterTypes);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }
    return JVMObj;
  }

  /**
   * The function getMethodInfo in JPF_java_lang_reflect_Constructor isn't
   * public, so we have to invoke it reflectively
   * 
   * @param JPFRef
   *          Ref to a JPF Method object
   * @param env
   *          MJIEnv
   * @return The MethodInfo object corresponding to the JPF Method ref
   */
  private MethodInfo getConstructorMethodInfo (int JPFRef, MJIEnv env) {
    MethodInfo mi = null;
    Method getMethodInfo = null;
    try {
      getMethodInfo = JPF_java_lang_reflect_Constructor.class.getDeclaredMethod("getMethodInfo", MJIEnv.class, int.class);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }
    
    getMethodInfo.setAccessible(true);

    try {
      mi = (MethodInfo) getMethodInfo.invoke(null, env, JPFRef);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }

    return mi;
  }

}
