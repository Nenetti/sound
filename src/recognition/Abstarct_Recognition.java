
package recognition;


import dictionary.Dictionary;
import dictionary.Language;
import dictionary.Session;
import effect.Effect;
import effect.SE;
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

	protected boolean isStop;

	protected Language language;
	
	public enum Response {
		Yes,
		No;
	}
	
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
		isStop=true;
		julius.pause();
	}

	/******************************************************************************************
	 * 
	 * 
	 */
	public void resume() {
		isStop=false;
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

	protected boolean isQuestion(Session session) {
		if(session!=null) {
			switch (session.answer) {
			case "Yes":
			case "No":
				return true;
			}
		}
		return false;
	}

	protected boolean isTrash(Session session) {
		if(session!=null) {
			if(session.answer.equals("Trash")) {
				return true;
			}
		}
		return false;
	}

	protected boolean isSystemCall(Session session) {
		if(session!=null) {
			if(session.answer.equals("System Call")) {
				return true;
			}
		}
		return false;
	}
	
	protected void playActive() {
		
	}
	
}
