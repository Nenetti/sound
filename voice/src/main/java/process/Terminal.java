package process;

import java.util.ArrayList;
import java.util.List;

public class Terminal {
	
	
	public static Process execute(String[] command, boolean isWait, boolean isInheritIO) {
		try {
			ProcessBuilder builder=new ProcessBuilder(command);
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
	
	private static List<String> toProcessCommand(String command){
		List<String> result=new ArrayList<>();
		String[] splits=command.split(" ");
		for(String str:splits) {
			result.add(str);
		}
		return result;
	}
	
}