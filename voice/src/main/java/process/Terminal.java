package process;

import java.lang.ProcessBuilder.Redirect;

public class Terminal {
	
	
	public static Process execute(String[] command, boolean isWait, boolean isInheritIO) {
		try {
			ProcessBuilder builder=new ProcessBuilder(command);
			if(isInheritIO) {
				builder.inheritIO();
			}
			builder.redirectOutput(Redirect.PIPE);
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
	
}