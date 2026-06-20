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
    public Card revealPersonalCard(int index) {
        if (index >= 0 && index < personalCards.size() && !revealedCardIndices.contains(index)) {
            Card card = personalCards.get(index);
            revealedCardIndices.add(index);
            revealedCards.add(card); // 공개된 카드 목록에도 추가 (베팅 순서 결정에 사용)
            return card;
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
    
    // AI가 공개할 카드를 선택합니다.
    // 우선순위: 1) 공유 카드와 값이 같은 카드 (상쇄 가능), 2) 가장 낮은 카드
    public Card aiRevealCard(List<Card> revealedSharedCards) {
        if (getRevealedCount() >= 1) return null;

        // 공유 카드와 매칭되는 개인 카드가 있으면 우선 공개 (서로 상쇄됨)
        for (Card shared : revealedSharedCards) {
            for (int i = 0; i < personalCards.size(); i++) {
                if (!revealedCardIndices.contains(i) && personalCards.get(i).getValue() == shared.getValue()) {
                    return revealPersonalCard(i);
                }
            }
        }

        // 매칭 없으면 가장 낮은 카드 공개
        int minIndex = -1, minValue = 11;
        for (int i = 0; i < personalCards.size(); i++) {
            if (!revealedCardIndices.contains(i) && personalCards.get(i).getValue() < minValue) {
                minValue = personalCards.get(i).getValue();
                minIndex = i;
            }
        }
        return (minIndex != -1) ? revealPersonalCard(minIndex) : null;
    }

    // AI 베팅 결정 - 공개 카드 비교, 상쇄 가능성, 칩 여유를 고려한 적당한 전략
    public String aiDecideBet(int minBet, List<Player> allPlayers, List<Card> revealedSharedCards) {
        if (hasFolded() || getChips() == 0 || getChips() < 10) return "FOLD";

        // 내 공개 카드 값 (낮을수록 베팅 순서 앞 = 불리하지만 점수에선 유리)
        int myRevealedValue = revealedCards.isEmpty() ? 5 : revealedCards.get(0).getValue();

        // 나보다 낮은 공개 카드를 가진 상대 수 계산
        int opponentsBeatingMe = 0, activeOpponents = 0;
        for (Player p : allPlayers) {
            if (p == this || p.hasFolded()) continue;
            activeOpponents++;
            if (!p.getRevealedCards().isEmpty() && p.getRevealedCards().get(0).getValue() < myRevealedValue) {
                opponentsBeatingMe++;
            }
        }
        // 상대적 불리함 (0=나 제일 낮음, 1=나 제일 높음)
        double disadvantage = (activeOpponents > 0) ? (double) opponentsBeatingMe / activeOpponents : 0.0;

        // 내 패 중 공유 카드와 상쇄 가능한 카드 수
        int cancelPotential = 0;
        for (Card sharedCard : revealedSharedCards) {
            for (Card myCard : personalCards) {
                if (myCard.getValue() == sharedCard.getValue()) {
                    cancelPotential++;
                    break;
                }
            }
        }

        // 베팅 부담 비율 (minBet이 내 칩의 절반 이상이면 부담)
        double betBurden = Math.min(1.0, (double) minBet / Math.max(1, getChips()));

        // 콜 확률: 기본 65%, 불리할수록 감소, 상쇄 카드 있으면 증가, 부담 크면 감소
        double callProb = 0.65
            - 0.30 * disadvantage
            + 0.15 * Math.min(1, cancelPotential)
            - 0.15 * betBurden;
        callProb = Math.max(0.25, Math.min(0.92, callProb));

        return Math.random() < callProb ? "CALL" : "FOLD";
    }
    
    public void setCardContainer(JPanel container) {
    	this.cardContainer = container;
    }
    public JPanel getCardContainer() {
    	return cardContainer;
    }
}