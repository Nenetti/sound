package ros;

import org.ros.node.ConnectedNode;

import ros.MessageType.Type;

public class Publisher<T>{
	
	private org.ros.node.topic.Publisher<T> publisher;
	private Type type;

	public Publisher(ConnectedNode connectedNode, String topic, String type) {
		publisher=connectedNode.newPublisher(topic, type);
		this.type=Type.getType(type);
	}
	
	@SuppressWarnings("unchecked")
	public void publish(Object data) {
		try {
			switch (type) {
			case String:
				std_msgs.String string=((std_msgs.String)publisher.newMessage());
				string.setData((String)data);
				publisher.publish((T)string);
				break;
			case Int32:
				std_msgs.Int32 int32=((std_msgs.Int32)publisher.newMessage());
				int32.setData((int)data);
				publisher.publish((T)int32);
				break;
			default:
				System.out.println("未実装メッセージ型です");
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}