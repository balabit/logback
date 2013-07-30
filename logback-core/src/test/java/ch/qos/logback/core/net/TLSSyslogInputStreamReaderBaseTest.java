package ch.qos.logback.core.net;

import ch.qos.logback.core.net.ssl.TLSSyslogFrame;
import ch.qos.logback.core.testUtil.TLSSyslogTestUtil;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Ignore
public class TLSSyslogInputStreamReaderBaseTest {
    protected TLSSyslogInputStreamReaderBase reader;
    protected ByteArrayOutputStream writeBuffer;
    protected ByteArrayInputStream readBuffer;
    protected TLSSyslogMessageFormat messageFormat;
    protected final String ENDLINE = "\n";

    @Before
    public void setUp() {
        writeBuffer = new ByteArrayOutputStream();
    }

    @Test
    public void readEmptyMessage() {
        try {
            String sentMessage = "";
            appendToWriteBuffer(sentMessage);
            createSyslogReader();
            String receivedMessage = reader.read();
            Assert.assertTrue(sentMessage.equals(receivedMessage));
        }
        catch (IOException e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void readLargeMessage() {
        try {
            StringBuilder largeBuf = new StringBuilder();
            for (int i = 0; i < 2 * 1024 * 1024; i++) {
                largeBuf.append('a');
            }
            String sentMessage = largeBuf.toString();
            appendToWriteBuffer(sentMessage);
            createSyslogReader();
            String receivedMessage = reader.read();
            Assert.assertTrue(sentMessage.equals(receivedMessage));
        }
        catch (IOException e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void readMessages() {
        try {
            List<String> sentMessages = TLSSyslogTestUtil.generateMessages(100, messageFormat);
            List<String> receivedMessages = new ArrayList<String>();
            appendToWriteBuffer(sentMessages);
            createSyslogReader();

            for (int i = 0; i < sentMessages.size(); i++) {
                String receivedMessage = reader.read();
                receivedMessages.add(receivedMessage);
            }

            Assert.assertTrue(sentMessages.equals(receivedMessages));
        }
        catch (IOException e) {
            Assert.assertTrue(false);
        }
    }

    protected void createSyslogReader() {
        readBuffer = new ByteArrayInputStream(writeBuffer.toByteArray());
        reader = new TLSSyslogInputStreamReader(readBuffer);
    }

    protected void appendToWriteBuffer(String message) throws IOException {
        TLSSyslogFrame frame = new TLSSyslogFrame(message);
        writeBuffer.write(frame.getBytes());
    }

    private void appendToWriteBuffer(List<String> messages) throws IOException {
        for (String message: messages)
            appendToWriteBuffer(message);
    }
}
