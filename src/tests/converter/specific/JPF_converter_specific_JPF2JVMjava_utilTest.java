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

import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;

import java.util.Arrays;
import java.util.Random;

import nhandler.conversion.ConversionException;
import nhandler.conversion.jpf2jvm.JPF2JVMConverter;

public class JPF_converter_specific_JPF2JVMjava_utilTest extends NativePeer{

  @MJI
  public static void convertRandomTest__Ljava_util_Random_2_3I__V
  (MJIEnv env, int objRef, int rRand, int rNum) {
    Random rand = null;
    try {
      rand = (Random) JPF2JVMConverter.obtainJVMObj(rRand, env);
    } catch (ConversionException e) {
      e.printStackTrace();
    }
    
    Random jvmRand = new Random(JPF2JVMjava_utilTest.SEED);
    
    /*
     * Half of num[] was filled in JPF and half in JVM
     * jvmNum[] was completely filled in JVM, starting
     * with the same seed
     * Both arrays should be equal
     */
    int[] num = env.getIntArrayObject(rNum);
    for(int i = 5; i < 10; i++) {
      num[i] = rand.nextInt();
    }
    
    int[] jvmNum = new int[10];
    for(int i = 0; i < 10; i++) {
      jvmNum[i] = jvmRand.nextInt();
    }
    
    TestJPF.assertTrue(Arrays.equals(jvmNum, num));
  }
}
