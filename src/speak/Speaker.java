
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
     * コンストラクター
     */
    public Speaker() {
        this.voice_jp = new VoiceMaker(storagePath, fileName, Language.Japanese);
        this.voice_en = new VoiceMaker(storagePath, fileName, Language.English);
    }

    /******************************************************************************************
     *
     */
    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("sounds/voice/speak");
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

    /******************************************************************************************
     *
     */
    @Override
    public void start() {
        // status_speaker=new Publisher(connectedNode, "status/speaker",
        // std_msgs.String._TYPE);
        speaker_publisher = new Publisher("status/speaker", std_msgs.String._TYPE);
        mic_publisher = new Publisher("status/mic", std_msgs.String._TYPE);
        status_publisher = new Publisher("status/text", std_msgs.String._TYPE);
        voice_server_jp = new ServiceServer("sound/voice/speak_jp", std_msgs.String._TYPE);
        voice_server_en = new ServiceServer("sound/voice/speak_en", std_msgs.String._TYPE);
        voice_server_jp.addMessageListener(new MessageListener<Object>() {
            @Override
            public void onNewMessage(Object message) {
                if ( !isProcess ) {
                    String data=((std_msgs.String) message).getData();
                    isProcess = true;
                    //mic_publisher.publish("OFF");
                    speaker_publisher.publish("ON");
                    //status_publisher.publish(data);
                    speak(data, Language.Japanese);
                    voice_server_jp.complete();
                    //speaker_publisher.publish("OFF");
                    mic_publisher.publish("ON");
                }
            }
        });
        voice_server_en.addMessageListener(new MessageListener<Object>() {
            @Override
            public void onNewMessage(Object message) {
                if ( !isProcess ) {
                    String data=((std_msgs.String) message).getData();
                    isProcess = true;
                    //mic_publisher.publish("OFF");
                    speaker_publisher.publish("ON");
                    //status_publisher.publish(data);
                    speak(data, Language.English);
                    voice_server_en.complete();
                    //speaker_publisher.publish("OFF");
                    mic_publisher.publish("ON");
                }
            }
        });
        NodeHandle.duration(1000);
        System.out.println("********************************************");
        System.out.println("OK");
        System.out.println("********************************************");
    }

}
