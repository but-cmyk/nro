package utils;

import java.util.Random;

import models.player.Player;

import java.util.regex.Pattern;

public class Functions {

    private static final String REGEX = "\\b(hehela)\\b";
    private static final Pattern pattern = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    public static boolean isSpam(Player player, String text) {
        return pattern.matcher(text.toLowerCase()).find() && (!player.name.equals("Ngọc Rồng Online"));
    }

    public static int maxint(long n) {
        return (int) (n > Integer.MAX_VALUE ? Integer.MAX_VALUE : n);
    }

    public static String generateRandomCharacters(int quantity) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < quantity; i++) {
            int type = random.nextInt(2);
            char generatedChar;

            if (type == 0) {
                generatedChar = (char) (random.nextInt(10) + '0');
            } else {
                generatedChar = (char) (random.nextInt(26) + 'A');
            }

            sb.append(generatedChar);
        }

        return sb.toString();
    }

    public static void sleep(long l) {
        try {
            Thread.sleep(l);
        } catch (InterruptedException _) {
        }
    }

}
