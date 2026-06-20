package project_2403;

/**
 * DatabaseManager(MySQL)와 LocalDataManager(파일)를 자동으로 전환해주는
 * 통합 데이터 접근 창구입니다.
 *
 * - MySQL 연결이 가능하면: 기존처럼 MySQL을 사용합니다.
 * - MySQL 연결이 불가능하면: 자동으로 로컬 파일(playerdata 폴더)을 사용합니다.
 *
 * 화면(Start, Home 등)에서는 더 이상 DatabaseManager.testConnection()을
 * 직접 호출해 분기할 필요 없이, 이 클래스의 메서드만 호출하면 됩니다.
 */
public class DataManager {

    // 현재 세션에서 로컬 모드로 동작 중인지 여부
    private static boolean localMode = false;

    // 이번 세션에서 MySQL 연결 가능 여부를 한 번만 검사해서 캐싱
    private static boolean checked = false;

    /** 로컬 파일 모드로 동작 중인지 여부 (설정 화면 등에서 안내 문구를 띄울 때 사용) */
    public static boolean isLocalMode() {
        ensureChecked();
        return localMode;
    }

    /** MySQL 연결을 확인하고, 실패하면 자동으로 로컬 모드로 전환합니다. */
    private static void ensureChecked() {
        if (checked) return;
        checked = true;
        if (DatabaseManager.testConnection()) {
            localMode = false;
            System.out.println("MySQL 연결 성공 - DB 모드로 동작합니다.");
        } else {
            localMode = true;
            System.out.println("MySQL 연결 실패 - 로컬 파일 모드로 자동 전환합니다. (playerdata 폴더에 저장됩니다)");
        }
    }

    /** 외부에서 강제로 다시 연결을 시도하고 싶을 때 (예: 설정에서 "DB 다시 연결" 버튼) */
    public static boolean recheckConnection() {
        checked = false;
        ensureChecked();
        return !localMode;
    }

    public static boolean playerExists(String playerName) {
        ensureChecked();
        return localMode ? LocalDataManager.playerExists(playerName)
                          : DatabaseManager.playerExists(playerName);
    }

    public static boolean registerPlayer(String playerName, String password) {
        ensureChecked();
        return localMode ? LocalDataManager.registerPlayer(playerName, password)
                          : DatabaseManager.registerPlayer(playerName, password);
    }

    public static boolean loginPlayer(String playerName, String password) {
        ensureChecked();
        return localMode ? LocalDataManager.loginPlayer(playerName, password)
                          : DatabaseManager.loginPlayer(playerName, password);
    }

    public static DatabaseManager.PlayerData loadPlayerData(String playerName) {
        ensureChecked();
        return localMode ? LocalDataManager.loadPlayerData(playerName)
                          : DatabaseManager.loadPlayerData(playerName);
    }

    /**
     * 플레이어 데이터를 저장합니다. DB 모드에서 저장 도중 연결이 끊기는 등
     * 예기치 못한 실패가 발생하면 자동으로 로컬 파일에 백업 저장하여
     * 게임 결과(전적/코인)가 유실되지 않도록 합니다.
     */
    public static boolean savePlayerData(String playerName, int wins, int losses, int coins) {
        ensureChecked();

        if (localMode) {
            return LocalDataManager.savePlayerData(playerName, wins, losses, coins);
        }

        boolean success = DatabaseManager.savePlayerData(playerName, wins, losses, coins);
        if (!success) {
            // DB 저장에 실패한 경우, 데이터를 잃지 않도록 로컬 파일에도 저장해 둡니다.
            System.err.println("DB 저장 실패 - 로컬 파일에 대신 저장합니다: " + playerName);
            return LocalDataManager.savePlayerData(playerName, wins, losses, coins);
        }
        return true;
    }
}
