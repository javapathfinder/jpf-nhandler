
import com.google.api.GoogleAPI;
import com.google.api.translate.Language;
import com.google.api.translate.Translate;

public class GoogleTranslator {
  public static void main (String[] args) throws Exception {
    // Set the HTTP referrer to your website address
    GoogleAPI.setHttpReferrer("http://www.nastaran.ca/");

    // Set the Google Translate API key
    GoogleAPI.setKey("AIzaSyAJJZ5NzUsP9EoaLpddheA3oTpiTVXOXZI");

    // Do the translation
    String translatedText = Translate.DEFAULT.execute("Hello world", Language.ENGLISH, Language.FRENCH);

    System.out.println(translatedText);
  }
}
