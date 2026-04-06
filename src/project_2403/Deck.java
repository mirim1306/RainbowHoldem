package project_2403;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private List<Card> cards;

    public Deck() {
        cards = new ArrayList<>();
        initializeDeck();
        shuffle();
    }

    private void initializeDeck() {
        // 1부터 10까지, 해당 숫자만큼의 카드 생성
        for (int value = 1; value <= 10; value++) {
            for (int i = 0; i < value; i++) {
                cards.add(new Card(value));
            }
        }
        // 총 카드 수: 1 + 2 + ... + 10 = 55장
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card drawCard() {
        if (cards.isEmpty()) {
            return null; // 카드가 없을 경우
        }
        return cards.remove(0);
    }
    
    // 남은 카드 수를 확인하는 디버깅용 메서드
    public int getRemainingCardCount() {
        return cards.size();
    }
}