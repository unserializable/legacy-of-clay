package ee.ut.algorithmics.image.finder;

/**
 * Created by Iurii on 12/13/2014.
 */

/**
 * Represent key phrase and image weight.
 */
public class KeyPhrase {

    private String phrase;
    private int weight;

    public KeyPhrase(String phrase, int weight){
        this.phrase = phrase;
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getPhrase() {
        return phrase;
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }
}
