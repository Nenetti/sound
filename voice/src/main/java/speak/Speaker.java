
package speak;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;

import ros.ServiceClient;
import ros.ServiceServer;
import speak.module.Open_JTalk;
import speak.module.SVOX_Pico;



public class Speaker extends AbstractNodeMain {

	private String storagePath="ros/sound";
	private String fileName="talk";
	
	private boolean isProcess=false;
	
	Open_JTalk open_JTalk;
	SVOX_Pico svox_Pico;
	
	private ServiceServer voice_server_en;
	//private Publisher status_speaker;
	private ServiceClient mic_client;
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
		//status_speaker=new Publisher(connectedNode, "status/speaker", std_msgs.String._TYPE);
		mic_client=new ServiceClient(connectedNode, "status/mic", std_msgs.String._TYPE);
		voice_server_en=new ServiceServer(connectedNode, "sound/voice/speak_en", std_msgs.String._TYPE);
		voice_server_en.addMessageListener(new MessageListener<Object>() {
			@Override
			public void onNewMessage(Object message) {
				if(!isProcess) {
					isProcess=true;
					speak(((std_msgs.String)message).getData());
					voice_server_en.complete();
				}
			}
		});
	}
	
	public void speak(String data) {
		System.out.println(data);
		//mic_client.publish("off").waitForServer();
		svox_Pico.speak(data);
		//mic_client.publish("on").waitForServer();
		isProcess=false;
	}
	
}
