package dictionary;

public class SessionType {

	public enum Type {

		Question, Response, SystemCall, Trash;

		public static Type getType(String value) {
			Type type = Type.valueOf(value);
			if (type != null) {
				return type;
			}
			return Trash;
		}

	}

	public enum Response {
		Yes, No;
	}

}