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
    append("import gov.nasa.jpf.jvm.MJIEnv;\n");
    append("import gov.nasa.jpf.nhandler.conversion.ConversionException;\n");
    append("import gov.nasa.jpf.nhandler.conversion.Converter;\n");
    append("import java.lang.reflect.Method;");
    nextLine();
    nextLine();
  }

  private void printClassHeader() {
    append("public class " + this.name);
    append(" {");
    nextLine();
    nextLine();
  }

  private void printDefaultConstructor() {
    append("  public " + this.name + "()");
    append(" {");
    nextLine();
    append("  }");
    nextLine();
    nextLine();
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
    
  private void nextLine() {
    append("\n");
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
      //nextLine();
      //addDoubleIndent();
      // append("throws IllegalArgumentException, SecurityException, NoSuchMethodException, ");
      // nextLine();
      // addDoubleIndent();
      // append("IllegalAccessException, ClassNotFoundException, ConversionException");
      append(" throws Exception");
      completeHeader();
    }

    protected void completeHeader() {
      append(" {");
      nextLine();
    }

    protected void printConvertorPart() {
      addDoubleIndent();
      append("Converter converter = new Converter(env);");
      nextLine();
    }

    protected void printCallerPart() {
      addDoubleIndent();

      if(mi.isStatic()) {
        append("Class caller = converter.getJVMCls(rcls);");
      } else {
        append("Object caller = converter.getJVMObj(robj);");
      }

      nextLine();
    }

    protected void printCreateArgsVal(int nArgs) {
      addDoubleIndent();
      append("Object argValue[] = new Object[" + nArgs + "];");
      nextLine();
    }

    protected void printSetObjArgVal(int index) {
      addDoubleIndent();
      append("argValue[" + index + "] = converter.getJVMObj(arg" + index + ");");
      nextLine();
    }

    protected void printSetPrimitiveArgVal(String wrapper, int index) {
        addDoubleIndent();
        append("argValue[" + index + "] = new " + wrapper + "(arg" + index + ");");
        nextLine();
    }

    protected void printCreateArgsType(int nArgs) {
      addDoubleIndent();
      append("Class argType[] = new Class[" + nArgs + "];");
      nextLine();
    }

    protected void printSetObjArgType(String type, int index) {
      addDoubleIndent();
      append("argType[" + index + "] = Class.forName(\"" + type + "\");");
      nextLine();
    }

    protected void printSetArrArgType(int index) {
      addDoubleIndent();
      append("argType[" + index + "] = argValue[" + index + "].getClass();");
      nextLine();
    }    

    protected void printSetPrimitiveArgType(String wrapper, int index) { 
      addDoubleIndent();
      append("argType[" + index + "] = " + wrapper + ".TYPE;");
      nextLine();
    }

    protected void printGetCallerClass() {
      addDoubleIndent();
      append("Class callerClass = caller.getClass();");
      nextLine();
    }

    protected void printGetMethod(String mname, boolean isStatic) {
      addDoubleIndent();
      if(isStatic) {
        append("Method method = caller.getDeclaredMethod(\"" + mname + "\", argType);");
      } else {
        append("Method method = callerClass.getDeclaredMethod(\"" + mname + "\", argType);");
      }
      nextLine();
    }

    protected void printSetAccessible() {
      addDoubleIndent();
      append("method.setAccessible(true);");
      nextLine();
    }

    protected void printInvokeMethod(boolean isStatic) {
      addDoubleIndent();
      if(isStatic) {
        append("Object returnValue = method.invoke(null, argValue);");
      } else {
        append("Object returnValue = method.invoke(caller, argValue);");
      }
      nextLine();
    }

    protected void printConvertJVM2JPF() {
      addDoubleIndent();
      append("int JPFObj = converter.getJPFObj(returnValue);");
      nextLine();
    }

    protected void printUpdateCaller(boolean isStatic) {
      addDoubleIndent();
      if(isStatic) {
        append("converter.getJPFCls(caller);");
      } else {
        append("converter.updateJPFObj(caller, robj);");
      }
      nextLine();
    }

    protected void printUpdateJPFArgs(int index) {
      addDoubleIndent();
      append("converter.updateJPFObj(argValue[" + index + "], arg" + index + ");");
      nextLine();
    }

    protected void printReturn() {
      addDoubleIndent();
      append("return;");
      nextLine();
    }

    protected void printReturnObj() {
      addDoubleIndent();
      append("return JPFObj;");
      nextLine();
    }

    protected void printReturnPrimitive(String wrapper, String methodName) {
      addDoubleIndent();
      append("return ((" + wrapper + ")returnValue)." + methodName + "();");
      nextLine();
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

      nextLine();
    }

    protected void wrapUpSource() {
      printFooter();
      writeToFile();
    }

    private void printFooter() {
      append("  }");
      nextLine();
      append("}");
      nextLine();
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
