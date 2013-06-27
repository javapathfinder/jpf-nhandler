package nhandler.conversion.jvm2jpf;

import gov.nasa.jpf.util.MethodInfoRegistry;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.JPF_java_lang_reflect_Constructor;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.StaticElementInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import nhandler.conversion.ConversionException;

public class JVM2JPFjava_lang_reflect_ConstructorConverter extends JVM2JPFConverter {

  /**
   * No static fields to set
   */
  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {

  }

  /**
   * Get the MethodInfo for the Method represented by JVMObj, then register it
   * to get the regIdx. Store this in the JPF object
   */
  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {
    Constructor<?> jvmCtor = (Constructor<?>) JVMObj;
    Class<?> ctorDeclClass = jvmCtor.getDeclaringClass();
    String paramString = "(" + Utilities.getParamString(jvmCtor.getParameterTypes()) + ")";

    ClassInfo ctorDeclCi = obtainJPFCls(ctorDeclClass, env);
    MethodInfo mi = ctorDeclCi.getMethod("<init>", paramString, false);
    System.out.println("methodInfo: " + mi);// TODO: remove

    // register methodinfo to get regIdx
    MethodInfoRegistry registry = getMethodInfoRegistry();
    int rIdx = registry.registerMethodInfo(mi);
    // Set regIdx for the JPF Constructor object
    ClassInfo ctorCi = getJPFCls(JVMObj.getClass(), env);
    FieldInfo idFi = ctorCi.getInstanceField("regIdx");
    dei.setIntField(idFi, rIdx);
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
}
