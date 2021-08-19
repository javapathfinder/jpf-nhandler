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

package converter.specific;

import java.io.File;

import nhandler.conversion.ConversionException;
import nhandler.conversion.ConverterBase;
import nhandler.conversion.jpf2jvm.JPF2JVMConverter;
import nhandler.conversion.jvm2jpf.JVM2JPFConverter;
import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;

public class JPF_converter_specific_java_ioTest extends NativePeer {
  
  @MJI
  public static int convertFileTest__Ljava_io_File_2__Ljava_io_File_2
  (MJIEnv env, int objRef, int rFile) {
    
    ConverterBase.reset(env);
    
    File file = null;
    
    try {
      file = (File) JPF2JVMConverter.obtainJVMObj(rFile, env);
    } catch (ConversionException e) {
      e.printStackTrace();
    }
    
    TestJPF.assertEquals(java_ioTest.FILE_PATH1, file.getPath());
    
    file = new File(java_ioTest.FILE_PATH2);
    
    int JPFRet = MJIEnv.NULL;
    try {
      JPFRet = JVM2JPFConverter.obtainJPFObj(file, env);
    } catch (ConversionException e) {
      e.printStackTrace();
    }
    
    return JPFRet;
  }
}
