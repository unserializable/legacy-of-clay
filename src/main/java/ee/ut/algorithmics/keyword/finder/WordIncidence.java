package ee.ut.algorithmics.keyword.finder;

/**
 * @author Taimo Peelo
 */
public class WordIncidence {
    private String word;
    private int incidence;

    public WordIncidence(String word, int incidence) {
        this.incidence = incidence;
        this.word = word;
    }

    @Override
    public String toString() {
        return "('" + word + "', " + incidence + ')';
    }

    public String getWord() {
        return word;
    }

    public int getIncidence() {
        return incidence;
    }
}
