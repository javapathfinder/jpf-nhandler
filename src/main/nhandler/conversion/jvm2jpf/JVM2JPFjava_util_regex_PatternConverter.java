package nhandler.conversion.jvm2jpf;

import java.util.regex.Pattern;

import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StaticElementInfo;
import nhandler.conversion.ConversionException;

public class JVM2JPFjava_util_regex_PatternConverter extends JVM2JPFConverter {

  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {

  }

  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {
    assert JVMObj instanceof Pattern;
    
    Pattern jvmPattern = (Pattern) JVMObj;
    
    String regex = jvmPattern.pattern();
    int flags = jvmPattern.flags();
    
    int regexRef = env.newString(regex);
    dei.setReferenceField("regex", regexRef);
    dei.setIntField("flags", flags);
  }

}
