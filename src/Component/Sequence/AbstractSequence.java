package Component.Sequence;

/**
 * Created by snowf on 2019/4/22.
 */

public abstract class AbstractSequence implements Sequence {
    protected String Seq;
    protected char Ori;
    public double Value;

    AbstractSequence(String seq) {
        this(seq, '+');
    }

    AbstractSequence(String seq, char ori) {
        this(seq, ori, 0d);
    }

    AbstractSequence(String seq, char ori, double v) {
        Seq = seq.toUpperCase();
        Ori = ori;
        Value = v;
    }

    public String getSeq() {
        return Seq;
    }

    public char getOri() {
        return Ori;
    }

    public void reverse() {
        Seq = get_reverse();
        Ori = Ori == '+' ? '-' : '+';
    }

    public String get_reverse() {
        return reverse(Seq);
    }


    public void reverse_complement() {
        Seq = get_reverse_complement();
    }

    public void complement() {
        Seq = get_complement();
        Ori = Ori == '+' ? '-' : '+';
    }

    public abstract String get_complement();

    public abstract String get_reverse_complement();

    public static String reverse(String seq) {
        return new StringBuffer(seq).reverse().toString();
    }

    public static String complement(String seq, char[] complementList) {
        char[] tempSeq = seq.toCharArray();
        for (int i = 0; i < tempSeq.length; i++) {
            tempSeq[i] = complementList[tempSeq[i]];
        }
        return new String(tempSeq);
    }


    @Override
    public String toString() {
        return Seq + "\t" + Ori + "\t" + Value;
    }

}
