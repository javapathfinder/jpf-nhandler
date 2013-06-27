package nhandler.conversion.jpf2jvm;

import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StaticElementInfo;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import nhandler.conversion.ConversionException;

public class JPF2JVMjava_util_RandomConverter extends JPF2JVMConverter{

  /**
   * Nothing to set here
   */
  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {
    
  }

  /**
   * We take the seed from the JPF object and set it on the Random object
   */
  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {
    ClassInfo randCi = dei.getClassInfo();
    FieldInfo seedFi = randCi.getInstanceField("seed");
    long seed = dei.getLongField(seedFi);
    //Now set the seed on the JMVObj:
    Field seedField = null;
    try {
     seedField = Random.class.getDeclaredField("seed");
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }
    seedField.setAccessible(true);
    try {
      seedField.set(JVMObj, new AtomicLong(seed));
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  /**
   * Just return a new Random object
   */
  @Override
  protected Object instantiateFrom (Class<?> cl, int JPFRef, MJIEnv env) {
    Object JVMObj = null;
    assert cl == Random.class;
    JVMObj = new Random();
    return JVMObj;
  }
}
