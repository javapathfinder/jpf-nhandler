package nhandler.conversion.jpf2jvm;

import java.util.concurrent.atomic.AtomicLongArray;

import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StaticElementInfo;
import nhandler.conversion.ConversionException;

public class JPF2JVMjava_util_concurrent_atomic_AtomicLongArrayConverter extends JPF2JVMConverter {

  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {

  }

  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {

  }

  @Override
  protected Object instantiateFrom (Class<?> cl, int JPFRef, MJIEnv env) {
    assert cl == AtomicLongArray.class;
    
    Object JVMObj = null;
    
    int JPFArrayRef = env.getReferenceField(JPFRef, "array");
    long[] JVMArray = null;
    try {
      JVMArray = (long[]) obtainJVMObj(JPFArrayRef, env);
    } catch (ConversionException e) {
      e.printStackTrace();
    }
    
    JVMObj = new AtomicLongArray(JVMArray);
    return JVMObj;
  }

}
