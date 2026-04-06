package project_2403;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * 멀티플레이 게임 화면
 * 서버로부터 받은 메시지를 기반으로 UI를 업데이트합니다.
 */
public class MultiGameScreen extends JFrame {

    private final String playerName;
    private final int initialCoins;
    private final Home homeReference;
    private final MultiplayerClient client;

    // ── 게임 상태 ──
    private List<String> playerNames = new ArrayList<>();
    private int[] personalCards;        // 내 3장 카드
    private int[] sharedCards = new int[4]; // 공유 카드 (-1: 미공개)
    private boolean[] folded;
    private int myIndex = -1;
    private int pot = 0;
    private int currentRoundBet = 0;
    private int round = 1;
    private boolean myTurn = false;
    private int selectedCardIndex = -1; // 공개할 카드 선택
    private boolean revealPhase = true; // true: 카드 공개 단계, false: 베팅 단계

    // ── UI ──
    private JLabel statusLabel;
    private JLabel potLabel;
    private JLabel roundLabel;
    private JPanel personalCardPanel;
    private JPanel sharedCardPanel;
    private JPanel opponentPanel;
    private JButton revealBtn;
    private JButton callBtn;
    private JButton foldBtn;
    private List<JLabel> personalCardLabels = new ArrayList<>();
    private List<JLabel> sharedCardLabels = new ArrayList<>();

    // 상대방 정보 레이블: playerIndex -> label
    private Map<Integer, JLabel> opponentLabels = new HashMap<>();

    public MultiGameScreen(String playerName, int initialCoins, Home home, MultiplayerClient client) {
        this.playerName = playerName;
        this.initialCoins = initialCoins;
        this.homeReference = home;
        this.client = client;

        Arrays.fill(sharedCards, -1);

        setTitle("레인보우 홀덤 - 멀티플레이");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setLayout(new BorderLayout(0, 0));

        buildUI();
        client.setMessageListener(this::handleServerMessage);
        setVisible(true);
    }

    // ──────────────────────────────────────────────
    // UI 구성
    // ──────────────────────────────────────────────
    private void buildUI() {
        // 상단: 상태바
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(20, 20, 50));
        topBar.setPreferredSize(new Dimension(100, 75));

        statusLabel = new JLabel("게임 시작 중...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        statusLabel.setForeground(Color.WHITE);

        roundLabel = new JLabel("라운드: 1", SwingConstants.LEFT);
        roundLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        roundLabel.setForeground(new Color(255, 215, 0));
        roundLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        potLabel = new JLabel("판돈: 0", SwingConstants.RIGHT);
        potLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        potLabel.setForeground(new Color(100, 255, 100));
        potLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));

        topBar.add(roundLabel, BorderLayout.WEST);
        topBar.add(statusLabel, BorderLayout.CENTER);
        topBar.add(potLabel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // 중앙: 게임 테이블
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(new Color(15, 75, 15));

        // 공유 카드 (중앙)
        sharedCardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        sharedCardPanel.setOpaque(false);
        sharedCardPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 0), 2),
                "공유 카드", 0, 0,
                new Font("맑은 고딕", Font.BOLD, 16), Color.WHITE));
        tablePanel.add(sharedCardPanel, BorderLayout.CENTER);

        // 상대방 패널 (상단)
        opponentPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 15));
        opponentPanel.setOpaque(false);
        tablePanel.add(opponentPanel, BorderLayout.NORTH);

        add(tablePanel, BorderLayout.CENTER);

        // 하단: 내 카드 + 버튼
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(25, 25, 25));
        bottomPanel.setPreferredSize(new Dimension(100, 240));

        // 내 카드
        personalCardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 15));
        personalCardPanel.setOpaque(false);
        personalCardPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 200, 255), 2),
                "내 카드", 0, 0,
                new Font("맑은 고딕", Font.BOLD, 16), Color.WHITE));
        bottomPanel.add(personalCardPanel, BorderLayout.CENTER);

        // 버튼 패널
        JPanel btnPanel = new JPanel(new GridLayout(3, 1, 8, 8));
        btnPanel.setBackground(new Color(25, 25, 25));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        revealBtn = makeBtn("공개", new Color(30, 120, 200));
        callBtn   = makeBtn("콜 (Call)", new Color(34, 139, 34));
        foldBtn   = makeBtn("다이 (Fold)", new Color(180, 50, 50));

        revealBtn.addActionListener(e -> handleReveal());
        callBtn.addActionListener(e -> sendBet("CALL"));
        foldBtn.addActionListener(e -> sendBet("FOLD"));

        revealBtn.setEnabled(false);
        callBtn.setEnabled(false);
        foldBtn.setEnabled(false);

        btnPanel.add(revealBtn);
        btnPanel.add(callBtn);
        btnPanel.add(foldBtn);
        bottomPanel.add(btnPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    // ──────────────────────────────────────────────
    // 서버 메시지 처리
    // ──────────────────────────────────────────────
    private void handleServerMessage(String msg) {
        String[] p = msg.split("\\|");
        switch (p[0]) {

            case "GAME_START":
                // 플레이어 수만 알림
                break;

            case "YOUR_CARDS":
                // YOUR_CARDS|v1,v2,v3
                String[] vals = p[1].split(",");
                personalCards = new int[]{
                    Integer.parseInt(vals[0]),
                    Integer.parseInt(vals[1]),
                    Integer.parseInt(vals[2])
                };
                drawPersonalCards();
                break;

            case "SHARED_CARD_COUNT":
                // 공유 카드 뒷면 4장 표시
                drawSharedCardBacks(Integer.parseInt(p[1]));
                break;

            case "ROUND":
                round = Integer.parseInt(p[1]);
                roundLabel.setText("라운드: " + round);
                break;

            case "REQUEST_REVEAL":
                revealPhase = true;
                statusLabel.setText(p[1]);
                revealBtn.setEnabled(true);
                callBtn.setEnabled(false);
                foldBtn.setEnabled(false);
                break;

            case "REVEALED":
                // REVEALED|playerIdx|cardIndex|value|playerName
                onCardRevealed(Integer.parseInt(p[1]), Integer.parseInt(p[2]),
                        Integer.parseInt(p[3]), p[4]);
                break;

            case "BETTING_START":
                revealPhase = false;
                statusLabel.setText("베팅 라운드 시작!");
                revealBtn.setEnabled(false);
                break;

            case "YOUR_TURN":
                // YOUR_TURN|playerIdx|currentRoundBet|pot
                int turnIdx = Integer.parseInt(p[1]);
                pot = Integer.parseInt(p[3]);
                potLabel.setText("판돈: " + pot);
                highlightCurrentBettor(turnIdx);
                break;

            case "MUST_BET":
                // MUST_BET|minBet|currentRoundBet — 내 차례
                myTurn = true;
                currentRoundBet = Integer.parseInt(p[2]);
                callBtn.setEnabled(true);
                foldBtn.setEnabled(true);
                statusLabel.setText("당신의 베팅 차례입니다! (최소: " + p[1] + ")");
                break;

            case "PLAYER_ACTION":
                // PLAYER_ACTION|playerIdx|action|name|amount(옵션)|pot(옵션)
                onPlayerAction(p);
                break;

            case "SHARED_REVEALED":
                // SHARED_REVEALED|index|value|round
                int si = Integer.parseInt(p[1]);
                int sv = Integer.parseInt(p[2]);
                sharedCards[si] = sv;
                updateSharedCardUI(si, sv);
                statusLabel.setText("라운드 " + p[3] + ": 공유 카드 공개!");
                break;

            case "GAME_RESULT":
                showResult(p);
                break;

            case "PLAYER_LEFT":
                statusLabel.setText("⚠ " + p[1] + "님이 게임을 떠났습니다.");
                break;

            case "DISCONNECTED":
                JOptionPane.showMessageDialog(this, "서버와의 연결이 끊겼습니다.", "연결 오류", JOptionPane.ERROR_MESSAGE);
                returnHome();
                break;
        }
    }

    // ──────────────────────────────────────────────
    // UI 업데이트 메서드들
    // ──────────────────────────────────────────────
    private void drawPersonalCards() {
        personalCardPanel.removeAll();
        personalCardLabels.clear();
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            JLabel lbl = new JLabel(cardText(personalCards[i]));
            lbl.setFont(new Font("맑은 고딕", Font.BOLD, 36));
            lbl.setForeground(Color.WHITE);
            lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(100, 200, 255), 2),
                    BorderFactory.createEmptyBorder(15, 20, 15, 20)));
            lbl.setBackground(new Color(40, 40, 90));
            lbl.setOpaque(true);
            lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            lbl.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (!revealBtn.isEnabled()) return;
                    selectedCardIndex = idx;
                    highlightSelectedCard();
                }
            });
            personalCardLabels.add(lbl);
            personalCardPanel.add(lbl);
        }
        personalCardPanel.revalidate();
        personalCardPanel.repaint();
    }

    private void highlightSelectedCard() {
        for (int i = 0; i < personalCardLabels.size(); i++) {
            JLabel l = personalCardLabels.get(i);
            if (i == selectedCardIndex) {
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.YELLOW, 4),
                        BorderFactory.createEmptyBorder(13, 18, 13, 18)));
                l.setBackground(new Color(70, 70, 0));
            } else {
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(100, 200, 255), 2),
                        BorderFactory.createEmptyBorder(15, 20, 15, 20)));
                l.setBackground(new Color(40, 40, 90));
            }
        }
    }

    private void drawSharedCardBacks(int count) {
        sharedCardPanel.removeAll();
        sharedCardLabels.clear();
        for (int i = 0; i < count; i++) {
            JLabel lbl = new JLabel("?");
            lbl.setFont(new Font("맑은 고딕", Font.BOLD, 36));
            lbl.setForeground(Color.LIGHT_GRAY);
            lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.GRAY, 2),
                    BorderFactory.createEmptyBorder(15, 20, 15, 20)));
            lbl.setBackground(new Color(40, 50, 40));
            lbl.setOpaque(true);
            sharedCardLabels.add(lbl);
            sharedCardPanel.add(lbl);
        }
        sharedCardPanel.revalidate();
        sharedCardPanel.repaint();
    }

    private void updateSharedCardUI(int index, int value) {
        if (index < sharedCardLabels.size()) {
            JLabel lbl = sharedCardLabels.get(index);
            lbl.setText(cardText(value));
            lbl.setForeground(Color.YELLOW);
            lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.YELLOW, 3),
                    BorderFactory.createEmptyBorder(14, 19, 14, 19)));
        }
    }

    private void onCardRevealed(int playerIdx, int cardIndex, int value, String pName) {
        if (pName.equals(playerName)) {
            // 내 카드 공개 표시 (회색)
            if (cardIndex < personalCardLabels.size()) {
                personalCardLabels.get(cardIndex).setBorder(
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(Color.GRAY, 3),
                                BorderFactory.createEmptyBorder(14, 19, 14, 19)));
            }
        } else {
            // 상대방 카드 공개
            JLabel lbl = opponentLabels.get(playerIdx);
            if (lbl != null) {
                String cur = lbl.getText();
                lbl.setText("<html>" + cur + "<br>공개 카드: <b>" + value + "</b></html>");
            }
        }
        statusLabel.setText(pName + "님이 카드를 공개했습니다.");
    }

    private void highlightCurrentBettor(int idx) {
        // 상대방 레이블 강조
        for (Map.Entry<Integer, JLabel> entry : opponentLabels.entrySet()) {
            Color bg = entry.getKey() == idx ?
                    new Color(70, 70, 0) : new Color(40, 40, 80);
            entry.getValue().setBackground(bg);
        }
        if (idx != myIndex) {
            myTurn = false;
            callBtn.setEnabled(false);
            foldBtn.setEnabled(false);
            if (!playerNames.isEmpty() && idx < playerNames.size()) {
                statusLabel.setText(playerNames.get(idx) + "님의 베팅 차례...");
            }
        }
    }

    private void onPlayerAction(String[] p) {
        // PLAYER_ACTION|playerIdx|action|name|amount|pot
        int idx = Integer.parseInt(p[1]);
        String action = p[2];
        String name = p[3];
        if (p.length > 5) pot = Integer.parseInt(p[5]);
        potLabel.setText("판돈: " + pot);

        String actText = action.equals("FOLD") ? "다이(FOLD)" : "콜(CALL) +" + (p.length > 4 ? p[4] : "");
        statusLabel.setText(name + "님: " + actText);

        if (action.equals("FOLD")) {
            JLabel lbl = opponentLabels.get(idx);
            if (lbl != null) {
                lbl.setText("<html>" + lbl.getText() + "<br><font color='red'>FOLD</font></html>");
                lbl.setBackground(new Color(60, 20, 20));
            }
        }
        myTurn = false;
        callBtn.setEnabled(false);
        foldBtn.setEnabled(false);
    }

    private void showResult(String[] p) {
        // GAME_RESULT|winnerIdx|pot|idx,score,chips|...
        int winnerIdx = Integer.parseInt(p[1]);
        int finalPot = Integer.parseInt(p[2]);

        StringBuilder sb = new StringBuilder();
        sb.append("🏆 게임 종료!\n\n");

        if (winnerIdx >= 0 && winnerIdx < playerNames.size()) {
            sb.append("승자: ").append(playerNames.get(winnerIdx)).append("\n");
            sb.append("판돈: ").append(finalPot).append(" 코인\n\n");
        }
        sb.append("─── 최종 결과 ───\n");
        for (int i = 3; i < p.length; i++) {
            String[] info = p[i].split(",");
            int idx = Integer.parseInt(info[0]);
            String scoreStr = info[1].equals("-1") ? "FOLD" : info[1] + "점";
            String name = (idx < playerNames.size()) ? playerNames.get(idx) : "플레이어" + idx;
            sb.append(name).append(": ").append(scoreStr)
              .append(" (코인: ").append(info[2]).append(")\n");
        }

        boolean won = (winnerIdx == myIndex);

        JOptionPane.showMessageDialog(this, sb.toString(), "게임 결과", JOptionPane.INFORMATION_MESSAGE);

        int myFinalCoins = initialCoins;
        for (int i = 3; i < p.length; i++) {
            String[] info = p[i].split(",");
            if (Integer.parseInt(info[0]) == myIndex) {
                myFinalCoins = Integer.parseInt(info[2]);
            }
        }
        homeReference.updatePlayerData(won, myFinalCoins);
        returnHome();
    }

    // ──────────────────────────────────────────────
    // 액션 메서드
    // ──────────────────────────────────────────────
    private void handleReveal() {
        if (selectedCardIndex == -1) {
            JOptionPane.showMessageDialog(this, "공개할 카드를 먼저 선택해주세요!", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }
        revealBtn.setEnabled(false);
        client.send("REVEAL_CARD|" + selectedCardIndex);
        selectedCardIndex = -1;
    }

    private void sendBet(String action) {
        if (!myTurn) return;
        myTurn = false;
        callBtn.setEnabled(false);
        foldBtn.setEnabled(false);
        client.send("BET_ACTION|" + action);
    }

    private void returnHome() {
        client.disconnect();
        dispose();
        homeReference.setVisible(true);
    }

    // ──────────────────────────────────────────────
    // 상대방 패널 초기화 (ROOM_STATE or GAME_START 로 playerNames 확정 후 호출)
    // ──────────────────────────────────────────────
    public void initOpponentPanels(List<String> names) {
        this.playerNames = names;
        for (int i = 0; i < names.size(); i++) {
            if (names.get(i).equals(playerName)) {
                myIndex = i;
                continue;
            }
            JLabel lbl = new JLabel("<html><b>" + names.get(i) + "</b></html>");
            lbl.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
            lbl.setForeground(Color.WHITE);
            lbl.setBackground(new Color(40, 40, 80));
            lbl.setOpaque(true);
            lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(100, 100, 180), 2),
                    BorderFactory.createEmptyBorder(10, 18, 10, 18)));
            opponentLabels.put(i, lbl);
            opponentPanel.add(lbl);
        }
        opponentPanel.revalidate();
        opponentPanel.repaint();
    }

    private JButton makeBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        return btn;
    }

    private String cardText(int value) {
        return "[" + value + "]";
    }
}