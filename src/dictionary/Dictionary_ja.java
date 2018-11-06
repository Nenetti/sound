package dictionary;


public class Dictionary_ja{
	
	public static enum SentenceType{
		肯定文,
		疑問文,
		否定文,
		命令文,
		システム文,
		ROS,
		ゴミ,
	}
	
	public static enum Parameter{
		Start,
		Stop,
		publish,
		
		
		;
	}
	
	public static enum Dictionary{
		
		Yes("イエス", "いえす", "肯定文"),
		No("ノー", "のー", "否定文"),
		bringup_s("roslaunch turtlebot_bringup minimal.launch", "ぶりんぐあっぷ", "ROS"),
		usb_camera("rosrun usb_cam usb_cam_node", "ゆーえすびーかめら", "ROS"),
		imageview("rosrun image_view image_view image:=/usb_cam/image_raw", "いめーじびゅー", "ROS"),
		
		あ("ゴミ", "あ", "ゴミ"),
		い("ゴミ", "い", "ゴミ"),
		う("ゴミ", "う", "ゴミ"),
		え("ゴミ", "え", "ゴミ"),
		お("ゴミ", "お", "ゴミ"),
		
		;
		private String text;
		private String yomi;
		private SentenceType sentenceType;
		private Parameter[] parameters;
		private Dictionary(String text, String yomi, String sentenceType, String... parameter) {
			this.text=text;
			this.yomi=yomi;
			this.sentenceType=getSentenceType(sentenceType);
			this.parameters=getParameter(parameter);
		}
		public String getText() {
			return text;
		}
		public String getYomi() {
			return yomi;
		}
		public SentenceType getSentenceType() {
			return sentenceType;
		}
		public Parameter[] getParameters() {
			return parameters;
		}
		private SentenceType getSentenceType(String sentenceType) {
			if(sentenceType!=null) {
				for(SentenceType s:SentenceType.values()) {
					if(sentenceType.equals(s.toString())) {
						return s;
					}
				}
			}
			return null;
		}
		private Parameter[] getParameter(String[] parameters) {
			if(parameters!=null) {
				Parameter[] commands=new Parameter[parameters.length];
				for(int i=0;i<parameters.length;i++) {
					String s=parameters[i];
					for(Parameter c:Parameter.values()) {
						if(s.equals(c.toString())) {
							commands[i]=c;
							break;
						}
					}
				}
				return commands;
			}
			return null;
		}
		public static Dictionary toDictionary(String s) {
			if(s!=null) {
				for(Dictionary d:Dictionary.values()) {
					if(s.equals(d.text)) {
						return d;
					}
				}
			}
			return null;
		}
	}
}