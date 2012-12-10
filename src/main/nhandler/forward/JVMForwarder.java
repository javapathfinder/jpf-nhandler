package nhandler.forward;

import java.io.File;
import java.lang.reflect.Method;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.NativeMethodInfo;
import gov.nasa.jpf.vm.NativePeer;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.MethodSpec;

/**
 * This listener makes JPF delegate or skip the execution of certain methods
 * to the JVM level according to what is specified in the properties file.
 * 
 * Right after a class is loaded, JVMForwarder goes through the methods of 
 * the class, and for the ones specified to be delegate or skipped, it replaces
 * their MethodInfo with the subclasses of NativeMethodInfo, and that makes the
 * executeNative() to behave differently.
 * 
 * @author Nastaran Shafiei
 * @author Franck van Breugel
 */
public class JVMForwarder extends PropertyListenerAdapter {

  private static String[] delegate_spec = null;

  private static String[] skip_spec = null;

  private static String[] filter_spec = null;

  private static boolean delegateNatives = false;

  private static boolean skipNatives = false;

  private static boolean initialized = false;

  private void init (Config conf){
    if (!initialized){
      delegate_spec = conf.getStringArray("nhandler.spec.delegate");
      skip_spec = conf.getStringArray("nhandler.spec.skip");
      filter_spec = conf.getStringArray("nhandler.spec.filter");
      delegateNatives = conf.getBoolean("nhandler.delegateNative");
      skipNatives = conf.getBoolean("nhandler.skipNative");
      initialized = true;
    }
  }

  @Override
  public void classLoaded (VM vm, ClassInfo ci){
    init(vm.getConfig());

    processNatives(ci);
    processDelegated(ci);
    processSkipped(ci);
  }

  private void processNatives (ClassInfo ci){
    if (delegateNatives){
      delegateNatives(ci);
    } else if (skipNatives){
      skipNatives(ci);
    }
  }

  private void delegateNatives (ClassInfo ci){
    MethodInfo[] mth = ci.getDeclaredMethodInfos();
    for (MethodInfo mi : mth){
      if (mi.isNative() && !isHandled(mi) && isAllowed(mi) && !isFiltered(mi)){
        delegateUnhandledNative(mi);
      }
    }
  }

  private void skipNatives (ClassInfo ci){
    MethodInfo[] mth = ci.getDeclaredMethodInfos();
    for (MethodInfo mi : mth){
      if (mi.isNative() && !isHandled(mi) && isAllowed(mi) && !isFiltered(mi)){
        skipUnhandledNative(mi);
      }
    }
  }

  private boolean isHandled(MethodInfo mi) {
    NativeMethodInfo nmi = (NativeMethodInfo) mi;
    NativePeer nativePeer = nmi.getNativePeer();

    // check if there is any native peer class associated to the class of this
    // method at all
    if(nativePeer == null) {
      return false;
    }

    Method[] mth = nativePeer.getPeerClass().getMethods();
    for(Method m: mth) {
      if(m.getName().equals(nmi.getJNIName())) {
        return true;
      }
    }

    return false;
  }

  // We do not allow user to delegate or skip the methods of certain classes that are
  // subjected to jpf-nhandler limitations.
  String[] builtinFiltered = {"java.lang.ClassLoader.*"};
  
  private boolean isAllowed(MethodInfo mi){
    for(String spec : builtinFiltered){
      MethodSpec ms = MethodSpec.createMethodSpec(spec);
      if (ms.matches(mi)){ 
        return false; 
      }
    }

    return true;
  }

  private boolean isFiltered (MethodInfo mi){
    if (filter_spec != null){
      for (String spec : filter_spec){
        MethodSpec ms = MethodSpec.createMethodSpec(spec);
        if (ms.matches(mi)){ 
          return true; 
        }
      }
    }

    return false;
  }

  private void processDelegated (ClassInfo ci){
    if (delegate_spec != null){
      MethodInfo[] mth = ci.getDeclaredMethodInfos();
      for (MethodInfo mi : mth){
        for (String spec : delegate_spec){
          MethodSpec ms = MethodSpec.createMethodSpec(spec);
          if (!isFiltered(mi) && ms.matches(mi)){
            delegateMethod(mi);
          }
        }
      }
    }
  }

  private void processSkipped (ClassInfo ci){
    if (skip_spec != null){
      MethodInfo[] mth = ci.getDeclaredMethodInfos();
      for (MethodInfo mi : mth){
        for (String spec : skip_spec){
          MethodSpec ms = MethodSpec.createMethodSpec(spec);
          if (ms.matches(mi)){
            skipMethod(mi);
          }
        }
      }
    }
  }

  private void delegateUnhandledNative (MethodInfo mi){
    MethodInfo new_m = new DelegatedNativeMethodInfo(mi);
    ClassInfo ci = mi.getClassInfo();
    ci.putDeclaredMethod(new_m);
  }

  private void skipUnhandledNative (MethodInfo mi){
    MethodInfo new_m = new SkippedNativeMethodInfo(mi);
    ClassInfo ci = mi.getClassInfo();
    ci.putDeclaredMethod(new_m);
  }

  private void delegateMethod (MethodInfo mi){
    MethodInfo new_m = new DelegatedMethodInfo(mi);
    ClassInfo ci = mi.getClassInfo();
    ci.putDeclaredMethod(new_m);
  }

  private void skipMethod (MethodInfo mi){
    MethodInfo new_m = new SkippedMethodInfo(mi);
    ClassInfo ci = mi.getClassInfo();
    ci.putDeclaredMethod(new_m);
  }

  /**
   * at the searchStarted event, if the option nhandler.reset is set to true,
   * all the peer classes created on the fly are removed. 
   */
  @Override
  public void searchStarted(Search search){
    Config config = search.getConfig();
    boolean reset = config.getBoolean("nhandler.clean");
    if(reset) {
      String path = config.getPath("jpf-nhandler") + "/onthefly";
      File onthefly = new File(path);
      String[] peers = onthefly.list();

      for(String name: peers) {
    	if((reset && name.startsWith("OTF_JPF_") && (name.endsWith(".class") || name.endsWith(".java")))) {
          File peer = new File(onthefly, name);
          peer.delete();
    	}
      }
    }
  }
}
