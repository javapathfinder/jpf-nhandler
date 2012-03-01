package gov.nasa.jpf.jvm;

import java.util.HashMap;

/**
 * I have to replace that actual customized hashMap!
 */
public class CutomizedHashMap<K, V> extends HashMap<K, V> {
  public boolean containsValue (Object value) {
    for (V v : this.values())
      if (value == v) return true;
    return false;
  }
}
