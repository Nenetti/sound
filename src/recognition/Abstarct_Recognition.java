
package recognition;


import dictionary.Dictionary;
import dictionary.Language;
import dictionary.Session;
import dictionary.SessionType.Response;
import dictionary.SessionType.Type;
import sound_effect.Effect;
import sound_effect.SE;
import recognition.module.Julius;
import recognition.module.Julius.Result;
import ros.NodeHandle;
import ros.Publisher;
import ros.ServiceClient;
import ros.ServiceServer;



public abstract class Abstarct_Recognition {


	protected String REPEAT;
	protected String NOANSWER;
	protected String QUESTION;
	protected String CAUTION;
	protected String OK;


	protected Dictionary dictionary;

	protected ServiceServer mic_server;
	protected Publisher mic_publisher;
	protected Publisher se_publisher;
	protected ServiceClient voice_client;
	protected Julius julius;

	protected boolean isPause;

	protected Language language;
	
	
	
	/******************************************************************************************
	 * 
	 * @param dic
	 * @param host
	 * @param post
	 */
	public void startup(String dic, String host, String post) {
		julius = new Julius(dic, host, Integer.valueOf(post), language);
		NodeHandle.duration(2000);
		pause();
	}

	/******************************************************************************************
	 * 
	 */
	public void changeLanguage(boolean isChange, Language language) {
		/*
		if(isChange) {
			switch (language) {
			case English:
				Recognition_en.instance.pause();
				Recognition_jp.instance.resume();
				Recognition_jp.instance.publishVoice("日本語に変更します");
				break;
			case Japanese:
				Recognition_jp.instance.pause();
				Recognition_en.instance.resume();
				Recognition_en.instance.publishVoice("Changed English.");
				break;
			}
		}else {
			switch (language) {
			case English:
				publishVoice("No changed.");
				break;
			case Japanese:
				publishVoice("キャンセルしました");
				break;
			}
		}*/
	}

	/******************************************************************************************
	 * 
	 * 
	 */
	public void pause() {
		isPause=true;
		julius.pause();
	}

	/******************************************************************************************
	 * 
	 * 
	 */
	public void resume() {
		isPause=false;
		julius.resume();
	}

	protected void publishVoice(String text) {
		julius.pause();
		voice_client.publish(text).waitForServer();
		SE.play(Effect.Active);
		julius.resume();
	}

	protected Response toQuestion(String template, String question) {
		String sentence=template.replaceAll("\\$", question);
		SE.play(Effect.Question);
		publishVoice(sentence);
		while(true) {
			Result response=null;
			//nullの間はwhileで繰り返す
			while((response=julius.recognition())==null);
			Session session=dictionary.getSession(response.sentence, language);
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

	protected boolean isHighScore(double score) {
		return score==1.0 ? true : false;
	}
	
	protected boolean isAskedScore(double score) {
		return score>=0.5 ? true : false;
	}
	
	protected boolean isQuestion(Session session) {
		return session.type==Type.Question ? true : false;
	}
	
	protected boolean isResponse(Session session) {
		return session.type==Type.Response ? true : false;
	}

	protected boolean isTrash(Session session) {
		return session.type==Type.Trash ? true : false;
	}

	protected boolean isSystemCall(Session session) {
		return session.type==Type.SystemCall ? true : false;
	}
	
}
