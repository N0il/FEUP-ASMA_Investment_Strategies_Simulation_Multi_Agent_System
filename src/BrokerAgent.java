import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.ContractNetResponder;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class BrokerAgent extends Agent {
    // TODO: check that only the commissions if, the allowed array and the AID are going to be different
    private List<Constants.ORDER_TYPES> allowedOrders = new ArrayList<>();

    protected void setup() {
        System.out.println("[BROKER] Broker Agent " + getAID().getName() + " is ready.");
        addBehaviour(new HandleOrderBehaviour());

        // initialize the list of allowed orders
        allowedOrders.add(Constants.ORDER_TYPES.SELL);
        allowedOrders.add(Constants.ORDER_TYPES.BUY);
        allowedOrders.add(Constants.ORDER_TYPES.SHORT);
    }

    private class HandleOrderBehaviour extends CyclicBehaviour {
        class MyContractNetResponder extends ContractNetResponder{

            public MyContractNetResponder(Agent a, MessageTemplate mt) {
                super(a, mt);
            }

            @Override
            protected ACLMessage handleCfp(ACLMessage cfp) {
                // check commissions and types of orders allowed
                double commission = 0.02; // default commission
/*
                try {
                    Order order = Order.deserialize(cfp.getContent());

                    double totalValue = order.getQuantity() * order.getValuePerAsset();

                    if (allowedOrders.contains(order.getOrderType())) {
                        if (totalValue <= 1000){
                            commission = 0.1;
                        } else if (totalValue > 1000 && totalValue < 10000){
                            commission = 0.05;
                        } else if (totalValue >= 10000) {
                            commission = 0.01;
                        }
                    } else {
                        // Send a refuse message
                        ACLMessage refuse = cfp.createReply();
                        refuse.setPerformative(ACLMessage.REFUSE);
                        refuse.setContent(Constants.UNSUPPORTED_ORDER_TYPE);
                        myAgent.send(refuse);
                        return null;
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }*/

                // Respond with best commission offer
                ACLMessage propose = cfp.createReply();
                propose.setPerformative(ACLMessage.PROPOSE);
                propose.setContent(Double.toString(commission));
                return propose;
            }

            @Override
            protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
                System.out.println("IN ACCEPT PROPOSAL");
                // Send order to Exchange agent
                ACLMessage orderMessage = new ACLMessage(ACLMessage.REQUEST);
                orderMessage.addReceiver(new AID(Constants.EXCHANGE_AGENT_NAME, AID.ISLOCALNAME));
                orderMessage.setContent(cfp.getContent());
                send(orderMessage);

                // Send confirmation message to Trader agent
                ACLMessage inform = accept.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                return inform;
            }
        }

        class MyRequestInitiator extends AchieveREInitiator {

			public MyRequestInitiator(Agent a, ACLMessage msg) {
				super(a, msg);
			}
			
			protected Vector<ACLMessage> prepareRequests(ACLMessage request) {
				Vector<ACLMessage> v = new Vector<ACLMessage>();
				v.add(request);
				return v;
			}
			
			protected void handleAgree(ACLMessage agree) {
				System.out.println("Broker agent handling agree message: " + agree.getContent());
			}
			
			protected void handleRefuse(ACLMessage refuse) {
				System.out.println("Broker agent handling refuse message: " + refuse.getContent());
			}
			
			protected void handleFailure(ACLMessage failure) {
				System.out.println("Broker agent handling failure message: " + failure.getContent());
			}
			
			protected void handleInform(ACLMessage inform) {
				System.out.println("Broker agent handling inform message: " + inform.getContent());
			}
			
			protected void handleAllResponses(Vector responses) {
				System.out.println("Broker agent handling all responses");
			}
			
			protected void handleAllResultNotifications(Vector notifications) {
				System.out.println("Broker agent handling all notifications");
			}
			
        	
        }
        public void action() {
            // Receive order messages from Trader agents
            ACLMessage msg = receive();
            if (msg != null) {
                // Handle order using Contract Net Protocol
                addBehaviour(new MyContractNetResponder(myAgent, MessageTemplate.MatchPerformative(ACLMessage.CFP)));
                addBehaviour(new MyRequestInitiator(myAgent, null));
            } else {
                block();
            }
        }
    }
}
