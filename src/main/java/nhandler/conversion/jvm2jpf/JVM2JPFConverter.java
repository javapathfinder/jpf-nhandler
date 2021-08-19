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

import gov.nasa.jpf.vm.ArrayFields;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ClassInfoException;
import gov.nasa.jpf.vm.ClassLoaderInfo;
import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StaticElementInfo;

import java.util.Iterator;

import nhandler.conversion.ConversionException;
import nhandler.conversion.ConverterBase;

/**
 * This class is used to converter objects and classes from JVM to JPF
 * 
 * @author Nastaran Shafiei
 */

public abstract class JVM2JPFConverter extends ConverterBase {

  public static ClassInfo obtainJPFCls (Class<?> JVMCls, MJIEnv env) throws ConversionException{
    if(JVMCls == null) {
      return null;
    }

    JVM2JPFConverter converter = ConverterBase.converterFactory.getJVM2JPFConverter(JVMCls.getName());
    return converter.getJPFCls(JVMCls, env);
  }

  public static int obtainJPFObj (Object JVMObj, MJIEnv env) throws ConversionException {
    if (JVMObj == null){
      return MJIEnv.NULL;
    }

    JVM2JPFConverter converter = ConverterBase.converterFactory.getJVM2JPFConverter(JVMObj.getClass().getName());
    return converter.getJPFObj(JVMObj, env);
  }

  public static void updateJPFObj (Object JVMObj, int JPFObj, MJIEnv env) throws ConversionException {
    if (JVMObj == null){ 
      return;
    }

    JVM2JPFConverter converter = ConverterBase.converterFactory.getJVM2JPFConverter(JVMObj.getClass().getName());
    converter.getUpdatedJPFObj(JVMObj, JPFObj, env);
  }

  /**
   * Returns a JPF class corresponding to the given JVM Class object. If such an
   * class exists, it is updated (if it has not been updated) corresponding to
   * the given JVMObj. Otherwise a new JPF class corresponding to the given JVM
   * class object is created and added to the list of the JPF loaded classes.
   * 
   * @param JVMCls
   *          a JVM Class object
   * 
   * @return a JPF class corresponding to the given JVM class, JVMCls
   * 
   * @throws ConversionException
   *           if any incorrect input parameter is observed
   */
  protected ClassInfo getJPFCls (Class<?> JVMCls, MJIEnv env) throws ConversionException{
    ClassInfo JPFCls = null;
    int JPFClsRef = Integer.MIN_VALUE;

    if (JVMCls != null){
      // retrieving the integer representing the Class in JPF
      JPFCls = ClassLoaderInfo.getCurrentResolvedClassInfo(JVMCls.getName());
      StaticElementInfo sei = JPFCls.getModifiableStaticElementInfo();

      if (sei != null){
        JPFClsRef = sei.getObjectRef();
      }

      // First check if the class has been already updated
      if (!ConverterBase.updatedJPFCls.contains(JPFClsRef)){
        
        /**
         * If the corresponding ClassInfo does not exist, a new ClassInfo object
         * is created and will be added to the loadedClasses.
         */
        if (!JPFCls.isRegistered()){
          JPFCls.registerClass(env.getThreadInfo());
          sei = JPFCls.getStaticElementInfo();
          JPFClsRef = sei.getObjectRef();
        } else {
          sei = JPFCls.getModifiableStaticElementInfo();
          JPFClsRef = sei.getObjectRef();
        }

        // This is to avoid JPF to initialized the class
        JPFCls.setInitialized();

        ConverterBase.updatedJPFCls.add(JPFClsRef);

        setStaticFields(JVMCls, sei, env);
      }
    }
    return JPFCls;
  }

  /**
   * Sets the static fields of a JPF class corresponding to the given JVM class
   * @param JVMCls a class object in JVM
   * @param sei captures the value of static fields
   * @throws ConversionException
   */
  protected abstract void setStaticFields(Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException;

  /**
   * Returns a JPF object corresponding to the given JVM object. If such an
   * object exists, it is updated (if it has not been updated) corresponding to
   * the given JVMObj. Otherwise a new JPF object corresponding to the given JVM
   * object is created.
   * 
   * @param JVMObj
   *          a JVM object
   * 
   * @return a JPF object corresponding to the given JVM object, JVMObj
   * 
   * @throws ConversionException
   *           if any incorrect input parameter is observed
   */
  protected int getJPFObj (Object JVMObj, MJIEnv env) throws ConversionException {
    int JPFRef = MJIEnv.NULL;
    if (JVMObj != null){
      if (JVMObj.getClass() == Class.class){
        JPFRef = (this.getJPFCls((Class<?>) JVMObj, env)).getClassObjectRef();
      }  
      /** by uncommenting this code, when converting JVM objects to JPF objects, 
          nhandler uses the current ClassLoaderInfo for the JPFClassLoader object **/
//      else if (JVMObj == env.getConfig().getClassLoader()) {
//        JPFRef = ClassLoaderInfo.getCurrentClassLoader().getClassLoaderObjectRef();
//      } 
      else{
        JPFRef = this.getExistingJPFRef(JVMObj, true, env);
        if (JPFRef == MJIEnv.NULL){
          JPFRef = this.getNewJPFRef(JVMObj, env);
        }
      }
    }
    return JPFRef;
  }

  /**
   * Updates the given JPF object, according to the given JVM object. For the
   * case of the non-array object, its JPF class is also updated according to
   * the class of the given JVM object.
   * 
   * @param JVMObj
   *          a JVM object
   * @param JPFObj
   *          a JPF object
   * 
   * @throws ConversionException
   *           if any incorrect input parameter is observed
   */
  protected void getUpdatedJPFObj (Object JVMObj, int JPFObj, MJIEnv env) throws ConversionException{
    //if (this.isValidJPFRef(JVMObj, JPFObj)){
      // Both JVM and JPF objects are null, No need for updating
      if (JVMObj == null){ 
        return; 
      }

      if (JVMObj.getClass().isArray()){
        this.updateJPFArrObj(JVMObj, JPFObj, env);
      } else{
        this.updateJPFNonArrObj(JVMObj, JPFObj, env);
      }
   // } else{
   //   throw new ConversionException("The given JPFObj is not valid!");
   // }
  }

  /**
   * Checks if the given JPF reference is valid, meaning that either the JPF
   * reference represents the same object as the given JVM object. This method
   * should be always invoked before updating the given JPF object corresponding
   * to the given JVM object.
   * 
   * @param JVMObj
   *          a JVM object
   * @param JPFObj
   *          a JPF object
   * @return true if the given JPFObj represents the same object as JPFObj, OW
   *         false is returned.
   * 
   * @throws ConversionException
   *           if any incorrect input parameter is observed
   */
  // todo - investigate this using Socket.jpf example
  protected boolean isValidJPFRef (Object JVMObj, int JPFObj, MJIEnv env) throws ConversionException{
    boolean isValid;

    if(!ConverterBase.isResetState()) {
      return true;
    }

    if (JPFObj == MJIEnv.NULL || JVMObj == null){
      isValid = (JPFObj == MJIEnv.NULL && JVMObj == null);
    } else if (JVMObj.getClass() == Class.class){
      isValid = true;
    } else{
      int existingJPFObj = this.getExistingJPFRef(JVMObj, false, env);
      isValid = (existingJPFObj == JPFObj);// || existingJPFObj == MJIEnv.NULL);
    }
    return isValid;
  }

  /**
   * Updates the given non-array JPF object according to the given non-array JVM
   * object. The class of the object is also updated according to the class of
   * the given JVM object.
   * 
   * @param JVMObj
   *          a non-array JVM object
   * @param JPFObj
   *          a non-array JPF object
   * 
   * @throws ConversionException
   *           if any incorrect input parameter is observed
   */
  protected void updateJPFNonArrObj (Object JVMObj, int JPFObj, MJIEnv env) throws ConversionException{
    if (JVMObj != null){
      // First check if the JPF object has been already updated
      if (!ConverterBase.updatedJPFObj.containsKey(JPFObj)){
        ConverterBase.updatedJPFObj.put(JPFObj, JVMObj);
        ConverterBase.objMapJPF2JVM.put(JPFObj, JVMObj);

        // Why do we need that? Because JPF might have not leaded the class
        // before! JPF classloader does not recognize them!
        // I don't now why exactly!
        // INVESTIGATE: Why not arrays?
        if (JVMObj.getClass() == Class.class){
          try{
            Class<?> temp = (Class<?>) JVMObj;
            if (!temp.isArray() && !temp.isPrimitive())
              env.getConfig().getClassLoader().loadClass(temp.getName());
          } catch (ClassNotFoundException e1){
            e1.printStackTrace();
          }
        }
        
        DynamicElementInfo dei = (DynamicElementInfo) env.getHeap().getModifiable(JPFObj);

        setInstanceFields(JVMObj, dei, env);
      }
    }
  }

  protected abstract void setInstanceFields(Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException;

  /**
   * Updates the given JPF array according to the given JVM array.
   * 
   * @param JVMArr
   *          a JVM array
   * @param JPFArr
   *          a JPF array
   * 
   * @throws ConversionException
   *           if any incorrect input parameter is observed
   */
  protected void updateJPFArrObj (Object JVMArr, int JPFArr, MJIEnv env) throws ConversionException{
    if (JVMArr != null){
      // First check if the JPF array has been already updated
      if (!ConverterBase.updatedJPFObj.containsKey(JPFArr)){
        ConverterBase.updatedJPFObj.put(JPFArr, JVMArr);
        ConverterBase.objMapJPF2JVM.put(JPFArr, JVMArr);

        DynamicElementInfo dei = (DynamicElementInfo) env.getHeap().getModifiable(JPFArr);

        ArrayFields fields = (ArrayFields) dei.getFields();

        // Array of primitive type
        if (dei.getClassInfo().getComponentClassInfo().isPrimitive()){
          JVM2JPFUtilities.setJPFPrimitiveArr(dei, JVMArr, env);
        }
        // Array of Non-primitives
        else{
          int arrSize = fields.arrayLength();
          Object[] arrObj = (Object[]) JVMArr;

          for (int i = 0; i < arrSize; i++){
            int elementValueRef = dei.getReferenceElement(i);

            if (arrObj[i] == null){
              elementValueRef = MJIEnv.NULL;
            } else if (elementValueRef == MJIEnv.NULL || ConverterBase.objMapJPF2JVM.get(elementValueRef) != arrObj[i]){
              elementValueRef = obtainJPFObj(arrObj[i], env);
            } else if (ConverterBase.objMapJPF2JVM.get(elementValueRef) == arrObj[i]){
              updateJPFObj(arrObj[i], elementValueRef, env);
            } else{
              throw new ConversionException("Unconsidered case observed! - JVM2JPF.updateArr()");
            }
            dei.setReferenceElement(i, elementValueRef);
          }
        }
      }
    }
  }

  /**
   * Looks info the existing hash tables, declared in Converter, to see if the
   * JPF object corresponding to the given JVM object has been already created.
   * 
   * @param JVMObj
   *          a JVM object
   * 
   * @return a non-null JPF object if there already exists a JPF object
   *         corresponding to the given JVM object. OW null JPF object is
   *         returned.
   * 
   * @throws ConversionException
   *           if any incorrect input parameter is observed
   */
  protected int getExistingJPFRef (Object JVMObj, boolean update, MJIEnv env) throws ConversionException{
    int JPFRef = MJIEnv.NULL;
    boolean found = false;
    if (ConverterBase.updatedJPFObj.containsValue(JVMObj)){
      Iterator<Integer> iterator = (ConverterBase.updatedJPFObj.keySet()).iterator();
      Integer key;
      while (!found && iterator.hasNext()){
        key = iterator.next();
        Object value = ConverterBase.updatedJPFObj.get(key);
        if (value == JVMObj){
          found = true;
          JPFRef = key;
        }
      }
    }

    if (!found && ConverterBase.objMapJPF2JVM.containsValue(JVMObj)){
      Iterator<Integer> iterator = (ConverterBase.objMapJPF2JVM.keySet()).iterator();
      Integer key;
      while (!found && iterator.hasNext()){
        key = iterator.next();
        Object value = ConverterBase.objMapJPF2JVM.get(key);
        if (value == JVMObj){
          found = true;
          JPFRef = key;
          if (update == true){
            getUpdatedJPFObj(JVMObj, JPFRef, env);
          }
        }
      }
    }
    return JPFRef;
  }

  /**
   * Creates a new JPF object corresponding to the given JVM object. It also
   * updates the class of the JPF object according to the class of the given JVM
   * object.
   * 
   * @param JVMObj
   *          a JVM object
   * @return a new JPF object corresponding to the given JVM object.
   * 
   * @throws ConversionException
   *           if any incorrect input parameter is observed
   * 
   */
  protected int getNewJPFRef (Object JVMObj, MJIEnv env) throws ConversionException{
    int JPFRef = MJIEnv.NULL;
    Class<?> JVMCls = JVMObj.getClass();
    if (!JVMCls.isArray()){
      // we treat Strings differently, until we immigrate to JDK7
      if(JVMObj.getClass()==String.class) {
        JPFRef = env.newString(JVMObj.toString());
      } else {
        ClassInfo fci = null;
        try{
          fci = this.getJPFCls(JVMCls, env);
        } catch (ClassInfoException e){
          System.out.println("WARNING: the class " + JVMCls + " is ignored!");
          return MJIEnv.NULL;
        }

        ElementInfo ei = env.getHeap().newObject(fci, env.getThreadInfo());
        JPFRef = ei.getObjectRef();
        
        this.updateJPFNonArrObj(JVMObj, JPFRef, env);
      }
    } else{
      JPFRef = JVM2JPFUtilities.createNewJPFArray(JVMObj, env);
      this.updateJPFArrObj(JVMObj, JPFRef, env);
    }

    return JPFRef;
  }
}