package Component.Sequence;

/**
 * Created by snowf on 2019/4/22.
 */

public class DNASequence extends AbstractSequence {
    public DNASequence(String seq) {
        super(seq);
    }

    public DNASequence(String seq, char ori) {
        super(seq, ori);
    }

    public DNASequence(String seq, char ori, double v) {
        super(seq, ori, v);
    }

    @Override
    public String get_complement() {
        return AbstractSequence.complement(Seq, ComplementList);
    }

    @Override
    public String get_transcript() {
        return null;
    }

    @Override
    public String get_translate() {
        return null;
    }

    @Override
    public String get_reverse_complement() {
        return reverse(complement(Seq));
    }

    public static String complement(String seq) {
        return AbstractSequence.complement(seq, ComplementList);
    }

    private static char[] ComplementList = new char[255];

    static {
        for (int i = 0; i < ComplementList.length; i++) {
            ComplementList[i] = 'N';
        }
        ComplementList['A'] = 'T';
        ComplementList['T'] = 'A';
        ComplementList['C'] = 'G';
        ComplementList['G'] = 'C';
        ComplementList['-'] = '-';
        ComplementList['N'] = 'N';
        ComplementList[' '] = ' ';
    }
}
