import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import java.util.HashMap;

public class ExchangeAgent extends Agent {
    public HashMap<String, Order> lastTrade; // only the last trader transactions, to not run out of space
    public HashMap<String,Integer> history; // number of trades per trader

    protected void setup() {
        System.out.println("[EXCHANGE] Exchange Agent " + getAID().getName() + " is ready.");

        lastTrade = new HashMap<>();
        history = new HashMap<>();
        addBehaviour(new StockBehaviour());
    }

    private class StockBehaviour extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                addBehaviour(new MyRequestResponder(myAgent, MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));
            } else {
                block();
            }
        }

        class MyRequestResponder extends AchieveREResponder {

            public MyRequestResponder(Agent a, MessageTemplate mt) {
                super(a, mt);
            }

            @Override
            protected ACLMessage handleRequest(ACLMessage request) {
                System.out.println("[EXCHANGE] received an order from  " + request.getSender());
                try {
                    Order order = Order.deserialize(request.getContent());
                    lastTrade.put(String.valueOf(request.getSender()), order);

                    // get the current value for the key, or 0 if it doesn't exist
                    int currentValue = history.getOrDefault(String.valueOf(request.getSender()), 0);

                    int newValue = currentValue + 1;
                    history.put(String.valueOf(request.getSender()), newValue);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                ACLMessage response = new ACLMessage(ACLMessage.AGREE);
                response.setContent(Constants.COMPLETED_ORDER);
                return response;
            }

            @Override
            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
                // System.out.println("prepareResultNotification() method not re-defined");
                return null;
            }
        }
    }
}
