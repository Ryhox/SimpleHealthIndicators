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

    private HealthBarConfig() {}

    public static HealthBarState.Mode loadMode() {
        Path path = getPath();
        if (!Files.exists(path)) return HealthBarState.Mode.HEARTS;

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            props.load(in);
        } catch (IOException e) {
            return HealthBarState.Mode.HEARTS;
        }

        return HealthBarState.Mode.parse(props.getProperty(KEY_MODE));
    }

    public static void saveMode(HealthBarState.Mode mode) {
        Path path = getPath();
        Properties props = new Properties();
        props.setProperty(KEY_MODE, mode.name().toLowerCase());

        try {
            Files.createDirectories(path.getParent());
            try (OutputStream out = Files.newOutputStream(path)) {
                props.store(out, "SimpleHealthIndicators");
            }
        } catch (IOException e) {
        }
    }

    private static Path getPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }
}
