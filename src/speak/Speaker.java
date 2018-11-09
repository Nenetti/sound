
package speak;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;

import dictionary.Language;
import ros.NodeHandle;
import ros.ServiceClient;
import ros.ServiceServer;
import speak.module.VoiceMaker;



public class Speaker extends AbstractNodeMain {

	private String storagePath="ros/sound";
	private String fileName="voice";

	private boolean isProcess=false;

	private ServiceServer voice_server_jp;
	private ServiceServer voice_server_en;
	//private Publisher status_speaker;
	private ServiceClient mic_client;
	private VoiceMaker voice_jp;
	private VoiceMaker voice_en;	
	
	/******************************************************************************************
	 * 
	 * コンストラクター
	 */
	public Speaker() {
		this.voice_jp=new VoiceMaker(storagePath, fileName, Language.Japanese);
		this.voice_en=new VoiceMaker(storagePath, fileName, Language.English);
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
		mic_client=new ServiceClient("status/mic", std_msgs.String._TYPE);
		voice_server_jp=new ServiceServer("sound/voice/speak_jp", std_msgs.String._TYPE);
		voice_server_en=new ServiceServer("sound/voice/speak_en", std_msgs.String._TYPE);
		voice_server_jp.addMessageListener(new MessageListener<Object>() {
			@Override
			public void onNewMessage(Object message) {
				if(!isProcess) {
					isProcess=true;
					mic_client.publish("OFF");
					speak(((std_msgs.String)message).getData(), Language.Japanese);
					voice_server_jp.complete();
					mic_client.publish("ON");
				}
			}
		});
		voice_server_en.addMessageListener(new MessageListener<Object>() {
			@Override
			public void onNewMessage(Object message) {
				if(!isProcess) {
					isProcess=true;
					mic_client.publish("OFF");
					speak(((std_msgs.String)message).getData(), Language.English);
					voice_server_en.complete();
					mic_client.publish("ON");
				}
			}
		});
	}

	public void speak(String text, Language language) {
		System.out.println(language+" : "+text);
		//mic_client.publish("off").waitForServer();
		switch (language) {
		case English:
			voice_en.speak(text);
			break;
		case Japanese:
			voice_jp.speak(text);
			break;
		}
		//mic_client.publish("on").waitForServer();
		isProcess=false;
	}

}
