package ros;

import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;


public class ServiceClient {
	
	private Subscriber subscriber;
	private Publisher publisher;
	private boolean isResponse=false;
	private boolean isError=false;
	
	public ServiceClient(ConnectedNode connectedNode, String topic, String type) {
		String server_topic=topic+"_"+"server";
		String client_topic=topic+"_"+"client";
		this.subscriber=new Subscriber(connectedNode, client_topic, std_msgs.Int32._TYPE);
		this.publisher=new Publisher(connectedNode, server_topic, type);
		this.subscriber.addMessageListener(new MessageListener<Object>() {
			@Override
			public void onNewMessage(Object message) {
				isResponse=true;
				/*if(message.getData()==0) {
					isError=false;
				}else {
					isError=true;
				}*/
			}
		});
	}
	
	public ServiceClient publish(Object data) {
		publisher.publish(data);
		return this;
	}
	
	public void waitForServer() {
		while(!isResponse) {
			NodeHandle.duration(1);
		}
		isResponse=false;
	}
	
	public boolean isError() {
		return isError;
	}
	
}