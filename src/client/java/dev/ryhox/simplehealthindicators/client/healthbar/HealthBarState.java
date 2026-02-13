package dev.ryhox.simplehealthindicators.client.healthbar;

public final class HealthBarState {
    private HealthBarState() {}

    public enum Mode {
        BAR, HEARTS, NUMERIC;

        public static Mode parse(String s) {
            if (s == null) return HEARTS;
            return switch (s.toLowerCase()) {
                case "bar" -> BAR;
                case "hearts" -> HEARTS;
                case "numeric" -> NUMERIC;
                default -> HEARTS;
            };
        }
    }

    public static volatile Mode MODE = Mode.HEARTS;
    public static volatile int EXTRA_LINES = 0;
}
