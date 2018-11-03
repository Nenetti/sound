
package speak.module;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import process.Terminal;



public class SVOX_Pico {
	
	private String shell="svox.sh";
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
	public SVOX_Pico(String path, String fileName) {
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
