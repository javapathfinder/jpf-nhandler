import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Scanner;

public class ScraperScanner {
  public static void main (String[] args) throws IOException {
    URL url = new URL("http://www.nastaran.ca/");
    InputStreamReader reader = new InputStreamReader(url.openStream());
    Scanner scanner = new Scanner(reader);
    while (scanner.hasNextLine()) {
      System.out.println(scanner.nextLine());
    }
    reader.close();
  }
}
