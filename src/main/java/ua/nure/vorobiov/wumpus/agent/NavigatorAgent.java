package ua.nure.vorobiov.wumpus.agent;

import aima.core.agent.impl.DynamicAction;
import aima.core.environment.wumpusworld.AgentPercept;
import aima.core.environment.wumpusworld.HybridWumpusAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;

public class NavigatorAgent extends Agent {

    private AID speleogistAgent;
    private HybridWumpusAgent agentLogic;

    @Override
    protected void setup() {

        agentLogic = new HybridWumpusAgent();

        System.out.println("Navigator agent " + getAID().getLocalName() + " is ready!");
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Wumpus-World-Navigator");
        sd.setName("Wumpus-Gold-finder");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        addBehaviour(new PerceptRequestBehavior());
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("Navigator agent terminating...");
    }

    private class PerceptRequestBehavior extends CyclicBehaviour {
        public void action() {
            ACLMessage message = myAgent.receive(MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                    MessageTemplate.MatchConversationId("Ask-for-action")));
            if (message != null) {
                if (speleogistAgent == null)
                    speleogistAgent = message.getSender();
                if (speleogistAgent.equals(message.getSender()))
                    myAgent.addBehaviour(new CreateSendPropose(message));
                else {
                    ACLMessage reply = message.createReply();
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                }
            } else {
                block();
            }
        }
    }

    private class CreateSendPropose extends OneShotBehaviour {

        ACLMessage msg;

        ArrayList<String> stenchKeyWords = new ArrayList<>();
        ArrayList<String> breezeKeyWords = new ArrayList<>();
        ArrayList<String> glitterKeyWords = new ArrayList<>();
        ArrayList<String> bumpKeyWords = new ArrayList<>();
        ArrayList<String> screamKeyWords = new ArrayList<>();

        public CreateSendPropose(ACLMessage m) {
            super();
            msg = m;
            stenchKeyWords.add("stink");
            stenchKeyWords.add("stench");
            stenchKeyWords.add("reek");
            stenchKeyWords.add("pong");
            stenchKeyWords.add("funk");

            breezeKeyWords.add("breeze");
            breezeKeyWords.add("wind");
            breezeKeyWords.add("gale");
            breezeKeyWords.add("air");

            glitterKeyWords.add("glitter");
            glitterKeyWords.add("flash");
            glitterKeyWords.add("brilliance");

            bumpKeyWords.add("bump");
            bumpKeyWords.add("strike");
            bumpKeyWords.add("hit");
            bumpKeyWords.add("bounce");

            screamKeyWords.add("scream");
            screamKeyWords.add("yell");
            screamKeyWords.add("bellow");
            screamKeyWords.add("wail");
        }

        public void action() {
            String content = msg.getContent();
            ACLMessage reply = msg.createReply();
            if (content != null) {
                reply.setPerformative(ACLMessage.PROPOSE);
                DynamicAction action = (DynamicAction) agentLogic.execute(extractPercept(content));
                reply.setContent(action.getName());
            } else {
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("not-available");
            }
            myAgent.send(reply);
            System.out.println(getAID().getLocalName() + ": " + reply.getContent());
        }

        private AgentPercept extractPercept(String message) {
            String content = message.toLowerCase();
            AgentPercept agentPercept = new AgentPercept();

            for (String stench : stenchKeyWords) {
                if (content.toLowerCase().contains(stench)) {
                    agentPercept.setStench(true);
                    break;
                }
            }

            for (String breeze : breezeKeyWords) {
                if (content.toLowerCase().contains(breeze)) {
                    agentPercept.setBreeze(true);
                    break;
                }
            }

            for (String glitter : glitterKeyWords) {
                if (content.toLowerCase().contains(glitter)) {
                    agentPercept.setGlitter(true);
                    break;
                }
            }

            for (String bump : bumpKeyWords) {
                if (content.toLowerCase().contains(bump)) {
                    agentPercept.setBump(true);
                    break;
                }
            }

            for (String scream : screamKeyWords) {
                if (content.toLowerCase().contains(scream)) {
                    agentPercept.setScream(true);
                    break;
                }
            }

            return agentPercept;
        }
    }
}