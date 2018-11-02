package ros;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;

import org.ros.internal.message.Message;
import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import ros.MessageType.Type;

public class ServiceClient<T> {
	
	private Subscriber<std_msgs.Int32> subscriber;
	private Publisher<T> publisher;
	private boolean isResponse=false;
	private boolean isError=false;
	private Type type;
	
	public ServiceClient(ConnectedNode connectedNode, String topic, String type) {
		String server_topic=topic+"_"+"server";
		String client_topic=topic+"_"+"client";
		this.type=Type.getType(type);
		this.subscriber=connectedNode.newSubscriber(client_topic, std_msgs.Int32._TYPE);
		this.publisher=connectedNode.newPublisher(server_topic, type);
		this.subscriber.addMessageListener(new MessageListener<std_msgs.Int32>() {
			@Override
			public void onNewMessage(std_msgs.Int32 message) {
				isResponse=true;
				if(message.getData()==0) {
					isError=false;
				}else {
					isError=true;
				}
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public void publish(Object obj) {
		try {
			switch (type) {
			case String:
				std_msgs.String string=((std_msgs.String)publisher.newMessage());
				string.setData((String)obj);
				publisher.publish((T)string);
				break;
			case Int32:
				std_msgs.Int32 int32=((std_msgs.Int32)publisher.newMessage());
				int32.setData((int)obj);
				publisher.publish((T)int32);
				break;
			default:
				break;
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void waitForServer() {
		while(!isResponse) {
			duration(10);
		}
		isResponse=false;
	}
	
	public boolean isError() {
		return isError;
	}
	
	private void duration(int time) {
		try {
			Thread.sleep(time);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}