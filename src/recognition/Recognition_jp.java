
package recognition;

import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;

import dictionary.Language;
import dictionary.Session;
import dictionary.Sessions;
import recognition.module.Julius.Result;
import ros.NodeHandle;
import ros.ServiceClient;
import ros.ServiceServer;




public class Recognition_jp extends Abstarct_Recognition{

	private String questionsName="/dictionary/session.csv";

	public static Recognition_jp instance=null;
	

	/******************************************************************************************
	 * 
	 * コンストラクター
	 * 
	 */
	public Recognition_jp() {
		super("session_jp.jconf", "localhost", 10501);
		super.REPEAT="OK。もう一度繰り返してください";
		super.NOANSWER="すいません。その質問には答えられません";
		super.QUESTION="すいません。あなたは";
		super.QUESTION2="、と言いましたか？ ";
		super.CAUTION="イエスかノーで答えてください";
		super.OK="OK,, ";
		super.language=Language.Japanese;
		instance=this;
		loadQuestions(toPath(path, questionsName));
	}

	/******************************************************************************************
	 * 
	 * rosjavaのメインメソッド
	 */
	public void connect() {
		super.voice_client=new ServiceClient("sound/voice/speak_jp", std_msgs.String._TYPE);
		super.mic_server = new ServiceServer("status/mic", std_msgs.String._TYPE);
		super.mic_server.addMessageListener(new MessageListener<Object>() {
			@Override
			public void onNewMessage(Object message) {
				if(message!=null) {
					String data=((std_msgs.String)message).getData();
					System.out.println("受信: "+data);
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

	/******************************************************************************************
	 * 
	 * 
	 */
	public void answerThread() {
		//接続待機
		while(!julius.isConnected()) {NodeHandle.duration(1);}
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(julius.isConnected()) {
					if(!isStop) {
						Result result=julius.recognition();
						if(result!=null) {
							String answer=null;
							Session session=dictionary.getSession(result.sentence, language);
							if(session!=null) {
								if(isQuestion(session.answer)){continue;}
								if(isTrash(session.answer)){continue;}
								if(isSystemCall(session.answer)) {
									//SystemCall
									continue;
								}
								if(result.score==1.0) {
									//おそらく正解
									answer=session.answer;
								}else {
									//精度が微妙なので確認を取る
									switch (toQuestion(QUESTION+result.sentence+QUESTION2)) {
									case Yes:
										answer=OK+session.answer;
										break;
									case No:
										answer=REPEAT;
										break;
									}
								}
							}
							if(answer==null) {
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
