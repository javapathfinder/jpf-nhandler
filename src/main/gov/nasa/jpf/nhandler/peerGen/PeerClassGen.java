package gov.nasa.jpf.nhandler.peerGen;

import gov.nasa.jpf.jvm.ClassInfo;
import gov.nasa.jpf.jvm.MJIEnv;
import gov.nasa.jpf.jvm.NativeMethodInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

import org.apache.bcel.Constants;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.util.BCELifier;

/**
 * Creates native peers classes on-the-fly using the Byte Code Engineering
 * Library (BCEL)
 * 
 * @author Nastaran Shafiei
 * @author Franck van Breugel
 */
public class PeerClassGen implements Constants {

  /**
   * Keeps the list of all PeerClassCreator objects to avoid recreating them
   */
  private static HashMap<String, PeerClassGen> Peers = new HashMap<String, PeerClassGen>();

  protected InstructionFactory _factory;

  protected ConstantPoolGen _cp;

  protected ClassGen _cg;

  /**
   * Directory that is used to keep native peers that are created on-the-fly
   */
  protected static String peersLocation = "/Users/Nastaran/workspaces/bitbucket/jpf-nhandler/onthefly/";

  /**
   * To distinguish the on-the-fly native peers from the rest, this prefixed is
   * added to the name of these classes.
   */
  private static final String prefix = "OTF_";

  /**
   * Stores the native peer class.
   */
  private Class<?> peer;

  /**
   * The complete path of the class.
   */
  private String path;

  private MJIEnv env;

  /**
   * Creates a new instance of PeerClassCreator.
   * 
   * @param ci
   *          a class that its native peer is going to be created
   */
  private PeerClassGen (ClassInfo ci, MJIEnv env) {
    String className = ci.getName();
    this.env = env;
    String peerName = PeerClassGen.getNativePeerClsName(className);
    this.path = PeerClassGen.peersLocation + peerName + ".class";

    try{
      this.peer = this.loadClass(peerName);
      _cg = new ClassGen(Repository.lookupClass(this.loadClass(peerName)));
      System.out.println("   Already has a OTF peer class!");
    } catch (ClassNotFoundException e){
      // do nothing!
    }

    if (this.peer == null){
      _cg = new ClassGen(peerName, "java.lang.Object", peerName + ".class", Constants.ACC_PUBLIC, new String[] {});
      _cg.addEmptyConstructor(Constants.ACC_PUBLIC);
    }

    _cp = _cg.getConstantPool();
    _factory = new InstructionFactory(_cg, _cp);

    // if (!NativePeer.peers.containsKey(className))
    // _cg.addEmptyConstructor(Constants.ACC_PUBLIC);

    PeerClassGen.Peers.put(className, this);
  }

  /**
   * Returns a PeerClassCreator object corresponding to the given class. If the
   * PeerClassCreator object has been already created, it is returned. OW a new
   * one is created.
   * 
   * @param ci
   *          a JPF class
   * 
   * @return a PeerClassCreator object corresponding to the given class
   */
  public static PeerClassGen getPeerCreator (ClassInfo ci, MJIEnv env){
    String className = ci.getName();
    PeerClassGen peerCreator = null;

    // find a better place to initialize this!
    if (PeerClassGen.peersLocation == null) {
      PeerClassGen.peersLocation = env.getConfig().getPath("jpf-nhandler") + "/onthefly/";
    }

    if (PeerClassGen.Peers.containsKey(className)){
      peerCreator = PeerClassGen.Peers.get(className);
      System.out.println("   Already has a PeerClassCreator!");
    } else{
      System.out.println("   Does not have a PeerClassCreator!");
      peerCreator = new PeerClassGen(ci, env);
    }
    return peerCreator;
  }

  /**
   * Returns a method of the peer class, if any, that corresponds to the given
   * NativeMethodInfo object. Null will be returned if such a method does not
   * exist.
   * 
   * @param mi
   *          an object that represents a native method in JPF
   * 
   * @return a method of the peer class that corresponds to the given
   *         NativeMethodInfo object
   */
  private Method getExistingMethod (NativeMethodInfo mi){
    if (this.peer != null){
      for (Method nm : this.peer.getMethods()){
        if (nm.getName().equals(mi.getJNIName())) {
          return nm;
        }
      }
    }
    return null;
  }

  /**
   * Creates a Method object corresponding to the given NativeMethodInfo object
   * within the native peer class.
   * 
   * @param mi
   *          an object that represents a native method in JPF
   * 
   * @param env
   *          an interface between JPF and the underlying JVM
   * 
   * @return a Method object corresponding to the given NativeMethodInfo object
   */
  public Method createMethod (NativeMethodInfo mi){
    Method method = this.getExistingMethod(mi);
    if (method != null) {
      return method;
    }

    PeerMethodGen nmthCreator = new PeerMethodGen(mi, env, this);
    nmthCreator.create();

    OutputStream out;
    try{
      out = new FileOutputStream(this.path);
      this._cg.getJavaClass().dump(out);
      out.close();
    } catch (FileNotFoundException e){
      e.printStackTrace();
    } catch (IOException e){
      e.printStackTrace();
    }

    Class<?> peerClass = null;
    try{
      peerClass = this.loadClass(this._cg.getClassName());
    } catch (ClassNotFoundException e1){
      e1.printStackTrace();
    }
    this.peer = peerClass;
    method = this.getExistingMethod(mi);

    return method;
  }

  /**
   * Creates a Method object with empty body corresponding to the given 
   * NativeMethodInfo object within the native peer class.
   * 
   * @param mi
   *          an object that represents a native method in JPF
   * 
   * @param env
   *          an interface between JPF and the underlying JVM
   * 
   * @return a Method object corresponding to the given NativeMethodInfo object
   */
  public Method createEmptyMethod (NativeMethodInfo mi){
    Method method = this.getExistingMethod(mi);
    if (method != null) {
      return method;
    }

    PeerMethodGen nmthCreator = new PeerMethodGen(mi, env, this);
    nmthCreator.createEmpty();

    OutputStream out;
    try{
      out = new FileOutputStream(this.path);
      this._cg.getJavaClass().dump(out);
      out.close();
    } catch (FileNotFoundException e){
      e.printStackTrace();
    } catch (IOException e){
      e.printStackTrace();
    }

    Class<?> peerClass = null;
    try{
      peerClass = this.loadClass(this._cg.getClassName());
    } catch (ClassNotFoundException e1){
      e1.printStackTrace();
    }
    this.peer = peerClass;
    method = this.getExistingMethod(mi);

    return method;
  }

  /**
   * Loads an on-the-fly native peer class with the given name.
   * 
   * @param className
   *          name of a class to be loaded
   * @return the on-the-fly native peer class with the given name
   * 
   * @throws ClassNotFoundException
   *           when no definition for the class with the given name could be
   *           found
   */
  private Class loadClass (String className) throws ClassNotFoundException{
    Class cls = null;
    URL[] urls = null;

    File otf_dir = new File(PeerClassGen.peersLocation);

    URL url = null;
    URL jpf_url = null;
    try{
      url = otf_dir.toURL();
    } catch (MalformedURLException e){
      e.printStackTrace();
    }
    urls = new URL[] { url };

    URLClassLoader cl = new URLClassLoader(urls, env.getConfig().getClassLoader());
    cls = cl.loadClass(className);
    return cls;
  }

  /**
   * Creates a name for on-the-fly native peers which is prefix "OTF_" followed
   * by the name of the regular native peer class
   * 
   * @param className
   *          a name of the class used to create the on-the-fly native peer name
   * 
   * @return a name for the on-the-fly native peer
   */
  protected static String getNativePeerClsName (String className){
    return (PeerClassGen.prefix + "JPF_" + className.replace('.', '_'));
  }

  /**
   * Returns the native peer class.
   * 
   * @return the native peer class
   */
  public Class<?> getPeer (){
    return this.peer;
  }

  public static void main (String[] args) throws SecurityException, NoSuchMethodException, IllegalAccessException, InvocationTargetException{
    JavaClass clazz = null;

    try{
      clazz = Repository.lookupClass("gov.nasa.jpf.nhandler.Test");
    } catch (ClassNotFoundException e){
      e.printStackTrace();
    }

    BCELifier test = new BCELifier(clazz, System.out);

    test.start();
  }
}
