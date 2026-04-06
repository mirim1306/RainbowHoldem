package project_2403;

import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class Player {
    private final String name;
    private final boolean isAI;
    private List<Card> personalCards; // 개인 카드 (3장)
    private List<Card> revealedCards; // 공개된 개인 카드 (베팅에 사용)
    private int chips;
    private int currentBetAmount; // 현재 라운드에서 이 플레이어가 베팅한 총 금액
    private boolean hasFolded;    // 다이(Fold) 했는지 여부
    private List<Integer> revealedCardIndices; //공개될 카드
    private JPanel cardContainer;
    
    // 추가된 필드: 승패 기록
    private int wins;
    private int losses;

    public Player(String name, boolean isAI, int initialChips) {
        this.name = name;
        this.isAI = isAI;
        this.chips = initialChips;
        this.personalCards = new ArrayList<>();
        this.revealedCards = new ArrayList<>();
        this.currentBetAmount = 0;
        this.hasFolded = false;
        this.revealedCardIndices = new ArrayList<>(); // 초기화
        this.wins = 0; // 초기화
        this.losses = 0; // 초기화
    }

    public void receiveCard(Card card) {
        personalCards.add(card);
    }

    // 개인 카드 중 한 장을 선택하여 공개합니다.
    // 카드를 제거하지 않고, 공개된 인덱스만 기록합니다.
    public Card revealPersonalCard(int index) {
        if (index >= 0 && index < personalCards.size() && !revealedCardIndices.contains(index)) {
            revealedCardIndices.add(index); // 인덱스 기록
            return personalCards.get(index); // 카드만 반환
        }
        return null;
    }
    
    // placeBet 메서드: 올인 로직 유지
    public boolean placeBet(int amount) {
        if (hasFolded) return false;
        
        // 올인 처리: 입력 금액이 가진 칩(코인)보다 많거나 같으면 올인으로 간주
        int actualBet = Math.min(amount, this.chips);
        
        if (this.chips > 0 && actualBet > 0) {
            this.chips -= actualBet;
            this.currentBetAmount += actualBet;
            return true;
        }
        return false;
    }
    
    public void fold() {
        this.hasFolded = true;
    }
    
    public void resetRoundBet() {
        this.currentBetAmount = 0;
    }

    // --- Getter/Setter 추가 ---
    public int getCurrentBetAmount() { return currentBetAmount; }
    
    public boolean hasFolded() { return hasFolded; }

    // --- Getter 메서드 ---
    public String getName() { return name; }
    public boolean isAI() { return isAI; }
    public List<Card> getPersonalCards() { return personalCards; }
    public List<Card> getRevealedCards() { return revealedCards; }
    public int getChips() { return chips; }
    
    // 승패 횟수 Getter 추가
    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    
    // --- 칩 관련 메서드 ---
    public void addChips(int amount) { this.chips += amount; }
    public boolean bet(int amount) {
        if (this.chips >= amount) {
            this.chips -= amount;
            return true;
        }
        return false; // 베팅 실패 (칩 부족)
    }
    
    // 승패 업데이트 메서드 추가
    public void addWin() { this.wins++; }
    public void addLoss() { this.losses++; }
    
    public List<Integer> getRevealedCardIndices() {
    	return revealedCardIndices;
    }
    
    // 현재까지 공개된 개인 카드의 개수를 반환합니다.
    public int getRevealedCount() {
        return revealedCardIndices.size();
    }
    
    // AI가 개인 카드 3장 중 공개할 카드를 선택하고 공개합니다. (가장 낮은 숫자 선택)
    public Card aiRevealCard() {
        if (getRevealedCount() >= 1) {
            return null; // 이미 한 장 공개했으면 추가 공개하지 않음
        }

        // 미공개 카드 중 가장 낮은 값의 인덱스 찾기
        int minIndex = -1;
        int minValue = 11; // 10보다 큰 값으로 초기화

        for (int i = 0; i < personalCards.size(); i++) {
            Card card = personalCards.get(i);
            // 이미 공개되지 않은 카드 중 가장 낮은 값을 찾습니다.
            if (!getRevealedCardIndices().contains(i) && card.getValue() < minValue) {
                minValue = card.getValue();
                minIndex = i;
            }
        }

        if (minIndex != -1) {
            // 찾은 인덱스로 카드 공개 (Player 클래스의 revealPersonalCard 메서드 사용)
            return revealPersonalCard(minIndex);
        }
        return null;
    }

    // AI의 베팅 결정을 처리합니다. (80% 콜, 20% 폴드)
    public String aiDecideBet(final int minBet) {
        if (hasFolded()) {
            return "PASS";
        }

        // 칩이 최소 베팅 금액보다 적으면 (강제 올인/폴드 로직은 이미 placeBet에 있음)
        if (getChips() < minBet && getChips() > 0) {
            // 현재는 MIN_BET이 10이므로, 칩이 10 미만인 경우 폴드로 처리
            if (getChips() < 10) { 
                return "FOLD";
            }
        } else if (getChips() == 0) {
            return "FOLD";
        }
        
        // 80% 확률로 콜
        double luck = Math.random();
        if (luck < 0.8) {
            return "CALL";
        } else {
            return "FOLD";
        }
    }
    
    public void setCardContainer(JPanel container) {
    	this.cardContainer = container;
    }
    public JPanel getCardContainer() {
    	return cardContainer;
    }
}