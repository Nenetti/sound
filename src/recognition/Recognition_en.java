
package recognition;

import dictionary.Language;
import dictionary.Session;
import dictionary.Sessions;
import recognition.module.Julius.Result;
import ros.NodeHandle;
import ros.ServiceClient;
import ros.ServiceServer;



public class Recognition_en extends Abstarct_Recognition{

	private String questionsName="quize.txt";

	public static Recognition_en instance=null;
	
	private Language language=Language.English;

	

	/******************************************************************************************
	 * 
	 * コンストラクター
	 * 
	 */
	public Recognition_en() {
		super("word_en.jconf", "localhost", 10500);
		super.REPEAT="OK, Please repeat once more your question.";
		super.NOANSWER="Sorry, I can't answer your question.";
		super.QUESTION="Sorry,, Are you Said,,, ";
		super.QUESTION2=", Yes or No.";
		super.CAUTION="Sorry, Please answer with, Yes or No.";
		super.OK="OK,, ";
		instance=this;
		loadQuestions(toPath(path, questionsName));
	}

	/******************************************************************************************
	 * 
	 * rosjavaのメインメソッド
	 */
	public void connect() {
		super.voice_client=new ServiceClient("sound/voice/speak_en", std_msgs.String._TYPE);
		//super.mic_publisher=new Publisher(connectedNode, "status/mic", std_msgs.String._TYPE);
		super.mic_server = new ServiceServer("status/mic", std_msgs.String._TYPE);
		super.mic_server.addMessageListener((Object message)->{
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
		});
		/*super.mic_server.addMessageListener(new MessageListener<Object>() {
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
		});*/
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
						if(result!=null&&result.result!=null) {
							String recognition=result.result.replaceAll("_", " ");
							System.out.println(recognition+" = "+recognition.equals("Change Language"));
							if(recognition.equals("Change Language")) {
								switch (toQuestion("Do you want to Change Language ?")) {
								case Yes:
									changeLanguage(true, language);
									break;
								case No:
									changeLanguage(false, language);
									break;
								}
								continue;
							}
							String answer=null;
							Sessions sessions=dictionary.getSession(recognition, language);
							if(sessions!=null) {
								Session session=sessions.getSession(language);
								if(session!=null) {
									if(result.score==1.0) {
										//おそらく正解
										answer=session.answer;
									}else {
										//精度が微妙なので確認を取る
										String question=QUESTION+recognition+QUESTION2;
										switch (toQuestion(question)) {
										case Yes:
											answer=OK+session.answer;
											break;
										case No:
											answer=REPEAT;
											break;
										}
									}
								}
							}
							if(answer==null) {
								//聞き取れなかった
								answer=NOANSWER;
							}
							System.out.println("A: "+recognition);
							publishVoice(answer);
						}
					}
					NodeHandle.duration(1);
				}
			}
		}).start();
	}
}
