 package nhandler.conversion.jvm2jpf;

import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StaticElementInfo;
import nhandler.conversion.ConversionException;

public class JVM2JPFjava_lang_StringConverter extends JVM2JPFConverter {

  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {

  }

  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {
    System.out.println("JVM2JPF String");
    assert JVMObj instanceof String;
    
    String jvmString = (String) JVMObj;
    char[] chars = jvmString.toCharArray();
    int jpfChars = obtainJPFObj(chars, env);
    
    dei.setReferenceField("value", jpfChars);
  }

}
