
package recognition;

import java.awt.geom.Area;
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

import recognition.module.Julius;
import recognition.module.Julius.Result;
import ros.NodeHandle;
import ros.Publisher;
import ros.ServiceClient;
import ros.ServiceServer;
import ros.Subscriber;



public class Recognition extends AbstractNodeMain {

	private String path="ros/sound/julius";
	private String fileName="voice.wav";
	private String questionsName="quize.txt";

	private HashMap<String, String> questions;

	private boolean isProcess=false;

	private ServiceServer mic_server;
	private Publisher mic_publisher;
	private Publisher se_publisher;
	private ServiceClient voice_client;
	private Julius julius;
	private Julius julius_question;

	
	/******************************************************************************************
	 * 
	 * コンストラクター
	 * 
	 */
	public Recognition() {
		julius = new Julius("word.jconf", "localhost", 10500);
		julius_question = new Julius("response.jconf", "localhost", 10501);
		loadQuestions();
	}

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
		voice_client=new ServiceClient(connectedNode, "sound/voice/speak_en", std_msgs.String._TYPE);
		mic_publisher=new Publisher(connectedNode, "status/mic", std_msgs.String._TYPE);
		mic_server = new ServiceServer(connectedNode, "status/mic", std_msgs.String._TYPE);
		mic_server.addMessageListener(new MessageListener<Object>() {
			@Override
			public void onNewMessage(Object message) {
				if(message!=null) {
					String data=((std_msgs.String)message).getData();
					switch (data) {
					case "ON":case "on":
						julius.resume();
						break;
					case "OFF":case "off":
						julius.pause();
						julius_question.pause();
						break;
					}
					mic_server.complete();
				}
			}			
		});
		answerThread();
	}


	public void answerThread() {
		//接続待機
		while(!julius.isConnected()||!julius_question.isConnected()) {NodeHandle.duration(1);}
		julius_question.pause();
		new Thread(new Runnable() {
			@Override
			public void run() {
				//se_publisher.publish("active");
				System.out.println("開始");
				while(julius.isConnected()) {
					Result result=julius.recognition();
					if(result!=null&&result.result!=null) {
						//mic_publisher.publish("off");
						String recognition=result.result.replaceAll("_", " ");
						String answer=questions.get(recognition);
						if(answer!=null) {
							if(result.score>0.9) {
								String question="Sorry,, Are you Said,,, "+recognition;
								julius.pause();
								voice_client.publish(question).waitForServer();
								julius_question.resume();
								Result response=null;
								while((response=julius_question.recognition())==null);
								julius_question.pause();
								switch (response.result) {
								case "Yes":
									answer="OK, "+answer;
									break;
								case "No":
									answer="Sorry, I can't answer your question.";
									break;
								}
							}else if(result.score>=0.5) {
								
							}
						}else {
							answer="Sorry, I can't answer your question.";
						}
						System.out.println("A: "+answer);
						voice_client.publish(answer).waitForServer();
						julius.resume();
					}else {
						//mic_publisher.publish("off");
					}
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
