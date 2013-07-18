package nhandler.conversion.jpf2jvm;

import java.util.regex.Pattern;

import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StaticElementInfo;
import nhandler.conversion.ConversionException;

public class JPF2JVMjava_util_regex_PatternConverter extends JPF2JVMConverter {

  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {

  }

  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {

  }

  @Override
  protected Object instantiateFrom (Class<?> cl, int JPFRef, MJIEnv env) {
    assert cl == Pattern.class;
    Object JVMObj = null;
    
    String regex = env.getStringField(JPFRef, "regex");
    int flags = env.getIntField(JPFRef, "flags");
    
    JVMObj = Pattern.compile(regex, flags);
    
    return JVMObj;
  }

}
