package nhandler.conversion.jvm2jpf;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.regex.Matcher;

import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.JPF_java_util_regex_Matcher;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;
import gov.nasa.jpf.vm.StaticElementInfo;
import nhandler.conversion.ConversionException;

public class JVM2JPFjava_util_regex_MatcherConverter extends JVM2JPFConverter {

  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {
    // TODO Auto-generated method stub

  }

  /**
   * This works like JVM2JPF for java.text.DecimalFormat
   * Please see that for explanation
   * We only update if the JVMobj was not a delegatee
   */
  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {
    assert JVMObj instanceof Matcher : "Not the right converter";
    
    int id = dei.getIntField("id");
    
    Field matchersField = null;
    try {
      matchersField = JPF_java_util_regex_Matcher.class.getDeclaredField("matchers");
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }
    matchersField.setAccessible(true);
    
    Map<Integer, Matcher> matchers = null;
    NativePeer nativePeer = dei.getClassInfo().getNativePeer();
    
    try {
      matchers = (Map<Integer, Matcher>) matchersField.get(nativePeer);
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    
    assert matchers != null;
    if (Utilities.hasMapExactObject(matchers, JVMObj)) {
      Matcher value = matchers.get(id);
      assert value != null;
      assert value == JVMObj;   // This is an update call, so JVMObj is an existing delegatee
    } else { // This is not an update call
      ClassInfo ci = dei.getClassInfo();
      StaticElementInfo sei = ci.getModifiableStaticElementInfo();
      int nInstances = sei.getIntField("nInstances");
      dei.setIntField("id", nInstances);
      sei.setIntField("nInstances", nInstances + 1);
      matchers.put(nInstances, (Matcher) JVMObj);
    }
  }

}
