package nhandler.conversion.jpf2jvm;

import java.io.FileInputStream;
import java.lang.reflect.Field;

import gov.nasa.jpf.util.DynamicObjectArray;
import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;
import gov.nasa.jpf.vm.StaticElementInfo;
import nhandler.conversion.ConversionException;

public class JPF2JVMjava_io_FileInputStreamConverter extends JPF2JVMConverter {

  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {

  }

  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {

  }

  /**
   * All the tasks are delegated by JPF FileInputStream to a JPF FileDescriptor object
   * That object, in turn, delegates to a JVM FileInputStream object
   * We have to get this object and return it
   */
  @Override
  protected Object instantiateFrom (Class<?> cl, int JPFRef, MJIEnv env) {
    assert cl == FileInputStream.class;
    FileInputStream JVMObj = null;
    
    int JPFFd = env.getReferenceField(JPFRef, "fd");
    DynamicObjectArray<Object> array = getDynamicObjectArrayFromPeer(JPFFd, env);
    
    int fdId = env.getIntField(JPFFd, "fd");
    
    Object value = array.get(fdId);
    assert value instanceof FileInputStream : "Didn't get the right object!";
    JVMObj = (FileInputStream) value;
    
    return JVMObj;
  }
  
  /**
   * The delegatees for FileInputStream and FileOutputStream are stored by
   * the FileDescriptor native peer in a DynamicObjectArray
   * @param JPFRef JPF ref for a FileDescriptor
   * @param env
   * @return The delegatee FileInputStream object
   */
  private DynamicObjectArray<Object> getDynamicObjectArrayFromPeer(int JPFRef, MJIEnv env) {
    NativePeer peer = env.getClassInfo(JPFRef).getNativePeer();
    Field DOAField = null;
    try {
      DOAField = peer.getClass().getDeclaredField("content");
    } catch (NoSuchFieldException e1) {
      e1.printStackTrace();
    } catch (SecurityException e1) {
      e1.printStackTrace();
    }
    DOAField.setAccessible(true);
    
    DynamicObjectArray<Object> array = null;
    try {
      array = (DynamicObjectArray<Object>) DOAField.get(peer);
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    return array;
  }

}
