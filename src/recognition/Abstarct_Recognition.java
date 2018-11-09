
package recognition;


import dictionary.Dictionary;
import dictionary.Language;
import dictionary.Session;
import recognition.module.Julius;
import recognition.module.Julius.Result;
import ros.NodeHandle;
import ros.Publisher;
import ros.ServiceClient;
import ros.ServiceServer;



public abstract class Abstarct_Recognition {

	protected String path="ros/sound/julius";

	protected String REPEAT;
	protected String NOANSWER;
	protected String QUESTION;
	protected String QUESTION2;
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
	 * コンストラクター
	 * 
	 */
	protected Abstarct_Recognition(String dic, String host, int post) {
		julius = new Julius(dic, host, post);
		NodeHandle.duration(2000);
		pause();
	}	

	/******************************************************************************************
	 * 
	 */
	public void changeLanguage(boolean isChange, Language language) {
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
		}
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

	protected String repeat() {
		String answer=null;
		while(true) {
			Result response=null;
			while((response=julius.recognition())==null);
			switch (response.sentence) {
			case "YES":case "Yes":case "yes":case "はい":
				return OK+answer;
			case "NO":case "No":case "no":case "いいえ":
				return REPEAT;
			default:
				publishVoice(CAUTION);
				continue;
			}
		}
	}
	
	
	protected void publishVoice(String text) {
		julius.pause();
		voice_client.publish(text).waitForServer();
		julius.resume();
	}

	protected Response toQuestion(String question) {
		publishVoice(question);
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
					publishVoice(CAUTION);
					continue;
				}
			}
		}
	}
	
	protected boolean isQuestion(String question) {
		Session session=dictionary.getSession(question, language);
		if(session!=null) {
			switch (session.answer) {
			case "Yes":
			case "No":
				return true;
			}
		}
		return false;
	}
	
	protected boolean isTrash(String question) {
		Session session=dictionary.getSession(question, language);
		if(session!=null) {
			if(session.answer.equals("Trash")) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean isSystemCall(String question) {
		Session session=dictionary.getSession(question, language);
		if(session!=null) {
			if(session.answer.equals("System Call")) {
				return true;
			}
		}
		return false;
	}

	protected void loadQuestions(String path) {
		try {
			this.dictionary=new Dictionary(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/******************************************************************************************
	 * 
	 * @param args
	 * @return
	 */
	protected String toPath(String... args) {
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
