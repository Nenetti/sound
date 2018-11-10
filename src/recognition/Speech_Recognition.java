
package recognition;

import dictionary.Dictionary;
import dictionary.Language;
import dictionary.Session;
import recognition.module.Julius.Result;
import ros.NodeHandle;
import ros.ServiceClient;
import ros.UserProperty;



public class Speech_Recognition extends Abstarct_Recognition{

	/******************************************************************************************
	 * 
	 * コンストラクター
	 * 
	 */
	public Speech_Recognition(Language language) {
		super.language=language;
		switch (language) {
		case English:
			startup(UserProperty.get("julius.en.jconf"), UserProperty.get("julius.en.host"), UserProperty.get("julius.en.port"));
			super.REPEAT=UserProperty.get("Response.en.Repeat");
			super.NOANSWER=UserProperty.get("Response.en.NoAnswer");
			super.QUESTION=UserProperty.get("Response.en.Question");
			super.CAUTION=UserProperty.get("Response.en.Caution");
			super.OK=UserProperty.get("Response.en.OK");
			super.voice_client=new ServiceClient("sound/voice/speak_en", std_msgs.String._TYPE);
			break;
		case Japanese:
			startup(UserProperty.get("julius.jp.jconf"), UserProperty.get("julius.jp.host"), UserProperty.get("julius.jp.port"));
			super.REPEAT=UserProperty.get("Response.jp.Repeat");
			super.NOANSWER=UserProperty.get("Response.jp.NoAnswer");
			super.QUESTION=UserProperty.get("Response.jp.Question");
			super.CAUTION=UserProperty.get("Response.jp.Caution");
			super.OK=UserProperty.get("Response.jp.OK");
			super.voice_client=new ServiceClient("sound/voice/speak_jp", std_msgs.String._TYPE);
			break;
		}
		super.language=language;
		super.dictionary=new Dictionary(System.getProperty("user.home")+"/"+UserProperty.get("julius.home")+"/"+UserProperty.get("julius.dictionary")+"/"+UserProperty.get("julius.session"));
		startAnswerThread();
	}

	/******************************************************************************************
	 * 
	 * 
	 */
	public void startAnswerThread() {
		while(!julius.isConnected()) {NodeHandle.duration(1);}
		new Thread(()->{
			while(julius.isConnected()) {
				if(isStop) { NodeHandle.duration(1); continue; }
				Result result=julius.recognition();
				if(result==null) { continue; }
				Session session=dictionary.getSession(result.sentence, language);
				if(isQuestion(session.question)){continue;}
				if(isTrash(session.question)){continue;}
				if(isSystemCall(session.answer)) {
					//SystemCall
					continue;
				}
				if(result.score==1.0) {
					//正解
					publishVoice(session.answer);
					continue;
				}else if(result.score>0.5) {
					//質疑判定
					switch (toQuestion(QUESTION, result.sentence)) {
					case Yes:
						publishVoice(OK+session.answer);
						break;
					case No:
						publishVoice(REPEAT);
						break;
					}
					continue;
				}
				//精度低すぎ。聞き取り不可
				publishVoice(NOANSWER);
				System.out.println("Answer: "+result.sentence);
			}
		}).start();
	}
}
