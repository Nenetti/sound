package ros;

import org.ros.node.ConnectedNode;

public class NodeHandle {
	
	private static ConnectedNode connectedNode;
	
	
	public static void init(ConnectedNode connectedNode) {
		NodeHandle.connectedNode=connectedNode;
	}
	
	/******************************************************************************************
	 * 
	 * @param time
	 */
	public static void duration(int time) {
		try {
			Thread.sleep(time);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static ConnectedNode connectedNode() {
		return connectedNode;
	}
}