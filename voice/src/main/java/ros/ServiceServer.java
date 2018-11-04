package ros;

import org.ros.message.MessageListener;


public class ServiceServer {
	
	private Subscriber subscriber;
	private Publisher publisher;
	
	public ServiceServer(String topic, String type) {
		String server_topic=topic+"_"+"server";
		String client_topic=topic+"_"+"client";
		this.subscriber=new Subscriber(server_topic, type);
		this.publisher=new Publisher(client_topic, std_msgs.Int32._TYPE);
	}
	
	public void addMessageListener(MessageListener<Object> messageListener) {
		this.subscriber.addMessageListener(messageListener);
	}
	
	public void complete() {
		this.publisher.publish(0);
	}
	
}