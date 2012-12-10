package nhandler.forward;

import nhandler.peerGen.PeerClassGen;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.NativePeer;

/** 
 * This is a subclass of NativeMethodInfo which makes executeNative() delegate
 * the execution of the method to the JVM level.
 * 
 * @author Nastaran Shafiei
 * @author Franck van Breugel
 */
public class DelegatedMethodInfo extends HandledMethodInfo {

  public DelegatedMethodInfo (MethodInfo mi) {
    super(mi);
  }

  @Override
  protected boolean isUnsatisfiedLinkError (MJIEnv env){
    if(mth == null){
      System.out.println(printInfo());

      PeerClassGen peerCreator = PeerClassGen.getPeerCreator(this.getClassInfo(), env);
      mth = peerCreator.createMethod(this);

      Class<?> peerClass = peerCreator.getPeer();
      this.peer = NativePeer.getInstance(peerClass, NativePeer.class);
      this.peer.initialize(peerClass, this.ci, true);

      //this.peer = new NativePeer(peerCreator.getPeer(), this.ci);
      assert (this.peer != null && mth != null);
    }

    return false;
  }

  @Override
  protected String printInfo() {
    return("* DELEGATING -> " + this.ci.getName() + "." + this.name);
  }
}
