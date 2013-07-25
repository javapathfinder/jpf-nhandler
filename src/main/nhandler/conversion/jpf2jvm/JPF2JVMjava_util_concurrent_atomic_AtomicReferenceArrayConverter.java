package nhandler.conversion.jpf2jvm;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReferenceArray;

import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StaticElementInfo;
import nhandler.conversion.ConversionException;

public class JPF2JVMjava_util_concurrent_atomic_AtomicReferenceArrayConverter extends JPF2JVMConverter {

  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {

  }

  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {

  }

  @Override
  protected Object instantiateFrom (Class<?> cl, int JPFRef, MJIEnv env) {
    assert cl == AtomicReferenceArray.class;
    Object JVMObj = null;
    
    int JPFArrayRef = env.getReferenceField(JPFRef, "array");
    Object[] JVMArray = null;
    try {
      JVMArray = (Object[]) obtainJVMObj(JPFArrayRef, env);
    } catch (ConversionException e) {
      e.printStackTrace();
    }
    JVMObj = new AtomicReferenceArray(JVMArray);
    return JVMObj;
  }

}
