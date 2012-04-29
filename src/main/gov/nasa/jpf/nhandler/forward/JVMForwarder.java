package gov.nasa.jpf.nhandler.forward;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.jvm.ClassInfo;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.MethodInfo;
import gov.nasa.jpf.util.MethodSpec;

public class JVMForwarder extends PropertyListenerAdapter {
  private static String[] delegate_spec = null;

  private static String[] skip_spec = null;

  private static String[] filter_spec = null;

  private static boolean delegateNatives = false;

  private static boolean skipNatives = false;

  private static boolean initialized = false;

  private void init (Config conf){
    if (!initialized){
      delegate_spec = conf.getStringArray("nhandler.delegate.spec");
      skip_spec = conf.getStringArray("nhandler.skip.spec");
      filter_spec = conf.getStringArray("nhandler.filter.spec");
      delegateNatives = conf.getBoolean("nhandler.delegate.natives");
      skipNatives = conf.getBoolean("nhandler.skip.natives");
      initialized = true;
    }
  }

  public void classLoaded (JVM vm){
    init(vm.getConfig());
    ClassInfo ci = vm.getLastClassInfo();

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
      if (mi.isNative()){
        delegateUnhandledNative(mi);
      }
    }
  }

  private void skipNatives (ClassInfo ci){
    MethodInfo[] mth = ci.getDeclaredMethodInfos();
    for (MethodInfo mi : mth){
      if (mi.isNative()){
        skipUnhandledNative(mi);
      }
    }
  }

  private boolean isFiltered (MethodInfo mi){
    if (filter_spec != null){
      for (String spec : filter_spec){
        MethodSpec ms = MethodSpec.createMethodSpec(spec);
        if (ms.matches(mi)){ return true; }
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
}
