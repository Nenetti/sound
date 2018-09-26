
package recognition;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.HashMap;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import recognition.module.Julius;
import ros.ServiceClient;
import ros.ServiceServer;



public class Recognition extends AbstractNodeMain {

	private String path="ros/sound/julius";
	private String fileName="voice.wav";
	private String questionsName="quize.txt";

	private HashMap<String, String> questions;

	private boolean isProcess=false;

	private Language language=Language.Japanese;

	private Publisher<std_msgs.String> status_mic;
	private ServiceServer<std_msgs.String> command;


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
		status_mic=connectedNode.newPublisher("status/mic", std_msgs.String._TYPE);
		command=new ServiceServer<>(connectedNode, "sound/voice/speak_en/command", std_msgs.String._TYPE);
		command.addMessageListener(new MessageListener<std_msgs.String>() {
			@Override
			public void onNewMessage(std_msgs.String message) {
				String data=message.getData();



				command.complete();
			}
		});
		loadQuestions();
		//final Julius julius_ja=new Julius("localhost", 10500);
		final Julius julius_en=new Julius("localhost", 10501);
		//final Publisher<std_msgs.String> publisher_ja=connectedNode.newPublisher("sound/voice/speak_ja", std_msgs.String._TYPE);

		final ServiceClient<std_msgs.String> publisher_en=new ServiceClient<>(connectedNode, "sound/voice/speak_en", std_msgs.String._TYPE);
		//final ServiceClient<std_msgs.String> publisher = connectedNode.newPublisher("sound/voice/recognition/result", std_msgs.String._TYPE);

		new Thread(new Runnable() {
			@Override
			public void run() {
				duration(1000);
				julius_en.playWav("active");
				status_mic.publish(createMessage("on"));
				while(true) {
					if(julius_en.isConnected()) {
						String result=julius_en.recognition();
						if(result!=null) {
							julius_en.pause();
							status_mic.publish(createMessage("off"));
							result=result.replaceAll("_", " ");
							String answer=questions.get(result);
							if(answer==null) {
								answer="Sorry I don't know what you say";
							}
							System.out.println("A: "+answer);
							publisher_en.publish(answer);
							publisher_en.waitForServer();
							julius_en.playWav("active");
							status_mic.publish(createMessage("on"));
							julius_en.resume();
						}
					}
					duration(10);
				}
			}
		}).start();
	}

	private std_msgs.String createMessage(String data) {
		std_msgs.String message=status_mic.newMessage();
		message.setData(data);
		return message;
	}	

	public void loadQuestions() {
		try {
			questions=new HashMap<>();
			File file=new File(toPath(path, questionsName));
			BufferedReader reader=new BufferedReader(new FileReader(file));
			String line;
			while((line=reader.readLine())!=null) {
				String[] split=line.split("\t");
				String question=split[0];
				String answer=split[1];
				questions.put(question, answer);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/******************************************************************************************
	 * 
	 * byte配列のwavデータをファイルに出力
	 * 
	 * @param message
	 */
	/*private void saveWaveFile(byte[] datas, int offSet) {
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
	}*/

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


	public void duration(int time) {
		try {
			Thread.sleep(time);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
