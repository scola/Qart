package free6om.research.qart4j;

/**
 * Created by free6om on 7/21/15.
 */
public class Alpha implements Encoding {
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:";

    private String alphaNumbers;

    public Alpha(String alphaNumbers) {
        this.alphaNumbers = alphaNumbers;
    }

    @Override
    public String validate() {
        if(alphaNumbers == null) return null;

        for(int i = 0;i < alphaNumbers.length();i++) {
            if (ALPHABET.indexOf(alphaNumbers.charAt(i)) < 0) {
                return "non-alphanumeric string " + alphaNumbers;
            }
        }

        return null;
    }

    private int[] alphaLen = new int[]{9, 11, 13};

    @Override
    public int availableBits(Version version) {
        return 4 + alphaLen[version.getSize()] + (11*alphaNumbers.length() + 1)/2;
    }

    @Override
    public void encode(Bits bits, Version version) {
        bits.write(2, 4);
        bits.write(alphaNumbers.length(), alphaLen[version.getSize()]);
        int i = 0;
        for(i = 0;i+2 <= alphaNumbers.length(); i+=2) {
            int w = ALPHABET.indexOf(alphaNumbers.charAt(i)) * 45 + ALPHABET.indexOf(alphaNumbers.charAt(i+1));
            bits.write(w, 11);
        }

        if(i < alphaNumbers.length()) {
            int w = ALPHABET.indexOf(alphaNumbers.charAt(i));
            bits.write(w, 6);
        }

    }

    @Override
    public String toString() {
        return "Alpha(" + alphaNumbers + ")";
    }
}
