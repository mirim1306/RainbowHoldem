package project_2403;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private Deck deck;
    private List<Player> players;
    private List<Card> sharedCards; // 공유 카드 (4장)
    private int round; // 현재 라운드 (1~4)
    private int pot; // 현재 판돈
    private int currentBet; // 현재 베팅 금액
    private int currentDealerIndex;    // 베팅 시작 기준 플레이어
    private int currentBettingPlayerIndex; // 현재 베팅 중인 플레이어의 인덱스
    private int currentRoundBet;    // 현재 라운드에서 가장 많이 베팅된 금액

    public Game(List<String> playerNames, int initialChips) {
        this.deck = new Deck();
        this.players = new ArrayList<>();
        this.sharedCards = new ArrayList<>();
        this.round = 0;
        this.pot = 0;
        this.currentBet = 0;
        
        // 플레이어 생성 (첫 번째 이름은 사용자, 나머지는 AI)
        for (int i = 0; i < playerNames.size(); i++) {
            boolean isAI = (i != 0);
            players.add(new Player(playerNames.get(i), isAI, initialChips));
        }
    }

    public void setupGame() {
        // 1. 개인 카드 분배 (플레이어당 3장)
        for (int i = 0; i < 3; i++) {
            for (Player player : players) {
                player.receiveCard(deck.drawCard());
            }
        }

        // 2. 공유 카드 분배 (4장)
        for (int i = 0; i < 4; i++) {
            sharedCards.add(deck.drawCard());
        }
        
        // 초기 베팅 (앤티)
        // 칩이 없으므로, 현재는 베팅 로직을 생략하고, 추후 필요 시 추가합니다.
        
        this.round = 1; // 1라운드 시작 준비 완료
    }
    
    // 다음 라운드로 진행하며 공유 카드를 공개합니다.
    public Card nextRound() {
        if (round >= 4) {
            return null; // 게임 종료
        }
        round++;
        return sharedCards.get(round - 1);
    }

    // --- Getter 메서드 ---
    public List<Player> getPlayers() { return players; }
    public List<Card> getSharedCards() { return sharedCards; }
    public int getRound() { return round; }
    //베팅 관련 Getter/Setter
    public int getPot() { 
        return pot; 
    }

    public void setPot(int pot) { 
        this.pot = pot; 
    }
    
    // 베팅 시작 플레이어 결정
    public int determineStartingPlayerIndex() {
        // 공개된 개인 카드 중 가장 낮은 카드를 가진 플레이어의 인덱스를 찾아 반환합니다.
        int minCardValue = 11;
        int startingIndex = -1;
        
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (player.hasFolded() || player.getRevealedCards().isEmpty()) continue; 
            
            // 현재는 1장의 개인 카드만 공개되었으므로 인덱스 0을 사용
            int revealedValue = player.getRevealedCards().get(0).getValue(); 
            
            // 가장 낮은 값 찾기 (동점일 경우 시계 방향(인덱스 순서)으로 먼저 위치한 플레이어)
            if (revealedValue < minCardValue) {
                minCardValue = revealedValue;
                startingIndex = i;
            }
        }
        
        // 만약 모두 동점이라면, 딜러 다음 순서 (현재는 0번 플레이어부터 시작)
        if (startingIndex == -1) {
            startingIndex = 0; 
        }
        
        this.currentDealerIndex = startingIndex;
        return startingIndex;
    }

    // 다음 베팅 플레이어 찾기
    public Player getNextBettingPlayer() {
        int total = players.size();
        
        // 라운드 시작 시: 베팅 순서 결정자부터 시작
        if (currentBettingPlayerIndex == -1) {
            currentBettingPlayerIndex = determineStartingPlayerIndex();
        } else {
            // 다음 순서로 이동
            currentBettingPlayerIndex = (currentBettingPlayerIndex + 1) % total;
        }
        
        // 폴드하지 않은 플레이어를 찾을 때까지 반복
        int startIndex = currentBettingPlayerIndex;
        do {
            Player nextPlayer = players.get(currentBettingPlayerIndex);
            if (!nextPlayer.hasFolded()) {
                return nextPlayer;
            }
            currentBettingPlayerIndex = (currentBettingPlayerIndex + 1) % total;
        } while (currentBettingPlayerIndex != startIndex); 

        return null; // 모든 플레이어가 폴드한 경우
    }

    // 라운드 종료 조건 확인
    public boolean isBettingRoundFinished() {
        int activePlayerCount = 0;
        
        // 1. 최소 1명 이상의 플레이어가 액션을 취했는지 확인
        boolean atLeastOneBet = players.stream().anyMatch(p -> p.getCurrentBetAmount() > 0 || p.hasFolded());
        if (!atLeastOneBet) return false;

        // 2. 모든 활성 플레이어의 베팅 금액이 현재 라운드의 최고 베팅 금액과 일치하는지 확인
        for (Player player : players) {
            if (!player.hasFolded()) {
                activePlayerCount++;
                
                // 개인 베팅 금액이 현재 라운드 최고 베팅 금액과 다르면
                if (player.getCurrentBetAmount() < currentRoundBet) {
                    return false;
                }
            }
        }
        
        // 3. 활성 플레이어가 1명 이하면 라운드 종료 (승리 확정)
        if (activePlayerCount <= 1) {
            return true; 
        }
        
        // 모두의 베팅 금액이 일치하고, 액티브 플레이어가 2명 이상일 때 true 반환
        return true; 
    }
    
    // 게임 진행 가능 여부 확인 메서드 추가
    public boolean canContinueGame() {
        int activePlayerCount = 0;
        for (Player player : players) {
            if (!player.hasFolded()) {
                activePlayerCount++;
            }
        }
        // 2명 이상이 남아있으면 게임 진행 가능
        return activePlayerCount >= 2;
    }

    // 활성 플레이어 수 반환 메서드 추가
    public int getActivePlayerCount() {
        int count = 0;
        for (Player player : players) {
            if (!player.hasFolded()) {
                count++;
            }
        }
        return count;
    }

    // 라운드 시작 시 베팅 상태 초기화
    public void resetRoundStatus() {
        currentBettingPlayerIndex = -1; // 베팅 순서 초기화
        currentRoundBet = 0;          // 라운드 최고 베팅 초기화
        
        for (Player player : players) {
            if (!player.hasFolded()) {
                player.resetRoundBet(); // Player의 currentBetAmount(개인 베팅) 초기화
            }
        }
    }
    
    public int getCurrentRoundBet() { 
        return currentRoundBet; 
    }

    public void setCurrentRoundBet(int bet) {
        this.currentRoundBet = bet;
    }
}