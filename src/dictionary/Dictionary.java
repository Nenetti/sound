package dictionary;

import java.util.HashMap;

import dictionary.SessionType.Type;
import reader.CSV_Reader;






public class Dictionary {

	private HashMap<String, Sessions> sessions_en = new HashMap<>();
	private HashMap<String, Sessions> sessions_jp = new HashMap<>();

	public Dictionary(String filePath) {
		String[][] cells=CSV_Reader.read(filePath);
		if(cells==null) {return;}
		for(String[] rows: cells) {
			Session english;
			Session japanese;
			switch(rows.length) {
			// 英語  : [0] = Question, [1] = 音素, [2] = 答え, [3] = セッションタイプ
			// 日本語: [4] = Question, [5] = 音素, [6] = 答え, [7] = セッションタイプ
			case 4:
				english=new Session(rows[0], rows[2], Type.getType(rows[3]));
				sessions_en.put(rows[0], new Sessions(english, null));
				break;
			case 8:
				//日本語あり
				english=new Session(rows[0], rows[2], Type.getType(rows[3]));
				japanese=new Session(rows[4], rows[6], Type.getType(rows[7]));
				Sessions sessions=new Sessions(english, japanese);
				sessions_en.put(rows[0], sessions);
				sessions_jp.put(rows[3], sessions);
				break;
			}
		}
	}

	public Sessions getSessions(String key, Language language) {
		switch (language) {
		case English:
			return sessions_en.get(key);
		case Japanese:
			return sessions_jp.get(key);
		}
		return null;
	}
	
	public Session getSession(String key, Language language) {
		Sessions sessions=getSessions(key, language);
		if(sessions!=null) {
			return sessions.getSession(language);
		}
		return null;
	}

	public static void main(String[] args) {
		new Dictionary("/home/ubuntu/ros/sound/julius/dictionary/word_en.yomi");
	}









}