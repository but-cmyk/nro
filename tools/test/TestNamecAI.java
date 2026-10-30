package tools.test;

import bot.NamecImitationAI;

public class TestNamecAI {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  STARTING NAMEC IMITATION AI VERIFICATION TEST");
        System.out.println("==================================================");

        try {
            // Test 1: Policy Parameters
            System.out.println("\n[Test 1] Checking learned Namec Policy parameters...");
            System.out.println("  - Mean stride: " + NamecImitationAI.MEAN_STRIDE_PX + " px");
            System.out.println("  - Attack trigger range: " + NamecImitationAI.ATTACK_TRIGGER_RANGE + " px");
            System.out.println("  - Demon probability: " + (NamecImitationAI.DEMON_PROBABILITY * 100) + "%");
            System.out.println("  - Masenko probability: " + (NamecImitationAI.MASENKO_PROBABILITY * 100) + "%");
            System.out.println("  - Heal threshold: " + (NamecImitationAI.HEAL_HP_THRESHOLD * 100) + "%");
            System.out.println("  - Mean delay jitter: " + NamecImitationAI.MEAN_DELAY_MS + " ms (std: " + NamecImitationAI.STD_DELAY_MS + " ms)");

            if (NamecImitationAI.DEMON_PROBABILITY < 0.90f) {
                throw new AssertionError("Demon probability should be >= 90% based on telemetry data");
            }

            // Test 2: Target farming maps
            System.out.println("\n[Test 2] Checking Namec farming maps allocation...");
            int[] maps = NamecImitationAI.NAMEC_FARM_MAPS;
            if (maps.length < 10) {
                throw new AssertionError("Expected at least 10 Namec farm maps, got: " + maps.length);
            }
            System.out.println("  - Target maps count: " + maps.length + " (Maps: 30, 38, 34, 33, 13, 29, 27, 11, 31, 32, 8, 9, 10, 7)");

            System.out.println("\n[SUCCESS] ALL NAMEC IMITATION AI TESTS PASSED 100%!");
        } catch (Throwable t) {
            System.err.println("\n[FAIL] Test failed:");
            t.printStackTrace();
            System.exit(1);
        }
    }
}
