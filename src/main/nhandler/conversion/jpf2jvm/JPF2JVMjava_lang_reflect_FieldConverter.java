package nhandler.conversion.jpf2jvm;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.JPF_java_lang_reflect_Field;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StaticElementInfo;
import nhandler.conversion.ConversionException;

public class JPF2JVMjava_lang_reflect_FieldConverter extends JPF2JVMConverter {

  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {

  }

  /**
   * Most of the work is done in instantiateFrom, here we only call
   * setAccessible(), if needed
   */
  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {

  }

  @Override
  protected Object instantiateFrom (Class<?> cl, int jPFRef, MJIEnv env) {
    
    assert cl == Field.class;
    Object JVMObj = null;
    Constructor<?> ctor = null;
    try {
      ctor = cl.getDeclaredConstructor(Class.class, String.class, Class.class, int.class, int.class, String.class, byte[].class);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }
    assert ctor != null;
    ctor.setAccessible(true);
    
    FieldInfo fi = getFieldInfo(jPFRef, env);
    
    ClassInfo fieldDeclCi = fi.getClassInfo();
    int JPFCls = fieldDeclCi.getClassObjectRef();
    Class<?> clazz = null;
    try {
      clazz = obtainJVMCls(JPFCls, env);
    } catch (ConversionException e) {
      e.printStackTrace();
    }
    
    String name = fi.getName();
    
    String typeName = fi.getType();
    System.out.println("typeName: " + typeName); // TODO: remove
    Class<?> type = Utilities.getClassesFromNames(new String[]{ typeName })[0];
    System.out.println("type: " + type); //TODO: remove
    
    int modifiers = fi.getModifiers();
    System.out.println("modifiers: " + modifiers); //TODO: remove
    
    String signature = fi.getGenericSignature();
    System.out.println("signature: " + signature); //TODO: remove
    
    int slot = 0; //TODO: Don't know what this is
    
    try {
      JVMObj = ctor.newInstance(clazz,
                                name,
                                type,
                                modifiers,
                                slot,
                                signature,
                                null); // Last one is related to annotations
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    
    return JVMObj;
  }

  private FieldInfo getFieldInfo (int JPFRef, MJIEnv env) {
    FieldInfo fi = null;
    Method method = null;
    try {
      method = JPF_java_lang_reflect_Field.class.getDeclaredMethod("getFieldInfo", MJIEnv.class, int.class);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }
    method.setAccessible(true);
    try {
      fi = (FieldInfo) method.invoke(null, env, JPFRef);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    return fi;
  }
}
