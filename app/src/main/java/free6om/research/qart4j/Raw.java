package free6om.research.qart4j;

import java.io.UnsupportedEncodingException;

/**
 * Created by free6om on 7/21/15.
 */
public class Raw implements Encoding {
    static final String DEFAULT_BYTE_MODE_ENCODING = "ISO-8859-1";

    private String raw;
    private String encoding;

    public Raw(String raw) {
        this.raw = raw;
        this.encoding = DEFAULT_BYTE_MODE_ENCODING;
    }

    public Raw(String raw, String encoding) {
        this.raw = raw;
        this.encoding = encoding;
    }

    @Override
    public String validate() {
        return null;
    }

    private int[] rawLength = new int[]{8, 16, 16};
    @Override
    public int availableBits(Version version) {
        try {
            return 4 + rawLength[version.getSize()] + raw.getBytes(encoding).length * 8;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void encode(Bits bits, Version version) {
        bits.write(4, 4);
        byte[] data = null;
        try {
            data = raw.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }

        bits.write(data.length, rawLength[version.getSize()]);

        for(int i = 0;i < data.length;i++) {
            bits.write(data[i], 8);
        }
    }

    @Override
    public String toString() {
        return "Raw(" + raw + ")";
    }
}
