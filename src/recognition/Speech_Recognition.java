
package recognition;

import dictionary.Dictionary;
import dictionary.Language;
import dictionary.Session;
import dictionary.SessionType.Response;
import recognition.module.Julius.Result;
import ros.NodeHandle;
import ros.ServiceClient;
import ros.UserProperty;
import sound_effect.Effect;
import sound_effect.SE;



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
		super.dictionary=new Dictionary(UserProperty.get("julius.dictionary.dir")+"/"+UserProperty.get("julius.session"));
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
				if(isPause) { NodeHandle.duration(1); continue; }
				//
				Result result=julius.recognition();
				if(result==null) { continue; }
				//
				Session session=dictionary.getSession(result.sentence, language);
				if(session==null) {continue;}
				if(isQuestion(session)){
					QuestionThread(result, session);
				}
			}
		}).start();
	}
	
	/******************************************************************************************
	 * 
	 * @param result
	 * @param session
	 */
	public void QuestionThread(Result result, Session session) {
		if(isHighScore(result.score)) {
			//正解
			publishVoice(session.answer);
			return;
		}else if(isAskedScore(result.score)) {
			//質疑判定
			switch (ResponseThread(result)) {
			case Yes:
				publishVoice(OK+session.answer);
				break;
			case No:
				publishVoice(REPEAT);
				break;
			}
			return;
		}
		//精度低すぎ。聞き取り不可
		publishVoice(NOANSWER);
	}
	
	/******************************************************************************************
	 * 
	 * @param result
	 * @return
	 */
	public Response ResponseThread(Result result) {
		String sentence=QUESTION.replaceAll("\\$", result.sentence);
		SE.play(Effect.Question);
		publishVoice(sentence);
		Result response;
		Session session;
		while(true) {
			while((response=julius.recognition())==null);
			session=dictionary.getSession(response.sentence, language);
			if(session!=null) {
				switch (session.answer) {
				case "Yes":
					return Response.Yes;
				case "No":
					return Response.No;
				default:
					SE.play(Effect.Question);
					publishVoice(CAUTION);
					continue;
				}
			}
		}
	}
}
