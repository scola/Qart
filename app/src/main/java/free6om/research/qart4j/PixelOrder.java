package free6om.research.qart4j;

/**
 * Created by free6om on 7/21/15.
 */
public class PixelOrder {
    private int offset;
    private int priority;

    public PixelOrder() {
    }

    public PixelOrder(int offset, int priority) {
        this.offset = offset;
        this.priority = priority;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
