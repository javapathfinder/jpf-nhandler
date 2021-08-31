/* 
 * Copyright (C) 2013  Nastaran Shafiei and Franck van Breugel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You can find a copy of the GNU General Public License at
 * <http://www.gnu.org/licenses/>.
 */

package nhandler.conversion.jvm2jpf;

import gov.nasa.jpf.util.DynamicObjectArray;
import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;
import gov.nasa.jpf.vm.StaticElementInfo;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import nhandler.conversion.ConversionException;

public class JVM2JPFjava_io_FileInputStreamConverter extends JVM2JPFConverter {

  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {
    
  }

  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {
    assert JVMObj instanceof FileInputStream;
    
    int JPFFd = constructJPFFileDescriptor((FileInputStream) JVMObj, env);
    dei.setReferenceField("fd", JPFFd);
  }
  
  private int constructJPFFileDescriptor(FileInputStream JVMObj, MJIEnv env) {
    int JPFFd = env.newObject("java.io.FileDescriptor");
    
    NativePeer peer = env.getClassInfo(JPFFd).getNativePeer();
    DynamicObjectArray<Object> array = getDynamicObjectArrayFromPeer(peer);
    
    int index = addIfNotPresent(array, JVMObj);
    incrementNativePeerCounter(peer);
    
    // TODO: JPF FileDescriptor stores the filename (String fileName),
    // Which we can't retrieve from a FileInputStream
    // The field is used to reopen FileInputStreams when JPF backtracks
    env.setReferenceField(JPFFd, "fileName", MJIEnv.NULL);
    
    // set state to FD_OPENED
    int FD_OPENED = env.getClassInfo(JPFFd).getStaticElementInfo().getIntField("FD_OPENED");
    env.setIntField(JPFFd, "state", FD_OPENED);
    // set mode to FD_READ
    int FD_READ = env.getClassInfo(JPFFd).getStaticElementInfo().getIntField("FD_READ");
    env.setIntField(JPFFd, "mode", FD_READ);
    // set offset to FileInputStream.position()
    try {
      env.setLongField(JPFFd, "off", JVMObj.getChannel().position());
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    return JPFFd;
  }
  
  private void incrementNativePeerCounter(NativePeer peer) {
    Field countField = null;
    try {
      countField = peer.getClass().getDeclaredField("count");
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }
    countField.setAccessible(true);
    
    int oldCount = 0;
    try {
      oldCount = countField.getInt(peer);
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    
    try {
      countField.setInt(peer, oldCount + 1);
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }
  
  private DynamicObjectArray<Object> getDynamicObjectArrayFromPeer(NativePeer peer) {
    DynamicObjectArray<Object> array = null;
    
    Field arrayField = null;
    try {
      arrayField = peer.getClass().getDeclaredField("content");
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }
    arrayField.setAccessible(true);
    
    try {
      array = (DynamicObjectArray<Object>) arrayField.get(peer);
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    
    return array;
  }
  
  private int addIfNotPresent(DynamicObjectArray<Object> array, FileInputStream fis) {
    
    for (int i = 0; i <= array.getMaxIndex(); i++) {
      if (array.get(i) == fis) {
        System.out.println("addIfNotPresent: present");
        return i;
      }
    }
    
    System.out.println("addIfNotPresent: not present");
    
    array.set(array.getMaxIndex() + 1, fis);
    
    return array.getMaxIndex();
  }

}
