package ros;

import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import ros.MessageType.Type;

public class ServiceServer<T> {
	
	private Subscriber<T> subscriber;
	private Publisher<std_msgs.Int32> publisher;
	private Type type;

	
	public ServiceServer(ConnectedNode connectedNode, String topic, String type) {
		String server_topic=topic+"_"+"server";
		String client_topic=topic+"_"+"client";
		this.type=Type.getType(type);
		this.subscriber=connectedNode.newSubscriber(server_topic, type);
		this.publisher=connectedNode.newPublisher(client_topic, std_msgs.Int32._TYPE);
	}
	
	public void addMessageListener(MessageListener<T> messageListener) {
		this.subscriber.addMessageListener(messageListener);
	}
	
	public void publish(int v) {
		std_msgs.Int32 data=publisher.newMessage();
		data.setData(v);
		publisher.publish(data);
	}
	
	public void complete() {
		std_msgs.Int32 data=this.publisher.newMessage();
		data.setData(0);
		this.publisher.publish(data);
	}
	
}