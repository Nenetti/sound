package ros;

import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;


public class ServiceServer {
	
	private Subscriber subscriber;
	private Publisher publisher;
	
	public ServiceServer(ConnectedNode connectedNode, String topic, String type) {
		String server_topic=topic+"_"+"server";
		String client_topic=topic+"_"+"client";
		this.subscriber=new Subscriber(connectedNode, server_topic, type);
		this.publisher=new Publisher(connectedNode, client_topic, std_msgs.Int32._TYPE);
	}
	
	public void addMessageListener(MessageListener<Object> messageListener) {
		this.subscriber.addMessageListener(messageListener);
	}
	
	public void complete() {
		this.publisher.publish(0);
	}
	
}