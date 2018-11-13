
package recognition;

import dictionary.Dictionary;
import dictionary.Language;
import dictionary.Session;
import dictionary.SessionType;
import dictionary.SessionType.Response;
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

    public ServiceServer mic_server;
    public Publisher mic_publisher;
    public Publisher se_publisher;
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
                startup(UserProperty.get("julius.en.jconf"), UserProperty.get("julius.en.host"),
                        UserProperty.get("julius.en.port"));
                this.REPEAT = UserProperty.get("Response.en.Repeat");
                this.NOANSWER = UserProperty.get("Response.en.NoAnswer");
                this.QUESTION = UserProperty.get("Response.en.Question");
                this.CAUTION = UserProperty.get("Response.en.Caution");
                this.OK = UserProperty.get("Response.en.OK");
                this.voice_client = new ServiceClient("sound/voice/speak_en", std_msgs.String._TYPE);
                break;
            case Japanese:
                startup(UserProperty.get("julius.jp.jconf"), UserProperty.get("julius.jp.host"),
                        UserProperty.get("julius.jp.port"));
                this.REPEAT = UserProperty.get("Response.jp.Repeat");
                this.NOANSWER = UserProperty.get("Response.jp.NoAnswer");
                this.QUESTION = UserProperty.get("Response.jp.Question");
                this.CAUTION = UserProperty.get("Response.jp.Caution");
                this.OK = UserProperty.get("Response.jp.OK");
                this.voice_client = new ServiceClient("sound/voice/speak_jp", std_msgs.String._TYPE);
                break;
        }
        this.dictionary = new Dictionary(
                UserProperty.get("julius.dictionary.dir") + "/" + UserProperty.get("julius.session"));
        startAnswerThread();
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
     *
     */
    public void startAnswerThread() {
        while (!julius.isConnected()) { NodeHandle.duration(1); }
        new Thread(() -> {
            while (julius.isConnected()) {
                if (isPause) {
                    NodeHandle.duration(1);
                    continue;
                }
                //
                Result result = startRecognition();
                if (result == null) {
                    continue;
                }
                //
                Session session = dictionary.getSession(result.sentence, language);
                if (session == null) {
                    continue;
                }
                if (isQuestion(session)) {
                    QuestionThread(result, session);
                }
            }
        }).start();
    }

    /******************************************************************************************
     *
     * @param result
     * @param session
     */
    public void QuestionThread(Result result, Session session) {
        if (isHighScore(result.score)) {
            // 正解
            publishVoice(session.answer);
            return;
        } else if (isAskedScore(result.score)) {
            // 質疑判定
            switch (ResponseThread(result)) {
                case Yes:
                    publishVoice(OK + session.answer);
                    break;
                case No:
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
     * @param result
     * @return
     */
    public Response ResponseThread(Result result) {
        String sentence = QUESTION.replaceAll("\\$", result.sentence);
        SE.play(Effect.Question);
        publishVoice(sentence);
        Result response;
        Session session;
        while (true) {
            while ((response = startRecognition()) == null)
                ;
            session = dictionary.getSession(response.sentence, language);
            if (session != null) {
                switch (session.answer) {
                    case "Yes":
                        return Response.Yes;
                    case "No":
                        return Response.No;
                    default:
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
    public Result startRecognition(){
        return  julius.recognition();
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
