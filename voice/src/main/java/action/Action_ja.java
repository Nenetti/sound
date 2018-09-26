package action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dictionary.Dictionary_ja.SentenceType;
import dictionary.Dictionary_ja.Dictionary;
import dictionary.Dictionary_ja.Parameter;

public class Action_ja extends Standard_Action{


	HashMap<Dictionary, Process> processMap=new HashMap<>();

	/******************************************************************************************
	 * 
	 * コンストラクター
	 */
	public Action_ja() {

	}



	public void toAction(String text) {
		String[] splits=text.split("::");
		if(splits.length==2) {
			//ロスコマンド
			ros_command(toDictionary(splits[1]), toParameter(splits[0]));
		}else {
			Dictionary d=Dictionary.toDictionary(text);
			SentenceType st=d.getSentenceType();
			switch(st) {
			case システム文:
				break;
			case 否定文:
				break;
			case 命令文:
				break;
			case 疑問文:
				break;
			case 肯定文:
				break;
			case ROS:
				break;
			}
		}
	}

	public void ros_command(Dictionary d, Parameter p) {
		Process process;
		switch(p) {
		case publish:

			break;
		case Start:
			process=processMap.get(d);
			if(process!=null&&process.isAlive()) {
				System.out.println("すでに起動済み");
			}else {
				System.out.println("起動");
				process=execute(d.getText(), false, true);
				processMap.put(d, process);
			}
			break;
		case Stop:
			process=processMap.get(d);
			if(process!=null) {
				System.out.println("終了");
				killed(process);
				processMap.remove(d);
			}
			break;
		}
	}

	private static Dictionary toDictionary(String s) {
		if(s!=null) {
			for(Dictionary d:Dictionary.values()) {
				if(s.equals(d.getText())) {
					return d;
				}
			}
		}
		return null;
	}


	private static Parameter toParameter(String s) {
		if(s!=null) {
			for(Parameter p:Parameter.values()) {
				if(s.equals(p.toString())) {
					return p;
				}
			}
		}
		return null;
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
	 * @param command
	 * @param isWait
	 */
	private void killed(Process process) {
		try {
			process.destroy();
			process.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
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