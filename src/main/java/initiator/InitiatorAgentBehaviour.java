package initiator;

import hand.Hand;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import partecipant.PartecipantAgentBehaviour;
import utils.LogAgent;

import java.util.*;

import static utils.LogAgent.GREEN;
import static utils.LogAgent.ANSI_RED;

public class InitiatorAgentBehaviour extends Behaviour {

    private static final String REGISTRATION = "REGISTRATION";
    private static final String CALL_FOR_PROPOSAL = "CALL-FOR-PROPOSAL";
    private static final String GET_PROPOSAL = "GET-PROPOSAL";
    private static final String ACCEPT_PROPOSAL = "ACCEPT-PROPOSAL";
    private static final String REJECT_PROPOSAL = "REJECT-PROPOSAL";
    private static final String RECEIVE_REPLY = "RECEIVE-REPLY";
    private static final String DELETE = "DELETE";

    public static final String SERVICE = "Initiator";

    private int repliesCnt = 0;
    private String step = REGISTRATION;
    private DFAgentDescription[] partecipants;
    private List<AID> acceptedProposals;
    private List<AID> rejectedProposal;
    private MessageTemplate replyToCFPTemplate, acceptProposalTemplate;

    private String contentMessage = "TASK";
    private String conversationId = "PARTITA";

    private int maxNumAccepted = 1;

    public static int values = 6;
    public static int handCardsNum = 3;

    private LogAgent logAgent;

    private List<Integer> cards;

    private int playerCount = values;

    private Map<AID,Integer> playerBidding = new HashMap<>();

    private int maxPoints = 0;
    private Set<AID> winners = new HashSet<>();
    private Set<AID> loosers = new HashSet<>();

    public InitiatorAgentBehaviour(LogAgent logAgent) {
        this.logAgent = logAgent;
        this.cards = generateCards();
    }

    @Override
    public void action() {
        switch (step){
            case REGISTRATION:
                logAgent.log("APERTURA TAVOLO DA GIOCO");
                DFAgentDescription dfd = new DFAgentDescription();
                dfd.setName(myAgent.getAID());
                ServiceDescription sd = new ServiceDescription();
                sd.setType(SERVICE);
                sd.setName(myAgent.getLocalName()+"-Service");
                dfd.addServices(sd);
                try {
                    DFService.register(myAgent, dfd);
                }
                catch (FIPAException fe) {
                    fe.printStackTrace();
                }
                step = CALL_FOR_PROPOSAL;
                break;
            case CALL_FOR_PROPOSAL:
                partecipants = getPartecipants(PartecipantAgentBehaviour.SERVICE);
                logAgent.log("DISTRIBUZIONE DELLE CARTE A " + partecipants.length +" GIOCATORI");
                acceptedProposals = new ArrayList<>();
                rejectedProposal = new ArrayList<>();

                ACLMessage cfp = new ACLMessage(ACLMessage.CFP);

                for (int i = 0; i < partecipants.length && playerCount-- > 0; i++){
                    cfp.addReceiver(partecipants[i].getName());
                    try {
                        cfp.setContentObject(generateHand(partecipants.length));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    cfp.setConversationId("PARTITA");
                    cfp.setReplyWith(i+"cfp"+System.currentTimeMillis());
                    myAgent.send(cfp);
                    replyToCFPTemplate = MessageTemplate.MatchConversationId(cfp.getConversationId());
                }
                logAgent.log("IN ATTESA DELLE PUNTATE");
                step = GET_PROPOSAL;
                break;
            case GET_PROPOSAL:
                ACLMessage replyToCFP = myAgent.receive(/*replyToCFPTemplate*/);
                if(replyToCFP != null && replyToCFP.getConversationId().equals("PARTITA")){

                    if(replyToCFP.getPerformative() == ACLMessage.PROPOSE){

                        logAgent.log(LogAgent.B_YELLOW+replyToCFP.getSender().getLocalName() + " FA LA PUNTATA");
                        playerBidding.put(replyToCFP.getSender(),Integer.parseInt(replyToCFP.getContent()));

                        if(Integer.parseInt(replyToCFP.getContent()) >= maxPoints){
                            maxPoints = Integer.parseInt(replyToCFP.getContent());
                        }
                    }else if(replyToCFP.getPerformative() == ACLMessage.REFUSE){
                        logAgent.log(ANSI_RED+replyToCFP.getSender().getLocalName() + " SI RITIRA");
                    }
                    repliesCnt++;
                    if(repliesCnt >= partecipants.length){
                        for(AID player : playerBidding.keySet())
                            if(playerBidding.get(player) == maxPoints)
                                winners.add(player);
                            else
                                loosers.add(player);

                        step = REJECT_PROPOSAL;
                    }
                }else
                    block();
                break;
            case REJECT_PROPOSAL:

                ACLMessage refuseProposal = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                for (AID aid : loosers){
                    logAgent.log(ANSI_RED+aid.getLocalName()+" HA PERSO");
                    refuseProposal.addReceiver(aid);
                    refuseProposal.setContent("NOT BEST PROPOSAL");
                    refuseProposal.setConversationId(conversationId);
                    myAgent.send(refuseProposal);
                }
                step = ACCEPT_PROPOSAL;

                break;
            case ACCEPT_PROPOSAL:
                ACLMessage acceptProposal = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                for (AID aid : winners){
                    logAgent.log(GREEN +aid.getLocalName() + " HA VINTO");
                    acceptProposal.addReceiver(aid);
                }
                acceptProposal.setContent(contentMessage);
                acceptProposal.setConversationId(conversationId);
                acceptProposal.setReplyWith("accept-proposal" + System.currentTimeMillis());
                myAgent.send(acceptProposal);
                acceptProposalTemplate =  MessageTemplate.and(
                        MessageTemplate.MatchConversationId(acceptProposal.getConversationId()),
                        MessageTemplate.MatchInReplyTo(acceptProposal.getReplyWith()));
                step = RECEIVE_REPLY;
                break;
            case RECEIVE_REPLY:
                ACLMessage replyToAccept = myAgent.receive(
                        MessageTemplate.and(
                                MessageTemplate.MatchConversationId(conversationId),
                                MessageTemplate.MatchPerformative(ACLMessage.INFORM)));
                if(replyToAccept != null){
                    if(replyToAccept.getPerformative() == ACLMessage.INFORM){
                        myAgent.doDelete();
                    }
                    step = DELETE;
                }else
                    block();
                break;

        }
    }

    @Override
    public boolean done() {
        if(step.equals(RECEIVE_REPLY)
                &&  playerBidding.size()==0){
            logAgent.log("NESSUN GIOCATORE HA PUNTATO");
            myAgent.doDelete();
            return true;
        }else if(step.equals(DELETE)){
            return true;
        }else
            return false;
    }

    private DFAgentDescription[] getPartecipants(String serviceType){
        DFAgentDescription[] result = new DFAgentDescription[0];
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(serviceType);
        template.addServices(sd);
        try {
            result = DFService.search(myAgent,
                    template);

        } catch (FIPAException e) {
            logAgent.log(e.toString());
        }
        logAgent.log("TROVATI " + result.length + " GIOCATORI");
        return result;
    }

    private List<Integer> generateCards(){
        List<Integer> list = new ArrayList<>();
        for (int card = 1; card <= values; card++) {
            for (int i = 0; i < handCardsNum ; i++) {
                list.add(card);
            }
        }
        return list;
    }

    private Hand generateHand(int player) throws Exception {
        if(cards.size() < handCardsNum && handCardsNum > 0)
            throw new Exception("Insufficient cards");

        Integer [] hand = new Integer[handCardsNum];

        for (int i = 0; i < hand.length; i++) {
            if(cards.size() == 1)
                hand[i] = cards.get(0);
            else
                hand[i] = cards.remove(new UniformIntegerDistribution(0,cards.size()-1).sample());
        }

        return new Hand(hand,player);
    }

    private boolean acceptProposal(ACLMessage proposalMessage){
        return maxNumAccepted-- > 0;
    }

}
