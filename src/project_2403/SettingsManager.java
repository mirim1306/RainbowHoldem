package project_2403;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 게임의 사운드 설정(배경음악 on/off, 볼륨 / 효과음 on/off, 볼륨)을
 * 관리하고 settings.properties 파일에 저장/로드합니다.
 *
 * MySQL과는 무관하게 항상 로컬 파일로 저장되므로,
 * DB 연결 여부와 상관없이 설정은 항상 정상적으로 동작합니다.
 */
public class SettingsManager {

    private static final String SETTINGS_FILE = "settings.properties";

    // 기본값
    private static boolean bgmEnabled = true;
    private static int bgmVolume = 70;     // 0 ~ 100
    private static boolean sfxEnabled = true;
    private static int sfxVolume = 80;     // 0 ~ 100

    private static boolean loaded = false;

    private SettingsManager() {}

    /** 프로그램 시작 시(혹은 최초 사용 시) 한 번 설정 파일을 읽어옵니다. */
    public static synchronized void load() {
        if (loaded) return;
        loaded = true;

        File file = new File(SETTINGS_FILE);
        if (!file.exists()) {
            // 설정 파일이 없으면 기본값으로 새로 만들어 둡니다.
            save();
            return;
        }

        Properties props = new Properties();
        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8)) {
            props.load(reader);

            bgmEnabled = Boolean.parseBoolean(props.getProperty("bgmEnabled", "true"));
            bgmVolume = clampVolume(parseIntSafe(props.getProperty("bgmVolume"), 70));
            sfxEnabled = Boolean.parseBoolean(props.getProperty("sfxEnabled", "true"));
            sfxVolume = clampVolume(parseIntSafe(props.getProperty("sfxVolume"), 80));
        } catch (IOException e) {
            System.err.println("설정 파일 로드 실패, 기본값을 사용합니다: " + e.getMessage());
        }
    }

    /** 현재 설정값을 settings.properties 파일에 저장합니다. */
    public static synchronized void save() {
        Properties props = new Properties();
        props.setProperty("bgmEnabled", String.valueOf(bgmEnabled));
        props.setProperty("bgmVolume", String.valueOf(bgmVolume));
        props.setProperty("sfxEnabled", String.valueOf(sfxEnabled));
        props.setProperty("sfxVolume", String.valueOf(sfxVolume));

        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(SETTINGS_FILE), StandardCharsets.UTF_8)) {
            props.store(writer, "Rainbow Holdem - Sound Settings");
        } catch (IOException e) {
            System.err.println("설정 파일 저장 실패: " + e.getMessage());
        }
    }

    // ---------- Getter / Setter ----------

    public static boolean isBgmEnabled() {
        load();
        return bgmEnabled;
    }

    public static void setBgmEnabled(boolean enabled) {
        load();
        bgmEnabled = enabled;
        save();
        SoundManager.onSettingsChanged();
    }

    public static int getBgmVolume() {
        load();
        return bgmVolume;
    }

    public static void setBgmVolume(int volume) {
        load();
        bgmVolume = clampVolume(volume);
        save();
        SoundManager.onSettingsChanged();
    }

    public static boolean isSfxEnabled() {
        load();
        return sfxEnabled;
    }

    public static void setSfxEnabled(boolean enabled) {
        load();
        sfxEnabled = enabled;
        save();
    }

    public static int getSfxVolume() {
        load();
        return sfxVolume;
    }

    public static void setSfxVolume(int volume) {
        load();
        sfxVolume = clampVolume(volume);
        save();
    }

    // ---------- 내부 헬퍼 ----------

    private static int clampVolume(int v) {
        if (v < 0) return 0;
        if (v > 100) return 100;
        return v;
    }

    private static int parseIntSafe(String value, int fallback) {
        if (value == null) return fallback;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
