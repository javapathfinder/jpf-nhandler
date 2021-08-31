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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StaticElementInfo;
import gov.nasa.jpf.vm.Types;
import nhandler.conversion.ConversionException;

/**
 * This class is used to convert objects and classes from JPF to JVM. This is only
 * applicable on types which are compatible between JPF and JVM, meaning that the same
 * classes are used to represent them in both environments.
 * 
 * @author Nastaran Shafiei
 */
public class JPF2JVMGenericConverter extends JPF2JVMConverter {
  
  @Override
  protected void setStaticFields(Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {
    ClassInfo ci = sei.getClassInfo();
    while (JVMCls!=null) {
      Field fld[] = JVMCls.getDeclaredFields();

      for (int i = 0; i < fld.length; i++) {
        boolean isStatic = ((Modifier.toString(fld[i].getModifiers())).indexOf("static") != -1);
        boolean isFinal = ((Modifier.toString(fld[i].getModifiers())).indexOf("final") != -1);

        // Provide access to private and final fields
        fld[i].setAccessible(true);
        FieldInfo fi = sei.getFieldInfo(fld[i].getName());

        // For class only set the values of static fields
        if (fi != null && isStatic) {
          /**
           * Why we check for !(isFinal)?
           * 
           * We do not set the value for "static final" fields. But we take
           * care of "non-static final" fields.
           * 
           * static final fields can be initialized at the declaration time,
           * OW it MUST be initialized inside the static block. By using
           * Class.forName() the class is initialized. Since when the class
           * is initialized the static blocks are executed, the static final
           * fields of object returned by Class.forName() have already have
           * the right values and we do not need to update their value.
           * 
           * Non-static final fields can be initialized at the declaration
           * time. But if the non-static field is final blank, it MUST be
           * initialized in the constructor. By using Class.newInstance()
           * the class is instantiated as if by a new expression with an
           * empty argument list. If the object represented by JPFRef
           * created using different constructor, the value of final blank
           * fields might be different when using the constructor with an
           * empty argument list. Therefore the values of non-static final
           * fields have to be set.
           */
          if (!isFinal) {
            String jpfType = Types.asTypeName(fi.getType());
            String jvmType = Types.asTypeName(fld[i].getType().getName());
            
            // note that if types are not the same, the current field is ignored 
            if (jpfType.equals(jvmType)) {
              // If the current field is of reference type
              if(!fld[i].getType().isPrimitive()) {
                int fieldValueRef = sei.getFields().getReferenceValue(fi.getStorageOffset());
                Object JVMField = obtainJVMObj(fieldValueRef, env);
                try {
                  fld[i].set(null, JVMField);
                } catch (IllegalArgumentException e) {
                  e.printStackTrace();
                } catch (IllegalAccessException e) {
                  e.printStackTrace();
                }
              }
              // If the current field is of primitive type
              else {
                try {
                  JPF2JVMUtilities.setJVMPrimitiveField(fld[i], JVMCls, sei, fi);
                } catch (IllegalAccessException e) {
                  e.printStackTrace();
                }
              }
            }
          }
        }
      }

      JVMCls = JVMCls.getSuperclass();
      ci = ci.getSuperClass();
      if(ci != null) {
        sei = ci.getStaticElementInfo();
      }
    }
  }

  @Override
  protected void setInstanceFields(Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {
    Class<?> cls = JVMObj.getClass();
    ClassInfo JPFCl = dei.getClassInfo();

    while (cls!=null && JPFCl!=null && cls.getName().equals(JPFCl.getName())) {
      Field fld[] = cls.getDeclaredFields();

      for (int i = 0; i < fld.length; i++) {

        // It is true if the field is declared as static.
        boolean isNonStaticField = ((Modifier.toString(fld[i].getModifiers())).indexOf("static") == -1);

        // Provide access to private and final fields
        fld[i].setAccessible(true);
        FieldInfo fi = JPFCl.getInstanceField(fld[i].getName());

        if (fi != null && isNonStaticField) {
          String jpfType = Types.asTypeName(fi.getType());
          String jvmType = Types.asTypeName(fld[i].getType().getName());
          
          // note that if types are not the same, the current field is ignored 
          if(jpfType.equals(jvmType)) {
            // Field is of reference type
            if(!fld[i].getType().isPrimitive()) {
              int fieldValueRef = dei.getFields().getReferenceValue(fi.getStorageOffset());
              Object JVMField = obtainJVMObj(fieldValueRef, env);

              try {
                fld[i].set(JVMObj, JVMField);
              } catch (IllegalArgumentException e) {
                e.printStackTrace();
              } catch (IllegalAccessException e) {
                e.printStackTrace();
              }
            }
            // Field is of primitive type
            else {
              try {
                JPF2JVMUtilities.setJVMPrimitiveField(fld[i], JVMObj, dei, fi);
              } catch (IllegalAccessException e) {
                e.printStackTrace();
              }
            }
          }
        }
      }
      cls = cls.getSuperclass();
      JPFCl = JPFCl.getSuperClass();
      
      if((cls==null && JPFCl!=null)||
          (cls!=null && JPFCl==null)||
          ((cls!=null && JPFCl!=null) && !cls.getName().equals(JPFCl.getName()))) {
        System.out.println("WARNING: inconsistencies between the model " + JVMObj.getClass() +
                           " and its corresponding standard class has been found.");
      }
    }
  }

  /**
   * Returns a new JVM object instantiated from the given class
   * 
   * @param cl A JVM class
   * @param JPFRef The JPF object being converted
   * @param env MJIEnv
   * 
   * @return a new JVM object instantiated from the given class, cl
   */
  @Override
  protected Object instantiateFrom (Class<?> cl, int JPFRef, MJIEnv env) {
    Object JVMObj = null;

    if (cl == Class.class) { 
      return cl; 
    }

    Constructor<?> ctor = getNoArgCtor(cl);
    try {
      ctor.setAccessible(true);
      JVMObj = ctor.newInstance();
    } catch (Exception e) {
      System.out.println("Cannot instantiate from " + cl + " using ctor " + ctor);
      e.printStackTrace();
    }
    return JVMObj;
  }

  /**
   * Returns a constructor with no arguments.
   * 
   * @param cl
   *          a JVM class
   * 
   * @return a constructor with no arguments
   */
  protected Constructor<?> getNoArgCtor (Class<?> cl) {
    Constructor<?>[] ctors = cl.getDeclaredConstructors();
    Constructor<?> ctor = null;

    // Check if the given class has a constructor with no arguments
    for (Constructor<?> c : ctors) {
      if (c.getParameterTypes().length == 0) {
        ctor = c;
      }
    }

    if (ctor == null) {
      try {
        ctor = sun.reflect.ReflectionFactory.getReflectionFactory().newConstructorForSerialization(cl, Object.class.getConstructor());
      } catch (Exception e1) {
        System.out.println("Cannot create a default constructor to instantiate from");
        e1.printStackTrace();
      }
    }
    return ctor;
  }
}
