
package recognition;

import java.awt.geom.Area;
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

import recognition.module.Julius;
import recognition.module.Julius.Result;
import ros.NodeHandle;
import ros.Publisher;
import ros.ServiceClient;
import ros.ServiceServer;
import ros.Subscriber;



public class Recognition extends AbstractNodeMain {

	private Recognition_en recognition_en;
	private Recognition_jp recognition_jp;
	
	/******************************************************************************************
	 * 
	 * コンストラクター
	 * 
	 */
	public Recognition() {
		recognition_en=new Recognition_en();
		recognition_jp=new Recognition_jp();
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
		recognition_en.start(connectedNode);
		recognition_jp.start(connectedNode);
	}
	
}
