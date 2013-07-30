package ch.qos.logback.core.net;


import java.io.IOException;
import java.io.InputStream;

public abstract class TLSSyslogInputStreamReaderBase {

    protected InputStream inputStream;
    protected TLSSyslogMessageFormat messageFormat;

    protected TLSSyslogInputStreamReaderBase(InputStream inputStream, TLSSyslogMessageFormat messageFormat) {
        this.inputStream = inputStream;
        this.messageFormat = messageFormat;
    }

    public String read() throws IOException {
        throw new UnsupportedOperationException();
    }
}
