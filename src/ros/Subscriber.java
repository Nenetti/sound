package ros;

import org.ros.message.MessageListener;

import ros.MessageType.Type;

public class Subscriber{
	
	private org.ros.node.topic.Subscriber<Object> subscriber;
	public Type type;

	public Subscriber(String topic, String type) {
		this.type=Type.getType(type);
		this.subscriber=NodeHandle.connectedNode().newSubscriber(topic, type);
	}
	
	public void addMessageListener(MessageListener<Object> listener) {
		this.subscriber.addMessageListener(listener);
	}
}