package free6om.research.qart4j;

/**
 * Created by free6om on 7/30/15.
 */
public class Rectangle {
    public Point start;
    public int width;
    public int height;

    public Rectangle(Point start, int width, int height) {
        this.start = start;
        this.width = width;
        this.height = height;
    }

    public void setStart(Point start) {
        this.start = start;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Rectangle intersect(Rectangle other) {
        if(other == null || other.start == null || this.start == null) {
            return null;
        }

        //not intersect
        if(other.start.x >= this.start.x + width ||
                other.start.x + other.width <= this.start.x ||
                other.start.y >= this.start.y + this.height ||
                other.start.y + other.height <= this.start.y) {
            return null;
        }

        int startX = Math.max(this.start.x, other.start.x);
        int width = Math.min(this.start.x + this.width, other.start.x + other.width) - startX;
        int startY = Math.max(this.start.y, other.start.y);
        int height = Math.min(this.start.y + this.height, other.start.y + other.height) - startY;

        return new Rectangle(new Point(startX, startY), width, height);
    }

    public Rectangle union(Rectangle other) {
        if(other == null || other.start == null || this.start == null) {
            return null;
        }

        int startX = Math.min(other.start.x, this.start.x);
        int startY = Math.min(other.start.y, this.start.y);
        int width = Math.max(other.start.x + other.width, this.start.x + this.width) - startX;
        int height = Math.max(other.start.y + other.height, this.start.y + this.height) - startY;

        return new Rectangle(new Point(startX, startY), width, height);
    }
}
