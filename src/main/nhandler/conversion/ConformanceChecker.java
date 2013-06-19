/**
 * 
 */
package nhandler.conversion;

/**
 * @author chinmay
 * A conformance checker will tell us whether or not a specific
 * Converter is needed for a model class - JVM class pair
 */
public interface ConformanceChecker {
  
  /**
   * Checks if a model class conforms to the corresponding JVM class
   * @param className name of the class to check for
   * @return true for conformant (no specific converter is needed)
   *    false for non-conformant (specific converter needed)
   */
  public abstract boolean isConformant(String className);
}
