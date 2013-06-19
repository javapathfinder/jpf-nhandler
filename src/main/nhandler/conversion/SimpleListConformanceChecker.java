package nhandler.conversion;

/**
 * @author chinmay
 * Simply checks whether the class is in a list and reports
 * accordingly about conformance. Will be replaced later by
 * a class which uses jpf-conformance-checker
 */
public class SimpleListConformanceChecker implements ConformanceChecker {

  public static final String[] nonConformantClasses = { "java.util.Random" };
  
  private static ConformanceChecker instance;

  @Override
  public boolean isConformant (String className) {
    for (String i : nonConformantClasses)
      if (i.equals(className)) return false;
    return true;
  }
  
  public static ConformanceChecker getInstance()
  {
    if(instance == null)    //Don't need synchronization here since JPF is serial (?)
      instance = new SimpleListConformanceChecker();
    return instance;
  }
}