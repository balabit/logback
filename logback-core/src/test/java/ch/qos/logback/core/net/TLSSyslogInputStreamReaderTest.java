package ch.qos.logback.core.net;

import ch.qos.logback.core.net.ssl.TLSSyslogFrame;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class TLSSyslogInputStreamReaderTest extends TLSSyslogInputStreamReaderBaseTest {

    public TLSSyslogInputStreamReaderTest() {
        messageFormat = TLSSyslogMessageFormat.SYSLOG;
    }

    @Override
    protected void createSyslogReader() {
        readBuffer = new ByteArrayInputStream(writeBuffer.toByteArray());
        reader = new TLSSyslogInputStreamReader(readBuffer);
    }

    @Override
    protected void appendToWriteBuffer(String message) throws IOException {
        TLSSyslogFrame frame = new TLSSyslogFrame(message);
        writeBuffer.write(frame.getBytes());
    }

}
