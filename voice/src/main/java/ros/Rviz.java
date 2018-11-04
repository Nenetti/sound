package ros;

import java.util.ArrayList;
import java.util.List;

import ros.Publisher;
import visualization_msgs.MarkerArray;


public class Rviz {

	private static Publisher markerCreator;

	private Publisher publisher;
	private List<visualization_msgs.Marker> markers;

	public enum Action {
		add(0),
		delete(2),
		deleteAll(3);

		public int id;
		private Action(int id) {this.id=id;}
	}

	public enum Type {
		ARROW(0),
		CUBE(1),
		SPHERE(2),
		CYLINDER(3),
		LINE_STRIP(4),
		LINE_LIST(5),
		CUBE_LIST(6),
		SPHERE_LIST(7),
		POINTS(8),
		TEXT_VIEW_FACING(9),
		MESH_RESOURCE(10),
		TRIANGLE_LIST(11);
		
		public int id;
		private Type(int id) {this.id=id;}
	}
	
	public Rviz(String topic) {
		publisher=new Publisher("rviz/test", MarkerArray._TYPE);
		this.markers=new ArrayList<>();
		if(markerCreator==null) {Rviz.markerCreator=new Publisher("rviz/test2", visualization_msgs.Marker._TYPE);}
	}
	
	public void addMarker(Marker marker) {
		this.markers.add(marker.getMarker());
	}
	
	public void publish() {
		this.publisher.publish(markers);
		markers=new ArrayList<>();
	}
	
	public static Publisher markerCreator() {
		return markerCreator;
	}

	

}