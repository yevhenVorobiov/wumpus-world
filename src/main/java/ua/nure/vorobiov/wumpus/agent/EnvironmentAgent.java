package ua.nure.vorobiov.wumpus.agent;

import aima.core.environment.wumpusworld.AgentPercept;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import ua.nure.vorobiov.wumpus.model.WumpusWorld;

import java.util.ArrayList;

public class EnvironmentAgent extends Agent {

    private AID speleologistAgent;
    private WumpusWorld wumpusWorld;


    @Override
    protected void setup() {
        System.out.println("Environment agent " + getAID().getName() + " is ready!");

        wumpusWorld = new WumpusWorld();

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Wumpus-World-Environment");
        sd.setName("Environment-wandering");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        addBehaviour(new RequestBehavior());
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("Environment agent terminating...");
    }

    private class RequestBehavior extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                if (speleologistAgent == null)
                    speleologistAgent = msg.getSender();
                if (speleologistAgent.equals(msg.getSender())) {
                    if (msg.getPerformative() == ACLMessage.REQUEST) {
                        myAgent.addBehaviour(new PerceptReplyBehaviour(msg));
                    }
                    if (msg.getPerformative() == ACLMessage.CFP) {
                        myAgent.addBehaviour(new WorldChangingBehaviour(msg));
                    }
                } else {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                    myAgent.send(reply);
                }
            } else {
                block();
            }
        }
    }

    private class PerceptReplyBehaviour extends OneShotBehaviour {

        ACLMessage msg;

        public PerceptReplyBehaviour(ACLMessage m) {
            super();
            msg = m;
        }

        public void action() {
            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            reply.setContent(generatePercept());
            myAgent.send(reply);
            System.out.println(getAID().getLocalName() + ": Sending percept " + reply.getContent());
        }

        private String generatePercept() {
            StringBuilder reply = new StringBuilder();
            reply.append("[");
            AgentPercept ap = wumpusWorld.getPercept();
            if (ap.isStench()) {
                reply.append("Here it stinks. ");
            } //Stench in this room everywhere.
            if (ap.isBreeze()) {
                reply.append("The wind blows. ");
            }//Breeze
            if (ap.isGlitter()) {
                reply.append("Something flashed. ");
            }
            if (ap.isBump()) {
                reply.append("Bump. ");
            }
            if (ap.isScream()) {
                reply.append("I hear a scream. ");
            }
            return reply.deleteCharAt(reply.length() - 1).append("]").toString();
        }
    }

    private class WorldChangingBehaviour extends OneShotBehaviour {

        ACLMessage msg;
        ArrayList<String> forwardKeyWords = new ArrayList<>();
        ArrayList<String> shootKeyWords = new ArrayList<>();
        ArrayList<String> climbKeyWords = new ArrayList<>();
        ArrayList<String> grabKeyWords = new ArrayList<>();
        ArrayList<String> rightKeyWords = new ArrayList<>();
        ArrayList<String> leftKeyWords = new ArrayList<>();

        public WorldChangingBehaviour(ACLMessage m) {
            super();
            msg = m;

            forwardKeyWords.add("forward");
            forwardKeyWords.add("ahead");
            forwardKeyWords.add("before");
            forwardKeyWords.add("along");

            shootKeyWords.add("shoot");
            shootKeyWords.add("fire");
            shootKeyWords.add("gun");

            climbKeyWords.add("climb");
            climbKeyWords.add("rise");
            climbKeyWords.add("lift");

            grabKeyWords.add("grab");
            grabKeyWords.add("take");
            grabKeyWords.add("capture");

            rightKeyWords.add("right");
            rightKeyWords.add("turnright");

            leftKeyWords.add("left");
            leftKeyWords.add("turnleft");
        }

        public void action() {
            String content = msg.getContent().toLowerCase();
            System.out.println(getAID().getLocalName() + ": Got action " + content);

            for (String forward : forwardKeyWords) {
                if (content.toLowerCase().contains(forward)) {
                    wumpusWorld.changeWorld("Forward");
                    return;
                }
            }

            for (String shoot : shootKeyWords) {
                if (content.toLowerCase().contains(shoot)) {
                    wumpusWorld.changeWorld("Shoot");
                    return;
                }
            }

            for (String climb : climbKeyWords) {
                if (content.toLowerCase().contains(climb)) {
                    wumpusWorld.changeWorld("Climb");
                    System.out.println(getAID().getLocalName() + ": Speleologist climbed out. Game finished successfully");
                    doDelete();
                    return;
                }
            }

            for (String grab : grabKeyWords) {
                if (content.toLowerCase().contains(grab)) {
                    wumpusWorld.changeWorld("Grab");
                    return;
                }
            }

            for (String right : rightKeyWords) {
                if (content.toLowerCase().contains(right)) {
                    wumpusWorld.changeWorld("TurnRight");
                    return;
                }
            }

            for (String left : leftKeyWords) {
                if (content.toLowerCase().contains(left)) {
                    wumpusWorld.changeWorld("TurnLeft");
                    return;
                }
            }
        }
    }
}