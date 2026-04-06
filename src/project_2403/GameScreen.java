package project_2403;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class GameScreen extends JFrame {

    private String playerName;
    private int totalPlayers;
    private int initialChips;
    private Home homeReference;
    
    // 게임 로직 필드
    private Game game;
    private Player userPlayer;
    
    // UI 컴포넌트 필드
    private JLabel gameStatusLabel;
    private JPanel sharedCardsPanel;
    private JPanel personalCardsPanel;
    private JPanel bettingPanel;
    private List<JLabel> personalCardLabels;
    private JLabel userChipLabel;
    private JLabel userBetLabel;
    private List<JLabel> aiInfoLabels;
    private List<JPanel> aiCardPanels;

    private JButton revealButton;
    private JButton callButton;
    private JButton foldButton;

    private int selectedCardIndex = -1;
    private List<JLabel> sharedCardLabels; // 공유 카드 UI 요소를 추적
    private static final int MIN_BET = 10;
    private static final int TURN_DELAY = 2000; // 2초 딜레이

    public GameScreen(String playerName, int totalPlayers, int initialChips, Home home) {  // home 파라미터 추가
        this.playerName = playerName;
        this.totalPlayers = totalPlayers;
        this.initialChips = initialChips;
        this.homeReference = home;
        
        // 1. 게임 로직 초기화: userPlayer 필드를 먼저 채웁니다.
        initializeGameLogic();
        
        // 2. 프레임 설정
        setTitle("레인보우 홀덤 - " + this.playerName + "님 (" + this.totalPlayers + "인 플레이)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setLayout(new BorderLayout());

        // ======================== 3. UI 컴포넌트 생성 및 배치 ========================
        
        // 상단: 게임 상태 및 정보
        gameStatusLabel = new JLabel("게임을 시작합니다! 카드를 선택해주세요.", SwingConstants.CENTER);
        gameStatusLabel.setFont(new Font("맑은 고딕", Font.BOLD, 30));
        gameStatusLabel.setPreferredSize(new Dimension(100, 80));
        gameStatusLabel.setBackground(new Color(50, 50, 50));
        gameStatusLabel.setForeground(Color.WHITE);
        gameStatusLabel.setOpaque(true);
        add(gameStatusLabel, BorderLayout.NORTH);
        
        // 중앙: 공유 카드 및 게임 판
        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setBackground(new Color(15, 75, 15));
        
        sharedCardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        sharedCardsPanel.setBorder(BorderFactory.createTitledBorder("공유 카드"));
        sharedCardsPanel.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(50, 0, 50, 0);
        centerContainer.add(sharedCardsPanel, gbc);
        add(centerContainer, BorderLayout.CENTER);

        // 하단 컨테이너
        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setPreferredSize(new Dimension(100, 300));
        bottomContainer.setBackground(new Color(30, 30, 30));

        // 사용자 칩/베팅 정보 표시 영역 (이제 userPlayer가 null이 아닙니다)
        JPanel userInfoPanel = new JPanel(new GridLayout(2, 1));
        userInfoPanel.setOpaque(false);
        
        userChipLabel = new JLabel(); // 초기화는 updateUI에서
        userBetLabel = new JLabel(); 
        
        Font infoFont = new Font("맑은 고딕", Font.BOLD, 18);
        userChipLabel.setFont(infoFont);
        userBetLabel.setFont(infoFont);
        userChipLabel.setForeground(Color.WHITE);
        userBetLabel.setForeground(Color.WHITE);
        
        userInfoPanel.add(userChipLabel);
        userInfoPanel.add(userBetLabel);
        bottomContainer.add(userInfoPanel, BorderLayout.WEST); 
        
        // 플레이어 개인 카드 패널
        personalCardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        personalCardsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        personalCardsPanel.setOpaque(false);
        
        // 베팅 버튼 패널
        bettingPanel = createBettingPanel();
        
        bottomContainer.add(personalCardsPanel, BorderLayout.CENTER);
        bottomContainer.add(bettingPanel, BorderLayout.EAST);
        add(bottomContainer, BorderLayout.SOUTH);
        
        // 4. 주변: AI 플레이어 정보 (AI 패널 생성)
        initializeAIPanels();

        // 5. 최종 데이터 기반 UI 업데이트 및 카드 표시
        updateUI(); // userChipLabel, userBetLabel 등 초기값 설정
        displaySharedCardBacks();
        displayPersonalCards();

        gameStatusLabel.setText("카드를 한 장 선택하고 '공개' 버튼을 누르세요!");
        
        setVisible(true);
    }
    
    // 게임 로직만 초기화하는 메서드 (가장 먼저 실행)
    private void initializeGameLogic() {
        List<String> playerNames = new ArrayList<>();
        playerNames.add(this.playerName);
        for (int i = 1; i < totalPlayers; i++) {
            playerNames.add("컴퓨터" + i);
        }
        
        this.game = new Game(playerNames, this.initialChips); 
        this.userPlayer = this.game.getPlayers().get(0);
        this.game.setupGame();
        
        // AI 정보 레이블 리스트 초기화
        aiInfoLabels = new ArrayList<>();
    }
    
    // AI 패널 UI만 생성하는 메서드
    private void initializeAIPanels() {
        int aiCount = totalPlayers - 1;
        List<Player> players = game.getPlayers();
        aiCardPanels = new ArrayList<>();
        
        if (aiCount >= 1) { // 컴퓨터 1 (AI 1)
            add(createAIPlayerPanel(players.get(1)), BorderLayout.WEST);
        }
        
        if (aiCount >= 2) { // 컴퓨터 2 (AI 2)
            add(createAIPlayerPanel(players.get(2)), BorderLayout.EAST);
        }
    }
    
    // 베팅 버튼을 만드는 보조 메서드
    private JPanel createBettingPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        
        // 베팅 버튼 패널 (기존 코드)
        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setOpaque(false);
        
        callButton = new JButton("콜 (Call)");
        foldButton = new JButton("다이 (Fold)");
        
        Font btnFont = new Font("맑은 고딕", Font.BOLD, 20);
        callButton.setFont(btnFont);
        foldButton.setFont(btnFont);

        callButton.addActionListener(e -> handleBetting("CALL"));
        foldButton.addActionListener(e -> handleBetting("FOLD"));
        
        callButton.setEnabled(false);
        foldButton.setEnabled(false);
        
        panel.add(callButton);
        panel.add(foldButton);
        
        container.add(panel, BorderLayout.CENTER);
        return container;
    }
    
    // AI 플레이어 정보를 표시하는 보조 메서드
    private JPanel createAIPlayerPanel(Player aiPlayer) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(150, 270)); 
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. 상태 및 칩/베팅 정보 레이블 (aiInfoLabels에 추가됨)
        JLabel aiStatusLabel = new JLabel("", SwingConstants.CENTER);
        aiStatusLabel.setForeground(Color.WHITE);
        aiStatusLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        aiStatusLabel.setVerticalAlignment(SwingConstants.TOP);
        aiInfoLabels.add(aiStatusLabel);
        
        // 2. 공개된 카드를 위한 패널: GridLayout으로 변경 (3행 1열)
        // 카드가 세로로 3장이 배열되도록 합니다.
        JPanel cardContainer = new JPanel(new GridLayout(3, 1, 0, 5)); // 3행 1열, 수직 간격 5
        cardContainer.setOpaque(false);
        cardContainer.setBorder(BorderFactory.createTitledBorder("공개 카드"));
        aiCardPanels.add(cardContainer); 
        
        // 3. 패널에 컴포넌트 추가
        panel.add(aiStatusLabel, BorderLayout.NORTH);
        panel.add(cardContainer, BorderLayout.CENTER); // 중앙에 카드 3장 컨테이너 배치
        
        return panel;
    }
    
    // 공유 카드 뒷면 표시 메서드 (Game 객체의 sharedCards를 사용하도록 추후 수정)
    private void displaySharedCardBacks() {
        sharedCardsPanel.removeAll();
        sharedCardLabels = new ArrayList<>(); // 리스트 초기화
        
        List<Card> sharedCards = game.getSharedCards();
        
        for (int i = 0; i < sharedCards.size(); i++) {
            String imagePath;
            
            // 첫 번째 카드만 앞면으로 공개, 나머지는 뒷면
            if (i == 0) {
                imagePath = "images/" + sharedCards.get(i).getValue() + ".png";
            } else {
                imagePath = "images/card_back.png";
            }
            
            JLabel cardLabel = new JLabel(new ImageIcon(imagePath));
            sharedCardLabels.add(cardLabel);
            sharedCardsPanel.add(cardLabel);
        }
        sharedCardsPanel.revalidate();
        sharedCardsPanel.repaint();
        
        // 초기 상태 메시지 업데이트
        gameStatusLabel.setText(game.getRound() + "번째 공유 카드 " + sharedCards.get(0).getValue() + "가 공개되었습니다. 개인 카드를 선택해주세요.");
    }
    
    // 개인 카드 표시 및 클릭 이벤트 추가 메서드 (Game 객체의 personalCards를 사용하도록 추후 수정)
    private void displayPersonalCards() {
        personalCardsPanel.removeAll();
        personalCardLabels = new ArrayList<>();
        
        List<Card> personalCards = userPlayer.getPersonalCards(); // 실제 개인 카드 가져오기
        
        for (int i = 0; i < personalCards.size(); i++) {
            Card card = personalCards.get(i);
            // 이미지 경로는 "images/5.png"처럼 실제 값 사용
            JLabel cardLabel = new JLabel(new ImageIcon("images/" + card.getValue() + ".png"));
            cardLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            
            final int cardIndex = i;
            cardLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectCard(cardLabel, cardIndex);
                }
            });
            
            personalCardLabels.add(cardLabel);
            personalCardsPanel.add(cardLabel);
        }
        
        // 공개 버튼 추가 (개인 카드 선택 후 누르는 버튼)
        revealButton = new JButton("선택 카드 공개"); // 필드에 연결
        revealButton.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        revealButton.setPreferredSize(new Dimension(200, 50));
        
        revealButton.addActionListener(e -> {
            if (selectedCardIndex != -1) {
                handleCardReveal(); // 새로운 카드 공개 처리 메서드 호출
            } else {
                JOptionPane.showMessageDialog(this, "먼저 공개할 카드를 선택해주세요.", "알림", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        // 버튼을 개인 카드 패널에 추가
        personalCardsPanel.add(Box.createHorizontalStrut(50));
        personalCardsPanel.add(revealButton);
        
        personalCardsPanel.revalidate();
        personalCardsPanel.repaint();
    }

    //사용자 카드를 공개하고, 다음 라운드(베팅)를 준비합니다.
    private void handleCardReveal() {
        // 1. 이미 1장의 카드가 공개되었다면 추가 공개 불가
        if (userPlayer.getRevealedCount() >= 1) { 
            gameStatusLabel.setText("개인 카드는 이미 한 장 공개되었습니다. 베팅을 완료하세요.");
            revealButton.setEnabled(false); // 버튼 비활성화 유지
            // 선택 상태 초기화
            selectedCardIndex = -1;
            // 이전에 선택된 카드 테두리 초기화 로직도 필요
            for (JLabel label : personalCardLabels) {
                // 공개된 카드의 BLUE 테두리는 유지
                if (!userPlayer.getRevealedCardIndices().contains(personalCardLabels.indexOf(label))) {
                    label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                }
            }
            return; 
        }
        
     // 1. 사용자 카드 공개
        Card userRevealedCard = userPlayer.revealPersonalCard(selectedCardIndex); 
        
        // 2. AI 카드 공개 시뮬레이션
        for (int i = 1; i < game.getPlayers().size(); i++) {
            Player aiPlayer = game.getPlayers().get(i);
            if (!aiPlayer.hasFolded()) {
                aiPlayer.aiRevealCard(); // AI가 가장 낮은 카드를 공개
            }
        }
        
        // 3. 베팅 순서 결정 및 첫 번째 턴 진행
        game.determineStartingPlayerIndex(); // 가장 낮은 카드 가진 플레이어 결정
        
        gameStatusLabel.setText("개인 카드가 공개되었습니다. 베팅을 시작합니다!");
        revealButton.setEnabled(false); 
        
        // 베팅 순서에 따라 턴을 진행
        processNextTurn(); // 베팅 순서의 첫 번째 플레이어에게 턴을 넘김
        
        // 1장 공개 후, 공개 버튼 비활성화 (요청 사항 반영)
        revealButton.setEnabled(false);
        
        // 2. UI 업데이트: 카드를 패널에서 제거하지 않고, 시각적으로 '공개됨'을 표시
        JLabel revealedLabel = personalCardLabels.get(selectedCardIndex);
        revealedLabel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 5)); // 공개된 카드 시각적 표시
        
        // 3. 베팅 단계 시작
        gameStatusLabel.setText("개인 카드가 공개되었습니다. 베팅을 시작하세요! (최소 " + MIN_BET + "코인)");
        revealButton.setEnabled(false); // 카드 공개는 라운드당 1회만
        
        // 개인 카드가 공개된 후 베팅 버튼 활성화
        callButton.setEnabled(true);
        foldButton.setEnabled(true);
        
        // 선택 상태 초기화 (다음 베팅 라운드까지는 카드 선택 불가능)
        selectedCardIndex = -1; 
        
        updateUI();
    }
    
    // 턴을 진행하는 핵심 메서드
    private void processNextTurn() {
        Player nextPlayer = game.getNextBettingPlayer();

        if (nextPlayer == null) {
            startNextTurnWithDelay();
            return;
        }

        // 2. 다음 플레이어가 사용자일 경우
        if (!nextPlayer.isAI()) {
        	final int requiredCallAmount = game.getCurrentRoundBet() - userPlayer.getCurrentBetAmount();
            gameStatusLabel.setText(nextPlayer.getName() + "님, 베팅할 차례입니다. (콜: " + requiredCallAmount + " 코인)");
            callButton.setEnabled(true);
            foldButton.setEnabled(true);
        } 
        // 3. 다음 플레이어가 AI일 경우
        else {
        	gameStatusLabel.setText(nextPlayer.getName() + " (AI)의 턴입니다...");
            callButton.setEnabled(false);
            foldButton.setEnabled(false);
            
            // AI 턴을 잠시 지연시켜 UI 업데이트를 볼 수 있도록 Timer 사용
            Timer timer = new Timer(TURN_DELAY, e -> {
                processAIBet(nextPlayer);
                ((Timer)e.getSource()).stop(); 
                startNextTurnWithDelay(); // AI 액션 후 다음 턴으로 딜레이 적용
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    // AI 베팅 로직 처리
    private void processAIBet(Player aiPlayer) {
        final int requiredCallAmount = game.getCurrentRoundBet() - aiPlayer.getCurrentBetAmount();
        
        String decision = aiPlayer.aiDecideBet(requiredCallAmount); // AI 결정 로직 사용
        
        if (decision.equals("FOLD")) {
            aiPlayer.fold();
            gameStatusLabel.setText(aiPlayer.getName() + " (AI)가 다이(Fold)했습니다.");
        } else if (decision.equals("CALL")) {
            // 베팅 금액은 콜 금액으로 설정 (레이즈 전략은 단순화)
            int betAmount = requiredCallAmount > 0 ? requiredCallAmount : MIN_BET; 
            
            // 올인/칩 부족 처리는 Player.placeBet에서 담당
            if (aiPlayer.placeBet(betAmount)) {
                int actualBet = aiPlayer.getCurrentBetAmount(); 
                
                // 최고 베팅 금액 업데이트
                if (actualBet > game.getCurrentRoundBet()) {
                    game.setCurrentRoundBet(actualBet);
                }
                game.setPot(game.getPot() + actualBet);
                
                gameStatusLabel.setText(aiPlayer.getName() + " (AI)가 " + actualBet + " 코인을 베팅했습니다.");
            } else {
                // 칩 부족으로 베팅 실패 (placeBet에서 이미 fold 처리됨)
                gameStatusLabel.setText(aiPlayer.getName() + " (AI)가 칩 부족으로 다이(Fold) 처리됩니다.");
            }
        }
        updateUI();
    }

    //콜 또는 다이 버튼 클릭 시 베팅을 처리합니다.
    private void handleBetting(String action) {
        callButton.setEnabled(false); // 베팅 버튼 일시 비활성화
        foldButton.setEnabled(false);
        
        final int roundBet = game.getCurrentRoundBet(); // 현재 라운드 최고 베팅
        final int requiredCallAmount = roundBet - userPlayer.getCurrentBetAmount(); // 콜에 필요한 금액
        final int minBet = Math.max(MIN_BET, requiredCallAmount); // 최소 베팅은 10 또는 콜 금액 중 큰 값

        if (action.equals("FOLD")) {
            userPlayer.fold();
            gameStatusLabel.setText(userPlayer.getName() + "님이 다이(Fold)했습니다.");
            callButton.setEnabled(false);
            foldButton.setEnabled(false);
            updateUI();
            startNextTurnWithDelay(); // 딜레이 적용
        } else if (action.equals("CALL")) {
        	String betInput = JOptionPane.showInputDialog(this, 
                    "베팅 금액을 입력하세요 (최소 콜 금액: " + minBet + " 코인, 현재 보유: " + userPlayer.getChips() + " 코인):");
            
            if (betInput == null) return;
            
            try {
                int amount = Integer.parseInt(betInput.trim());
                
                // 2. 베팅 금액 유효성 검사 (최소 베팅 금액 10 충족 확인)
                if (amount < minBet && amount < userPlayer.getChips()) {
                    JOptionPane.showMessageDialog(this, "베팅 금액은 최소 " + minBet + " 코인 이상이어야 합니다.", "경고", JOptionPane.WARNING_MESSAGE);
                    callButton.setEnabled(true); // 다시 활성화
                    foldButton.setEnabled(true);
                    return;
                }
                
                // 3. 베팅 처리 (올인 로직은 Player 클래스에 유지)
                if (userPlayer.placeBet(amount)) {
                    int placedAmount = userPlayer.getCurrentBetAmount();
                    
                    // 현재 라운드 최고 베팅 업데이트
                    if (placedAmount > roundBet) {
                        game.setCurrentRoundBet(placedAmount);
                    }
                    game.setPot(game.getPot() + placedAmount);
                    
                    gameStatusLabel.setText(userPlayer.getName() + "님이 " + placedAmount + " 코인을 베팅했습니다.");
                    
                    updateUI();
                    startNextTurnWithDelay(); // 딜레이 적용
                } else {
                    // 칩 부족으로 베팅 실패 (다이 처리)
                    userPlayer.fold(); 
                    JOptionPane.showMessageDialog(this, "칩이 부족하여 다이(Fold) 처리됩니다.", "경고", JOptionPane.WARNING_MESSAGE);
                    updateUI();
                    startNextTurnWithDelay(); // 딜레이 적용
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "유효한 숫자를 입력하세요.", "경고", JOptionPane.WARNING_MESSAGE);
                callButton.setEnabled(true);
                foldButton.setEnabled(true);
            }
        }
    }
    
    // 턴 진행 전에 딜레이를 적용하는 메서드
    private void startNextTurnWithDelay() {
        // 1. 라운드 종료 판정
        if (game.isBettingRoundFinished()) {
            // 활성 플레이어가 1명만 남았는지 확인
            if (game.getActivePlayerCount() <= 1) {
                // 즉시 게임 종료 (한 명만 남음)
                gameStatusLabel.setText("게임 종료! 최종 결과를 확인합니다...");
                Timer endTimer = new Timer(TURN_DELAY, e -> {
                    showFinalResultWithOnlyOnePlayer();
                    ((Timer)e.getSource()).stop();
                });
                endTimer.setRepeats(false);
                endTimer.start();
                return;
            }
            
            // 2명 이상 남았으면 다음 라운드 진행
            gameStatusLabel.setText("베팅 라운드 종료. 잠시 후 공유 카드를 공개합니다...");
            
            // 딜레이 후 공유 카드 자동 공개
            Timer roundTimer = new Timer(TURN_DELAY * 2, e -> {
                revealNextSharedCard(); // 다음 공유 카드 공개
                ((Timer)e.getSource()).stop();
            });
            roundTimer.setRepeats(false);
            roundTimer.start();
            return;
        }
        
        // 2. 다음 턴 진행
        Timer turnTimer = new Timer(TURN_DELAY, e -> {
            processNextTurn();
            ((Timer)e.getSource()).stop();
        });
        turnTimer.setRepeats(false);
        turnTimer.start();
    }
    
    // 한 명만 남았을 때 최종 결과 표시
    private void showFinalResultWithOnlyOnePlayer() {
        Player winner = null;
        
        for (Player p : game.getPlayers()) {
            if (!p.hasFolded()) {
                winner = p;
                break;
            }
        }
        
        if (winner != null) {
            winner.addChips(game.getPot());
            boolean userWon = (winner == userPlayer);
            
            String resultMessage = winner.getName() + "님이 승리했습니다!\n" +
                                   "판돈 " + game.getPot() + " 코인을 획득합니다.\n\n" +
                                   "--- 최종 결과 ---";
            
            for (Player p : game.getPlayers()) {
                String status = p.hasFolded() ? "FOLD" : "승리";
                resultMessage += String.format("\n%s: %s (칩: %d)", p.getName(), status, p.getChips());
            }
            
            JOptionPane.showMessageDialog(this, resultMessage, "게임 종료", JOptionPane.INFORMATION_MESSAGE);
            
            // Home으로 게임 결과 전달
            if (homeReference != null) {
                homeReference.updatePlayerData(userWon, userPlayer.getChips());
            }
            
            dispose();
            if (homeReference != null) {
                homeReference.setVisible(true);
            } else {
                new Home(playerName, 0, 0, userPlayer.getChips());
            }
        }
    }
    
    // 카드를 선택했을 때 UI를 업데이트하는 메서드
    private void selectCard(JLabel clickedLabel, int index) {
        // 이전에 선택된 카드의 테두리를 초기화
        for (JLabel label : personalCardLabels) {
            label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        }
        
        // 새로 선택된 카드의 테두리를 하이라이트
        clickedLabel.setBorder(BorderFactory.createLineBorder(Color.RED, 4));
        selectedCardIndex = index;
        gameStatusLabel.setText(playerName + "님의 " + (index + 1) + "번 카드가 선택되었습니다. '선택 카드 공개' 버튼을 누르세요.");
    }
    
    //칩, 베팅 등 모든 플레이어의 정보를 업데이트합니다.
    private void updateUI() {
        final int MIN_BET = 10;
        
        // 1. 사용자 정보 업데이트
        userChipLabel.setText("칩: " + userPlayer.getChips());
        userBetLabel.setText("베팅: " + userPlayer.getCurrentBetAmount());
        
        // 2. AI 정보 업데이트 및 카드 이미지 표시
        List<Player> players = game.getPlayers();
        
        for (int i = 1; i < players.size(); i++) {
            Player ai = players.get(i);
            
            // A. AI 정보 레이블 업데이트 (이전 로직 유지)
            if (i - 1 < aiInfoLabels.size()) {
                JLabel aiLabel = aiInfoLabels.get(i - 1);
                String status = ai.hasFolded() ? " (FOLD)" : "";
                String infoText = "<html>" + ai.getName() + status + "<br>" +
                                  "칩: " + ai.getChips() + "<br>" +
                                  "베팅: " + ai.getCurrentBetAmount() + "</html>";
                aiLabel.setText(infoText);
            }

            if (i - 1 < aiCardPanels.size()) {
                JPanel cardContainer = aiCardPanels.get(i - 1);
                cardContainer.removeAll(); 
                
                // 현재 AI(ai)가 공개한 카드들을 순회하며 UI에 추가
                for (Integer cardIndex : ai.getRevealedCardIndices()) {
                    Card revealedCard = ai.getPersonalCards().get(cardIndex); 
                    
                    // 이미지 크기 조정
                    ImageIcon originalIcon = new ImageIcon("images/" + revealedCard.getValue() + ".png");
                    Image scaledImage = originalIcon.getImage().getScaledInstance(50, 70, Image.SCALE_SMOOTH); 
                    ImageIcon scaledIcon = new ImageIcon(scaledImage);
                    
                    JLabel cardLabel = new JLabel(scaledIcon);
                    cardLabel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
                    cardContainer.add(cardLabel);
                }
                cardContainer.revalidate();
                cardContainer.repaint();
            }
        }

        // 3. 게임 상태 및 판돈(Pot) 업데이트 (루프 바깥으로 이동)
        gameStatusLabel.setText("현재 판돈: " + game.getPot() + " | 최소 베팅: " + MIN_BET);

        revalidate();
        repaint();
    }
    
    // 다음 라운드 시작 시 턴 초기화 및 자동 턴 시작
    private void revealNextSharedCard() {
        Card revealedCard = game.nextRound(); 
        
        if (revealedCard != null) {
        	int roundIndex = game.getRound() - 1; // 현재 라운드 인덱스 (1라운드면 인덱스 0)
            game.resetRoundStatus(); // 새 라운드 시작 시 베팅 상태 초기화
            game.determineStartingPlayerIndex(); // 새 베팅 순서 결정
            
            // UI 업데이트: 해당 라운드의 공유 카드 뒷면을 앞면으로 교체
            if (roundIndex < sharedCardLabels.size()) {
                JLabel cardLabel = sharedCardLabels.get(roundIndex);
                
                game.resetRoundStatus(); // 새 라운드 시작 시 베팅 상태 초기화
                game.determineStartingPlayerIndex(); // 새 베팅 순서 결정
                
                // 이미지 교체
                cardLabel.setIcon(new ImageIcon("images/" + revealedCard.getValue() + ".png"));
                
                gameStatusLabel.setText(game.getRound() + "번째 공유 카드 (" + revealedCard.getValue() + ")가 공개되었습니다. 베팅을 시작합니다!");
                
                // 베팅 버튼 다시 활성화 및 라운드 초기화 준비
                callButton.setEnabled(true);
                foldButton.setEnabled(true);
                
                // 3라운드 이후 개인 카드 공개 버튼 비활성화
                if (game.getRound() >= 1) {
                    revealButton.setEnabled(false); 
                }
                
                // 개인 카드 공개 버튼을 다시 활성화 (총 3장 중 1장씩 순차적으로 공개 가능)
                revealButton.setEnabled(true); 

                // 자동 턴 시작
                startNextTurnWithDelay();
                
            }
        } else {
            // 5라운드 진입 시도 (모든 공유 카드 공개 완료)
            gameStatusLabel.setText("모든 공유 카드가 공개되었습니다. 남은 개인 카드를 공개합니다!");
            
            // 최종 개인 카드 공개 로직 호출 (베팅이 끝난 후 실행된다고 가정)
            revealRemainingPersonalCards(); 
            // TODO: showFinalResult();
        }
        sharedCardsPanel.revalidate();
        sharedCardsPanel.repaint();
    }
    
    // 최종 공개 로직 메서드
    private void revealRemainingPersonalCards() {
        // 1. 사용자 남은 카드 공개
        List<Card> userPersonalCards = userPlayer.getPersonalCards();
        List<Integer> userRevealedIndices = userPlayer.getRevealedCardIndices();
        
        for (int i = 0; i < userPersonalCards.size(); i++) {
            if (!userRevealedIndices.contains(i)) {
                userPlayer.revealPersonalCard(i); 
                JLabel cardLabel = personalCardLabels.get(i);
                cardLabel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 5));
            }
        }
        
        // 2. AI 플레이어의 모든 개인 카드 공개 및 UI 업데이트
        List<Player> players = game.getPlayers();
        for (int i = 1; i < players.size(); i++) { // 사용자 제외 AI만
            Player aiPlayer = players.get(i);
            if (aiPlayer.hasFolded()) {
                continue;
            }

            // AI 패널의 카드 컨테이너 가져오기
            if (i - 1 < aiCardPanels.size()) {
                JPanel aiCardContainer = aiCardPanels.get(i - 1);
                aiCardContainer.removeAll();
                
                List<Card> personalCards = aiPlayer.getPersonalCards();

                for (int j = 0; j < personalCards.size(); j++) {
                    if (!aiPlayer.getRevealedCardIndices().contains(j)) {
                        aiPlayer.revealPersonalCard(j); // 남은 카드를 객체에 기록
                    }
                }
                
                for (int j = 0; j < personalCards.size(); j++) {
                    Card cardToShow = personalCards.get(j);
                    JLabel cardLabel = new JLabel(new ImageIcon("images/" + cardToShow.getValue() + ".png"));
                    cardLabel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2)); 
                    aiCardContainer.add(cardLabel);
                }
                
                aiCardContainer.revalidate();
                aiCardContainer.repaint();
            }
        }

        personalCardsPanel.revalidate();
        personalCardsPanel.repaint();
        
        gameStatusLabel.setText("모든 카드가 공개되었습니다. 최종 점수를 계산합니다.");
        
        // 딜레이 후 최종 결과 표시
        Timer resultTimer = new Timer(TURN_DELAY * 2, e -> {
            showFinalResult();
            ((Timer)e.getSource()).stop();
        });
        resultTimer.setRepeats(false);
        resultTimer.start();
    }
    
    // 주어진 플레이어의 최종 점수를 계산합니다.
    // (공유 카드 4장 + 개인 카드 3장, 중복 카드 0점 처리)
    private int calculateFinalScore(Player player) {
        // 1. 모든 카드(공유 4장 + 개인 3장)를 하나의 리스트에 모읍니다.
        List<Card> allCards = new ArrayList<>();
        allCards.addAll(game.getSharedCards());
        allCards.addAll(player.getPersonalCards());

        Map<Integer, Integer> valueCounts = new HashMap<>(); // <카드 숫자, 개수>
        
        // 2. 각 카드의 숫자를 세어 중복 여부를 확인합니다.
        for (Card card : allCards) {
            int value = card.getValue();
            valueCounts.put(value, valueCounts.getOrDefault(value, 0) + 1);
        }

        int finalScore = 0;
        
        // 3. 최종 점수 계산: 중복되지 않은(개수가 1개인) 카드만 합산합니다.
        for (Card card : allCards) {
            int value = card.getValue();
            if (valueCounts.get(value) == 1) {
                finalScore += value;
            }
        }
        
        return finalScore;
    }
    
    // 게임 종료 후 최종 승자를 판정하고 결과를 표시합니다.
    private void showFinalResult() {
        List<Player> activePlayers = new ArrayList<>();
        for (Player p : game.getPlayers()) {
            if (!p.hasFolded()) {
                activePlayers.add(p);
            }
        }

        boolean userWon = false;
        
        // 1. 활성 플레이어가 1명 이하인 경우
        if (activePlayers.size() <= 1) {
            Player winner = activePlayers.isEmpty() ? null : activePlayers.get(0);
            if (winner != null) {
                winner.addChips(game.getPot());
                userWon = (winner == userPlayer);
                JOptionPane.showMessageDialog(this, 
                    winner.getName() + "님이 모든 플레이어의 폴드로 승리했습니다! 판돈 " + game.getPot() + " 코인을 획득합니다.", 
                    "게임 종료", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "모든 플레이어가 폴드하여 승자가 없습니다. 판돈은 다음 라운드로 이월됩니다.", 
                    "게임 종료", JOptionPane.INFORMATION_MESSAGE);
                game.setPot(0);
            }
        } else {
            // 2. 여러 플레이어가 활성 상태인 경우: 점수 계산
            Map<Player, Integer> finalScores = new HashMap<>();
            int minFinalScore = Integer.MAX_VALUE;
            
            // 최종 점수 계산
            for (Player player : activePlayers) {
                int playerScore = calculateFinalScore(player);
                finalScores.put(player, playerScore);
                
                if (playerScore < minFinalScore) {
                    minFinalScore = playerScore;
                }
            }
            
            // 최저 점수를 가진 플레이어 찾기 (동점자 포함)
            List<Player> winners = new ArrayList<>();
            for (Player player : activePlayers) {
                if (finalScores.get(player) == minFinalScore) {
                    winners.add(player);
                }
            }
            
            // 동점자 처리
            if (winners.size() > 1) {
                // 동점일 경우: 인덱스가 작은 플레이어 우선 (시계방향 순서)
                winners.sort((p1, p2) -> {
                    int idx1 = game.getPlayers().indexOf(p1);
                    int idx2 = game.getPlayers().indexOf(p2);
                    return Integer.compare(idx1, idx2);
                });
                
                // 판돈을 동점자 수로 나눔
                int splitPot = game.getPot() / winners.size();
                int remainder = game.getPot() % winners.size(); // 나머지
                
                String winnerNames = "";
                for (int i = 0; i < winners.size(); i++) {
                    Player winner = winners.get(i);
                    int prize = splitPot;
                    
                    // 첫 번째 승자에게 나머지 추가 (시계방향 우선)
                    if (i == 0) {
                        prize += remainder;
                    }
                    
                    winner.addChips(prize);
                    
                    if (winner == userPlayer) {
                        userWon = true;
                    }
                    
                    winnerNames += winner.getName();
                    if (i < winners.size() - 1) {
                        winnerNames += ", ";
                    }
                }
                
                String resultMessage = "동점입니다! " + winnerNames + "님이 승리했습니다!\n" +
                                       "최종 점수: " + minFinalScore + "점\n" +
                                       "판돈 " + game.getPot() + " 코인을 " + winners.size() + "명이 나눠 가집니다.\n\n" +
                                       "--- 최종 결과 ---";
                
                for (Player p : game.getPlayers()) {
                    int score = finalScores.getOrDefault(p, -1);
                    String scoreDisplay = (p.hasFolded()) ? "FOLD" : String.valueOf(score) + "점";
                    resultMessage += String.format("\n%s: %s (칩: %d)", p.getName(), scoreDisplay, p.getChips());
                }
                
                JOptionPane.showMessageDialog(this, resultMessage, "게임 종료", JOptionPane.INFORMATION_MESSAGE);
                
            } else {
                // 단독 승자
                Player winner = winners.get(0);
                winner.addChips(game.getPot());
                userWon = (winner == userPlayer);
                
                String resultMessage = winner.getName() + "님이 최종 점수 " + minFinalScore + "점으로 승리했습니다!\n" +
                                       "판돈 " + game.getPot() + " 코인을 획득합니다.\n\n" +
                                       "--- 최종 결과 ---";
                
                for (Player p : game.getPlayers()) {
                    int score = finalScores.getOrDefault(p, -1);
                    String scoreDisplay = (p.hasFolded()) ? "FOLD" : String.valueOf(score) + "점";
                    resultMessage += String.format("\n%s: %s (칩: %d)", p.getName(), scoreDisplay, p.getChips());
                }
                
                JOptionPane.showMessageDialog(this, resultMessage, "게임 종료", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        
        // 4. Home으로 게임 결과 전달 및 파일 저장
        if (homeReference != null) {
            homeReference.updatePlayerData(userWon, userPlayer.getChips());
        }
        
        // 5. 게임 화면 닫고 홈 화면으로 복귀
        dispose(); 
        if (homeReference != null) {
            homeReference.setVisible(true);
        } else {
            new Home(playerName, 0, 0, userPlayer.getChips());
        }
    }
}