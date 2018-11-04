package ros;

import org.ros.message.Time;

import javafx.scene.paint.Color;
import ros.Rviz.Action;
import ros.Rviz.Type;

public class Marker {

	private visualization_msgs.Marker marker;

	public Marker() {
		this.marker=(visualization_msgs.Marker)Rviz.markerCreator().newMessage();
	}

	public void setHeader(String frame, Time time) {
		this.marker.getHeader().setFrameId(frame);
		this.marker.getHeader().setStamp(time);
	}

	public void setParam(String ns, int id, Type type, Action action) {
		this.marker.setNs(ns);
		this.marker.setId(id);
		this.marker.setType(type.id);
		this.marker.setAction(action.id);
	}

	public void setPosition(double x, double y, double z) {
		this.marker.getPose().getPosition().setX(x);
		this.marker.getPose().getPosition().setY(y);
		this.marker.getPose().getPosition().setZ(z);
	}

	public void setOrientation(double x, double y, double z, double w) {
		this.marker.getPose().getOrientation().setX(x);
		this.marker.getPose().getOrientation().setY(y);
		this.marker.getPose().getOrientation().setZ(z);
		this.marker.getPose().getOrientation().setW(w);
	}

	public void setScale(double scale) {
		setScale(scale, scale, scale);
	}

	public void setScale(double x, double y, double z) {
		this.marker.getScale().setX(x);
		this.marker.getScale().setY(y);
		this.marker.getScale().setZ(z);
	}

	public void setColor(Color color) {
		this.marker.getColor().setR((float)color.getRed());
		this.marker.getColor().setG((float)color.getGreen());
		this.marker.getColor().setB((float)color.getBlue());
		this.marker.getColor().setA((float)color.getOpacity());
	}
	
	public void setColor(float r, float g, float b, float a) {
		this.marker.getColor().setR(r);
		this.marker.getColor().setG(g);
		this.marker.getColor().setB(b);
		this.marker.getColor().setA(a);
	}

	public visualization_msgs.Marker getMarker(){
		return marker;
	}

}