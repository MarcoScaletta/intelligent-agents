package partecipant;

import hand.Hand;
import initiator.InitiatorAgentBehaviour;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.commons.math3.distribution.BinomialDistribution;
import prob.ProbCards;
import utils.LogAgent;

import static utils.LogAgent.ANSI_RESET;

public class PartecipantAgentBehaviour extends Behaviour {


    private static final String REGISTRATION = "REGISTRATION";
    public static final String SERVICE = "Partecipant";
    private static final String DELETE = "DELETE";
    private static final String WAIT_CALL_FOR_PROPOSAL = "WAIT-CALL-FOR-PROPOSAL";
    private static final String WAIT_ANSWER_TO_PROPOSAL = "WAIT-ANSWER-TO-PROPOSAL";

    private String step = REGISTRATION;

    private String conversationId = "PARTITA";

    private LogAgent logAgent;

    private int[] handCards;

    public PartecipantAgentBehaviour(LogAgent logAgent) {
        this.logAgent = logAgent;
    }


    @Override
    public void action() {
        switch (step) {
            case REGISTRATION:
                DFAgentDescription dfd = new DFAgentDescription();
                dfd.setName(myAgent.getAID());
                ServiceDescription sd = new ServiceDescription();
                sd.setType(SERVICE);
                sd.setName(myAgent.getLocalName() + "-Service");
                dfd.addServices(sd);
                try {
                    DFService.register(myAgent, dfd);
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
                logAgent.log("MI SIEDO AL TAVOLO E ASPETTO LE CARTE");
                step = WAIT_CALL_FOR_PROPOSAL;
                break;
            case WAIT_CALL_FOR_PROPOSAL:

                ACLMessage cfp = myAgent.receive();
                Hand hand = null;
                double probPlaying;
                boolean iWillPlay;
                if(cfp != null){

                    if(cfp.getPerformative() == ACLMessage.CFP){

                        ACLMessage replyCFP = cfp.createReply();

                        try {
                            hand = (Hand)cfp.getContentObject();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if(hand != null){
                            probPlaying =
                                    (double)(ProbCards.mapPointVal.get(hand)+ InitiatorAgentBehaviour.values)
                                    /
                                    (ProbCards.mapPointVal.size()+InitiatorAgentBehaviour.values);
                            iWillPlay = (new BinomialDistribution(1,probPlaying).sample() == 1);
                            try {
                                logAgent.log("CARTE RICEVUTE: " + hand.toString() +" (CON VALORE " +ProbCards.mapPointVal.get(hand)+"/"+ ProbCards.mapPointVal.size() +")");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if(iWillPlay){
                                logAgent.log(LogAgent.B_YELLOW+"VOGLIO PUNTARE");
                                replyCFP.setContent(String.valueOf(ProbCards.mapPointVal.get(hand)));
                                replyCFP.setPerformative(ACLMessage.PROPOSE);
                                conversationId = cfp.getConversationId();
                                replyCFP.setConversationId(conversationId);
                                myAgent.send(replyCFP);
                                conversationId = cfp.getConversationId();
                                step = WAIT_ANSWER_TO_PROPOSAL;

                            }else{
                                logAgent.log(LogAgent.ANSI_RED+"MI RITIRO");
                                replyCFP.setContent(String.valueOf(-1));
                                replyCFP.setPerformative(ACLMessage.REFUSE);
                                conversationId = cfp.getConversationId();
                                replyCFP.setConversationId(conversationId);
                                myAgent.send(replyCFP);
                                step = DELETE;

                            }
                        }
                    }
                }else{
                    block();
                }
                break;
            case WAIT_ANSWER_TO_PROPOSAL:
                ACLMessage ansPropMsg = myAgent.receive(MessageTemplate.or(
                        MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
                        MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL)));
                if(ansPropMsg != null ){
                    if(ansPropMsg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                        logAgent.log(LogAgent.GREEN +"HO VINTO, RITIRO I SOLDI");
                        ACLMessage informMessage = ansPropMsg.createReply();
                        informMessage.setPerformative(ACLMessage.INFORM);
                        informMessage.setConversationId(conversationId);
                        myAgent.send(informMessage);
                    }else if(ansPropMsg.getPerformative() == ACLMessage.REJECT_PROPOSAL)
                        logAgent.log(LogAgent.ANSI_RED+"HO PERSO");
                    step = DELETE;
                }else
                    block();

                break;
        }

    }



    @Override
    public boolean done() {
        if(step.equals(DELETE))
            myAgent.doDelete();
        return step.equals(DELETE);
    }
}
