package free6om.research.qart4j;

/**
 * Created by free6om on 7/21/15.
 */
public class PixelInfo {
    private int x, y;
    private Pixel pixel;
    private byte target;
    private int ditherTarget;
    private int contrast;
    private boolean hardZero;
    private BitBlock block;
    private int bitIndex;

    public PixelInfo(int x, int y, Pixel pixel, byte target, int contrast) {
        this.x = x;
        this.y = y;
        this.pixel = pixel;
        this.target = target;
        this.contrast = contrast;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Pixel getPixel() {
        return pixel;
    }

    public void setPixel(Pixel pixel) {
        this.pixel = pixel;
    }

    public byte getTarget() {
        return target;
    }

    public void setTarget(byte target) {
        this.target = target;
    }

    public int getDitherTarget() {
        return ditherTarget;
    }

    public void setDitherTarget(int ditherTarget) {
        this.ditherTarget = ditherTarget;
    }

    public int getContrast() {
        return contrast;
    }

    public void setContrast(int contrast) {
        this.contrast = contrast;
    }

    public boolean isHardZero() {
        return hardZero;
    }

    public void setHardZero(boolean hardZero) {
        this.hardZero = hardZero;
    }

    public BitBlock getBlock() {
        return block;
    }

    public void setBlock(BitBlock block) {
        this.block = block;
    }

    public int getBitIndex() {
        return bitIndex;
    }

    public void setBitIndex(int bitIndex) {
        this.bitIndex = bitIndex;
    }
}
