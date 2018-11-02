package ros;


public class NodeHandle {
	
	
	
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
}