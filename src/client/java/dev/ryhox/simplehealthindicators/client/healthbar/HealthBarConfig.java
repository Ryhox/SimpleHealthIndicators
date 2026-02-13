package dev.ryhox.simplehealthindicators.client.healthbar;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class HealthBarConfig {
    private static final String FILE_NAME = "simplehealthindicators.properties";
    private static final String KEY_MODE = "healthbar_mode";
    private static final String KEY_EXTRA_LINES = "healthbar_extra_lines";
    private static final int MIN_EXTRA_LINES = 0;
    private static final int MAX_EXTRA_LINES = 5;

    private HealthBarConfig() {}

    public static HealthBarState.Mode loadMode() {
        Path path = getPath();
        Properties props = loadProperties(path);
        return HealthBarState.Mode.parse(props.getProperty(KEY_MODE));
    }

    public static int loadExtraLines() {
        Path path = getPath();
        Properties props = loadProperties(path);
        String raw = props.getProperty(KEY_EXTRA_LINES, "0");
        try {
            return clampExtraLines(Integer.parseInt(raw.trim()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static void saveMode(HealthBarState.Mode mode) {
        Path path = getPath();
        Properties props = loadProperties(path);
        props.setProperty(KEY_MODE, mode.name().toLowerCase());
        props.setProperty(KEY_EXTRA_LINES, Integer.toString(clampExtraLines(loadExtraLines())));
        storeProperties(path, props);
    }

    public static void saveExtraLines(int lines) {
        Path path = getPath();
        Properties props = loadProperties(path);
        props.setProperty(KEY_MODE, HealthBarState.MODE.name().toLowerCase());
        props.setProperty(KEY_EXTRA_LINES, Integer.toString(clampExtraLines(lines)));
        storeProperties(path, props);
    }

    private static Path getPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    private static Properties loadProperties(Path path) {
        Properties props = new Properties();
        if (!Files.exists(path)) return props;

        try (InputStream in = Files.newInputStream(path)) {
            props.load(in);
        } catch (IOException e) {
            return new Properties();
        }
        return props;
    }

    private static void storeProperties(Path path, Properties props) {
        try {
            Files.createDirectories(path.getParent());
            try (OutputStream out = Files.newOutputStream(path)) {
                props.store(out, "SimpleHealthIndicators");
            }
        } catch (IOException e) {
        }
    }

    private static int clampExtraLines(int lines) {
        return Math.max(MIN_EXTRA_LINES, Math.min(MAX_EXTRA_LINES, lines));
    }
}
