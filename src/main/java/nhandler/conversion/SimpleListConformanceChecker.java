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

/**
 * Simply checks whether the class is in a list and reports accordingly about
 * conformance. Will be replaced later by a class which uses
 * jpf-conformance-checker
 * 
 * @author Chinmay Dabral
 */
public class SimpleListConformanceChecker implements ConformanceChecker {

  public static final String[] nonConformantClasses = { "java.util.Random", "java.lang.reflect.Method",
                                                        "java.lang.reflect.Field", "java.lang.reflect.Constructor",
                                                        "java.io.File", "java.text.DecimalFormat",
                                                        "java.lang.Class", "java.text.SimpleDateFormat",
                                                        "java.util.regex.Pattern", "java.lang.String",
                                                        "java.util.concurrent.atomic.AtomicIntegerArray",
                                                        "java.util.concurrent.atomic.AtomicLongArray",
                                                        "java.util.concurrent.atomic.AtomicReferenceArray",
                                                        "java.io.FileInputStream", "java.util.regex.Matcher" };

  private static ConformanceChecker instance;

  @Override
  public boolean isConformant (String className) {
    for (String i : nonConformantClasses)
      if (i.equals(className)) return false;
    return true;
  }

  public static ConformanceChecker getInstance () {
    if (instance == null) // Don't need synchronization here since JPF is serial
                          // (?)
      instance = new SimpleListConformanceChecker();
    return instance;
  }
}
