package project_2403;

import java.sql.*;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/rainbow_holdem?useSSL=false&serverTimezone=Asia/Seoul";
    private static final String USER = "root"; 
    private static final String PASSWORD = "ghks084@"; // 본인의 MySQL 비밀번호
    
    // 데이터베이스 연결
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }
    
    // 연결 테스트 메서드
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("MySQL 연결 성공!");
            return true;
        } catch (SQLException e) {
            System.err.println("MySQL 연결 실패: " + e.getMessage());
            return false;
        }
    }
    
    // 플레이어 데이터 로드
    public static PlayerData loadPlayerData(String playerName) {
        String query = "SELECT wins, losses, coins FROM players WHERE player_name = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, playerName);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int wins = rs.getInt("wins");
                int losses = rs.getInt("losses");
                int coins = rs.getInt("coins");
                System.out.println("플레이어 데이터 로드 성공: " + playerName);
                return new PlayerData(wins, losses, coins, true);
            } else {
                System.out.println("새로운 플레이어: " + playerName);
                return new PlayerData(0, 0, 100, false);
            }
        } catch (SQLException e) {
            System.err.println("데이터 로드 실패: " + e.getMessage());
            e.printStackTrace();
            return new PlayerData(0, 0, 100, false);
        }
    }
    
    // 플레이어 데이터 저장 (password는 업데이트하지 않음)
    public static boolean savePlayerData(String playerName, int wins, int losses, int coins) {
        String query = "UPDATE players SET wins = ?, losses = ?, coins = ? WHERE player_name = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, wins);
            pstmt.setInt(2, losses);
            pstmt.setInt(3, coins);
            pstmt.setString(4, playerName);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("플레이어 데이터 저장 성공: " + playerName);
                return true;
            } else {
                System.out.println("플레이어 데이터 저장 실패: 존재하지 않는 사용자");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("데이터 저장 실패: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // 플레이어 존재 여부 확인
    public static boolean playerExists(String playerName) {
        String query = "SELECT COUNT(*) FROM players WHERE player_name = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, playerName);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // 회원가입
    public static boolean registerPlayer(String playerName, String password) {
        // 이미 존재하는 사용자 확인
        if (playerExists(playerName)) {
            System.out.println("이미 존재하는 사용자: " + playerName);
            return false;
        }
        
        String query = "INSERT INTO players (player_name, password, wins, losses, coins) VALUES (?, ?, 0, 0, 100)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, playerName);
            pstmt.setString(2, hashPassword(password)); // 비밀번호 해싱
            
            int result = pstmt.executeUpdate();
            System.out.println("회원가입 성공: " + playerName);
            return result > 0;
            
        } catch (SQLException e) {
            System.err.println("회원가입 실패: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // 로그인
    public static boolean loginPlayer(String playerName, String password) {
        String query = "SELECT password FROM players WHERE player_name = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, playerName);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                boolean isValid = verifyPassword(password, storedPassword);
                
                if (isValid) {
                    System.out.println("로그인 성공: " + playerName);
                } else {
                    System.out.println("로그인 실패: 비밀번호 불일치");
                }
                return isValid;
            } else {
                System.out.println("로그인 실패: 존재하지 않는 사용자");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("로그인 오류: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // 비밀번호 해싱 (SHA-256 사용)
    private static String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
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
    
    // 비밀번호 검증
    private static boolean verifyPassword(String inputPassword, String storedHash) {
        String inputHash = hashPassword(inputPassword);
        return inputHash.equals(storedHash);
    }
    
    // 플레이어 데이터 클래스
    public static class PlayerData {
        public final int wins;
        public final int losses;
        public final int coins;
        public final boolean exists;
        
        public PlayerData(int wins, int losses, int coins, boolean exists) {
            this.wins = wins;
            this.losses = losses;
            this.coins = coins;
            this.exists = exists;
        }
    }
}