package free6om.research.qart4j;

/**
 * Created by free6om on 7/20/15.
 */
public class Mask {
    private int mask;

    public Mask(int mask) {
        this.mask = mask;
    }

    public int getMask() {
        return mask;
    }

    public void setMask(int mask) {
        this.mask = mask;
    }

    public boolean shouldInvert(int y, int x) {
        switch (mask) {
            case 0:
                return (y + x) % 2 == 0;
            case 1:
                return y % 2 == 0;
            case 2:
                return x % 3 == 0;
            case 3:
                return (y + x) % 3 == 0;
            case 4:
                return (y/2 + x/3) % 2 == 0;
            case 5:
                return y*x%2 + y*x%3 == 0;
            case 6:
                return (y*x%2 + y*x%3) % 2 == 0;
            case 7:
                return (y*x%3 + (y+x)%2) % 2 == 0;
        }

        return false;
    }
}
