package dictionary;

import dictionary.SessionType.Type;

public class Session {

    public String question;
    public String answer;
    public Type type;

    public Session(String question, String answer, Type type) {
        this.question = question;
        this.answer = answer;
        this.type = type;
    }

    @Override
    public String toString() {
        return question+" : "+answer;
    }
}