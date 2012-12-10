package nhandler.forward;

import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.NativeMethodInfo;

public abstract class HandledMethodInfo extends NativeMethodInfo {

  public HandledMethodInfo (MethodInfo mi) {
    super(mi, null, null);
  }

  @Override
  protected abstract boolean isUnsatisfiedLinkError (MJIEnv env);

  protected abstract String printInfo();
}
