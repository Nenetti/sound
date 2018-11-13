
package speak.module;

import dictionary.Language;
import process.Terminal;

public class VoiceMaker {

    private String shell;
    private String home = System.getProperty("user.home");
    private String path;

    /******************************************************************************************
     *
     * コンストラクター
     *
     * @param path
     * @param fileName
     */
    public VoiceMaker(String path, String fileName, Language language) {
        switch (language) {
            case Japanese:
                shell = "voice_text_web_api.sh";
                break;
            case English:
                shell = "svox.sh";
                break;
        }
        this.path = path;
    }

    /******************************************************************************************
     *
     * 音声ファイルを作成 & 発話させる
     *
     * @param text
     */
    public void speak(String text) {
        String[] exec_create = new String[]{"bash", home + "/" + path + "/" + shell, text};
        Terminal.execute(exec_create, true, false);
    }
}
