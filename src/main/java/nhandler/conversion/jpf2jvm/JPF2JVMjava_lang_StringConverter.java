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

package nhandler.conversion.jpf2jvm;

import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StaticElementInfo;
import nhandler.conversion.ConversionException;

public class JPF2JVMjava_lang_StringConverter extends JPF2JVMConverter {

  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {

  }

  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {

  }

  @Override
  protected Object instantiateFrom (Class<?> cl, int JPFRef, MJIEnv env) {
    assert cl == String.class;
    
    try {
      Object JVMObj = env.getStringObject(JPFRef);
      return JVMObj;
    } catch (gov.nasa.jpf.JPFException e) {
      // Fallback for compact strings mismatch (JDK 11 byte[] vs legacy char[])
      // Handles cases like JarFile String fields where JPF String value is CharArrayFields
      // but getStringBytes expects ByteArrayFields (see jpf-nhandler#14, jpf-core#507 context)
      if (e.getMessage() != null && e.getMessage().contains("not a byte[]")) {
        try {
          char[] chars = env.getStringChars(JPFRef);
          if (chars != null) {
            System.out.println("INFO: String conversion fallback to char[] for JPFRef " + JPFRef);
            return new String(chars);
          }
        } catch (Exception e2) {
          // ignore, rethrow original
        }
      }
      throw e;
    }
  }

}
