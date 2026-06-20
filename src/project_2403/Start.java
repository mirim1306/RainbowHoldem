package project_2403;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Start extends JFrame {
    private JTextField nameField;
    private JPasswordField passwordField;
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JButton loginButton;
    private JButton registerButton;
    private boolean isLoginMode = true; // true: 로그인, false: 회원가입

    public Start() {
        setTitle("레인보우 홀덤");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        
        // 메인 패널 설정
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                
                // 그라디언트 배경
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(25, 25, 112),  // 미드나잇 블루
                    0, getHeight(), new Color(72, 61, 139)  // 다크 슬레이트 블루
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // 카드 장식 그리기
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                drawCardDecorations(g2d);
            }
            
            private void drawCardDecorations(Graphics2D g2d) {
                // 반투명 카드 모양 장식들
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
                
                // 좌측 상단
                drawCard(g2d, 50, 50, 80, 120, new Color(255, 255, 255));
                drawCard(g2d, 150, 100, 80, 120, new Color(255, 215, 0));
                
                // 우측 상단
                drawCard(g2d, getWidth() - 150, 50, 80, 120, new Color(255, 0, 0));
                drawCard(g2d, getWidth() - 250, 100, 80, 120, new Color(0, 255, 0));
                
                // 좌측 하단
                drawCard(g2d, 100, getHeight() - 200, 80, 120, new Color(0, 191, 255));
                
                // 우측 하단
                drawCard(g2d, getWidth() - 200, getHeight() - 200, 80, 120, new Color(255, 105, 180));
                
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
            
            private void drawCard(Graphics2D g2d, int x, int y, int width, int height, Color color) {
                g2d.setColor(color);
                g2d.fillRoundRect(x, y, width, height, 10, 10);
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(x, y, width, height, 10, 10);
            }
        };
        mainPanel.setLayout(new GridBagLayout());
        
        // 중앙 컨테이너
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        
        // 제목
        titleLabel = new JLabel("RAINBOW HOLDEM");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 72));
        titleLabel.setForeground(new Color(255, 215, 0)); // 골드
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 부제목
        subtitleLabel = new JLabel("카드로 승부하는 전략 게임");
        subtitleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 32));
        subtitleLabel.setForeground(Color.WHITE);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 간격
        centerPanel.add(Box.createVerticalStrut(80));
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(subtitleLabel);
        centerPanel.add(Box.createVerticalStrut(60));
        
        // 입력 패널
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setOpaque(false);
        
        // 아이디 입력
        JLabel nameLabel = new JLabel("아이디");
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        nameField = new JTextField(20);
        nameField.setFont(new Font("맑은 고딕", Font.PLAIN, 24));
        nameField.setMaximumSize(new Dimension(400, 50));
        nameField.setHorizontalAlignment(JTextField.CENTER);
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0), 3),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        inputPanel.add(nameLabel);
        inputPanel.add(Box.createVerticalStrut(15));
        inputPanel.add(nameField);
        inputPanel.add(Box.createVerticalStrut(30));
        
        // 비밀번호 입력
        JLabel passwordLabel = new JLabel("비밀번호");
        passwordLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        passwordLabel.setForeground(Color.WHITE);
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("맑은 고딕", Font.PLAIN, 24));
        passwordField.setMaximumSize(new Dimension(400, 50));
        passwordField.setHorizontalAlignment(JTextField.CENTER);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0), 3),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        // Enter 키로 로그인/회원가입
        passwordField.addActionListener(e -> handleAction());
        
        inputPanel.add(passwordLabel);
        inputPanel.add(Box.createVerticalStrut(15));
        inputPanel.add(passwordField);
        inputPanel.add(Box.createVerticalStrut(40));
        
        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);
        
        loginButton = createStyledButton("로그인", new Color(34, 139, 34));
        registerButton = createStyledButton("회원가입", new Color(30, 144, 255));
        JButton exitButton = createStyledButton("종료", new Color(220, 20, 60));
        
        loginButton.addActionListener(e -> {
            isLoginMode = true;
            handleAction();
        });
        
        registerButton.addActionListener(e -> {
            isLoginMode = false;
            handleAction();
        });
        
        exitButton.addActionListener(e -> System.exit(0));
        
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        buttonPanel.add(exitButton);
        
        inputPanel.add(buttonPanel);
        centerPanel.add(inputPanel);
        
        mainPanel.add(centerPanel);
        add(mainPanel);
        
        setVisible(true);
        nameField.requestFocusInWindow();
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        button.setPreferredSize(new Dimension(180, 60));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 호버 효과
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private void handleAction() {
        String username = nameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        // 기본 입력 체크
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "아이디와 비밀번호를 입력해주세요!", 
                "입력 오류", 
                JOptionPane.WARNING_MESSAGE);
            nameField.requestFocusInWindow();
            return;
        }
        
        // 아이디 유효성 검사: 특수문자와 공백 불가
        if (!isValidUsername(username)) {
            JOptionPane.showMessageDialog(this,
                "아이디는 영문자, 숫자, 한글만 사용 가능합니다.\n(특수문자, 공백 사용 불가)",
                "입력 오류",
                JOptionPane.WARNING_MESSAGE);
            nameField.requestFocusInWindow();
            return;
        }
        
        // 비밀번호 유효성 검사: 공백 불가
        if (!isValidPassword(password)) {
            JOptionPane.showMessageDialog(this,
                "비밀번호에는 공백을 사용할 수 없습니다.",
                "입력 오류",
                JOptionPane.WARNING_MESSAGE);
            passwordField.requestFocusInWindow();
            return;
        }
        
        // 비밀번호 길이 체크 (회원가입 시)
        if (!isLoginMode && password.length() < 4) {
            JOptionPane.showMessageDialog(this,
                "비밀번호는 최소 4자 이상이어야 합니다.",
                "입력 오류",
                JOptionPane.WARNING_MESSAGE);
            passwordField.requestFocusInWindow();
            return;
        }
        
        if (isLoginMode) {
            // 로그인 처리
            handleLogin(username, password);
        } else {
            // 회원가입 처리
            handleRegister(username, password);
        }
    }

    // 아이디 유효성 검사 메서드 추가
    private boolean isValidUsername(String username) {
        // 영문자, 숫자, 한글만 허용 (특수문자, 공백 불가)
        // 정규식: ^[a-zA-Z0-9가-힣]+$
        return username.matches("^[a-zA-Z0-9가-힣]+$");
    }

    // 비밀번호 유효성 검사 메서드 추가
    private boolean isValidPassword(String password) {
        // 공백이 포함되어 있으면 false
        return !password.contains(" ");
    }
    
    private void handleLogin(String username, String password) {
        if (DataManager.loginPlayer(username, password)) {
            // 플레이어 데이터 로드
            DatabaseManager.PlayerData data = DataManager.loadPlayerData(username);
            
            JOptionPane.showMessageDialog(this,
                "환영합니다, " + username + "님!",
                "로그인 성공",
                JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
            SwingUtilities.invokeLater(() -> {
                new Home(username, data.wins, data.losses, data.coins);
            });
        } else {
            JOptionPane.showMessageDialog(this,
                "아이디 또는 비밀번호가 올바르지 않습니다.",
                "로그인 실패",
                JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            passwordField.requestFocusInWindow();
        }
    }
    
    private void handleRegister(String username, String password) {
        if (DataManager.registerPlayer(username, password)) {
            JOptionPane.showMessageDialog(this,
                "회원가입이 완료되었습니다!\n이제 로그인해주세요.",
                "회원가입 성공",
                JOptionPane.INFORMATION_MESSAGE);
            
            // 회원가입 후 로그인 모드로 전환
            isLoginMode = true;
            subtitleLabel.setText("로그인");
            passwordField.setText("");
            nameField.requestFocusInWindow();
        } else {
            JOptionPane.showMessageDialog(this,
                "이미 존재하는 아이디이거나 저장에 실패했습니다.",
                "회원가입 실패",
                JOptionPane.ERROR_MESSAGE);
            nameField.requestFocusInWindow();
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Start();
        });
    }
}