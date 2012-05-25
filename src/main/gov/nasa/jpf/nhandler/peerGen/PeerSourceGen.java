package gov.nasa.jpf.nhandler.peerGen;

import gov.nasa.jpf.jvm.MethodInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.bcel.generic.Type;

/**
 * Creates the source code for the clases that are created on the fly. To
 * make this feature work, the property nhandler.genSource has to be set 
 * to true.
 * 
 * @author Nastaran Shafiei
 * @author Franck van Breugel
 */
public class PeerSourceGen {

  private File file;

  private String name;

  private String path;

  private StringBuilder content;

  protected static boolean addComment = false;

  protected static boolean genSource = false;

  protected PeerSourceGen (String name) throws IOException {
    this.name = name;
    this.path = PeerClassGen.peersLocation + name + ".java";
    this.file = new File(this.path);

    setContent();
  }

  protected File setContent () throws IOException{
    content = new StringBuilder();

    if (!this.file.exists()){
      this.file.createNewFile();
      generateMinimalPeerSource();
    } else{
      loadContent();
    }

    return this.file;
  }

  private void generateMinimalPeerSource () throws FileNotFoundException{
    printImport();
    printClassHeader();
    printDefaultConstructor();
    printClassFooter();
  }

  private void printImport (){
    append("import gov.nasa.jpf.jvm.MJIEnv;");
    gotoNextLine();
    append("import gov.nasa.jpf.nhandler.conversion.ConversionException;");
    gotoNextLine();
    append("import gov.nasa.jpf.nhandler.conversion.Converter;");
    gotoNextLine();
    append("import java.lang.reflect.InvocationTargetException;");
    gotoNextLine();
    append("import java.lang.reflect.Method;");
    addBlankLine();
  }

  private void printClassHeader (){
    append("public class " + this.name);
    append(" {");
    addBlankLine();
  }

  private void printDefaultConstructor (){
    append("  public " + this.name + "()");
    append(" {");
    gotoNextLine();
    append("  }");
    addBlankLine();
  }

  protected void printClassFooter (){
    append("}");
  }

  private void append (String s){
    content.append(s);
  }

  private void loadContent (){
    BufferedReader reader = null;
    try{
      reader = new BufferedReader(new FileReader(path));
    } catch (FileNotFoundException e1){
      e1.printStackTrace();
    }

    String line = null;
    String ls = System.getProperty("line.separator");

    try{
      while ((line = reader.readLine()) != null){
        content.append(line);
        content.append(ls);
      }
    } catch (IOException e){
      e.printStackTrace();
    }
  }

  protected void removeClassFooter (){
    content.deleteCharAt(content.lastIndexOf("}"));
  }

  private void addDoubleIndent (){
    append("    ");
  }

  private void gotoNextLine (){
    append("\n");
  }

  private void addBlankLine (){
    append("\n\n");
  }

  private void addComment (String comment){
    if(addComment) {
      addDoubleIndent();
      append("// " + comment);
      gotoNextLine();
    }
  }

  protected class MethodGen {
    private MethodInfo mi;

    protected MethodGen (MethodInfo mi) {
      this.mi = mi;
    }

    protected void printMethodHeader (Type returnType, String name, Type[] argsType){
      removeClassFooter();

      append("  public static");
      append(" " + ((returnType.equals(Type.OBJECT)) ? "int" : returnType.toString()));
      append(" " + name);

      if (mi.isStatic()){
        append(" (MJIEnv env, int rcls");
      } else{
        append(" (MJIEnv env, int robj");
      }

      for (int i = 2; i < argsType.length; i++){
        append(", " + argsType[i].toString());
        append(" arg" + (i - 2));
      }

      append(")");
    }

    protected void printThrowsExceptions (){
      gotoNextLine();
      addDoubleIndent();
      addDoubleIndent();
      append("throws java.lang.IllegalArgumentException,");
      gotoNextLine();
      addDoubleIndent();
      addDoubleIndent();
      addDoubleIndent();
      append("SecurityException, NoSuchMethodException, IllegalAccessException,");
      gotoNextLine();
      addDoubleIndent();
      addDoubleIndent();
      addDoubleIndent();
      addDoubleIndent();
      append(" ClassNotFoundException, ConversionException, InvocationTargetException");
      completeHeader();
    }

    protected void completeHeader (){
      append(" {");
      gotoNextLine();
    }

    protected void printConvertorPart (){
      gotoNextLine();
      addComment("Creates the engine for converting objects/classes between JPF & JVM");

      addDoubleIndent();
      append("Converter converter = new Converter(env);");
      addBlankLine();
    }

    protected void printCallerPart (){
      if (mi.isStatic()){
        addComment("Captures the class that invokes the static method to be handled in JVM");

        addDoubleIndent();
        append("Class<?> caller = converter.getJVMCls(rcls);");
      } else{
        addComment("Captures the object that invokes the method to be handled in JVM");

        addDoubleIndent();
        append("Object caller = converter.getJVMObj(robj);");
      }

      addBlankLine();
    }

    protected void printCreateArgsVal (int nArgs){
      addComment("Captures the input parameters of the method to be handled in JVM");

      addDoubleIndent();
      append("Object argValue[] = new Object[" + nArgs + "];");
      gotoNextLine();
    }

    protected void printSetObjArgVal (int index){
      addDoubleIndent();
      append("argValue[" + index + "] = converter.getJVMObj(arg" + index + ");");
      gotoNextLine();
    }

    protected void printSetPrimitiveArgVal (String wrapper, int index){
      addDoubleIndent();
      append("argValue[" + index + "] = new " + wrapper + "(arg" + index + ");");
      gotoNextLine();
    }

    protected void printCreateArgsType (int nArgs){
      gotoNextLine();

      addComment("Captures the input parameters types of the method to be hanlded in JVM");

      addDoubleIndent();
      append("Class<?> argType[] = new Class[" + nArgs + "];");
      gotoNextLine();
    }

    protected void printSetObjArgType (String type, int index){
      addDoubleIndent();
      append("argType[" + index + "] = Class.forName(\"" + type + "\");");
      gotoNextLine();
    }

    protected void printSetArrArgType (int index){
      addDoubleIndent();
      append("argType[" + index + "] = argValue[" + index + "].getClass();");
      gotoNextLine();
    }

    protected void printSetPrimitiveArgType (String wrapper, int index){
      addDoubleIndent();
      append("argType[" + index + "] = " + wrapper + ".TYPE;");
      gotoNextLine();
    }

    protected void printGetCallerClass (){
      gotoNextLine();

      addComment("Obtains the class of the object that invokes the method to be handled");

      addDoubleIndent();
      append("Class<?> callerClass = caller.getClass();");
      gotoNextLine();
    }

    protected void printGetMethod (String mname, boolean isStatic){
      gotoNextLine();

      addComment("Obtains the Method instance of the method to be handled");

      addDoubleIndent();
      if (isStatic){
        append("Method method = caller.getDeclaredMethod(\"" + mname + "\", argType);");
      } else{
        append("Method method = callerClass.getDeclaredMethod(\"" + mname + "\", argType);");
      }
      addBlankLine();
    }

    protected void printSetAccessible (){
      addComment("Makes the method with any access modifier accessible");

      addDoubleIndent();
      append("method.setAccessible(true);");
      addBlankLine();
    }

    protected void printInvokeMethod (boolean isStatic){
      addComment("Invokes the method to be handled in JVM");

      addDoubleIndent();
      if (isStatic){
        append("Object returnValue = method.invoke(null, argValue);");
      } else{
        append("Object returnValue = method.invoke(caller, argValue);");
      }
      addBlankLine();
    }

    protected void printConvertReturnValue (){
      addComment("Converts the return value from JVM to JPF");

      addDoubleIndent();
      append("int JPFObj = converter.getJPFObj(returnValue);");
      addBlankLine();
    }

    protected void printUpdateCaller (boolean isStatic){
      if (isStatic){
        addComment("Updates the class that invokes the method to be handle in JPF");

        addDoubleIndent();
        append("converter.getJPFCls(caller);");
      } else{
        addComment("Updates the object that invokes the method to be handle in JPF");

        addDoubleIndent();
        append("converter.updateJPFObj(caller, robj);");
      }
      addBlankLine();

      addUpdateArgsComment = true;
    }

    private boolean addUpdateArgsComment = false;

    protected void printUpdateJPFArgs (int index, int nArgs){
      if (addUpdateArgsComment){
        addComment("Updates the input parameters objects of the method to be handled");
        addUpdateArgsComment = false;
      }

      addDoubleIndent();
      append("converter.updateJPFObj(argValue[" + index + "], arg" + index + ");");
      if(index == nArgs-1) {
        addBlankLine();
      } else {
        gotoNextLine();
      }
    }

    protected void printReturn (){
      addDoubleIndent();
      append("return;");
      gotoNextLine();
    }

    protected void printReturnObj (){
      addComment("Returns the return value that is converted to a JPF object");

      addDoubleIndent();
      append("return JPFObj;");
      gotoNextLine();
    }

    protected void printReturnPrimitive (String wrapper, String methodName){
      addDoubleIndent();
      append("return ((" + wrapper + ")returnValue)." + methodName + "();");
      gotoNextLine();
    }

    protected void printDummyReturnStatement (){
      String returnType = this.mi.getReturnTypeName();
      addDoubleIndent();

      if (!PeerMethodGen.isPrimitiveType(returnType)){
        append("return -1;");
      } else if ("void".equals(returnType)){
        append("return;");
      } else{
        if ("boolean".equals(returnType)){
          append("return false;");
        } else if ("int".equals(returnType)){
          append("return 0;");
        } else if ("long".equals(returnType)){
          append("return 0;");
        } else if ("byte".equals(returnType)){
          append("return 0;");
        } else if ("char".equals(returnType)){
          append("return 0;");
        } else if ("short".equals(returnType)){
          append("return 0;");
        } else if ("float".equals(returnType)){
          append("return 0.0;");
        } else if ("double".equals(returnType)){
          append("return 0.0;");
        }
      }

      gotoNextLine();
    }

    protected void wrapUpSource (){
      printFooter();
      writeToFile();
    }

    private void printFooter (){
      append("  }");
      gotoNextLine();
      append("}");
      gotoNextLine();
    }

    private void writeToFile (){
      PrintWriter pw = null;

      try{
        pw = new PrintWriter(file);
      } catch (FileNotFoundException e){
        e.printStackTrace();
      }

      pw.append(content);
      pw.flush();
    }
  }
}
