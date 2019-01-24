package partecipant;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import utils.LogAgent;
public class PartecipantAgent extends Agent {

    LogAgent log;



    @Override
    protected void setup() {

        log = new LogAgent(this);
        addBehaviour(new PartecipantAgentBehaviour(log));
    }

    @Override
    protected void takeDown() {
        log.log("LASCIO IL TAVOLO");
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
        }

    }
}
