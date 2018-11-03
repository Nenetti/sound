
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
import speak.Speaker.Language;



public class Recognition_jp extends Abstarct_Recognition{

	private String path="ros/sound/julius";
	private String questionsName="quize.txt";

	public static Recognition_jp instance=null;


	/******************************************************************************************
	 * 
	 * コンストラクター
	 * 
	 */
	public Recognition_jp() {
		super("word_jp.jconf", "localhost", 10501);
		super.REPEAT="OK。もう一度繰り返してください";
		super.NOANSWER="すいません。その質問には答えられません";
		super.QUESTION="すいません。あなたは";
		super.QUESTION2="、と言いましたか？ ";
		super.CAUTION="イエスかノーで答えてください";
		super.OK="OK,, ";
		instance=this;
		instance=this;
		loadQuestions(toPath(path, questionsName));
	}

	/******************************************************************************************
	 * 
	 * rosjavaのメインメソッド
	 */
	public void connect(ConnectedNode connectedNode) {
		voice_client=new ServiceClient(connectedNode, "sound/voice/speak_jp", std_msgs.String._TYPE);
		/*mic_publisher=new Publisher(connectedNode, "status/mic", std_msgs.String._TYPE);
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
		});*/
		answerThread();
	}

	
	

	public void answerThread() {
		//接続待機
		while(!julius.isConnected()) {NodeHandle.duration(1);}
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(julius.isConnected()) {
					if(!isStop) {
						Result result=julius.recognition();
						if(result!=null&&result.result!=null) {
							//mic_publisher.publish("off");
							String recognition=result.result.replaceAll("_", " ");
							if(recognition.equals("言語変更")) {
								switch (toQuestion("言語変更しますか？")) {
								case Yes:
									changeLanguage(true, Language.jp);
									break;
								case No:
									changeLanguage(false, Language.jp);
									break;
								}
								continue;
							}
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
					NodeHandle.duration(1);
				}
			}
		}).start();
	}

}
