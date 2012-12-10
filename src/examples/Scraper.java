import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Scraper {
    public static void main(String[] args) throws MalformedURLException, IOException {
        URL url = new URL("http://www.swimrankings.net/index.php?page=meetDetail&meetId=516878&gender=1&styleId=2");
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        String line = reader.readLine();
        while (line != null) {
            System.out.print(line);
            line = reader.readLine();
        }
        reader.close();
    }
}