package ros;

import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;

import ros.MessageType.Type;


public class ServiceClient {
	
	private Subscriber subscriber;
	private Publisher publisher;
	private Object result;
	private boolean isResponse=false;
	private boolean isError=false;
	private Type type;
	
	public ServiceClient(ConnectedNode connectedNode, String topic, String type) {
		constructor(connectedNode, topic, type, std_msgs.Int32._TYPE);
	}
	
	public ServiceClient(ConnectedNode connectedNode, String topic, String publish_type, String subscribe_type) {
		constructor(connectedNode, topic, publish_type, subscribe_type);
	}
	
	public void constructor(ConnectedNode connectedNode, String topic, String publish_type, String subscribe_type) {
		String server_topic=topic+"_"+"server";
		String client_topic=topic+"_"+"client";
		this.type=Type.getType(subscribe_type);
		this.subscriber=new Subscriber(connectedNode, client_topic, subscribe_type);
		this.publisher=new Publisher(connectedNode, server_topic, publish_type);
		this.subscriber.addMessageListener(new MessageListener<Object>() {
			@Override
			public void onNewMessage(Object message) {
				switch (type) {
				case Int32:
					result=((std_msgs.Int32)message).getData();
					break;
				case String:
					result=((std_msgs.String)message).getData();
					break;
				}
				isResponse=true;
			}
		});
	}
	
	public ServiceClient publish(Object data) {
		isResponse=false;
		publisher.publish(data);
		return this;
	}
	
	public Object waitForServer() {
		while(!isResponse) {
			NodeHandle.duration(1);
		}
		return getResult();
	}
	
	public Object getResult() {
		return result;
	}
	
	public boolean isError() {
		return isError;
	}
	
}