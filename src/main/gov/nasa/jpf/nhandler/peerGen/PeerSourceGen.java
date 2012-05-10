package gov.nasa.jpf.nhandler.peerGen;

import gov.nasa.jpf.jvm.MethodInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.bcel.generic.Type;

public class PeerSourceGen {

  private File file;
  private String name;
  private String path;
  private StringBuilder content;
  
  protected static boolean createSource = false;

  protected PeerSourceGen(String name) throws IOException {
    this.name = name;
    this.path = PeerClassGen.peersLocation + name + ".java";
    System.out.println("path: " + path);
    this.file = new File(this.path);
    
    setContent();
  }

  protected File setContent() throws IOException {
    content = new StringBuilder();

    if(!this.file.exists()) {
      this.file.createNewFile();
      generateMinimalPeerSource();
    } else {
      loadContent();
    }
    
    return this.file;
  }

  private void generateMinimalPeerSource() throws FileNotFoundException {
    printImport();
    printClassHeader();
    printDefaultConstructor();
    printClassFooter();
  }

  private void printImport() {
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

  private void printClassHeader() {
    append("public class " + this.name);
    append(" {");
    addBlankLine();
  }

  private void printDefaultConstructor() {
    append("  public " + this.name + "()");
    append(" {");
    gotoNextLine();
    append("  }");
    addBlankLine();
  }

  protected void printClassFooter() {
    append("}");
  }

  private void append(String s) {
    content.append(s);
  }

  private void loadContent() {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader (path));
    } catch (FileNotFoundException e1) {
      e1.printStackTrace();
    }

    String line  = null;
    String ls = System.getProperty("line.separator");

    try {
      while( ( line = reader.readLine() ) != null ) {
        content.append( line );
        content.append( ls );
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  protected void removeClassFooter() {
    content.deleteCharAt(content.lastIndexOf("}"));
  }

  private void addDoubleIndent() {
    append("    ");
  }
    
  private void gotoNextLine() {
    append("\n");
  }

  private void addBlankLine() {
    append("\n\n");
  }

  protected class MethodGen {
	private MethodInfo mi;

	protected MethodGen(MethodInfo mi) {
      this.mi = mi;
	}

    protected void printMethodHeader(Type returnType, String name, Type[] argsType) {
      removeClassFooter();

      append("  public static");
      append(" " + ((returnType.equals(Type.OBJECT)) ? "int" : returnType.toString()));
      append(" " + name);
      
      if(mi.isStatic()) {
        append(" (MJIEnv env, int rcls");
      } else {
        append(" (MJIEnv env, int robj");
      }

      for(int i=2; i<argsType.length; i++) {
        append(", " + argsType[i].toString());
        append(" arg" + (i-2));
      }

      append(")");
    }

    protected void printThrowsExceptions() {
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

    protected void completeHeader() {
      append(" {");
      gotoNextLine();
    }

    protected void printConvertorPart() {
      gotoNextLine();
      addDoubleIndent();
      append("// Creates the engine for converting objects/classes between JPF & JVM");
      gotoNextLine();
      
      addDoubleIndent();
      append("Converter converter = new Converter(env);");
      addBlankLine();
    }

    protected void printCallerPart() {
      addDoubleIndent();

      if(mi.isStatic()) {
        append("// Captures the class that invokes the static method to be handled in JVM");
        gotoNextLine();
        
        addDoubleIndent();
        append("Class caller = converter.getJVMCls(rcls);");
      } else {
        append("// Captures the object that invokes the method to be handled in JVM");
        gotoNextLine();
        
        addDoubleIndent();
        append("Object caller = converter.getJVMObj(robj);");
      }

      addBlankLine();
    }

    
    protected void printCreateArgsVal(int nArgs) {
      addDoubleIndent();
      append("// Captures the input parameters of the method to be handled in JVM");
      gotoNextLine();

      addDoubleIndent();
      append("Object argValue[] = new Object[" + nArgs + "];");
      gotoNextLine();
    }

    protected void printSetObjArgVal(int index) {
      addDoubleIndent();
      append("argValue[" + index + "] = converter.getJVMObj(arg" + index + ");");
      gotoNextLine();
    }

    protected void printSetPrimitiveArgVal(String wrapper, int index) {
        addDoubleIndent();
        append("argValue[" + index + "] = new " + wrapper + "(arg" + index + ");");
        gotoNextLine();
    }

    protected void printCreateArgsType(int nArgs) {
      gotoNextLine();

      addDoubleIndent();
      append("// Captures the input parameters types of the method to be hanlded in JVM");
      gotoNextLine();

      addDoubleIndent();
      append("Class argType[] = new Class[" + nArgs + "];");
      gotoNextLine();
    }

    protected void printSetObjArgType(String type, int index) {
      addDoubleIndent();
      append("argType[" + index + "] = Class.forName(\"" + type + "\");");
      gotoNextLine();
    }

    protected void printSetArrArgType(int index) {
      addDoubleIndent();
      append("argType[" + index + "] = argValue[" + index + "].getClass();");
      gotoNextLine();
    }    

    protected void printSetPrimitiveArgType(String wrapper, int index) { 
      addDoubleIndent();
      append("argType[" + index + "] = " + wrapper + ".TYPE;");
      gotoNextLine();
    }

    protected void printGetCallerClass() {
      gotoNextLine();

      addDoubleIndent();
      append("// Obtains the class of the object that invokes the method to be handled");
      gotoNextLine();

      addDoubleIndent();
      append("Class callerClass = caller.getClass();");
      gotoNextLine();
    }

    protected void printGetMethod(String mname, boolean isStatic) {
      gotoNextLine();

      addDoubleIndent();
      append("// Obtains the Method instance of the method to be handled");
      gotoNextLine();

      addDoubleIndent();
      if(isStatic) {
        append("Method method = caller.getDeclaredMethod(\"" + mname + "\", argType);");
      } else {
        append("Method method = callerClass.getDeclaredMethod(\"" + mname + "\", argType);");
      }
      addBlankLine();
    }

    protected void printSetAccessible() {
      addDoubleIndent();
      append("// Makes the method with any access modifier accessible");
      gotoNextLine();
      
      addDoubleIndent();
      append("method.setAccessible(true);");
      addBlankLine();
    }

    protected void printInvokeMethod(boolean isStatic) {
      addDoubleIndent();
      append("// Invokes the method to be handled in JVM");
      gotoNextLine();

      addDoubleIndent();
      if(isStatic) {
        append("Object returnValue = method.invoke(null, argValue);");
      } else {
        append("Object returnValue = method.invoke(caller, argValue);");
      }
      addBlankLine();
    }

    protected void printConvertReturnValue() {
      addDoubleIndent();
      append("// Converts the return value from JVM to JPF");
      gotoNextLine();

      addDoubleIndent();
      append("int JPFObj = converter.getJPFObj(returnValue);");
      addBlankLine();
    }

    protected void printUpdateCaller(boolean isStatic) {
      addDoubleIndent();
      if(isStatic) {
        append("// Updates the class that invokes the method to be handle in JPF");
        gotoNextLine();

        addDoubleIndent();
        append("converter.getJPFCls(caller);");
      } else {
        append("// Updates the object that invokes the method to be handle in JPF");
        gotoNextLine();

        addDoubleIndent();
        append("converter.updateJPFObj(caller, robj);");
      }
      addBlankLine();

      addUpdateArgsComment = true;
    }

    private boolean addUpdateArgsComment = false;
    protected void printUpdateJPFArgs(int index) {
      if(addUpdateArgsComment) {
        addDoubleIndent();
        append("// Updates the input parameters objects of the method to be handled");
        gotoNextLine();
        addUpdateArgsComment = false;
      }
 
      addDoubleIndent();
      append("converter.updateJPFObj(argValue[" + index + "], arg" + index + ");");
      addBlankLine();
    }

    protected void printReturn() {
      addDoubleIndent();
      append("return;");
      gotoNextLine();
    }

    protected void printReturnObj() {
      addDoubleIndent();
      append("// Returns the return value that is converted to a JPF object");
      gotoNextLine();

      addDoubleIndent();
      append("return JPFObj;");
      gotoNextLine();
    }

    protected void printReturnPrimitive(String wrapper, String methodName) {
      addDoubleIndent();
      append("return ((" + wrapper + ")returnValue)." + methodName + "();");
      gotoNextLine();
    }

    protected void printDummyReturnStatement() {
      String returnType = this.mi.getReturnTypeName();
      addDoubleIndent();

      if (!PeerMethodGen.isPrimitiveType(returnType)) {
        append("return -1;");
      } else if ("void".equals(returnType)) {
        append("return;");
      } else {
        if ("boolean".equals(returnType)) {
          append("return false;");
        } else if ("int".equals(returnType)) {
          append("return 0;");
        } else if ("long".equals(returnType)) {
          append("return 0;");
        } else if ("byte".equals(returnType)) {
          append("return 0;");
        } else if ("char".equals(returnType)) {
          append("return 0;");
        } else if ("short".equals(returnType)) {
          append("return 0;");
        } else if ("float".equals(returnType)) {
          append("return 0.0;");
        } else if ("double".equals(returnType)) {
          append("return 0.0;");
        }
      }

      gotoNextLine();
    }

    protected void wrapUpSource() {
      printFooter();
      writeToFile();
    }

    private void printFooter() {
      append("  }");
      gotoNextLine();
      append("}");
      gotoNextLine();
    }

    private void writeToFile() {
      PrintWriter pw = null;

	  try {
		pw = new PrintWriter(file);
	  } catch (FileNotFoundException e) {
		e.printStackTrace();
	  }

	  pw.append(content);
      pw.flush();
    }
  }
}
