package action;

import java.util.ArrayList;
import java.util.List;

public class Standard_Action{
	
	
	/******************************************************************************************
	 * 
	 * コンストラクター
	 */
	public Standard_Action() {
		
	}
	
	public void execute_command(String text) {
		execute(text, false, true);
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
	
}