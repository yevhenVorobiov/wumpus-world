package ua.nure.vorobiov.wumpus;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import ua.nure.vorobiov.wumpus.agent.EnvironmentAgent;
import ua.nure.vorobiov.wumpus.agent.NavigatorAgent;
import ua.nure.vorobiov.wumpus.agent.SpeleologistAgent;

import static jade.core.Runtime.instance;

public class Application {
    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        properties.setProperty(Profile.GUI, Boolean.TRUE.toString());

        Profile profile = new ProfileImpl(properties);
        AgentContainer agentContainer = instance().createMainContainer(profile);

        agentContainer.acceptNewAgent(EnvironmentAgent.class.getSimpleName(), new EnvironmentAgent()).start();
        agentContainer.acceptNewAgent(NavigatorAgent.class.getSimpleName(), new NavigatorAgent()).start();
        agentContainer.acceptNewAgent(SpeleologistAgent.class.getSimpleName(), new SpeleologistAgent()).start();
    }
}
