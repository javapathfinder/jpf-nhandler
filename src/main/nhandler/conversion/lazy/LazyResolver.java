package nhandler.conversion.lazy;

import gov.nasa.jpf.jvm.ClassFile;
import gov.nasa.jpf.jvm.ClassFileParser;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ClassParseException;
import gov.nasa.jpf.vm.MJIEnv;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class LazyResolver {
  public static void resolve(Class<?> clazz, Method method) {
    System.out.println("**Lazy Resolver**");
    System.out.println(clazz.getName());
    System.out.println(method.getName());
    
    
    URL classFileUrl = clazz.getResource(clazz.getSimpleName() + ".class");
    URI classFileUri = null;
    try {
      classFileUri = classFileUrl.toURI();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    ClassFile classFile = null;
    try {
      classFile = new ClassFile(new File(classFileUri));
    } catch (ClassParseException e) {
      e.printStackTrace();
    }
    
    ClassFileParser parser = new ClassFileParser(classFile);
    ClassInfo ci = null;
    try {
      ci = new ClassInfo(clazz.getName(), null, parser, classFileUrl.toString());
    } catch (ClassParseException e) {
      e.printStackTrace();
    }
    
    
  }
  
  public static void resolve(Class<?> clazz, Constructor<?> constructor) {
    System.out.println("**Lazy Resolver**");
    System.out.println(clazz.getName());
    System.out.println(constructor.getName());
  }
  
  public static void main (String[] args) {
    
    try {
      resolve(LazyResolver.class, LazyResolver.class.getDeclaredMethod("resolve", new Class<?>[] { Class.class, Method.class }));
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }
  }
}