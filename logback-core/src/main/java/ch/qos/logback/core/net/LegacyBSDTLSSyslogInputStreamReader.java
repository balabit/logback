package ch.qos.logback.core.net;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class LegacyBSDTLSSyslogInputStreamReader extends TLSSyslogInputStreamReaderBase {
    private ByteArrayOutputStream buffer;

    public LegacyBSDTLSSyslogInputStreamReader(InputStream inputStream) {
        super(inputStream, TLSSyslogMessageFormat.LEGACY_BSD);
        buffer = new ByteArrayOutputStream();
    }

    @Override
    public String read() throws IOException {
        String message = "";
        try {
            while (true) {
                int b = inputStream.read();
                if (b == -1)
                    throw new EOFException("The stream has been closed or the end of stream has been reached");
                if (b == '\n')
                    break;
                buffer.write(b);
            }
        }
        catch (EOFException e) {
            if (buffer.size() > 0) {
                message = buffer.toString();
                buffer.reset();
                return message;
            }
            throw e;
        }
        message = buffer.toString();
        buffer.reset();
        return message;
    }
}
