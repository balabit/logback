package ch.qos.logback.core.net;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class LegacyBSDTLSSyslogInputStreamReaderTest extends TLSSyslogInputStreamReaderBaseTest {

    public LegacyBSDTLSSyslogInputStreamReaderTest() {
        messageFormat = TLSSyslogMessageFormat.LEGACY_BSD;
    }

    @Override
    protected void createSyslogReader() {
        readBuffer = new ByteArrayInputStream(writeBuffer.toByteArray());
        reader = new LegacyBSDTLSSyslogInputStreamReader(readBuffer);
    }

    @Override
    protected void appendToWriteBuffer(String message) throws IOException {
        message = message + ENDLINE;
        writeBuffer.write(message.getBytes());
    }

}
