
package recognition;

import dictionary.Dictionary;
import dictionary.Language;
import dictionary.Session;
import dictionary.SessionType;
import dictionary.SessionType.Response;
import reader.CSV_Reader;
import reader.CSV_Writer;
import recognition.module.Julius;
import recognition.module.Result;
import ros.*;
import sound_effect.Effect;
import sound_effect.SE;

public class Speech_Recognition {


    public String REPEAT;
    public String NOANSWER;
    public String QUESTION;
    public String CAUTION;
    public String OK;

    public Dictionary dictionary;


    public Publisher status_main_publisher;
    public Publisher status_sub_publisher;
    public ServiceClient voice_client;
    public Julius julius;

    public boolean isPause;

    public Language language;

    /******************************************************************************************
     *
     * コンストラクター
     *
     */
    public Speech_Recognition(Language language) {

        this.language = language;
        switch (language) {
            case English:
                startup(UserProperty.getKey("julius.en.jconf"), UserProperty.getKey("julius.en.host"),
                    UserProperty.getKey("julius.en.port"));
                this.REPEAT = UserProperty.getKey("Response.en.Repeat");
                this.NOANSWER = UserProperty.getKey("Response.en.NoAnswer");
                this.QUESTION = UserProperty.getKey("Response.en.Question");
                this.CAUTION = UserProperty.getKey("Response.en.Caution");
                this.OK = UserProperty.getKey("Response.en.OK");
                this.voice_client = new ServiceClient("sound/voice/speak_en", std_msgs.String._TYPE);
                break;
            case Japanese:
                startup(UserProperty.getKey("julius.jp.jconf"), UserProperty.getKey("julius.jp.host"),
                    UserProperty.getKey("julius.jp.port"));
                this.REPEAT = UserProperty.getKey("Response.jp.Repeat");
                this.NOANSWER = UserProperty.getKey("Response.jp.NoAnswer");
                this.QUESTION = UserProperty.getKey("Response.jp.Question");
                this.CAUTION = UserProperty.getKey("Response.jp.Caution");
                this.OK = UserProperty.getKey("Response.jp.OK");
                this.voice_client = new ServiceClient("sound/voice/speak_jp", std_msgs.String._TYPE);
                break;
        }
        this.dictionary = new Dictionary(UserProperty.getKey("julius.dictionary.dir") + "/" + UserProperty.getKey("julius.session"));

        status_main_publisher = new Publisher("status/main_text", std_msgs.String._TYPE);
        status_sub_publisher = new Publisher("status/sub_text", std_msgs.String._TYPE);


        startThread();
    }

    /******************************************************************************************
     *
     * @param dic
     * @param host
     * @param post
     */
    public void startup(String dic, String host, String post) {
        julius = new Julius(dic, host, Integer.valueOf(post), language);
        NodeHandle.duration(2000);
        pause();
    }

    /******************************************************************************************
     *
     * マルチスレッド
     */
    public void startThread() {
        while (!julius.isConnected()) {
            NodeHandle.duration(1);
        }
        new Thread(() -> {
            while (julius.isConnected()) {
                if ( isPause ) {
                    NodeHandle.duration(1);
                    continue;
                }
                Result result = startRecognition();
                if ( result == null ) continue;

                Session session = dictionary.getSession(result.sentence, language);
                if ( session == null ) continue;
                if ( isQuestion(session) ) {
                    QuestionThread(result, session);
                }
            }
        }).start();
    }

    /******************************************************************************************
     *
     * 質問スレッド
     *
     * @param result
     * @param session
     */
    public void QuestionThread(Result result, Session session) {
        if ( isHighScore(result.score) ) {
            // 正解
            status_main_publisher.publish(session.answer);
            status_sub_publisher.publish("");
            publishVoice(session.answer);
            return;
        } else if ( isAskedScore(result.score) ) {
            // 質疑判定
            switch (ResponseThread(result)) {
                case Yes:
                    status_main_publisher.publish("A.  "+session.answer);
                    status_sub_publisher.publish("");
                    publishVoice(OK + session.answer);
                    break;
                case No:
                    status_main_publisher.publish(REPEAT);
                    status_sub_publisher.publish("");
                    publishVoice(REPEAT);
                    break;
            }
            return;
        }
        // 精度低すぎ。聞き取り不可
        publishVoice(NOANSWER);
    }

    /******************************************************************************************
     *
     * イエスノー返事スレッド
     *
     * @param result
     * @return
     */
    public Response ResponseThread(Result result) {
        String sentence = QUESTION.replaceAll("\\$", result.sentence);
        SE.play(Effect.Question);
        status_main_publisher.publish(result.sentence);
        status_sub_publisher.publish("Please Answer with\n  \nYes it is. or No it isn't");
        publishVoice(sentence);
        Result response;
        Session session;
        while (true) {
            response = startRecognition();
            if ( response == null ) continue;
            session = dictionary.getSession(response.sentence, language);
            if ( session != null ) {
                switch (session.answer) {
                    case "Yes":
                        return Response.Yes;
                    case "No":
                        return Response.No;
                    default:
                        status_sub_publisher.publish(CAUTION);
                        SE.play(Effect.Question);
                        publishVoice(CAUTION);
                        continue;
                }
            }
        }
    }

    /******************************************************************************************
     *
     */
    public Result startRecognition() {
        return julius.recognition();
    }

    /******************************************************************************************
     *
     *
     */
    public void pause() {
        isPause = true;
        julius.pause();
    }

    /******************************************************************************************
     *
     *
     */
    public void resume() {
        isPause = false;
        julius.resume();
        SE.play(Effect.Active);
    }

    public void publishVoice(String text) {
        pause();
        voice_client.publish(text).waitForServer();
        resume();
    }

    public boolean isHighScore(double score) {
        return score == 1.0 ? true : false;
    }

    public boolean isAskedScore(double score) {
        return score >= 0.5 ? true : false;
    }

    public boolean isQuestion(Session session) {
        return session.type == SessionType.Type.Question ? true : false;
    }

    public boolean isResponse(Session session) {
        return session.type == SessionType.Type.Response ? true : false;
    }

    public boolean isTrash(Session session) {
        return session.type == SessionType.Type.Trash ? true : false;
    }

    public boolean isSystemCall(Session session) {
        return session.type == SessionType.Type.SystemCall ? true : false;
    }

}
