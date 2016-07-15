package free6om.research.qart4j;

/**
 * Created by free6om on 7/30/15.
 */
public class QRCode {
    private byte[] bytes;
    private Pixel[][] pixels;

    public QRCode(byte[] bytes, Pixel[][] pixels) {
        this.bytes = bytes;
        this.pixels = pixels;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public Pixel[][] getPixels() {
        return pixels;
    }

    public void setPixels(Pixel[][] pixels) {
        this.pixels = pixels;
    }
}
