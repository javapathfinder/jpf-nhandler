package nhandler.conversion.jpf2jvm;

import java.util.concurrent.atomic.AtomicIntegerArray;

import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StaticElementInfo;
import nhandler.conversion.ConversionException;

public class JPF2JVMjava_util_concurrent_atomic_AtomicIntegerArrayConverter extends JPF2JVMConverter {

  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {

  }

  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {

  }

  @Override
  protected Object instantiateFrom (Class<?> cl, int JPFRef, MJIEnv env) {
    assert cl == AtomicIntegerArray.class : "Not the right converter";
    Object JVMObj = null;
    
    int JPFArrayRef = env.getReferenceField(JPFRef, "array");
    int[] JVMArray = null;
    try {
      JVMArray = (int[]) obtainJVMObj(JPFArrayRef, env);
    } catch (ConversionException e) {
      e.printStackTrace();
    }
    
    JVMObj = new AtomicIntegerArray(JVMArray);
    return JVMObj;
  }

}
