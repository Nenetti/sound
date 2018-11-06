
package speak.module;

import process.Terminal;
import speak.Speaker.Language;




public class VoiceMaker {
	
	private String shell;
	private String home=System.getProperty("user.home");
	private String path;
	private String[] exec_play;

	/******************************************************************************************
	 * 
	 * コンストラクター
	 * 
	 * @param path
	 * @param fileName
	 */
	public VoiceMaker(String path, String fileName, Language language) {
		switch (language) {
		case jp:
			shell="open_JTalk.sh";
			break;
		case en:
			shell="svox.sh";
			break;
		}
		this.path=path;
		this.exec_play=new String[]{"aplay", home+"/"+path+"/"+fileName+".wav"};
	}
	
	/******************************************************************************************
	 * 
	 * 発話させる
	 * 
	 * @param text
	 */
	public void speak(String text) {
		createVoice(text);
		Terminal.execute(exec_play, true, false);
	}
	
	/******************************************************************************************
	 * 
	 * 音声ファイルを作成
	 * 
	 * @param text
	 */
	private void createVoice(String text) {
		String[] exec_create=new String[]{"bash", home+"/"+path+"/"+shell, text};
		Terminal.execute(exec_create, true, false);
	}
}
