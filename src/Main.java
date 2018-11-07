import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;

import geometry_msgs.Transform;
import geometry_msgs.TransformStamped;
import javafx.scene.paint.Color;
import nav_msgs.Odometry;
import ros.Marker;
import ros.NodeHandle;
import ros.Publisher;
import ros.Rviz;
import ros.Rviz.Type;
import ros.Rviz.Action;
import ros.Subscriber;
import sensor_msgs.LaserScan;



public class Main extends AbstractNodeMain {


	private Subscriber subscriber;
	private Transform odometry;
	private Rviz rviz;
	private Color[] colors;

	public Main() {
		colors=new Color[1000];
		Random random=new Random(0);
		for(int i=0;i<colors.length;i++) {
			colors[i]=new Color((double)(Math.abs(random.nextInt()%255))/255, (double)(Math.abs(random.nextInt()%255))/255, (double)(Math.abs(random.nextInt()%255))/255, 1);
		}
	}

	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("rviz/test");
	}


	@Override
	public void onStart(ConnectedNode connectedNode) {
		NodeHandle.init(connectedNode);
		this.rviz=new Rviz("rviz/test");
		subscriber=new Subscriber("/scan", LaserScan._TYPE);
		subscriber.addMessageListener(new MessageListener<Object>() {
			@Override
			public void onNewMessage(Object message) {
				LaserScan data=((LaserScan)message);
				double angle=data.getAngleMin();
				int ci=0;
				for(int i=1;i<data.getRanges().length;i++) {
					if(Math.abs(data.getRanges()[i]-data.getRanges()[i-1])>1) {ci++;}
					double x=data.getRanges()[i]*Math.cos(angle);
					double y=data.getRanges()[i]*Math.sin(angle);
					if(!Double.isFinite(x)) {x=0;}
					if(!Double.isFinite(y)) {y=0;}
					angle+=data.getAngleIncrement();
					Marker marker=new Marker();
					marker.setHeader("base_link", data.getHeader().getStamp());
					marker.setParam("scan_shapes", 10000+i, Type.SPHERE, Action.add);
					marker.setPosition(x, y, 0.3);
					marker.setOrientation(0, 0, 0, 1);
					marker.setScale(0.08);
					marker.setColor(colors[ci]);
					rviz.addMarker(marker);
				}
				rviz.publish();
			}
		});	
	}
}