package recognition.module;

public class Result {
    public String sentence;
    public double score;

    public Result(String sentence, double score) {
        this.sentence = sentence;
        this.score = score;
    }

    @Override
    public String toString() {
        return sentence+" : "+score;
    }
}
