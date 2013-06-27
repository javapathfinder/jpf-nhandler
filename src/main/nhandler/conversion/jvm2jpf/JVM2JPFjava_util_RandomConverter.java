package nhandler.conversion.jvm2jpf;

import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StaticElementInfo;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import nhandler.conversion.ConversionException;

public class JVM2JPFjava_util_RandomConverter extends JVM2JPFConverter {
  /**
   * No static fields to set
   */
  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {

  }

  /**
   * Take the seed from the JVM object and set it on the JPF object
   */
  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {
    assert JVMObj.getClass() == Random.class;

    Field seedField = null;
    try {
      seedField = Random.class.getDeclaredField("seed");
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }
    seedField.setAccessible(true);
    long seed = 0;
    try {
      seed = ((AtomicLong) seedField.get(JVMObj)).get();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }

    FieldInfo seedFi = dei.getClassInfo().getDeclaredInstanceField("seed");
    dei.setLongField(seedFi, seed);
  }

}
