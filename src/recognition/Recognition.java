
package recognition;

import org.ros.namespace.GraphName;

import dictionary.Language;
import ros.NodeHandle;
import ros.UserProperty;

public class Recognition extends NodeHandle {

    private Speech_Recognition recognition_en;
    //private Speech_Recognition recognition_jp;

    /******************************************************************************************
     *
     */
    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("sounds/voice/recognition");
    }

    /******************************************************************************************
     *
     *
     */
    @Override
    public void start() {
        recognition_en = new Speech_Recognition(Language.English);
        // recognition_jp=new Speech_Recognition(Language.Japanese);
        NodeHandle.duration(1000);
        switch (UserProperty.getKey("julius.language")) {
            case "English":
                recognition_en.resume();
                break;
            case "Japanese":
                // recognition_jp.resume();
                break;
        }
    }
}
