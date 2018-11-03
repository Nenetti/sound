
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
	private String questionsName="quize.txt";


	private final static String REPEAT="OK, Please repeat once more your question.";
	private final static String NOANSWER="Sorry, I can't answer your question.";
	private final static String QUESTION="Sorry,, Are you Said,,, ";
	private final static String QUESTION2=", Yes or No.";
	private final static String CAUTION="Sorry, Please answer with, Yes or No.";
	private final static String OK="OK,, ";




	private HashMap<String, String> questions;

	private boolean isProcess=false;

	private ServiceServer mic_server;
	private Publisher mic_publisher;
	private Publisher se_publisher;
	private ServiceClient voice_client;
	private Julius julius;


	/******************************************************************************************
	 * 
	 * コンストラクター
	 * 
	 */
	public Recognition() {
		julius = new Julius("word.jconf", "localhost", 10500);
		NodeHandle.duration(2000);
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
		while(!julius.isConnected()) {NodeHandle.duration(1);}
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(julius.isConnected()) {
					Result result=julius.recognition();
					if(result!=null&&result.result!=null) {
						//mic_publisher.publish("off");
						String recognition=result.result.replaceAll("_", " ");
						String answer=questions.get(recognition);
						if(answer!=null) {
							if(result.score>1.1) {
								//正解なのでanswerはそのまま
							}else {
								//精度が微妙なので確認を取る
								String question=QUESTION+recognition+QUESTION2;
								publishVoice(question);
								while(true) {
									Result response=null;
									while((response=julius.recognition())==null);
									switch (response.result) {
									case "YES":case "Yes":case "yes":
										answer=OK+answer;
										break;
									case "NO":case "No":case "no":
										answer=REPEAT;
										break;
									default:
										publishVoice(CAUTION);
										continue;
									}
									break;
								}
							}
						}else {
							//聞き取れなかった
							answer=NOANSWER;
						}
						System.out.println("A: "+answer);
						publishVoice(answer);
					}

				}
			}
		}).start();
	}

	public void publishVoice(String text) {
		julius.pause();
		voice_client.publish(text).waitForServer();
		julius.resume();
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
