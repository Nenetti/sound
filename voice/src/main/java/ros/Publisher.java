package ros;

import java.util.List;

import org.ros.internal.node.topic.TopicParticipant;
import org.ros.node.ConnectedNode;

import ros.MessageType.Type;

public class Publisher{
	
	private org.ros.node.topic.Publisher<Object> publisher;
	public Type type;

	public Publisher(ConnectedNode connectedNode, String topic, String type) {
		this.type=Type.getType(type);
		publisher=connectedNode.newPublisher(topic, type);
	}
	
	@SuppressWarnings("unchecked")
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
			case LaserScan:
				break;
			case Marker:
				break;
			case MarkerArray:
				((visualization_msgs.MarkerArray)message).setMarkers((List<visualization_msgs.Marker>)data);
				break;
			case TF2:
				break;
			}
			publisher.publish(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Object newMessage() {
		return publisher.newMessage();
	}
}