
package speak.module;

import java.util.ArrayList;
import java.util.List;



public class SVOX_Pico {
	
	
	private String shellFile="svox.sh";
	
	private String storagePath;
	private String fileName;
	
	/******************************************************************************************
	 * 
	 * コンストラクター
	 * 
	 * @param storagePath
	 * @param fileName
	 */
	public SVOX_Pico(String storagePath, String fileName) {
		this.storagePath=storagePath;
		this.fileName=fileName;
	}
	
	/******************************************************************************************
	 * 
	 * 発話させる
	 * 
	 * @param text
	 */
	public void speak(String text) {
		createVoice(text);
		execute("aplay "+toPath(storagePath, fileName+".wav"), true, false);
	}
	
	/******************************************************************************************
	 * 
	 * 音声ファイルを作成
	 * 
	 * @param text
	 */
	private void createVoice(String text) {
		execute("bash "+toPath(storagePath, shellFile)+" "+text, true, false);
	}
	
	/******************************************************************************************
	 * @param command
	 * @param isWait
	 */
	private Process execute(String command, boolean isWait, boolean isInheritIO) {
		try {
			ProcessBuilder builder=new ProcessBuilder(toProcessCommand(command));
			if(isInheritIO) {
				builder.inheritIO();
			}
			Process process=builder.start();
			if(isWait) {
				process.waitFor();
			}
			return process;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/******************************************************************************************
	 * 
	 * @param args
	 * @return
	 */
	private String toPath(String... args) {
		if(args!=null) {
			String path=System.getProperty("user.home");
			for(String arg: args) {
				path+="/"+arg;
			}
			return path;
		}
		return null;
	}
	
	/******************************************************************************************
	 * 
	 * @param str
	 * @return
	 */
	private List<String> toProcessCommand(String command){
		List<String> result=new ArrayList<>();
		String[] splits=command.split(" ");
		for(String str:splits) {
			result.add(str);
		}
		return result;
	}
	
}
