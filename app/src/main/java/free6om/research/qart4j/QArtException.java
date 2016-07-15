package free6om.research.qart4j;

/**
 * Created by free6om on 7/20/15.
 */
public class QArtException extends Exception {
    public QArtException() {
    }

    public QArtException(String message) {
        super(message);
    }

    public QArtException(String message, Throwable cause) {
        super(message, cause);
    }

    public QArtException(Throwable cause) {
        super(cause);
    }

    public QArtException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
