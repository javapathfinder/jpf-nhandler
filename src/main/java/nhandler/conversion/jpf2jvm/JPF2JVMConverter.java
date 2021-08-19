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

import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.ReferenceArrayFields;
import gov.nasa.jpf.vm.StaticElementInfo;

import java.lang.reflect.Array;

import nhandler.conversion.ConversionException;
import nhandler.conversion.ConverterBase;

/**
 * This class is used to converter objects and classes from JPF to JVM
 * 
 * @author Nastaran Shafiei
 */
public abstract class JPF2JVMConverter extends ConverterBase {

  public static Class<?> obtainJVMCls (int JPFRef, MJIEnv env) throws ConversionException {
    if (JPFRef == MJIEnv.NULL) {
      return null;
    }

    ClassInfo ci = env.getReferredClassInfo(JPFRef);
    JPF2JVMConverter converter = ConverterBase.converterFactory.getJPF2JVMConverter(ci.getName());
    return converter.getJVMCls(JPFRef, env);
  }

  public static Object obtainJVMObj (int JPFRef, MJIEnv env) throws ConversionException {
    if (JPFRef == MJIEnv.NULL) {
      return null;
    }

    DynamicElementInfo dei = (DynamicElementInfo) env.getHeap().get(JPFRef);
    ClassInfo ci = dei.getClassInfo();
    JPF2JVMConverter converter = ConverterBase.converterFactory.getJPF2JVMConverter(ci.getName());
    return converter.getJVMObj(JPFRef, env);
  }

  protected Class<?> loadClass(String cname, MJIEnv env) throws ClassNotFoundException {
    if(JPF2JVMUtilities.isArray(cname)) {
      return Class.forName(cname);
    } else {
      ClassLoader cl = env.getConfig().getClassLoader();
      return cl.loadClass(cname);
    }
  }
  
  /**
   * Returns a new JVM Class object corresponding to the given JPF class. If
   * such a Class object already exists, it is returned. Otherwise a new one is
   * created.
   */
  protected Class<?> getJVMCls (int JPFRef, MJIEnv env) throws ConversionException {
    Class<?> JVMCls = null;
    if (JPFRef != MJIEnv.NULL) {
      // First check if the class object has been already created.
      JVMCls = ConverterBase.classMapJPF2JVM.get(JPFRef);

      /**
       * If the Class object has not been created & the given JPF class is not
       * NULL, the corresponding JVM class object is created from JPFRef
       */
      if (JVMCls == null) {
        ClassInfo ci = env.getReferredClassInfo(JPFRef);

        // Used to store static fields
        StaticElementInfo sei = ci.getStaticElementInfo();

        try {
          JVMCls = loadClass(sei.getClassInfo().getName(), env);
          ConverterBase.classMapJPF2JVM.put(JPFRef, JVMCls);
        } catch (ClassNotFoundException e) {
          throw new NoClassDefFoundError(sei.getClassInfo().getName());
        }

        assert (JVMCls.getName() != ci.getName());

        setStaticFields(JVMCls, sei, env);
      }
    }
    return JVMCls;
  }

  protected abstract void setStaticFields(Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException;
  
  /**
   * Returns a JVM object corresponding to the given JPF object. If such an
   * object already exists, it is returned. Otherwise a new one is created.
   */
  protected Object getJVMObj (int JPFRef, MJIEnv env) throws ConversionException {
    if (env.isArray(JPFRef)) {
      return this.getJVMArrObj(JPFRef, env);
    } else {
      return this.getJVMNonArrObj(JPFRef, env);
    }
  }

  /**
   * Returns a non-array JVM object corresponding to the given non-array JPF
   * object. If such an object already exists, it is returned. Otherwise a new
   * one is created.
   */
  protected Object getJVMNonArrObj (int JPFRef, MJIEnv env) throws ConversionException {
    Object JVMObj = null;
    if (JPFRef != MJIEnv.NULL) {
      // First check if the object has been already created
      JVMObj = ConverterBase.objMapJPF2JVM.get(JPFRef);
      /**
       * If the object has not been created & the given JPF object is not NULL,
       * the corresponding JVM object is created from JPFRef
       */
      if (JVMObj == null) {
        // Used to store instance fields
        DynamicElementInfo dei = (DynamicElementInfo) env.getHeap().get(JPFRef);
        ClassInfo JPFCl = dei.getClassInfo();

        if (!JPFCl.isRegistered()){
          JPFCl.registerClass(env.getThreadInfo());
        }
        
        // we treat Strings differently
        /*if(JPFCl.isStringClassInfo()) {
          JVMObj = createStringObject(JPFRef, env);
        } else*/ {
          int JPFClsRef = JPFCl.getStaticElementInfo().getClassObjectRef();
          Class<?> JVMCl = this.getJVMCls(JPFClsRef, env);

          // There is only one instance of every class. There is no need to update
          // Class objects
          if (JVMCl == Class.class) {
            try {
              String name = env.getReferredClassInfo(JPFRef).getName();
              if (JPF2JVMUtilities.isPrimitiveClass(name)) {
                JVMObj = JPF2JVMUtilities.getPrimitiveClass(name);
              } else {
                JVMObj = loadClass(name, env);
              }
            } catch (ClassNotFoundException e) {
              e.printStackTrace();
            }
            return JVMObj;
          } else {
            // Creates a new instance of JVMCl
            JVMObj = instantiateFrom(JVMCl, JPFRef, env);
          }

          ConverterBase.objMapJPF2JVM.put(JPFRef, JVMObj);
          setInstanceFields(JVMObj, dei, env);
        }
      }
    }
    return JVMObj;
  }

  protected abstract void setInstanceFields(Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException;

  /**
   * Returns a JVM array corresponding to the given JPF array. If such an array
   * already exists, it is returned. Otherwise a new one is created.
   * 
   * @param JPFRef
   *          an integer representing a JPF array
   * 
   * @return a JVM array corresponding to the given JPF array, JPFRef
   * 
   * @throws ConversionException
   *           if any incorrect input parameter is observed
   */
  protected Object getJVMArrObj (int JPFRef, MJIEnv env) throws ConversionException {
    Object JVMArr = null;
    if (JPFRef != MJIEnv.NULL) {
      // First check if the array has been already created
      JVMArr = ConverterBase.objMapJPF2JVM.get(JPFRef);

      /**
       * If the array has not been created & the given JPF array is not NULL,
       * the corresponding JVM array is created from JPFRef
       */
      if (JVMArr == null) {
        // Used to store array elements
        DynamicElementInfo dei = (DynamicElementInfo) env.getHeap().get(JPFRef);

        // Array of primitive type
        if (dei.getClassInfo().getComponentClassInfo().isPrimitive()) {
          JVMArr = JPF2JVMUtilities.createJVMPrimitiveArr(dei);
        }
        // Array of Non-primitives
        else {
          int[] JPFArr = ((ReferenceArrayFields) dei.getFields()).asReferenceArray();
          int arrSize = JPFArr.length;

          Class<?> compType = null;
          try {
            compType = loadClass(dei.getClassInfo().getComponentClassInfo().getName(), env);
          } catch (ClassNotFoundException e) {
            e.printStackTrace();
          }

          Object[] arrObj = (Object[]) Array.newInstance(compType, arrSize);

          for (int i = 0; i < arrSize; i++) {
            arrObj[i] = obtainJVMObj(JPFArr[i], env);
          }
          JVMArr = arrObj;
        }
        ConverterBase.objMapJPF2JVM.put(JPFRef, JVMArr);
      }
    }
    return JVMArr;
  }

  protected abstract Object instantiateFrom (Class<?> cl, int JPFRef, MJIEnv env);

  protected Object createStringObject(int JPFRef, MJIEnv env) throws ConversionException {
    DynamicElementInfo str = (DynamicElementInfo) env.getHeap().get(JPFRef);
    if(!str.getClassInfo().isStringClassInfo()) {
      throw new ConversionException();
    }

    FieldInfo fi = str.getFieldInfo("value");
    int fieldValueRef = str.getFields().getReferenceValue(fi.getStorageOffset());

    // this is String.value which is of type of char[]
    Object value = this.getJVMObj(fieldValueRef, env);
    Object JVMObj = new String((char[])value);
    ConverterBase.objMapJPF2JVM.put(JPFRef, JVMObj);
    return JVMObj;
  }

}
