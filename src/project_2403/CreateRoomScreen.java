package project_2403;

import javax.swing.*;
import java.awt.*;

/**
 * [생성] 버튼 클릭 시 표시되는 방 생성 화면
 */
public class CreateRoomScreen extends JFrame {

    private final String playerName;
    private final int coins;
    private final Home homeReference;
    private MultiplayerClient client;

    public CreateRoomScreen(String playerName, int coins, Home home) {
        this.playerName = playerName;
        this.coins = coins;
        this.homeReference = home;

        setTitle("레인보우 홀덤 - 방 생성");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);

        buildUI();
        connectToServer();
        setVisible(true);
    }

    private void buildUI() {
        getContentPane().setBackground(new Color(30, 30, 60));
        setLayout(new BorderLayout(10, 10));

        // 타이틀
        JLabel title = new JLabel("방 생성", SwingConstants.CENTER);
        title.setFont(new Font("맑은 고딕", Font.BOLD, 32));
        title.setForeground(new Color(255, 215, 0));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // 입력 폼
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(new Color(40, 40, 80));
        form.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 8, 10, 8);

        Font labelFont = new Font("맑은 고딕", Font.BOLD, 17);
        Font fieldFont = new Font("맑은 고딕", Font.PLAIN, 16);

        // 방 이름
        JLabel nameLabel = new JLabel("방 이름");
        nameLabel.setFont(labelFont);
        nameLabel.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        form.add(nameLabel, gbc);

        JTextField nameField = new JTextField(playerName + "의 방");
        nameField.setFont(fieldFont);
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 2;
        form.add(nameField, gbc);

        // 최대 인원
        JLabel maxLabel = new JLabel("최대 인원");
        maxLabel.setFont(labelFont);
        maxLabel.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        form.add(maxLabel, gbc);

        JComboBox<String> maxCombo = new JComboBox<>(new String[]{"2명", "3명"});
        maxCombo.setFont(fieldFont);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2;
        form.add(maxCombo, gbc);

        // 공개 여부
        JLabel typeLabel = new JLabel("방 공개 여부");
        typeLabel.setFont(labelFont);
        typeLabel.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        form.add(typeLabel, gbc);

        JRadioButton publicBtn = new JRadioButton("공개");
        JRadioButton privateBtn = new JRadioButton("비공개 (비밀번호)");
        publicBtn.setFont(fieldFont);
        privateBtn.setFont(fieldFont);
        publicBtn.setOpaque(false);
        privateBtn.setOpaque(false);
        publicBtn.setForeground(Color.WHITE);
        privateBtn.setForeground(Color.WHITE);
        publicBtn.setSelected(true);

        ButtonGroup typeGroup = new ButtonGroup();
        typeGroup.add(publicBtn);
        typeGroup.add(privateBtn);

        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        typePanel.setOpaque(false);
        typePanel.add(publicBtn);
        typePanel.add(privateBtn);
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2;
        form.add(typePanel, gbc);

        // 비밀번호 필드
        JLabel pwLabel = new JLabel("비밀번호");
        pwLabel.setFont(labelFont);
        pwLabel.setForeground(Color.LIGHT_GRAY);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        form.add(pwLabel, gbc);

        JTextField pwField = new JTextField();
        pwField.setFont(fieldFont);
        pwField.setEnabled(false);
        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 2;
        form.add(pwField, gbc);

        // 비공개 선택 시 비밀번호 활성화
        privateBtn.addActionListener(e -> {
            pwField.setEnabled(true);
            pwLabel.setForeground(Color.WHITE);
        });
        publicBtn.addActionListener(e -> {
            pwField.setEnabled(false);
            pwField.setText("");
            pwLabel.setForeground(Color.LIGHT_GRAY);
        });

        add(form, BorderLayout.CENTER);

        // 하단 버튼
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        btnPanel.setBackground(new Color(30, 30, 60));

        JButton createBtn = new JButton("🏠 방 만들기");
        createBtn.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        createBtn.setPreferredSize(new Dimension(180, 55));
        createBtn.setBackground(new Color(34, 139, 34));
        createBtn.setForeground(Color.WHITE);
        createBtn.setFocusPainted(false);
        createBtn.setBorderPainted(false);

        JButton backBtn = new JButton("← 돌아가기");
        backBtn.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        backBtn.setPreferredSize(new Dimension(150, 55));
        backBtn.setBackground(new Color(80, 80, 80));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setBorderPainted(false);

        createBtn.addActionListener(e -> {
            String rName = nameField.getText().trim();
            if (rName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "방 이름을 입력해주세요.", "알림", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int maxP = maxCombo.getSelectedIndex() + 2; // 2 or 3
            String pw = privateBtn.isSelected() ? pwField.getText().trim() : "";

            if (privateBtn.isSelected() && pw.isEmpty()) {
                JOptionPane.showMessageDialog(this, "비밀번호를 입력해주세요.", "알림", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (client == null || !client.isConnected()) {
                JOptionPane.showMessageDialog(this, "서버에 연결되어 있지 않습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }
            client.send("CREATE_ROOM|" + rName + "|" + maxP + "|" + pw);
        });

        backBtn.addActionListener(e -> {
            if (client != null) client.disconnect();
            dispose();
            homeReference.setVisible(true);
        });

        btnPanel.add(createBtn);
        btnPanel.add(backBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

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
            case "ROOM_CREATED":
                // ROOM_CREATED|code|name|maxPlayers
                dispose();
                new WaitingRoomScreen(playerName, coins, homeReference, client, parts[1], parts[2]);
                break;

            case "ROOM_STATE":
                dispose();
                new WaitingRoomScreen(playerName, coins, homeReference, client, parts[1], parts[2]);
                break;

            case "JOIN_FAIL":
                JOptionPane.showMessageDialog(this, parts[1], "오류", JOptionPane.ERROR_MESSAGE);
                break;
        }
    }
}