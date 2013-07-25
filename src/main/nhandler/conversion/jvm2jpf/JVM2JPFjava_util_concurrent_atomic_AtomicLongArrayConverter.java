package nhandler.conversion.jvm2jpf;

import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StaticElementInfo;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicLongArray;

import nhandler.conversion.ConversionException;

public class JVM2JPFjava_util_concurrent_atomic_AtomicLongArrayConverter extends JVM2JPFConverter {

  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {

  }

  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {
    assert JVMObj instanceof AtomicLongArray : "Not the right converter";
    
    int JPFArrayRef = obtainJPFObj(getJVMArray((AtomicLongArray) JVMObj), env);
    dei.setReferenceField("array", JPFArrayRef);
  }
  
  private long[] getJVMArray(AtomicLongArray JVMObj) {
    Field arrayField = null;
    try {
      arrayField = AtomicLongArray.class.getDeclaredField("array");
    } catch (NoSuchFieldException e1) {
      e1.printStackTrace();
    } catch (SecurityException e1) {
      e1.printStackTrace();
    }
    arrayField.setAccessible(true);
    
    long[] array = null;
    try {
      array = (long[]) arrayField.get(JVMObj);
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    
    return array;
  }

}
