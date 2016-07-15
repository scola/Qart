package free6om.research.qart4j;

/**
 * Created by free6om on 7/21/15.
 */
public class Request {
    private int width;
    private int height;
    private int quietZone;

    public Request(int width, int height, int quietZone) {
        this.width = width;
        this.height = height;
        this.quietZone = quietZone;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getQuietZone() {
        return quietZone;
    }

    public void setQuietZone(int quietZone) {
        this.quietZone = quietZone;
    }
}
