package prob;

import hand.Hand;
import initiator.InitiatorAgentBehaviour;

import java.util.*;

public class ProbCards {

    public static Map<Hand,Integer> mapPointVal = new HashMap<>();

    static {
        int num = InitiatorAgentBehaviour.values;

        Set<Hand> set = new HashSet<>();
        List<Hand> list;
        int [] vals = new int[num];
        for (int i = 0; i < num; i++) {
            vals[i] = i+1;
        }

        Hand hand;

        for (int i = 0; i < vals.length; i++) {
            for (int j = 0; j < vals.length; j++) {
                for (int k = 0; k < vals.length; k++) {
                    if(i>=j && j>=k){
                        hand = new Hand(new Integer[]{vals[i],vals[j],vals[k]});
                        set.add(hand);
                    }
                }
            }
        }
        list = new ArrayList<>(set);
        list.sort((a, b) -> {
            try {
                return Integer.compare(ProbCards.valHand((a)), ProbCards.valHand(b));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return -1;
        });
        for (int i = 0; i < list.size(); i++) {
            mapPointVal.put(list.get(i),i+1);

        }
    }

    public static int valHand(Hand hand) throws Exception {
        return valHand(hand.getCards());
    }

    private static int valHand(Integer[] cards) throws Exception {
        if(cards.length != InitiatorAgentBehaviour.handCardsNum)
            throw new Exception("Wrong handCards length ");
        Map<Integer,Integer> mapCardNum = new HashMap<>();
        int count;
        int maxCount=0;
        int max = 0;
        int val,indexMax = 0;
        boolean cartaAlta, coppia,tris, scala = true;
        for (int i = 0; i < cards.length; i++) {

            if(i>0) {
                scala = scala && (cards[i] - cards[i-1]) == 1;
            }

            count = (mapCardNum.containsKey(cards[i])) ? mapCardNum.get(cards[i])+1 : 1;
            mapCardNum.put(cards[i],count);
            if(count > maxCount){
                maxCount = count;
                indexMax = i;
            }else
                if(!cards[indexMax].equals(cards[i]) && cards[i]>max)
                    max = cards[i];
        }
        cartaAlta = maxCount == 1 && !scala;
        coppia = maxCount == 2;
        tris = maxCount == 3;


        if(cartaAlta){
            val = 100;
        }else if(coppia){
            val = 200;
        }else if(scala){
            val = 300;
        }else if(tris){
            val = 400;
        }
        else val = -1;


        if(coppia || tris)
            val += cards[indexMax]*10;

        for (int card : mapCardNum.keySet()) {
            if(cartaAlta || scala || card != cards[indexMax])
                val+=card;
        }
        return val;
    }

}
