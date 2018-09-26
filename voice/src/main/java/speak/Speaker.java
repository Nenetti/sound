
package speak;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import ros.ServiceServer;
import speak.module.Open_JTalk;
import speak.module.SVOX_Pico;



public class Speaker extends AbstractNodeMain {

	private String storagePath="ros/sound";
	private String fileName="talk";
	
	private boolean isProcess=false;
	
	Open_JTalk open_JTalk;
	SVOX_Pico svox_Pico;
	
	private ServiceServer<std_msgs.String> subscriber_jp;
	private ServiceServer<std_msgs.String> subscriber_en;
	private Publisher<std_msgs.String> status_speaker;
	/******************************************************************************************
	 * 
	 * コンストラクター
	 */
	public Speaker() {
		this.open_JTalk=new Open_JTalk(storagePath, fileName);
		this.svox_Pico=new SVOX_Pico(storagePath, fileName);
	}
	
	/******************************************************************************************
	 * 
	 */
	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("sounds/voice/speak");
	}

	/******************************************************************************************
	 * 
	 * メインメソッド
	 */
	@Override
	public void onStart(ConnectedNode connectedNode) {
		status_speaker=connectedNode.newPublisher("status/speaker", std_msgs.String._TYPE);
		subscriber_jp = new ServiceServer<>(connectedNode, "sound/voice/speak_jp", std_msgs.String._TYPE);
		subscriber_jp.addMessageListener(new MessageListener<std_msgs.String>() {
			@Override
			public void onNewMessage(std_msgs.String message) {
				if(!isProcess) {
					isProcess=true;
					System.out.println(message.getData());
					status_speaker.publish(createMessage("on"));
					open_JTalk.speak(message.getData());
					status_speaker.publish(createMessage("off"));
					isProcess=false;
					subscriber_jp.complete();
				}
			}
		});
		subscriber_en = new ServiceServer<>(connectedNode, "sound/voice/speak_en", std_msgs.String._TYPE);
		subscriber_en.addMessageListener(new MessageListener<std_msgs.String>() {
			@Override
			public void onNewMessage(std_msgs.String message) {
				if(!isProcess) {
					isProcess=true;
					System.out.println(message.getData());
					status_speaker.publish(createMessage("on"));
					svox_Pico.speak(message.getData());
					status_speaker.publish(createMessage("off"));
					isProcess=false;
					subscriber_en.complete();
				}
			}
		});
	}
	
	private std_msgs.String createMessage(String data) {
		std_msgs.String message=status_speaker.newMessage();
		message.setData(data);
		return message;
	}
	
}
