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
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.JPF_java_lang_reflect_Field;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StaticElementInfo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import nhandler.conversion.ConversionException;

/**
 * A JVM2JPFConverter to convert java.lang.reflect.Field objects to their JPF
 * counterparts
 * 
 * @author Chinmay Dabral
 */

public class JVM2JPFjava_lang_reflect_FieldConverter extends JVM2JPFConverter {

  /**
   * No static fields to set
   */
  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {

  }

  /**
   * Register the FieldInfo to get regIdx and set it on the Field object
   */
  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {
    Field jvmField = (Field) JVMObj;
    Class<?> fieldDeclClass = jvmField.getDeclaringClass();
    boolean isStatic = ((Modifier.toString(jvmField.getModifiers())).indexOf("static") != -1);

    ClassInfo fieldDeclCi = obtainJPFCls(fieldDeclClass, env);
    FieldInfo fi = null;
    if (isStatic)
      fi = fieldDeclCi.getDeclaredStaticField(jvmField.getName());
    else
      fi = fieldDeclCi.getDeclaredInstanceField(jvmField.getName());
    System.out.println("fieldInfo: " + fi);// TODO: remove

    // register FieldInfo to get regIdx and set it on the Field object
    int rIdx = registerFieldInfo(env, fi);
    ClassInfo fieldCi = obtainJPFCls(Field.class, env);
    FieldInfo regFi = fieldCi.getInstanceField("regIdx");
    dei.setIntField(regFi, rIdx);
  }

  /**
   * Register the given FieldInfo, by reflectively calling the registerFieldInfo
   * method of the JPF_java_lang_reflect_Field class
   * 
   * @param env
   * @param fi
   *          The FieldInfo to register
   * @return The regIdx from registration
   */
  private int registerFieldInfo (MJIEnv env, FieldInfo fi) {
    int regIdx = MJIEnv.NULL;
    try {
      Method registerFi = JPF_java_lang_reflect_Field.class.getDeclaredMethod("registerFieldInfo", FieldInfo.class);
      registerFi.setAccessible(true);
      try {
        regIdx = (Integer) registerFi.invoke(null, fi);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }
    return regIdx;
  }
}
