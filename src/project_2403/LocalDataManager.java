package project_2403;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.Properties;

/**
 * MySQL 연결이 불가능한 환경에서 사용하는 파일 기반 데이터 저장소입니다.
 * DatabaseManager와 동일한 기능(회원가입/로그인/전적 저장 및 로드)을
 * 로컬 파일(playerdata/이름.properties)을 이용해 제공합니다.
 *
 * 이 클래스만 단독으로 사용해도 게임이 정상적으로 동작하도록 설계되어,
 * MySQL이 전혀 없는 PC에서도 "회원가입 -> 로그인 -> 플레이 -> 전적 저장"이
 * 모두 가능합니다.
 */
public class LocalDataManager {

    // 플레이어별 데이터 파일이 저장되는 폴더
    private static final String DATA_DIR = "playerdata";

    static {
        // 프로그램 시작 시 데이터 폴더가 없으면 생성
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            System.err.println("로컬 데이터 폴더 생성 실패: " + e.getMessage());
        }
    }

    // 플레이어 이름 -> 안전한 파일 경로로 변환
    // 한글 등 유니코드 사용자명이 시스템 인코딩(특히 영문 Windows/Linux 환경)에 따라
    // 파일 경로 생성 자체가 실패하는 것을 막기 위해, 이름을 영문/숫자로만 이루어진
    // 안전한 형태로 인코딩하여 파일명으로 사용합니다. (내부 동작용이며 사용자에게 노출되지 않음)
    private static Path getPlayerFile(String playerName) {
        String safeName = toSafeFileName(playerName);
        return Paths.get(DATA_DIR, safeName + ".properties");
    }

    private static String toSafeFileName(String playerName) {
        StringBuilder sb = new StringBuilder();
        byte[] bytes = playerName.getBytes(StandardCharsets.UTF_8);
        for (byte b : bytes) {
            int v = b & 0xFF;
            String hex = Integer.toHexString(v);
            if (hex.length() == 1) sb.append('0');
            sb.append(hex);
        }
        return sb.toString();
    }

    // 플레이어 존재 여부 확인
    public static boolean playerExists(String playerName) {
        return Files.exists(getPlayerFile(playerName));
    }

    // 회원가입 (파일 생성)
    public static boolean registerPlayer(String playerName, String password) {
        if (playerExists(playerName)) {
            System.out.println("[로컬 모드] 이미 존재하는 사용자: " + playerName);
            return false;
        }

        Properties props = new Properties();
        props.setProperty("playerName", playerName);
        props.setProperty("password", hashPassword(password));
        props.setProperty("wins", "0");
        props.setProperty("losses", "0");
        props.setProperty("coins", "100");

        return saveProperties(playerName, props, "회원가입 (로컬 파일)");
    }

    // 로그인 (비밀번호 검증)
    public static boolean loginPlayer(String playerName, String password) {
        Properties props = loadProperties(playerName);
        if (props == null) {
            System.out.println("[로컬 모드] 로그인 실패: 존재하지 않는 사용자 - " + playerName);
            return false;
        }

        String storedHash = props.getProperty("password", "");
        boolean valid = hashPassword(password).equals(storedHash);
        System.out.println(valid ? "[로컬 모드] 로그인 성공: " + playerName
                                  : "[로컬 모드] 로그인 실패: 비밀번호 불일치");
        return valid;
    }

    // 플레이어 데이터 로드
    public static DatabaseManager.PlayerData loadPlayerData(String playerName) {
        Properties props = loadProperties(playerName);
        if (props == null) {
            System.out.println("[로컬 모드] 새로운 플레이어: " + playerName);
            return new DatabaseManager.PlayerData(0, 0, 100, false);
        }

        int wins = parseIntSafe(props.getProperty("wins"), 0);
        int losses = parseIntSafe(props.getProperty("losses"), 0);
        int coins = parseIntSafe(props.getProperty("coins"), 100);

        System.out.println("[로컬 모드] 플레이어 데이터 로드 성공: " + playerName);
        return new DatabaseManager.PlayerData(wins, losses, coins, true);
    }

    // 플레이어 데이터 저장 (전적/코인 갱신, 비밀번호는 유지)
    public static boolean savePlayerData(String playerName, int wins, int losses, int coins) {
        Properties props = loadProperties(playerName);
        if (props == null) {
            // 기존 파일이 없다면(예: DB 모드에서 가입 후 로컬로 전환된 경우)
            // 비밀번호 없이 새로 생성합니다.
            props = new Properties();
            props.setProperty("playerName", playerName);
            props.setProperty("password", "");
        }

        props.setProperty("wins", String.valueOf(wins));
        props.setProperty("losses", String.valueOf(losses));
        props.setProperty("coins", String.valueOf(coins));

        return saveProperties(playerName, props, "플레이어 데이터 저장 (로컬 파일)");
    }

    // ---------- 내부 헬퍼 ----------

    private static Properties loadProperties(String playerName) {
        Path file = getPlayerFile(playerName);
        if (!Files.exists(file)) {
            return null;
        }
        Properties props = new Properties();
        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(file.toFile()), StandardCharsets.UTF_8)) {
            props.load(reader);
            return props;
        } catch (IOException e) {
            System.err.println("[로컬 모드] 데이터 로드 실패: " + e.getMessage());
            return null;
        }
    }

    private static boolean saveProperties(String playerName, Properties props, String actionLabel) {
        Path file = getPlayerFile(playerName);
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(file.toFile()), StandardCharsets.UTF_8)) {
            props.store(writer, "Rainbow Holdem - Local Player Data");
            System.out.println(actionLabel + " 성공: " + playerName);
            return true;
        } catch (IOException e) {
            System.err.println(actionLabel + " 실패: " + e.getMessage());
            return false;
        }
    }

    private static int parseIntSafe(String value, int fallback) {
        if (value == null) return fallback;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    // 비밀번호 해싱 (DatabaseManager와 동일한 SHA-256 방식)
    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return password;
        }
    }
}
