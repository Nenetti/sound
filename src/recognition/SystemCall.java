package recognition;

import dictionary.Language;
import dictionary.Session;

public class SystemCall {


    public static void execute(Session session, Language language){
        switch (language){
            case English:
                execute_en(session);
                break;
            case Japanese:
                execute_jp(session);
                break;
        }
    }

    private static void execute_en(Session session){
        switch (session.answer){
            case "Change Language":

                break;
        }
    }

    private static void execute_jp(Session session){
        switch (session.answer){
            case "Change Language":

                break;
        }
    }

    /*
     *
     * public static void execute(Session session, Language language) {
     * if(session.question.equals("Change Language")) { switch
     * (toQuestion("Do you want to "+session.question+" ?")) { case Yes:
     * changeLanguage(true, language); break; case No: changeLanguage(false,
     * language); break; } continue; } }
     *
     * public void changeLanguage(boolean isChange, Language language) {
     * if(isChange) { switch (language) { case English:
     * Recognition_en.instance.pause(); Recognition_jp.instance.resume();
     * Recognition_jp.instance.publishVoice("日本語に変更します"); break; case Japanese:
     * Recognition_jp.instance.pause(); Recognition_en.instance.resume();
     * Recognition_en.instance.publishVoice("Changed English."); break; } }else {
     * switch (language) { case English: publishVoice("No changed."); break; case
     * Japanese: publishVoice("キャンセルしました"); break; } } }
     */

}