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

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.MJIEnv;

import java.util.Random;

import org.junit.Test;

public class JPF2JVMjava_utilTest extends TestJPF {
  // override vm.class and nhandler.resetVMState property as they may have been set by other extensions of JPF
  private final static String[] JPF_ARGS = { "+test.vm.class = gov.nasa.jpf.vm.SingleProcessVM", "+test.nhandler.resetVMState=true" };

  private static MJIEnv env;

  public static void main (String[] args) {
    runTestsOfThisClass(args);
  }

  public static void setEnv (MJIEnv env) {
    JPF2JVMjava_utilTest.env = env;
  }
  
  public static final int SEED = 42;
  
  private native void convertRandomTest (Random rand, int[] num);
  
  @Test
  public void convertRandomTest() {
    if(verifyNoPropertyViolation(JPF_ARGS)) {
      Random rand = new Random(SEED);
      int[] num = new int[10];
      for(int i = 0; i < 5; i++)
        num[i] = rand.nextInt();
      convertRandomTest(rand, num);
    }
  }
}
