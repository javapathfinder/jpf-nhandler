package nhandler.conversion.jvm2jpf;

import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.JPF_java_lang_reflect_Field;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StaticElementInfo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;

import nhandler.conversion.ConversionException;
import nhandler.conversion.ConverterBase;

/**
 * 
 * @author Chinmay Dabral
 */

public class JVM2JPFjava_lang_reflect_FieldConverter extends JVM2JPFConverter {

  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {

  }

  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {

  }

  @Override
  protected int getJPFObj (Object JVMObj, MJIEnv env) throws ConversionException {
    int JPFRef = MJIEnv.NULL;
    if (JVMObj != null) {
      // First check if we already converted this object:
      JPFRef = getExistingJPFRef(JVMObj, false, env);

      if (JPFRef == MJIEnv.NULL) {
        Field jvmField = (Field) JVMObj;
        Class<?> fieldClass = jvmField.getDeclaringClass();
        boolean isStatic = ((Modifier.toString(jvmField.getModifiers())).indexOf("static") != -1);

        ClassInfo fieldCi = obtainJPFCls(fieldClass, env);
        FieldInfo fi = null;
        if (isStatic)
          fi = fieldCi.getDeclaredStaticField(jvmField.getName());
        else
          fi = fieldCi.getDeclaredInstanceField(jvmField.getName());
        System.out.println("fieldInfo: " + fi);// TODO: remove

        // register FieldInfo to get regIdx
        int rIdx = registerFieldInfo(env, fi);
        // create a Method object, and set regIdx for it
        JPFRef = getNewJPFFieldRef(env);
        env.setIntField(JPFRef, "regIdx", rIdx);
        // put in map
        ConverterBase.updatedJPFObj.put(JPFRef, jvmField);
      }
    }
    return JPFRef;
  }

  private int getNewJPFFieldRef (MJIEnv env) {
    int JPFRef = MJIEnv.NULL;
    ClassInfo ci = null;
    try {
      ci = getJPFCls(Field.class, env);
    } catch (ConversionException e) {
      e.printStackTrace();
      System.exit(1);
    }
    JPFRef = env.newObject(ci);
    return JPFRef;
  }

  private int registerFieldInfo (MJIEnv env, FieldInfo fi) {
    int regIdx = MJIEnv.NULL;
    try {
      Method registerFi = JPF_java_lang_reflect_Field.class.getDeclaredMethod("registerFieldInfo", FieldInfo.class);
      registerFi.setAccessible(true);
      try {
        regIdx = (Integer) registerFi.invoke(null, fi);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }
    return regIdx;
  }
  
  @Override
  protected int getExistingJPFRef (Object JVMObj, boolean update, MJIEnv env) throws ConversionException {
    int JPFRef = MJIEnv.NULL;
    boolean found = false;
    if (ConverterBase.updatedJPFObj.containsValue(JVMObj)) {
      Iterator<Integer> iterator = (ConverterBase.updatedJPFObj.keySet()).iterator();
      Integer key;
      while (!found && iterator.hasNext()) {
        key = iterator.next();
        Object value = ConverterBase.updatedJPFObj.get(key);
        if (value == JVMObj) {
          found = true;
          JPFRef = key;
        }
      }
    }

    return JPFRef;
  }

}
