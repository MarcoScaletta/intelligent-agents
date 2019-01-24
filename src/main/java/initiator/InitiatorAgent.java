package initiator;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import utils.LogAgent;

public class InitiatorAgent extends Agent {

    LogAgent log;

    @Override
    protected void setup() {
        log = new LogAgent(this, LogAgent.B_WHITE);

        addBehaviour(new InitiatorAgentBehaviour(log));

    }

    @Override
    protected void takeDown() {
        log.log("IL BANCO CHIUDE");
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
        }
    }

}
