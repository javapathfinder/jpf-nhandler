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

package nhandler.conversion;

import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.MJIEnv;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import nhandler.util.ValueIdentityHashMap;

/**
 * This is just a common root of all Converters and keeps key elements used by 
 * Converter classes 
 * 
 * @author Nastaran Shafiei
 */
public class ConverterBase {

  static boolean resetState;

  /**
   * Keeps track of the JVM objects that have been already created from their
   * corresponding JPF objects, while performing conversion from JPF to JVM
   */
  protected static ValueIdentityHashMap<Integer, Object> objMapJPF2JVM = new ValueIdentityHashMap<Integer, Object>();

  /**
   * Keeps track of the JVM classes that have been already created from their
   * corresponding JPF classes, while performing conversion from JPF to JVM
   */
  protected static ValueIdentityHashMap<Integer, Class<?>> classMapJPF2JVM = new ValueIdentityHashMap<Integer, Class<?>>();

  /**
   * Keeps track of the JPF objects that have been already updated from their
   * corresponding JVM objects, while performing conversion from JVM to JPF
   */
  protected static HashMap<Integer, Object> updatedJPFObj = new ValueIdentityHashMap<Integer, Object>();

  /**
   * Keeps track of the JPF classes that have been already updated from their
   * corresponding JVM classes, while performing conversion from JVM to JPF
   */
  protected static Set<Integer> updatedJPFCls = new HashSet<Integer>();

  public static ConverterFactory converterFactory;

  public static void init() {
    converterFactory = new DefaultConverterFactory();
  }

  public static boolean isResetState() {
    return resetState;
  }

  /**
   * This needs to be invoked at the beginning of every method created on-the-fly
   */
  public static void reset(MJIEnv env) {
    ConverterBase.resetState = env.getConfig().getBoolean("nhandler.resetVMState");

    if (ConverterBase.resetState) {
      // these are reset on-demond by setting the nhandler.resetVMState
      // property in the properties file
      ConverterBase.objMapJPF2JVM.clear();
      ConverterBase.classMapJPF2JVM.clear();
    } 
    // for the cases that nhandler is configured to re-use existing maps (that
    // is "nhandler.resetVMState" is set to false) we need to identify and remove 
    // those objects that have been garbage collected or don't exist anymore due 
    // to bracktracking - note that this is provided that SGOIDs are unique.
    else {
      Integer[] keys = objMapJPF2JVM.keySet().toArray(new Integer[objMapJPF2JVM.size()]);
      for(int i=0; i<keys.length; i++){
        int key = keys[i];
        ElementInfo ei = env.getElementInfo(key);
        if(ei==null) {
          ConverterBase.objMapJPF2JVM.remove(key);
        }
      }
    }

    // these always need to be reset
    ConverterBase.updatedJPFObj.clear();
    ConverterBase.updatedJPFCls.clear();
  }
}
