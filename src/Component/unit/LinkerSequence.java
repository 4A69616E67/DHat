package Component.unit;

public class LinkerSequence {
    private String Seq;
    private String Type;
    boolean Vaild;

    public LinkerSequence(String seq, String type) {
        this(seq, type, false);
    }

    public LinkerSequence(String seq, String type, boolean vaild) {
        Seq = seq;
        Type = type;
        Vaild = vaild;
    }

    @Override
    public String toString() {
        return Seq;
    }

    public String getSeq() {
        return Seq;
    }

    public String getType() {
        return Type;
    }

    public boolean isVaild() {
        return Vaild;
    }
}
