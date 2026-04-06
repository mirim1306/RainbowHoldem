package project_2403;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * [참가] 버튼 클릭 시 표시되는 화면
 * - 빠른 참가 탭: 2인/3인 대기열 입장
 * - 방 찾기 탭: 공개 방 목록 조회 및 방 코드 직접 입력
 */
public class JoinRoomScreen extends JFrame {

    private final String playerName;
    private final int coins;
    private final Home homeReference;

    private MultiplayerClient client;

    // 방 찾기 탭 - 테이블
    private DefaultTableModel roomTableModel;

    public JoinRoomScreen(String playerName, int coins, Home home) {
        this.playerName = playerName;
        this.coins = coins;
        this.homeReference = home;

        setTitle("레인보우 홀덤 - 멀티플레이 참가");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);

        buildUI();
        connectToServer();
        setVisible(true);
    }

    // ──────────────────────────────────────────────
    // UI 구성
    // ──────────────────────────────────────────────
    private void buildUI() {
        getContentPane().setBackground(new Color(30, 30, 60));
        setLayout(new BorderLayout(10, 10));

        // 상단 타이틀
        JLabel title = new JLabel("멀티플레이 참가", SwingConstants.CENTER);
        title.setFont(new Font("맑은 고딕", Font.BOLD, 32));
        title.setForeground(new Color(255, 215, 0));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
        add(title, BorderLayout.NORTH);

        // 탭 패널
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        tabs.setBackground(new Color(50, 50, 90));
        tabs.setForeground(Color.WHITE);

        tabs.addTab("⚡ 빠른 참가", buildQuickJoinTab());
        tabs.addTab("🔍 방 찾기", buildRoomSearchTab());

        add(tabs, BorderLayout.CENTER);

        // 하단 돌아가기 버튼
        JButton backBtn = new JButton("← 돌아가기");
        backBtn.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        backBtn.setBackground(new Color(80, 80, 80));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setBorderPainted(false);
        backBtn.addActionListener(e -> {
            if (client != null) client.disconnect();
            dispose();
            homeReference.setVisible(true);
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        bottomPanel.setBackground(new Color(30, 30, 60));
        bottomPanel.add(backBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /** 빠른 참가 탭 */
    private JPanel buildQuickJoinTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(40, 40, 80));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.gridwidth = 2;
        gbc.gridx = 0;

        JLabel desc = new JLabel("<html><center>빠른 참가를 선택하면 같은 인원 수를 기다리는<br>다른 플레이어와 자동으로 매칭됩니다!</center></html>",
                SwingConstants.CENTER);
        desc.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        desc.setForeground(Color.LIGHT_GRAY);
        gbc.gridy = 0;
        panel.add(desc, gbc);

        // 상태 레이블
        JLabel statusLabel = new JLabel("인원 수를 선택해주세요.", SwingConstants.CENTER);
        statusLabel.setFont(new Font("맑은 고딕", Font.BOLD, 17));
        statusLabel.setForeground(new Color(100, 220, 100));
        gbc.gridy = 1;
        panel.add(statusLabel, gbc);

        // 2인 / 3인 버튼
        gbc.gridwidth = 1;
        gbc.gridy = 2;

        JButton btn2 = createBigButton("👤 2인 매칭", new Color(34, 139, 34));
        JButton btn3 = createBigButton("👥 3인 매칭", new Color(30, 100, 200));

        btn2.addActionListener(e -> quickJoin(2, statusLabel));
        btn3.addActionListener(e -> quickJoin(3, statusLabel));

        gbc.gridx = 0;
        panel.add(btn2, gbc);
        gbc.gridx = 1;
        panel.add(btn3, gbc);

        return panel;
    }

    /** 방 찾기 탭 */
    private JPanel buildRoomSearchTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(40, 40, 80));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 방 목록 테이블
        String[] cols = {"방 코드", "방 이름", "인원", "공개 여부"};
        roomTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(roomTableModel);
        table.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
        table.setRowHeight(30);
        table.setBackground(new Color(30, 30, 60));
        table.setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 15));
        table.getTableHeader().setBackground(new Color(50, 50, 90));
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(30, 30, 60));
        panel.add(scroll, BorderLayout.CENTER);

        // 하단 입력 영역
        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        inputRow.setBackground(new Color(40, 40, 80));

        JLabel codeLabel = new JLabel("방 코드:");
        codeLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        codeLabel.setForeground(Color.WHITE);

        JTextField codeField = new JTextField(10);
        codeField.setFont(new Font("맑은 고딕", Font.PLAIN, 16));

        JLabel pwLabel = new JLabel("비밀번호:");
        pwLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        pwLabel.setForeground(Color.WHITE);

        JTextField pwField = new JTextField(10);
        pwField.setFont(new Font("맑은 고딕", Font.PLAIN, 16));

        JButton joinBtn = new JButton("입장");
        joinBtn.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        joinBtn.setBackground(new Color(34, 139, 34));
        joinBtn.setForeground(Color.WHITE);
        joinBtn.setFocusPainted(false);
        joinBtn.setBorderPainted(false);

        JButton refreshBtn = new JButton("🔄 새로고침");
        refreshBtn.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        refreshBtn.setBackground(new Color(100, 100, 150));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorderPainted(false);

        // 테이블 행 선택 시 코드 자동 입력
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                codeField.setText((String) roomTableModel.getValueAt(row, 0));
            }
        });

        joinBtn.addActionListener(e -> {
            String code = codeField.getText().trim();
            String pw = pwField.getText().trim();
            if (code.isEmpty()) {
                JOptionPane.showMessageDialog(this, "방 코드를 입력해주세요.", "알림", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (client == null || !client.isConnected()) {
                JOptionPane.showMessageDialog(this, "서버에 연결되어 있지 않습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }
            client.send("JOIN_ROOM|" + code + "|" + pw);
        });

        refreshBtn.addActionListener(e -> {
            if (client != null && client.isConnected()) client.send("ROOM_LIST");
        });

        inputRow.add(codeLabel);
        inputRow.add(codeField);
        inputRow.add(pwLabel);
        inputRow.add(pwField);
        inputRow.add(joinBtn);
        inputRow.add(refreshBtn);
        panel.add(inputRow, BorderLayout.SOUTH);

        return panel;
    }

    // ──────────────────────────────────────────────
    // 서버 연결 및 메시지 처리
    // ──────────────────────────────────────────────
    private void connectToServer() {
        client = MultiplayerClient.getInstance();
        if (!client.isConnected()) {
            boolean ok = client.connect();
            if (!ok) {
                JOptionPane.showMessageDialog(this,
                        "서버에 연결할 수 없습니다.\n로컬 서버(GameServer)가 실행 중인지 확인하세요.",
                        "연결 실패", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        client.setMessageListener(this::handleServerMessage);
        client.send("LOGIN|" + playerName);
    }

    private void handleServerMessage(String msg) {
        String[] parts = msg.split("\\|");
        switch (parts[0]) {
            case "LOGIN_OK":
                break;

            case "QUEUE_JOINED":
                // QUEUE_JOINED|maxPlayers
                break;

            case "ROOM_LIST":
                // ROOM_LIST|code,name,인원,공개여부|...
                roomTableModel.setRowCount(0);
                for (int i = 1; i < parts.length; i++) {
                    String[] info = parts[i].split(",");
                    if (info.length >= 4) {
                        roomTableModel.addRow(new Object[]{info[0], info[1], info[2], info[3]});
                    }
                }
                break;

            case "JOIN_OK":
            case "ROOM_CREATED":
                // 대기실 화면으로 전환
                dispose();
                new WaitingRoomScreen(playerName, coins, homeReference, client, parts[1], parts[2]);
                break;

            case "JOIN_FAIL":
                JOptionPane.showMessageDialog(this, parts[1], "참가 실패", JOptionPane.ERROR_MESSAGE);
                break;

            case "ROOM_STATE":
                // 방이 생성/참가 된 후 대기실로 이동
                dispose();
                String roomCode = parts[1];
                String roomName = parts[2];
                new WaitingRoomScreen(playerName, coins, homeReference, client, roomCode, roomName);
                break;
        }
    }

    /** 빠른 참가 요청 */
    private void quickJoin(int maxPlayers, JLabel statusLabel) {
        if (client == null || !client.isConnected()) {
            JOptionPane.showMessageDialog(this, "서버에 연결되어 있지 않습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }
        statusLabel.setText("⏳ " + maxPlayers + "인 매칭 대기 중...");
        client.send("QUICK_JOIN|" + maxPlayers);
    }

    private JButton createBigButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        btn.setPreferredSize(new Dimension(220, 80));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}