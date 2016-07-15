package free6om.research.qart4j;


/**
 * Created by free6om on 7/21/15.
 */
public class Number implements Encoding {
    private String numbers;

    public Number(String numbers) {
        this.numbers = numbers;
    }

    @Override
    public String validate() {
        if(numbers == null) return null;

        for(int i = 0;i < numbers.length();i++) {
            if(numbers.charAt(i) < '0' || numbers.charAt(i) > '9') {
                return "non-numeric string " + numbers;
            }
        }
        return null;
    }

    private int[] numberLength = new int[]{10, 12, 14};
    @Override
    public int availableBits(Version version) {
        return 4 + numberLength[version.getSize()] + (10*numbers.length() + 2)/3;
    }

    @Override
    public void encode(Bits bits, Version version) {
        bits.write(1, 4);
        bits.write(numbers.length(), numberLength[version.getSize()]);
        int i = 0;
        for(i = 0;i+3 <= numbers.length();i += 3) {
            int w = (numbers.charAt(i) - '0') * 100 + (numbers.charAt(i+1) - '0') * 10 + (numbers.charAt(i+2) - '0');
            bits.write(w, 10);
        }

        switch (numbers.length() - i) {
            case 1:
                int w = numbers.charAt(i) - '0';
                bits.write(w, 4);
                break;
            case 2:
                w = (numbers.charAt(i) - '0')*10 + (numbers.charAt(i+1) - '0');
                bits.write(w, 7);
                break;
        }
    }

    @Override
    public String toString() {
        return numbers;
    }
}
