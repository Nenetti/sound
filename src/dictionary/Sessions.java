package dictionary;

public class Sessions {

	Session english;
	Session japanese;

	public Sessions(Session english, Session japanese) {
		this.english = english;
		this.japanese = japanese;
	}

	public Session getSession(Language language) {
		switch (language) {
		case English:
			return english;
		case Japanese:
			return japanese;
		}
		return null;
	}

}