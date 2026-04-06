package project_2403;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 방에 입장한 후 다른 플레이어를 기다리는 대기실 화면
 * 모든 플레이어가 모이면 자동으로 멀티 게임 화면으로 전환됩니다.
 */
public class WaitingRoomScreen extends JFrame {

    private final String playerName;
    private final int coins;
    private final Home homeReference;
    private final MultiplayerClient client;
    private final String roomCode;
    private final String roomName;

    private JLabel statusLabel;
    private JPanel playersPanel;
    private JLabel roomCodeLabel;

    public WaitingRoomScreen(String playerName, int coins, Home home,
                              MultiplayerClient client, String roomCode, String roomName) {
        this.playerName = playerName;
        this.coins = coins;
        this.homeReference = home;
        this.client = client;
        this.roomCode = roomCode;
        this.roomName = roomName;

        setTitle("레인보우 홀덤 - 대기실");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);

        buildUI();
        client.setMessageListener(this::handleServerMessage);
        setVisible(true);
    }

    private void buildUI() {
        getContentPane().setBackground(new Color(30, 30, 60));
        setLayout(new BorderLayout(10, 10));

        // 상단: 방 정보
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        topPanel.setBackground(new Color(20, 20, 50));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        JLabel nameLabel = new JLabel("🏠 " + roomName, SwingConstants.CENTER);
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 26));
        nameLabel.setForeground(new Color(255, 215, 0));

        roomCodeLabel = new JLabel("방 코드: " + roomCode, SwingConstants.CENTER);
        roomCodeLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        roomCodeLabel.setForeground(Color.LIGHT_GRAY);

        topPanel.add(nameLabel);
        topPanel.add(roomCodeLabel);
        add(topPanel, BorderLayout.NORTH);

        // 중앙: 참가자 목록
        playersPanel = new JPanel();
        playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.Y_AXIS));
        playersPanel.setBackground(new Color(40, 40, 80));
        playersPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 180), 1),
                "참가자", 0, 0,
                new Font("맑은 고딕", Font.BOLD, 15), Color.WHITE));

        JScrollPane scroll = new JScrollPane(playersPanel);
        scroll.getViewport().setBackground(new Color(40, 40, 80));
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        // 하단: 상태 및 버튼
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(new Color(30, 30, 60));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 15, 20));

        statusLabel = new JLabel("다른 플레이어를 기다리는 중...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("맑은 고딕", Font.BOLD, 17));
        statusLabel.setForeground(new Color(100, 220, 100));

        JButton leaveBtn = new JButton("← 나가기");
        leaveBtn.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        leaveBtn.setBackground(new Color(180, 50, 50));
        leaveBtn.setForeground(Color.WHITE);
        leaveBtn.setFocusPainted(false);
        leaveBtn.setBorderPainted(false);
        leaveBtn.addActionListener(e -> {
            client.send("LEAVE_ROOM");
            dispose();
            homeReference.setVisible(true);
        });

        bottomPanel.add(statusLabel, BorderLayout.CENTER);
        bottomPanel.add(leaveBtn, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void handleServerMessage(String msg) {
        String[] parts = msg.split("\\|");
        switch (parts[0]) {
            case "ROOM_STATE":
                // ROOM_STATE|code|name|maxPlayers|currentCount|name1|name2...
                updatePlayerList(parts);
                break;

            case "GAME_START":
                statusLabel.setText("🎮 게임 시작!");
                Timer t = new Timer(800, e -> {
                    ((Timer) e.getSource()).stop();
                    dispose();
                    new MultiGameScreen(playerName, coins, homeReference, client);
                });
                t.setRepeats(false);
                t.start();
                break;

            case "PLAYER_LEFT":
                statusLabel.setText(parts[1] + "님이 나갔습니다. 대기 중...");
                break;
        }
    }

    private void updatePlayerList(String[] parts) {
        // parts: ROOM_STATE|code|name|maxPlayers|currentCount|p1|p2...
        int maxP = Integer.parseInt(parts[3]);
        int curP = Integer.parseInt(parts[4]);

        playersPanel.removeAll();
        for (int i = 5; i < parts.length; i++) {
            JLabel pl = new JLabel("  👤 " + parts[i] + (parts[i].equals(playerName) ? "  (나)" : ""));
            pl.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
            pl.setForeground(Color.WHITE);
            pl.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            playersPanel.add(pl);
        }
        // 빈 슬롯
        for (int i = curP; i < maxP; i++) {
            JLabel pl = new JLabel("  ⏳ 대기 중...");
            pl.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
            pl.setForeground(Color.GRAY);
            pl.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            playersPanel.add(pl);
        }
        playersPanel.revalidate();
        playersPanel.repaint();

        statusLabel.setText("참가자: " + curP + " / " + maxP + " — 인원이 모이면 자동 시작됩니다.");
    }
}