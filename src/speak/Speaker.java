
package speak;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import dictionary.Language;
import ros.NodeHandle;
import ros.Publisher;
import ros.ServiceClient;
import ros.ServiceServer;
import speak.module.VoiceMaker;

public class Speaker extends NodeHandle {

    private String storagePath = "ros/sound";
    private String fileName = "voice";

    private boolean isProcess = false;

    private ServiceServer voice_server_jp;
    private ServiceServer voice_server_en;
    private Publisher speaker_publisher;
    private Publisher mic_publisher;
    private Publisher status_publisher;
    private VoiceMaker voice_jp;
    private VoiceMaker voice_en;

    /******************************************************************************************
     *
     */
    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("sounds/voice/speak");
    }

    /******************************************************************************************
     *
     */
    @Override
    public void start() {
        this.voice_jp = new VoiceMaker(storagePath, fileName, Language.Japanese);
        this.voice_en = new VoiceMaker(storagePath, fileName, Language.English);

        this.speaker_publisher = new Publisher("/rione/status/speaker", std_msgs.String._TYPE);
        this.mic_publisher = new Publisher("/rione/status/mic", std_msgs.String._TYPE);
        this.status_publisher = new Publisher("/rione/status/text", std_msgs.String._TYPE);
        this.voice_server_jp = new ServiceServer("/rione/sound/voice/speak_jp", std_msgs.String._TYPE);
        this.voice_server_en = new ServiceServer("/rione/sound/voice/speak_en", std_msgs.String._TYPE);
        this.voice_server_jp.addMessageListener((message)->{
            if ( !isProcess ) {
                String data=((std_msgs.String) message).getData();
                isProcess = true;
                speaker_publisher.publish("ON");
                speak(data, Language.Japanese);
                voice_server_jp.complete();
                mic_publisher.publish("ON");
            }
        });
        voice_server_en.addMessageListener((message)->{
            if ( !isProcess ) {
                String data=((std_msgs.String) message).getData();
                isProcess = true;
                speaker_publisher.publish("ON");
                speak(data, Language.English);
                voice_server_en.complete();
                mic_publisher.publish("ON");
            }
        });
        NodeHandle.duration(1000);
        System.out.println("********************************************");
        System.out.println("OK");
        System.out.println("********************************************");
    }

    /******************************************************************************************
     *
     * @param text
     * @param language
     */
    public void speak(String text, Language language) {
        System.out.println(language + " : " + text);
        // mic_client.publish("off").waitForServer();
        switch (language) {
            case English:
                voice_en.speak(text);
                break;
            case Japanese:
                voice_jp.speak(text);
                break;
        }
        // mic_client.publish("on").waitForServer();
        isProcess = false;
    }

}
