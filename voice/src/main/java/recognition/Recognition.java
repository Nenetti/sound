
package recognition;


import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;

import ros.NodeHandle;




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
		recognition_en.connect(connectedNode);
		recognition_jp.connect(connectedNode);
		NodeHandle.duration(1000);
		recognition_en.resume();
	}
	
}
