package gov.nasa.jpf.nhandler.peerGen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class PeerSourceGen {

  private File file;
  private String name;
  private String path;

  protected PeerSourceGen(String cname) throws IOException {
    name = PeerClassGen.getNativePeerClsName(cname);
    path = PeerClassGen.peersLocation + cname + ".java";
    System.out.println("path: " + path);
    file = new File(path);
  }

  protected File getSourceFile() throws IOException {
    if(!file.exists()) {
      file.createNewFile();
      generateMinimalPeerSource();
    }
    
    return file;
  }

  protected void generateMinimalPeerSource() throws FileNotFoundException {
    PrintWriter pw = new PrintWriter(file);
    printImport(pw);
    printClassHeader(pw);
    printDefaultConstructor(pw);
    printFooter(pw);
    pw.flush();
  }

  protected void printImport(PrintWriter pw) {
    pw.append("import gov.nasa.jpf.jvm.MJIEnv;\n");
    pw.append("import gov.nasa.jpf.nhandler.conversion.ConversionException;\n");
    pw.append("import gov.nasa.jpf.nhandler.conversion.Converter;\n");
    pw.append("import java.lang.reflect.Method;");
    pw.append("\n\n");
  }

  protected void printClassHeader(PrintWriter pw) {
    pw.append("public class " + name);
    pw.append(" {");
    pw.append("\n\n");
  }

  protected void printDefaultConstructor(PrintWriter pw) {
    pw.append("  public " + name + "()");
    pw.append(" {");
    pw.append("\n");
    pw.append("  }");
    pw.append("\n\n");
  }

  protected void printFooter(PrintWriter pw) {
    pw.append("}");
  }

  protected void removeFooter() throws FileNotFoundException {
    BufferedReader bf = new BufferedReader(new FileReader("c:\\test.txt"));
  }

  public static void main(String[] args) throws IOException {
	PeerSourceGen sourceGen = new PeerSourceGen("java.lang.String");
    sourceGen.getSourceFile();
  }
}
