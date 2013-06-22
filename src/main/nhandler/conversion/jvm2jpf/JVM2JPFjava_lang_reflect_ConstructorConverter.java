package nhandler.conversion.jvm2jpf;

import gov.nasa.jpf.util.MethodInfoRegistry;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.JPF_java_lang_reflect_Constructor;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.StaticElementInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Iterator;

import nhandler.conversion.ConversionException;
import nhandler.conversion.ConverterBase;

public class JVM2JPFjava_lang_reflect_ConstructorConverter extends JVM2JPFConverter {

  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {

  }

  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {

  }

  /**
   * This works the same way as the JVM2JPFConverter for Method
   */
  @Override
  protected int getJPFObj (Object JVMObj, MJIEnv env) throws ConversionException {
    int JPFRef = MJIEnv.NULL;
    if (JVMObj != null) {
      // First check if we already converted this object:
      JPFRef = getExistingJPFRef(JVMObj, false, env);

      if (JPFRef == MJIEnv.NULL) {
        Constructor<?> jvmCtor = (Constructor<?>) JVMObj;
        Class<?> ctorClass = jvmCtor.getDeclaringClass();
        String paramString = "(" + Utilities.getParamString(jvmCtor.getParameterTypes()) + ")";

        ClassInfo ctorCi = obtainJPFCls(ctorClass, env);
        MethodInfo mi = ctorCi.getMethod("<init>", paramString, false);
        System.out.println("methodInfo: " + mi);// TODO: remove

        // register methodinfo to get regIdx
        MethodInfoRegistry registry = getMethodInfoRegistry();
        int rIdx = registry.registerMethodInfo(mi);
        // create a Method object, and set regIdx for it
        JPFRef = getNewJPFConstructorRef(env);
        env.setIntField(JPFRef, "regIdx", rIdx);
        // put in map
        ConverterBase.updatedJPFObj.put(JPFRef, jvmCtor);
      }
    }
    return JPFRef;
  }

  /**
   * Reflectively get MethodInfoRegistry from JPF_java_lang_reflect_Constructor
   * 
   * @return the MethodInfoRegistry contained in
   *         JPF_java_lang_reflect_Constructor.registry
   */
  private MethodInfoRegistry getMethodInfoRegistry () {
    MethodInfoRegistry registry = null;
    try {
      Field registryField = JPF_java_lang_reflect_Constructor.class.getDeclaredField("registry");
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

  /**
   * Create a new JPF object of java.lang.reflect.Constructor type
   * 
   * @param env
   * @return The ref for the created object
   */
  private int getNewJPFConstructorRef (MJIEnv env) {
    int JPFRef = MJIEnv.NULL;
    ClassInfo ci = null;
    try {
      ci = getJPFCls(Constructor.class, env);
    } catch (ConversionException e) {
      e.printStackTrace();
      System.exit(1);
    }
    JPFRef = env.newObject(ci);
    return JPFRef;
  }

  /*
   * TODO: This might fail in some corner case as we're not checking jpf2jvm
   * maps. Perhaps override getUpdatedJPFObj instead, get regIdx to check
   * whether it still refers to the same Method as the JVM object. Same for
   * Field and Method.
   * 
   * (non-Javadoc)
   * 
   * @see
   * nhandler.conversion.jvm2jpf.JVM2JPFConverter#getExistingJPFRef(java.lang
   * .Object, boolean, gov.nasa.jpf.vm.MJIEnv)
   */
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
