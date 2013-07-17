package nhandler.conversion.jvm2jpf;

import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StaticElementInfo;
import nhandler.conversion.ConversionException;

public class JVM2JPFjava_lang_ClassConverter extends JVM2JPFConverter {

  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {
  }

  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {
  }
  
  @Override
  protected int getJPFObj (Object JVMObj, MJIEnv env) throws ConversionException {
    assert JVMObj.getClass() == Class.class;
    System.out.println("get class");
    int JPFRef = MJIEnv.NULL;
    JPFRef = (this.getJPFCls((Class<?>) JVMObj, env)).getClassObjectRef();
    return JPFRef;
  }
  
  @Override
  protected void updateJPFNonArrObj (Object JVMObj, int JPFObj, MJIEnv env) throws ConversionException {
    super.updateJPFNonArrObj(JVMObj, JPFObj, env);
    System.out.println("update class");
    
    // Why do we need that? Because JPF might have not loaded the class
    // before! JPF classloader does not recognize them!
    // I don't now why exactly!
    // INVESTIGATE: Why not arrays?
    if (JVMObj.getClass() == Class.class){
      try{
        Class<?> temp = (Class<?>) JVMObj;
        if (!temp.isArray() && !temp.isPrimitive())
          env.getConfig().getClassLoader().loadClass(temp.getName());
      } catch (ClassNotFoundException e1){
        e1.printStackTrace();
      }
    }
  }
  
}
