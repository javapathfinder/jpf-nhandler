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
 * A conformance checker will tell us whether or not a specific Converter is
 * needed for a model class - JVM class pair
 * 
 * @author Chinmay Dabral
 */
public interface ConformanceChecker {

  /**
   * Checks if a model class conforms to the corresponding JVM class
   * 
   * @param className
   *          name of the class to check for
   * @return true for conformant (no specific converter is needed) false for
   *         non-conformant (specific converter needed)
   */
  public abstract boolean isConformant (String className);
}
