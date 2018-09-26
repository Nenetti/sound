package ros;







public class MessageType {
	
	public enum Type {
		String("std_msgs/String"),
		Int32("std_msgs/Int32");
		
		
		
		
		
		
		
		private String name;
		private Type(String name) {
			this.name=name;
		}
		public static Type getType(String str) {
			for(Type t:Type.values()) {
				if(t.name.equals(str)) {
					return t;
				}
			}
			return null;
		}
	}
	
}