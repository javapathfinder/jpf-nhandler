package gov.nasa.jpf.nhandler;

import java.util.HashMap;

/**
 * <2do> - need to be replaced
 */
@SuppressWarnings("serial")
public class ValueIdentityHashMap<K, V> extends HashMap<K, V> {
  public boolean containsValue (Object value){
    for (V v : this.values()){
      if (value == v){ 
        return true; 
      }
    }
    return false;
  }
}