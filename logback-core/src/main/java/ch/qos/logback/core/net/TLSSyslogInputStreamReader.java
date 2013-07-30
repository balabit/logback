package ch.qos.logback.core.net;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: btibi
 * Date: 7/23/13
 * Time: 3:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class TLSSyslogInputStreamReader extends TLSSyslogInputStreamReaderBase {
    private static final char SPACE = ' ';

    private ByteArrayOutputStream messageBuffer;
    private byte[] messagePartBuffer;
    private byte[] lengthBuffer;
    private int messagePartBufferSize = 8192;
    private int lengthBufferSize = 8192;
    private int position = 0;
    private int nextMessageLength = 0;

    public TLSSyslogInputStreamReader(InputStream inputStream) {
        super(inputStream, TLSSyslogMessageFormat.SYSLOG);
        this.messageBuffer = new ByteArrayOutputStream(messagePartBufferSize);
        this.lengthBuffer = new byte[lengthBufferSize];
        this.messagePartBuffer = new byte[messagePartBufferSize];
    }

    @Override
    public String read() throws IOException {
        readMessageLength();
        readMessage();
        String message =  buildMessage();
        return message;
    }

    private void readMessageLength() throws IOException {
        readBytesUntilNextSpace();
        calculateNextMessageLength();
    }

    private void readMessage() throws IOException {
        int remainder = nextMessageLength;
        while (remainder > 0)  {
            int bytesToRead = Math.min(remainder, messagePartBufferSize);
            int n = inputStream.read(messagePartBuffer, 0, bytesToRead);
            messageBuffer.write(messagePartBuffer, 0, n);
            remainder -= n;
        }
    }

    private String buildMessage() {
        String message = messageBuffer.toString();
        messageBuffer.reset();
        return message;
    }

    private void readBytesUntilNextSpace() throws IOException {
        for (int i = 0; i < lengthBufferSize; i++) {
            int b = inputStream.read();
            if (b < 0)
                throw new EOFException("The stream has been closed or the end of stream has been reached");
            byte currentByte = (byte)(b & 0xff);
            if (currentByte == SPACE) {
                position = i;
                break;
            }
            lengthBuffer[i] = currentByte;
        }
    }

    private void calculateNextMessageLength() {
        byte[] length = Arrays.copyOfRange(lengthBuffer, 0, position);
        nextMessageLength = new Integer(new String(length));
    }
}
