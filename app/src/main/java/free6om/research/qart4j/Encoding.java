package free6om.research.qart4j;

/**
 * Created by free6om on 7/20/15.
 */
public interface Encoding {
    String validate();
    int availableBits(Version version);
    void encode(Bits bits, Version version);
}
