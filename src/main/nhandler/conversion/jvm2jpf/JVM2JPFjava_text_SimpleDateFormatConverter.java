package nhandler.conversion.jvm2jpf;

import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.JPF_java_text_Format;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StaticElementInfo;

import java.lang.reflect.Field;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import nhandler.conversion.ConversionException;

public class JVM2JPFjava_text_SimpleDateFormatConverter extends JVM2JPFConverter {

  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {

  }

  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {
    assert JVMObj instanceof SimpleDateFormat;

    int JPFref = dei.getObjectRef();
    int formatterId = env.getIntField(JPFref, "id");

    Field formattersField = null;
    try {
      formattersField = JPF_java_text_Format.class.getDeclaredField("formatters");
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }
    formattersField.setAccessible(true);
    HashMap<Integer, Format> formatters = null;
    try {
      formatters = (HashMap<Integer, Format>) formattersField.get(null);
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    assert formatters != null : "JPF_java_text_Format.init() hasn't been called";
    // TODO: Do we call init() ourselves if formatters == null?

    if (Utilities.hasMapExactObject(formatters, JVMObj)) {
      // This means the JVM object is a delegatee for a pre-existing
      // JPF object. So we don't need to do anything
      // This also means that the given JPF object should have this JVM
      // object as the value in the hashmap:
      Format value = formatters.get(formatterId);
      assert value != null : "value for formatterId null";
      assert value == JVMObj : "value for formatterId not JVMObj";
      System.out.println("setInstanceFields: existing delegatee");
    } else {
      // This means that this JVM object was created inside the delegated
      // method. We need to put it in the hashmap. This also means that
      // the id field of the JPF object wasn't set properly and nInstances
      // wasn't incremented
      ClassInfo ci = dei.getClassInfo().getSuperClass("java.text.Format");
      StaticElementInfo sei = ci.getModifiableStaticElementInfo();
      int nInstances = sei.getIntField("nInstances");
      dei.setIntField("id", nInstances);
      sei.setIntField("nInstances", nInstances + 1);
      formatters.put(nInstances, (Format) JVMObj);
    }
  }

}
