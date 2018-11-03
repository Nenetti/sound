package ros;

import org.ros.node.ConnectedNode;

import ros.MessageType.Type;

public class Publisher{
	
	private org.ros.node.topic.Publisher<Object> publisher;
	public Type type;

	public Publisher(ConnectedNode connectedNode, String topic, String type) {
		this.type=Type.getType(type);
		publisher=connectedNode.newPublisher(topic, type);
	}
	
	public void publish(Object data) {
		try {
			Object message=publisher.newMessage();
			switch (type) {
			case String:
				((std_msgs.String)message).setData((String)data);
				break;
			case Int32:
				((std_msgs.Int32)message).setData((int)data);
				break;
			default:
				System.out.println("未実装メッセージ型です. "+data);
				break;
			}
			publisher.publish(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}