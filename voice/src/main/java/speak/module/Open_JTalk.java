
package speak.module;

import java.io.File;
import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;




public class Open_JTalk {


	private HashMap<String, String> dictionaries;
	private HashMap<String, String> voices;

	private String path="ros/sound/open_JTalk";
	private String dictionary="dictionary";
	private String voice="voices";
	private String useCharacter="mei_normal";

	private String storagePath;
	private String fileName;
	private Properties parameters;
	
	/******************************************************************************************
	 * 
	 * コンストラクター
	 * 
	 * @param storagePath
	 * @param fileName
	 */
	public Open_JTalk(String storagePath, String fileName) {
		this.dictionary=toPath(path, dictionary);
		this.voice=toPath(path, voice);
		this.parameters=createParameters(useCharacter);
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
		createVoice(text, parameters);
		duration(100);
		execute("aplay "+toPath(storagePath, fileName+".wav"), true, false);
	}

	/******************************************************************************************
	 * 
	 * 発話用パラメータの設定
	 * 
	 * @param name
	 * @return
	 */
	private Properties createParameters(String name) {
		Properties param = new Properties();
		//詳細はここに載ってる
		//http://moblog.absgexp.net/openjtalk/#i-7
		param.put("x", "UTF-8");
		param.put("m", name);
		//param.put("g", "1.0"); 	// 音量	なぜかこれがあると通らない
		param.put("a", "0.5"); // オールパス
		param.put("b", "0.3"); 	// ポストフィルター
		param.put("r", "1"); 	// 速度
		param.put("fm", "2.5");	//音の高さ
		//param.put("u", "0"); 	// 有声無声境界値
		param.put("jm", "1");	// 音質
		param.put("jf", "3");	// 抑揚
		return param;
	}

	/******************************************************************************************
	 * 
	 * 音声ファイルを作成
	 * 
	 * @param text
	 * @param param
	 */
	private void createVoice(String text, Properties param) {
		if (param.size() == 0 || (param.get("x") == null || param.get("m") == null)) throw new InvalidParameterException();

		String script_dir = generateScript(text);
		dictionaries = loadDictionaries();
		voices = loadVoices();

		String command = "open_jtalk -x " + dictionaries.get(param.get("x")) + " -m " + voices.get(param.get("m"));
		if (param.get("s") != null) {command += " -s " + param.get("s");}
		if (param.get("p") != null) {command += " -p " + param.get("p");}
		if (param.get("a") != null) {command += " -a " + param.get("a");}
		if (param.get("b") != null) {command += " -b " + param.get("b");}
		if (param.get("r") != null) {command += " -r " + param.get("r");}
		if (param.get("fm") != null) {command += " -fm " + param.get("fm");}
		if (param.get("u") != null) {command += " -u " + param.get("u");}
		if (param.get("jm") != null) {command += " -jm " + param.get("jm");}
		if (param.get("jf") != null) {command += " -jf " + param.get("jf");}
		if (param.get("g") != null) {command += " -g " + param.get("g");}

		command+=" -ow "+toPath(storagePath, fileName+".wav")+" "+ script_dir;
		execute(command, true, false);
	}

	/******************************************************************************************
	 * 
	 * 辞書ファイルの読み込み
	 * 
	 * @return
	 */
	private HashMap<String, String> loadDictionaries() {
		HashMap<String, String> dic = new HashMap<String, String>();
		File f = new File(dictionary);
		for (File file : f.listFiles()) {
			if (file.isDirectory())
				dic.put(file.getName(), file.toString());
		}
		return dic;
	}

	/******************************************************************************************
	 * 
	 * 音素ファイルの読み込み
	 * 
	 * @return
	 */
	private HashMap<String, String> loadVoices() {
		HashMap<String, String> voices = new HashMap<String, String>();
		File f = new File(voice);
		for (File files : f.listFiles()) {
			if (files.isDirectory()) {
				for (File subfile : files.listFiles()) {
					if (subfile.isFile()) {
						String str=subfile.toString().replaceAll("^.*\\.([^.]+)$", "$1");
						if(str.equals("htsvoice")) {
							int i=subfile.getName().lastIndexOf('.');
							String path=subfile.getName().substring(0,i);
							voices.put(path,subfile.getPath());
						}
					}
				}
			}
		}
		return voices;
	}

	/******************************************************************************************
	 * 
	 * 単語ファイルを作成
	 * 
	 * @param text
	 * @return
	 */
	private String generateScript(String text) {
		String tmp_dir = toPath(storagePath, fileName)+".txt";
		try {
			PrintWriter writer=new PrintWriter(tmp_dir, "UTF-8");
			writer.write(text);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tmp_dir;
	}

	/******************************************************************************************
	 * 
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
	 * @param time
	 */
	private void duration(int time) {
		try {
			Thread.sleep(time);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
