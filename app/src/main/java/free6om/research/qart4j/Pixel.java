package free6om.research.qart4j;

/**
 *
 * ----------------------------------------------------------------------------------
 * | 26bits for offset | 4bits for Role | 1bit for invert | 1bit for black or white |
 * ----------------------------------------------------------------------------------
 *
 * Created by free6om on 7/20/15.
 */
public class Pixel {
    public enum PixelRole {
        UNKNOWN, //not used
        POSITION, //position pattern
        ALIGNMENT,
        TIMING,
        FORMAT,
        VERSION_PATTERN,
        UNUSED,
        DATA,
        CHECK,
        EXTRA

    }



    public static final Pixel BLACK = new Pixel(1);
    public static final Pixel INVERT = new Pixel(2);

    private int data;

    public Pixel(int value) {
        data = value;
    }

    public Pixel(PixelRole role) {
        this.data = role.ordinal() << 2;
    }

    public Pixel(Pixel pixel) {
        this.data = pixel.data;
    }

    public int getOffset() {
        return data >> 6;
    }

    public void setOffset(int offset) {
        data = ((offset << 6) | (data & 0x03F));
    }

    public PixelRole getPixelRole() {
        int ordinal = (this.data >> 2) & 0x0F;
        if(ordinal < PixelRole.UNKNOWN.ordinal() || ordinal > PixelRole.EXTRA.ordinal()){
            return null;
        }

        return PixelRole.values()[ordinal];
    }

    public boolean shouldInvert() {
        return ((data>>1)&0x1) == 1;
    }

    public void setInvert(boolean invert) {
        int i = invert ? 1 : 0;
        this.data = (this.data >> 2 << 2) | (i << 1) | (this.data & 0x1);
    }

    public int getPixel() {
        return this.data & 0x01;
    }
    public void setPixel(int value) {
        this.data = ((this.data >> 1 << 1) | (value&0x01));
    }
    public void orPixel(int value) {
        this.data |= (value & 0x01);
    }
    public void xorPixel(int value) {
        this.data ^= (value & 0x01);
    }

}
