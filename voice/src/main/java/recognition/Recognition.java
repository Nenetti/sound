
package recognition;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.HashMap;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import recognition.module.Julius;
import recognition.module.Julius.Result;
import ros.NodeHandle;
import ros.ServiceClient;
import ros.ServiceServer;



public class Recognition extends AbstractNodeMain {

	private String path="ros/sound/julius";
	private String fileName="voice.wav";
	private String questionsName="quize.txt";

	private HashMap<String, String> questions;

	private boolean isProcess=false;

	private Subscriber<std_msgs.String> status_mic;
	private ServiceServer<std_msgs.String> command;
	private Julius julius;

	
	/******************************************************************************************
	 * 
	 */
	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("sounds/voice/recognition");
	}

	/******************************************************************************************
	 * 
	 * rosjavaのメインメソッド
	 */
	@Override
	public void onStart(ConnectedNode connectedNode) {
		julius = new Julius("localhost", 10500);
		status_mic = connectedNode.newSubscriber("status/mic", std_msgs.String._TYPE);
		status_mic.addMessageListener(new MessageListener<std_msgs.String>() {
			@Override
			public void onNewMessage(std_msgs.String message) {
				if(message!=null) {
					switch (message.getData()) {
					case "ON":case "on":
						julius.pause();
						break;
					case "OFF":case "off":
						julius.resume();
						break;
					}
				}
			}			
		});
		loadQuestions();
		Publisher<std_msgs.String> publisher=connectedNode.newPublisher("sound/voice/speak", std_msgs.String._TYPE);
		
	}
	
	public void answerThread() {
		while(!julius.isConnected())
		new Thread(new Runnable() {
			@Override
			public void run() {
				NodeHandle.duration(1000);
				//julius.playWav("active");
				while(true) {
					if(julius.isConnected()) {
						Result result=julius.recognition();
						if(result!=null) {
							status_mic.publish("off");
							result=result.replaceAll("_", " ");
							String answer=questions.get(result);
							if(answer==null) {
								answer="Sorry I don't know what you say";
							}
							System.out.println("A: "+answer);
							publisher_en.publish(answer);
							publisher_en.waitForServer();
							julius_en.playWav("active");
							status_mic.publish(createMessage("on"));
							julius_en.resume();
						}
					}
					duration(10);
				}
			}
		}).start();
	}
	

	public void loadQuestions() {
		try {
			questions=new HashMap<>();
			File file=new File(toPath(path, questionsName));
			BufferedReader reader=new BufferedReader(new FileReader(file));
			String line;
			while((line=reader.readLine())!=null) {
				String[] split=line.split("\t");
				String question=split[0];
				String answer=split[1];
				questions.put(question, answer);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/******************************************************************************************
	 * 
	 * @param args
	 * @return
	 */
	private String toPath(String... args) {
		if(args!=null) {
			String path=System.getProperty("user.home");
			for(String arg: args) {
				path+="/"+arg;
			}
			return path;
		}
		return null;
	}
	
}
