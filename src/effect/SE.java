package effect;

import process.Terminal;

public class SE {



	public static void play(String filePath) {
		if(filePath!=null) {
			int index=filePath.lastIndexOf(".");
			if(index>0) {
				String extension=filePath.substring(index+1);
				switch (extension) {
				case "wav":
					Terminal.commands("aplay", filePath).waitFor().execute();
					break;
				case "mp3":
					Terminal.commands("mpg321", filePath).waitFor().execute();
					break;
				}
			}
		}
	}







}