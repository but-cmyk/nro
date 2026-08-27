package utils;
import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ErrorRes {
    public static void howToFix(String er) {
        try {
            String query = "https://chatgpt.com/?q=" + URLEncoder.encode(er, StandardCharsets.UTF_8);
            Desktop.getDesktop().browse(new URI(query));
        } catch (Exception ignored) {

        }
    }
}
