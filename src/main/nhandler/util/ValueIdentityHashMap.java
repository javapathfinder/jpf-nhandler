package nhandler.util;

import java.util.HashMap;

/**
 * This is a value identity HashMap. Its contiansValue method compares 
 * the identity of the given object with the identities of the ones in 
 * the map. 
 */
@SuppressWarnings("serial")
public class ValueIdentityHashMap<K, V> extends HashMap<K, V> {

  /**
   * Compares the identity of the given object with the identities of
   * the objects in this map.
   * 
   * @param value
   *          the object to be compared with the map content
   *
   * @return true if the map includes an object with the same identity 
   * as the given object  
   */
  public boolean containsValue (Object value){
    for (V v : this.values()){
      if (value == v){ 
        return true; 
      }
    }
    return false;
  }
}