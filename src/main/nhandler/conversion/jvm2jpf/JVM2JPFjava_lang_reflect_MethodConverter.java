package nhandler.conversion.jvm2jpf;

import gov.nasa.jpf.util.MethodInfoRegistry;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.JPF_java_lang_reflect_Method;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.StaticElementInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import nhandler.conversion.ConversionException;

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
   * Get the MethodInfo for the Method represented by JVMObj, then register it
   * to get the regIdx. Store this in the JPF object
   */

  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {
    Method jvmMeth = (Method) JVMObj;
    Class<?> methdDeclClass = jvmMeth.getDeclaringClass();
    String paramString = "(" + JVM2JPFUtilities.getParamString(jvmMeth.getParameterTypes()) + ")";

    ClassInfo methDeclCi = obtainJPFCls(methdDeclClass, env);
    MethodInfo mi = methDeclCi.getMethod(jvmMeth.getName(), paramString, false);
    System.out.println("methodInfo: " + mi);// TODO: remove

    // register methodinfo to get regIdx
    MethodInfoRegistry registry = getMethodInfoRegistry();
    int rIdx = registry.registerMethodInfo(mi);
    //Set regIdx for the object:
    ClassInfo methCi = obtainJPFCls(Method.class, env);
    FieldInfo fi = methCi.getDeclaredInstanceField("regIdx");
    dei.setIntField(fi, rIdx);
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
}
