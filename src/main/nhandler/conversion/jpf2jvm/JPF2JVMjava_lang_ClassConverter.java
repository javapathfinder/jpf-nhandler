package nhandler.conversion.jpf2jvm;

import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StaticElementInfo;
import nhandler.conversion.ConversionException;

public class JPF2JVMjava_lang_ClassConverter extends JPF2JVMConverter {

  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {

  }

  /**
   * There is only one instance of every class. There is no need to update
   * Class objects
   */
  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {

  }

  @Override
  protected Object instantiateFrom (Class<?> cl, int JPFRef, MJIEnv env) {
    assert cl == Class.class;
    Object JVMObj = null;
    try {
      String name = env.getReferredClassInfo(JPFRef).getName();
      if (Utilities.isPrimitiveClass(name)) {
        JVMObj = Utilities.getPrimitiveClass(name);
      } else {
        JVMObj = loadClass(name, env);
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  return JVMObj;
  }
}
