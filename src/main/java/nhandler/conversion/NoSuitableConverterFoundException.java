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
 * Thrown when the generic converter cannot be used and there is no suitable
 * converter to use with a class
 * 
 * @author Chinmay Dabral
 */
@SuppressWarnings("serial")
public class NoSuitableConverterFoundException extends ConversionException {

  /**
   * Construct an exception without message
   */
  public NoSuitableConverterFoundException () {
    super();
  }

  /**
   * Construct an exception with message
   * 
   * @param message
   */
  public NoSuitableConverterFoundException (String message) {
    super(message);
  }
}
