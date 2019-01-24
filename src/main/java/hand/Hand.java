package hand;

import java.io.Serializable;
import java.util.Arrays;

@SuppressWarnings("ALL")
public class Hand implements Serializable {

    private Integer cards[];
    private int player;

    public Hand(Integer[] cards) {

        Arrays.sort(cards);
        this.cards = cards;
    }

    public Hand(Integer[] cards,int player) {

        Arrays.sort(cards);
        this.cards = cards;
        this.player = player;
    }

    public Integer[] getCards() {
        return cards;
    }

    public int getPlayer() {
        return player;
    }

    @Override
    public String toString() {
        return Arrays.toString(cards);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hand hand = (Hand) o;
        return Arrays.equals(cards, hand.cards);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(cards);
    }

}
