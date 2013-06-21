package nhandler.conversion.jvm2jpf;

import gov.nasa.jpf.util.MethodInfoRegistry;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.JPF_java_lang_reflect_Method;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.StaticElementInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;

import nhandler.conversion.ConversionException;
import nhandler.conversion.ConverterBase;

/**
 * A JVM2JPFConverter to convert JVM java.lang.reflect.Method objects to their
 * JPF counterparts
 * 
 * @author Chinmay Dabral
 */

public class JVM2JPFjava_lang_reflect_MethodConverter extends JVM2JPFConverter {

  /**
   * No static fields in both java.lang.reflect.Method model and JVM classes
   */
  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {
    return;
  }

  /**
   * This is never called in this case, everything is handled by the overridden
   * getJPFObj() itself
   */

  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {

  }

  /**
   * We get the existing MethodInfo object associated with the method that the
   * given JVM object points to. We then create a JPF Method object and set its
   * regIdx field to point to the index of the corresponding MethodInfo in the
   * MethodInfoRegistry
   */
  @Override
  protected int getJPFObj (Object JVMObj, MJIEnv env) throws ConversionException {
    int JPFRef = MJIEnv.NULL;
    if (JVMObj != null) {
      // First check if we already converted this object:
      JPFRef = getExistingJPFRef(JVMObj, false, env);

      if (JPFRef == MJIEnv.NULL) {
        Method jvmMeth = (Method) JVMObj;
        Class<?> methClass = jvmMeth.getDeclaringClass();
        String paramString = "(" + Utilities.getParamString(jvmMeth.getParameterTypes()) + ")";

        ClassInfo methCi = obtainJPFCls(methClass, env);
        MethodInfo mi = methCi.getMethod(jvmMeth.getName(), paramString, false);
        System.out.println("methodInfo: " + mi);// TODO: remove

        // register methodinfo to get regIdx
        MethodInfoRegistry registry = getMethodInfoRegistry();
        int rIdx = registry.registerMethodInfo(mi);
        // create a Method object, and set regIdx for it
        JPFRef = getNewJPFMethodRef(env);
        env.setIntField(JPFRef, "regIdx", rIdx);
        // put in map
        ConverterBase.updatedJPFObj.put(JPFRef, jvmMeth);
      }
    }
    return JPFRef;
  }

  /**
   * Reflectively get MethodInfoRegistry from JPF_java_lang_reflect_Method
   * 
   * @return the MethodInfoRegistry contained in
   *         JPF_java_lang_reflect_Method.registry
   */
  private MethodInfoRegistry getMethodInfoRegistry () {
    MethodInfoRegistry registry = null;
    try {
      Field registryField = JPF_java_lang_reflect_Method.class.getDeclaredField("registry");
      registryField.setAccessible(true);
      try {
        registry = (MethodInfoRegistry) registryField.get(null);
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }
    return registry;
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

  /**
   * Create a new JPF object of java.lang.reflect.Method type
   * 
   * @param env
   * @return The ref for the created object
   */
  private int getNewJPFMethodRef (MJIEnv env) {
    int JPFRef = MJIEnv.NULL;
    ClassInfo ci = null;
    try {
      ci = getJPFCls(Method.class, env);
    } catch (ConversionException e) {
      e.printStackTrace();
      System.exit(1);
    }
    JPFRef = env.newObject(ci);
    return JPFRef;
  }
}
