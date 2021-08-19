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

import gov.nasa.jpf.vm.ArrayFields;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import nhandler.conversion.ConversionException;

/**
 * Helper methods needed for the conversion from JPF to JVM
 * 
 * @author Nastaran Shafiei
 */
public class JPF2JVMUtilities {

  /**
   * Sets a primitive field of a JVM object to a value of the corresponding
   * field of the given JPF object.
   * 
   * @param fld
   *          a field of a JVM object which is of primitive type.
   * @param obj
   *          The JVM object that includes the field fld.
   * @param ei
   *          a JPF object which is corresponding to the given JVM object, obj.
   * 
   * @throws IllegalAccessException
   *           when method trying to access a private field of an JVM object
   *           whose "isAccessible" is not true. But that will not happen,
   *           because in "getJVMObj" before invoking this method we always
   *           invoke "fld.setAccessible(true)".
   * 
   * @throws ConversionException
   *           if the given field is not of primitive type
   */
  public static void setJVMPrimitiveField (Field fld, Object obj, ElementInfo ei, FieldInfo fi) throws IllegalAccessException, ConversionException {
    if (fi.isBooleanField()) {
      fld.setBoolean(obj, ei.getBooleanField(fi));
    } else if (fi.isByteField()) {
      fld.setByte(obj, ei.getByteField(fi));
    } else if (fi.isShortField()) {
      fld.setShort(obj, ei.getShortField(fi));
    } else if (fi.isIntField()) {
      fld.setInt(obj, ei.getIntField(fi));
    } else if (fi.isLongField()) {
      fld.setLong(obj, ei.getLongField(fi));
    } else if (fi.isCharField()) {
      fld.setChar(obj, ei.getCharField(fi));
    } else if (fi.isFloatField()) {
      fld.setFloat(obj, ei.getFloatField(fi));
    } else if (fi.isDoubleField()) {
      fld.setDouble(obj, ei.getDoubleField(fi));
    } else {
      throw new ConversionException("Unknown premitive type " + fi.getType());
    }
  }

  /**
   * Creates an array of primitive type which is corresponding to the given JPF
   * array.
   * 
   * @param ei
   *          An ElementInfo which represents a JPF array of primitive type
   * 
   * @return a JVM array of primitive type which is created corresponding to the
   *         given JPF array represented by ei
   * 
   * @throws ConversionException
   *           if the given array is not of primitive type
   */
  public static Object createJVMPrimitiveArr (ElementInfo ei) throws ConversionException {
    String type = ei.getType();
    Object JVMObj = null;

    // byte[]
    if (type.equals("[B")) {
      JVMObj = ((ArrayFields) ei.getFields()).asByteArray();
    }
    // char[]
    else if (type.equals("[C")) {
      JVMObj = ((ArrayFields) ei.getFields()).asCharArray();
    }
    // short[]
    else if (type.equals("[S")) {
      JVMObj = ((ArrayFields) ei.getFields()).asShortArray();
    }
    // int[]
    else if (type.equals("[I")) {
      JVMObj = ((ArrayFields) ei.getFields()).asIntArray();
    }
    // float[]
    else if (type.equals("[F")) {
      JVMObj = ((ArrayFields) ei.getFields()).asFloatArray();
    }
    // long[]
    else if (type.equals("[J")) {
      JVMObj = ((ArrayFields) ei.getFields()).asLongArray();
    }
    // double[]
    else if (type.equals("[D")) {
      JVMObj = ((ArrayFields) ei.getFields()).asDoubleArray();
    }
    // boolean[]
    else if (type.equals("[Z")) {
      JVMObj = ((ArrayFields) ei.getFields()).asBooleanArray();
    } else {
      throw new ConversionException("Unknown array type " + type);
    }
    return JVMObj;
  }

  public static boolean isPrimitiveClass (String name) {
    return (name.equals("boolean") || name.equals("byte") || name.equals("int") || name.equals("short") || name.equals("long") || name.equals("char") || name.equals("float") || name.equals("double"));
  }

  /**
   * Returns a class corresponding to the given primitive type
   * 
   * @param name
   *          primitive type name
   * 
   * @return class corresponding to the given primitive type
   */
  public static Class<?> getPrimitiveClass (String name) {
    if (name.equals("boolean")) {
      return boolean.class;
    } else if (name.equals("byte")) {
      return byte.class;
    } else if (name.equals("int")) {
      return int.class;
    } else if (name.equals("short")) {
      return short.class;
    } else if (name.equals("long")) {
      return long.class;
    } else if (name.equals("char")) {
      return char.class;
    } else if (name.equals("float")) {
      return float.class;
    } else if (name.equals("double")) {
      return double.class;
    } else if (name.equals("void")) { return void.class; }
    return null;
  }

  public static boolean isArray (String cname) {
    return cname.startsWith("[");
  }
  
  public static Class<?>[] getClassesFromNames (String[] names) {
    if (names == null) return null;
    int length = names.length;
    Class<?>[] classes = new Class<?>[length];
    for (int i = 0; i < length; i++) {
      String name = names[i];
      if(name == null) {
        classes[i] = null;
        continue;
      }
      int arrayDim = 0;
      StringBuilder sb = new StringBuilder();
      while (name.endsWith("[]")) {
        name = name.substring(0, name.length() - 2);
        arrayDim++;
        sb.append("[");
      }
      if (arrayDim == 0) {
        // Not an array
        classes[i] = getPrimitiveClass(name);
        if (classes[i] == null) {
          try {
            classes[i] = Class.forName(name); // TODO: Do we need some special
                                              // classloader here?
          } catch (ClassNotFoundException e) {
            e.printStackTrace();
          }
        }
      } else {
        if (isPrimitiveClass(name))
          try {
            sb.append(nhandler.conversion.jvm2jpf.JVM2JPFUtilities.getPrimitiveTypeSymbol(name));
          } catch (ConversionException e1) {
            e1.printStackTrace();
          }
        else
          sb.append('L').append(name).append(';');
        try {
          classes[i] = Class.forName(sb.toString());
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }
      }
    }
    return classes;
  }
}
