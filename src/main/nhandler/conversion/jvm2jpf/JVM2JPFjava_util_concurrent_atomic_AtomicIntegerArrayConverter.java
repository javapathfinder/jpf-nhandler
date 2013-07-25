package nhandler.conversion.jvm2jpf;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicIntegerArray;

import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StaticElementInfo;
import nhandler.conversion.ConversionException;

public class JVM2JPFjava_util_concurrent_atomic_AtomicIntegerArrayConverter extends JVM2JPFConverter {

  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {

  }

  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {
    assert JVMObj instanceof AtomicIntegerArray;
    
    int JPFArrayRef = obtainJPFObj(getJVMArray((AtomicIntegerArray) JVMObj), env);
    dei.setReferenceField("array", JPFArrayRef);
  }
  
  private int[] getJVMArray(AtomicIntegerArray JVMObj) {
    Field arrayField = null;
    try {
      arrayField = AtomicIntegerArray.class.getDeclaredField("array");
    } catch (NoSuchFieldException e1) {
      e1.printStackTrace();
    } catch (SecurityException e1) {
      e1.printStackTrace();
    }
    int[] array = null;
    try {
      array = (int[]) arrayField.get(JVMObj);
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    
    return array;
  }

}
