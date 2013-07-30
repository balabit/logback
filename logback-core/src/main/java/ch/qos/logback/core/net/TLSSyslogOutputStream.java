package ch.qos.logback.core.net;

import ch.qos.logback.core.net.ssl.TLSSyslogFrame;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.Arrays;

public class TLSSyslogOutputStream extends SyslogOutputStream {
    public static final int DEFAULT_PORT = 6514;
    public static final TLSSyslogMessageFormat DEFAULT_MESSAGE_FORMAT = TLSSyslogMessageFormat.SYSLOG;
    private static final String DEFAULT_HOST = "localhost";
    private static final int NEW_LINE = 0xff & '\n';

    private OutputStream stream;
    private String syslogHost;
    private SSLSocket clientSocket;
    private TLSSyslogMessageFormat messageFormat = TLSSyslogMessageFormat.SYSLOG;
    private int port;

    public TLSSyslogOutputStream(SSLSocket socket, TLSSyslogMessageFormat format) throws IOException {
        this.setMessageFormat(format);
        this.clientSocket = socket;
        this.stream = clientSocket.getOutputStream();
        this.syslogHost = clientSocket.getInetAddress().getHostName();
        this.port = clientSocket.getPort();
    }

    public TLSSyslogOutputStream(SSLSocket socket) throws IOException {
        this(socket, TLSSyslogMessageFormat.SYSLOG);
    }

    public TLSSyslogOutputStream(String syslogHost, int port, TLSSyslogMessageFormat format) throws IOException, UnknownHostException {
        this.setMessageFormat(format);
        this.syslogHost = syslogHost;
        this.port = port;
        SSLSocketFactory sslFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        clientSocket = (SSLSocket) sslFactory.createSocket(syslogHost, port);
        stream = clientSocket.getOutputStream();
    }

    public TLSSyslogOutputStream(String syslogHost, int port) throws IOException {
        this(syslogHost, port, DEFAULT_MESSAGE_FORMAT);
    }

    public TLSSyslogOutputStream(String syslogHost) throws IOException {
        this(syslogHost, DEFAULT_PORT, DEFAULT_MESSAGE_FORMAT);
    }

    public TLSSyslogOutputStream(int port) throws IOException {
        this(DEFAULT_HOST, port, DEFAULT_MESSAGE_FORMAT);
    }

    public TLSSyslogOutputStream() throws IOException {
        this(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_MESSAGE_FORMAT);
    }

    public void write(String message) throws IOException {
        byte[] b = message.getBytes();
        this.write(b, 0, b.length);
    }

    @Override
    public void write(int b) throws IOException {
        throw new UnsupportedOperationException();
    }

    private void writeLegacyBSDMessage(byte[] message) throws IOException {
        byte[] formattedMessage = null;

        formattedMessage = encode(message, TLSSyslogMessageFormat.LEGACY_BSD);
        stream.write(formattedMessage);
        stream.write(NEW_LINE);
    }

    private void writeSyslogMessage(byte[] message) throws IOException {
        byte[] formattedMessage = null;

        formattedMessage = encode(message, TLSSyslogMessageFormat.SYSLOG);
        stream.write(formattedMessage);
    }

    @Override
    public void write(byte[] byteArray, int offset, int len) throws IOException {
        byte [] message = null;
        byte[] formattedMessage = null;

        message = Arrays.copyOfRange(byteArray, offset, len);
        if (messageFormat == TLSSyslogMessageFormat.LEGACY_BSD) {
            writeLegacyBSDMessage(message);
        }
        else if (messageFormat == TLSSyslogMessageFormat.SYSLOG)   {
            writeSyslogMessage(message);
        }
    }

    public void flush() throws IOException {
        this.stream.flush();
    }

    public void close() {
        try {
            this.stream.close();
        } catch (IOException e) {
        }
        try {
            this.clientSocket.close();
        } catch (IOException e) {
        }
    }

    public void setMessageFormat(TLSSyslogMessageFormat format) {
        this.messageFormat = format;
    }

    public TLSSyslogMessageFormat getMessageFormat() {
        return this.messageFormat;
    }

    public int getPort() {
        return port;
    }

    private static byte[] encode(byte[] message, TLSSyslogMessageFormat format) {
        switch (format) {
            case LEGACY_BSD:
                return encodeAsLegacyBSDSyslogOverTLS(message);
            case SYSLOG:
                return  encodeAsSyslogOverTLS(message);
            default:
                return null;
        }
    }

    private static byte[] encodeAsLegacyBSDSyslogOverTLS(byte[] message) {
        return message;
    }

    private static byte[] encodeAsSyslogOverTLS(byte[] message) {
        TLSSyslogFrame frame = new TLSSyslogFrame(new String(message));
        return frame.getBytes();
    }
}
