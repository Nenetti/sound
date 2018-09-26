
package recognition;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import action.Action_ja;
import dictionary.Dictionary_ja;
import dictionary.Dictionary_ja.SentenceType;
import recognition.module.Julius;



public class Recognition_Test extends AbstractNodeMain {

	private static String path="ros/sound/julius";
	private static String fileName="voice.wav";
	private static String file_ja="word_ja";
	private static String dic_path=System.getProperty("user.home")+"/ros/sound/julius/";

	private boolean isProcess=false;

	private Language language=Language.Japanese;
	private Action_ja action_ja=new Action_ja();

	public enum Language{
		Japanese,
		English;
	}

	/******************************************************************************************
	 * 
	 */
	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("sounds/voice/recognition");
	}

	/******************************************************************************************
	 * 
	 * rosjavaのメインメソッド
	 */
	@Override
	public void onStart(ConnectedNode connectedNode) {
		try {
			writeFile();
			createDic();
		} catch (Exception e) {
			e.printStackTrace();
		}
		final Julius julius_ja=new Julius("localhost", 10500);
		final Julius julius_en=new Julius("localhost", 10501);
		final Publisher<std_msgs.String> publisher_ja=connectedNode.newPublisher("sound/voice/speak_ja", std_msgs.String._TYPE);
		final Publisher<std_msgs.String> publisher_en=connectedNode.newPublisher("sound/voice/speak_en", std_msgs.String._TYPE);
		final Publisher<std_msgs.String> publisher = connectedNode.newPublisher("sound/voice/recognition/result", std_msgs.String._TYPE);
		Subscriber<std_msgs.String> subscriber_language = connectedNode.newSubscriber("sound/voice/language", std_msgs.String._TYPE);
		subscriber_language.addMessageListener(new MessageListener<std_msgs.String>() {
			@Override
			public void onNewMessage(std_msgs.String message) {
				switch (message.getData()) {
				case "Japanese":
					language=Language.Japanese;
					publish(publisher_ja, "システムを日本語に変更しました");
					break;
				case "English":
					language=Language.English;
					publish(publisher_en, "System Changed English");
					break;
				default:
					publish(publisher_ja, "不明な言語が指定されました");
					break;
				}
			}
		});
		
		while(true) {
			if(!isProcess) {
				isProcess=true;
				String result;
				switch (language) {
				case Japanese:
					result=julius_ja.recognition();
					if(result!=null) {
						publish(publisher_ja, result);
						publish(publisher, result);
						action_ja.toAction(result);
					}
					break;
				case English:
					result=julius_en.recognition();
					if(result!=null) {
						publish(publisher_en, result);
						publish(publisher, result);
					}
					break;
				}
				isProcess=false;
			}
		}
	}

	private static void writeFile() throws Exception{
		File file=new File(dic_path+file_ja+".yomi");
		BufferedWriter writer=new BufferedWriter(new FileWriter(file));
		for(Dictionary_ja.Dictionary d: Dictionary_ja.Dictionary.values()) {
			String text=d.getText().replace(' ', '^');
			String yomi=d.getYomi();
			switch (d.getSentenceType()) {
			case ゴミ:
				break;
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
				writer.write("Start::"+text+"\t"+"すたーと"+yomi+"\n");
				writer.write("Stop::"+text+"\t"+"すとっぷ"+yomi+"\n");
				continue;
	
			}
			writer.write(text+"\t"+yomi+"\n");
		}
		writer.close();
	}
	
	private static void createDic() throws Exception{
		ProcessBuilder builder=new ProcessBuilder(toProcessCommand("bash "+dic_path+"make_dictionary.sh "+file_ja));
		builder.inheritIO();
		builder.start();
	}
	
	/******************************************************************************************
	 * 
	 * @param str
	 * @return
	 */
	private static List<String> toProcessCommand(String command){
		List<String> result=new ArrayList<>();
		String[] splits=command.split(" ");
		for(String str:splits) {
			result.add(str);
		}
		return result;
	}
	
	/******************************************************************************************
	 * 
	 * publishする
	 * 
	 * @param publisher
	 * @param result
	 */
	private void publish(Publisher<std_msgs.String> publisher, String result) {
		std_msgs.String data=publisher.newMessage();
		data.setData(result);
		publisher.publish(data);
	}

	/******************************************************************************************
	 * 
	 * byte配列のwavデータをファイルに出力
	 * 
	 * @param message
	 */
	private void saveWaveFile(byte[] datas, int offSet) {
		try {
			for(int i=0;i<datas.length;i++) {
				datas[i]+=128;
			}

			File file=new File(toPath(path, fileName));
			if(file.exists()) {
				file.delete();
			}
			file.createNewFile();
			BufferedOutputStream stream=new BufferedOutputStream(new FileOutputStream(file));
			stream.write(datas, offSet, datas.length-offSet);
			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/******************************************************************************************
	 * 
	 * @param args
	 * @return
	 */
	private String toPath(String... args) {
		if(args!=null) {
			String path=System.getProperty("user.home");
			for(String arg: args) {
				path+="/"+arg;
			}
			return path;
		}
		return null;
	}

}
