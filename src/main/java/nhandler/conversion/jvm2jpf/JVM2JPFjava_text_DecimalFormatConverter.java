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

import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.JPF_java_text_Format;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StaticElementInfo;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.HashMap;

import nhandler.conversion.ConversionException;

public class JVM2JPFjava_text_DecimalFormatConverter extends JVM2JPFConverter {

  /**
   * No static fields to set
   */
  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {

  }

  /**
   * If this was called from getUpdatedJPFObj() for an existing object, then we
   * don't need to do anything, as the JVMObj that we supplied during JPF2JVM is
   * the delegatee and any changes to it will be reflected to this object
   * 
   * If this was called for translating a newly created object (one that was
   * created in JVM code), then we need to register it with the HashMap in the
   * Format native peer
   */
  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {
    assert JVMObj instanceof DecimalFormat;

    int JPFref = dei.getObjectRef();
    int formatterId = env.getIntField(JPFref, "id");

    Field formattersField = null;
    try {
      formattersField = JPF_java_text_Format.class.getDeclaredField("formatters");
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }
    formattersField.setAccessible(true);
    HashMap<Integer, Format> formatters = null;
    try {
      formatters = (HashMap<Integer, Format>) formattersField.get(null);
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    assert formatters != null : "JPF_java_text_Format.init() hasn't been called";
    // TODO: Do we call init() ourselves if formatters == null?

    if (JVM2JPFUtilities.hasMapExactObject(formatters, JVMObj)) {
      // This means the JVM object is a delegatee for a pre-existing
      // JPF object. So we don't need to do anything
      // This also means that the given JPF object should have this JVM
      // object as the value in the hashmap:
      Format value = formatters.get(formatterId);
      assert value != null : "value for formatterId null";
      assert value == JVMObj : "value for formatterId not JVMObj";
      System.out.println("setInstanceFields: existing delegatee");
    } else {
      // This means that this JVM object was created inside the delegated
      // method. We need to put it in the hashmap. This also means that
      // the id field of the JPF object wasn't set properly and nInstances
      // wasn't incremented
      ClassInfo ci = dei.getClassInfo().getSuperClass("java.text.Format");
      StaticElementInfo sei = ci.getModifiableStaticElementInfo();
      int nInstances = sei.getIntField("nInstances");
      dei.setIntField("id", nInstances);
      sei.setIntField("nInstances", nInstances + 1);
      formatters.put(nInstances, (Format) JVMObj);
    }
  }

}
