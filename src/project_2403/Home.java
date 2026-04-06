package project_2403;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class Home extends JFrame {
    private String playerName;
    private int wins = 0;
    private int losses = 0;
    private int coins = 100;
    private Player currentPlayer;
    private JLabel infoLabel;
    private JLabel nameLabel;
    private JLabel recordLabel;
    private JLabel coinLabel;
    
    public Home() {
        // 기본 생성자
    }
    
    public Home(String name, int wins, int losses, int coins) {
        this.playerName = name;
        this.wins = wins;
        this.losses = losses;
        this.coins = coins;
        createAndShowHomeGUI();
    }
    
    public void savePlayerData() {       
        boolean success = DatabaseManager.savePlayerData(playerName, wins, losses, coins);
        if (!success) {
            System.err.println("데이터 저장 실패: " + playerName);
            JOptionPane.showMessageDialog(this, 
                "데이터 저장에 실패했습니다.", 
                "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void updatePlayerData(boolean won, int finalCoins) {
        if (won) {
            this.wins++;
        } else {
            this.losses++;
        }
        this.coins = finalCoins;
        savePlayerData();
        updateLabels();
    }

    private void createAndShowHomeGUI() {
        setTitle("레인보우 홀덤");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(100, 149, 237));
        topPanel.setPreferredSize(new Dimension(1000, 150));

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 20));
        infoPanel.setOpaque(false);

        nameLabel = new JLabel("이름: " + playerName);
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 30));
        recordLabel = new JLabel("전적: " + wins + "승 " + losses + "패");
        recordLabel.setFont(new Font("맑은 고딕", Font.BOLD, 30));
        coinLabel = new JLabel("코인: " + coins);
        coinLabel.setFont(new Font("맑은 고딕", Font.BOLD, 30));

        infoPanel.add(nameLabel);
        infoPanel.add(recordLabel);
        infoPanel.add(coinLabel);
        
        topPanel.add(infoPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 20));
        buttonPanel.setOpaque(false);
        
        JButton settingsButton = new JButton("⚙️");
        settingsButton.setPreferredSize(new Dimension(60, 60));
        settingsButton.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        
        JButton exitButton = new JButton("🚪");
        exitButton.setPreferredSize(new Dimension(60, 60));
        exitButton.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        
        buttonPanel.add(settingsButton);
        buttonPanel.add(exitButton);
        
        topPanel.add(buttonPanel, BorderLayout.EAST);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);

        JPanel centerPanelContainer = new JPanel(new GridBagLayout());
        centerPanelContainer.setBackground(new Color(106, 90, 205));

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 50));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        centerPanel.setBackground(new Color(106, 90, 205));

        JButton explainButton = createButton("설명");
        JButton aiButton = createButton("AI");
        JButton joinButton = createButton("참가");
        JButton createRoomButton = createButton("생성");

        // ── AI 게임 ──
        aiButton.addActionListener(e -> {
            int totalPlayers = showPlayerSelectionDialog();
            if (totalPlayers != 0) {
                dispose();
                new GameScreen(playerName, totalPlayers, coins, Home.this);
            }
        });

        // ── 설명 ──
        explainButton.addActionListener(e -> showGameExplanation());

        // ── 참가 (멀티플레이 참가 화면) ──
        joinButton.addActionListener(e -> {
            setVisible(false);
            new JoinRoomScreen(playerName, coins, Home.this);
        });

        // ── 생성 (멀티플레이 방 생성 화면) ──
        createRoomButton.addActionListener(e -> {
            setVisible(false);
            new CreateRoomScreen(playerName, coins, Home.this);
        });

        ActionListener notImplementedListener = e ->
            JOptionPane.showMessageDialog(Home.this, "아직 구현되지 않았습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);

        settingsButton.addActionListener(notImplementedListener);
        exitButton.addActionListener(e -> System.exit(0));
        
        centerPanel.add(explainButton);
        centerPanel.add(aiButton);
        centerPanel.add(joinButton);
        centerPanel.add(createRoomButton);
        
        centerPanelContainer.add(centerPanel);
        mainPanel.add(centerPanelContainer, BorderLayout.CENTER);

        getContentPane().add(mainPanel);
        setVisible(true);
    }
    
    private void updateLabels() {
        if (nameLabel != null) nameLabel.setText("이름: " + playerName);
        if (recordLabel != null) recordLabel.setText("전적: " + wins + "승 " + losses + "패");
        if (coinLabel != null) coinLabel.setText("코인: " + coins);
    }
    
    private static JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("맑은 고딕", Font.BOLD, 40));
        button.setPreferredSize(new Dimension(250, 250));
        return button;
    }

    private static void showGameExplanation() {
        String explanation = getGameExplanationFromFile();
        JTextArea textArea = new JTextArea(explanation);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        JOptionPane.showMessageDialog(null, scrollPane, "게임 설명", JOptionPane.PLAIN_MESSAGE);
    }

    private static String getGameExplanationFromFile() {
        StringBuilder explanation = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader("game_explanation.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                explanation.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "게임 설명 파일을 찾을 수 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
        return explanation.toString();
    }
    
    private int showPlayerSelectionDialog() {
        String[] options = {"2명", "3명"};
        String selectedOption = (String) JOptionPane.showInputDialog(
            this, "함께 플레이할 총 인원 수를 선택하세요.\n(나를 포함한 총 인원 수)",
            "인원 선택", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (selectedOption != null) {
            try {
                return Integer.parseInt(selectedOption.substring(0, 1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}