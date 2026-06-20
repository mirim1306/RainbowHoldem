package project_2403;

import javax.sound.sampled.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 배경음악(BGM)과 효과음(SFX) 재생을 담당합니다.
 *
 * - SettingsManager에 저장된 on/off, 볼륨 값을 그대로 반영합니다.
 * - 사운드 파일이 sounds/ 폴더에 없어도 프로그램이 죽지 않고
 *   조용히 무시하도록 방어적으로 작성되어 있습니다.
 *   (사운드 리소스는 추후 sounds/bgm.wav, sounds/click.wav 등을 넣으면 바로 재생됩니다)
 */
public class SoundManager {

    private static final String SOUND_DIR = "sounds";
    private static final String DEFAULT_BGM = SOUND_DIR + "/bgm.wav";

    private static Clip bgmClip;
    private static String currentBgmFile = null;

    // 효과음은 짧게 여러 번 재생될 수 있으므로 파일별로 캐시해 둡니다.
    private static final Map<String, Clip> sfxCache = new HashMap<>();

    private SoundManager() {}

    // ---------------- 배경음악 ----------------

    /** 기본 배경음악(sounds/bgm.wav)을 재생합니다. 설정상 꺼져 있으면 재생하지 않습니다. */
    public static void playDefaultBgm() {
        playBgm(DEFAULT_BGM);
    }

    /** 지정한 파일을 배경음악으로 반복 재생합니다. */
    public static synchronized void playBgm(String filePath) {
        SettingsManager.load();

        currentBgmFile = filePath;

        if (!SettingsManager.isBgmEnabled()) {
            return; // 설정에서 꺼져 있으면 재생하지 않음
        }

        File file = new File(filePath);
        if (!file.exists()) {
            // 사운드 리소스가 아직 없는 경우, 조용히 넘어갑니다.
            return;
        }

        try {
            stopBgmInternal();

            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            bgmClip = AudioSystem.getClip();
            bgmClip.open(stream);
            applyVolume(bgmClip, SettingsManager.getBgmVolume());
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
        } catch (Exception e) {
            // 지원하지 않는 포맷, 파일 손상 등 어떤 이유로든 BGM 재생에 실패해도
            // 게임 진행에는 영향이 없어야 하므로 예외를 삼키고 로그만 남깁니다.
            System.err.println("배경음악 재생 실패: " + e.getMessage());
        }
    }

    /** 배경음악을 정지합니다. */
    public static synchronized void stopBgm() {
        stopBgmInternal();
    }

    private static void stopBgmInternal() {
        if (bgmClip != null) {
            if (bgmClip.isRunning()) {
                bgmClip.stop();
            }
            bgmClip.close();
            bgmClip = null;
        }
    }

    // ---------------- 효과음 ----------------

    /** sounds/ 폴더의 효과음 파일을 1회 재생합니다. (예: "click.wav", "card.wav") */
    public static void playSfx(String fileName) {
        SettingsManager.load();

        if (!SettingsManager.isSfxEnabled()) {
            return;
        }

        String path = SOUND_DIR + "/" + fileName;
        File file = new File(path);
        if (!file.exists()) {
            return; // 리소스가 아직 없으면 조용히 무시
        }

        try {
            Clip clip = sfxCache.get(path);
            if (clip == null) {
                AudioInputStream stream = AudioSystem.getAudioInputStream(file);
                clip = AudioSystem.getClip();
                clip.open(stream);
                sfxCache.put(path, clip);
            }
            if (clip.isRunning()) {
                clip.stop();
            }
            applyVolume(clip, SettingsManager.getSfxVolume());
            clip.setFramePosition(0);
            clip.start();
        } catch (Exception e) {
            System.err.println("효과음 재생 실패 (" + fileName + "): " + e.getMessage());
        }
    }

    // ---------------- 설정 변경 반영 ----------------

    /**
     * 설정 화면에서 on/off 또는 볼륨이 바뀔 때 호출됩니다.
     * 현재 재생 중인 배경음악에 즉시 반영합니다.
     */
    public static synchronized void onSettingsChanged() {
        if (!SettingsManager.isBgmEnabled()) {
            stopBgmInternal();
            return;
        }

        if (bgmClip == null && currentBgmFile != null) {
            // 꺼져 있다가 다시 켠 경우 재생을 재개합니다.
            playBgm(currentBgmFile);
            return;
        }

        if (bgmClip != null) {
            applyVolume(bgmClip, SettingsManager.getBgmVolume());
        }
    }

    private static void applyVolume(Clip clip, int volumePercent) {
        try {
            if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                return;
            }
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

            // 0~100(%) 값을 데시벨로 변환합니다. 0%는 사실상 무음(최저값) 처리합니다.
            float min = gainControl.getMinimum();
            float max = gainControl.getMaximum();

            float percent = Math.max(0, Math.min(100, volumePercent)) / 100f;
            float gain;
            if (percent <= 0f) {
                gain = min;
            } else {
                // 사람이 느끼는 음량 변화에 가깝도록 로그 스케일 적용
                gain = (float) (Math.log10(percent) / Math.log10(10)) * (max - min) * 0.5f + max;
                gain = Math.max(min, Math.min(max, gain));
            }
            gainControl.setValue(gain);
        } catch (Exception e) {
            // 볼륨 조절이 안 되는 환경이어도 재생 자체는 계속되도록 무시합니다.
        }
    }
}
