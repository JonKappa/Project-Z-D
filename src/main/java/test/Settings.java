package test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

// The player's settings, kept in a small file in their home folder so they survive restarts
// and moving or repackaging the game.
public class Settings {

    static final Path FILE = Path.of(System.getProperty("user.home"), ".project-z-d", "settings.properties");
    static final int VOLUME_STEP = 10;

    // 0 to 100; nothing plays sound yet, these are ready for when it does
    int musicVolume = 80;
    int effectsVolume = 80;

    // missing or unreadable values fall back to the defaults above
    static Settings load() {
        Settings settings = new Settings();
        if (!Files.exists(FILE)) {
            return settings;
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            props.load(in);
        } catch (IOException e) {
            System.err.println("Couldn't read " + FILE + ", using default settings: " + e.getMessage());
            return settings;
        }
        settings.musicVolume = readInt(props, "musicVolume", settings.musicVolume, 0, 100);
        settings.effectsVolume = readInt(props, "effectsVolume", settings.effectsVolume, 0, 100);
        return settings;
    }

    void save() {
        Properties props = new Properties();
        props.setProperty("musicVolume", Integer.toString(musicVolume));
        props.setProperty("effectsVolume", Integer.toString(effectsVolume));
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) {
                props.store(out, "Project Z-D settings");
            }
        } catch (IOException e) {
            System.err.println("Couldn't save settings to " + FILE + ": " + e.getMessage());
        }
    }

    private static int readInt(Properties props, String key, int fallback, int min, int max) {
        try {
            int value = Integer.parseInt(props.getProperty(key, "").trim());
            return Math.max(min, Math.min(max, value));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
