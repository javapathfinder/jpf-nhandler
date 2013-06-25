package nhandler.conversion.jvm2jpf;

import java.io.File;

import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StaticElementInfo;
import nhandler.conversion.ConversionException;

/**
 * JVM2JPF converter for java.io.File Just need to set the filename, as the
 * native peer JPF_java_io_File uses JVM File internally, getting a new object
 * every time from the stored filename
 * 
 * @author Chinmay Dabral
 */
public class JVM2JPFjava_io_FileConverter extends JVM2JPFConverter {

  /**
   * No static fields to set
   */
  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {

  }

  /**
   * Similar to JPF_java_io_File.createJPFFile
   */
  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {
    File file = (File) JVMObj;
    int fileNameRef = env.newString(file.getPath());
    dei.setReferenceField("filename", fileNameRef);
  }
}
