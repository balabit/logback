package ch.qos.logback.core.net.ssl;

public class TLSSyslogFrame {
    public static final char SPACE = ' ';

    private String message;
    private int messageLengthInBytes;

    public TLSSyslogFrame(String message) {
        setMessage(message);
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
        setLengthInBytes();
    }

    private void setLengthInBytes() {
        messageLengthInBytes = message.length();
    }

    public byte[] getBytes() {
        String frame = Integer.toString(messageLengthInBytes) + SPACE + message;
        return frame.getBytes();
    }

    @Override
    public String toString() {
        String length = Integer.toString(messageLengthInBytes);
        return length + SPACE + message.toString();
    }

    @Override
    public boolean equals(Object frame) {
        return super.equals(frame);
    }

    public boolean equals(TLSSyslogFrame frame) {
        return isLengthEquals(frame) && isMessageEquals(frame);
    }

    private boolean isLengthEquals(TLSSyslogFrame frame) {
        return this.messageLengthInBytes == frame.messageLengthInBytes;
    }

    private boolean isMessageEquals(TLSSyslogFrame frame) {
        return this.message.equals(frame.message);
    }
}
