package nhandler.conversion.jpf2jvm;

import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.JPF_java_text_DecimalFormat;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StaticElementInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;

import nhandler.conversion.ConversionException;

public class JPF2JVMjava_text_DecimalFormatConverter extends JPF2JVMConverter {

  /**
   * No static fields to set
   */
  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {

  }

  /**
   * No instance fields to set. We just use the delegatee maintained by the
   * java.text.Format model class (returned by instantiateFrom())
   */
  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {

  }

  /**
   * We return the delegatee from the Native Peer
   */
  @Override
  protected Object instantiateFrom (Class<?> cl, int jPFRef, MJIEnv env) {
    assert cl == DecimalFormat.class : "Not the right converter!";
    Object JVMObj = null;
    JVMObj = getInstanceFromNativePeer(jPFRef, env);
    return JVMObj;
  }
  
  /**
   * Reflectively call JPF_java_text_DecimalFormat.getinstance() to get
   * the delegatee object corresponding to JPFRef
   * @param JPFRef JPF object to get the delegatee of
   * @param env MJIEnv
   * @return delegatee corresponding to JPFRef
   */
  private DecimalFormat getInstanceFromNativePeer(int JPFRef, MJIEnv env) {
    DecimalFormat obj = null;
    JPF_java_text_DecimalFormat nativePeer = (JPF_java_text_DecimalFormat) env.getClassInfo(JPFRef).getNativePeer();
    Method getInstance = null;
    try {
      getInstance = JPF_java_text_DecimalFormat.class.getDeclaredMethod("getInstance", MJIEnv.class, int.class);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }
    getInstance.setAccessible(true);
    try {
      obj = (DecimalFormat) getInstance.invoke(nativePeer, env, JPFRef);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    return obj;
  }
}
