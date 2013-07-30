package ch.qos.logback.classic.net.mock;

import ch.qos.logback.core.net.*;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class MockTLSSyslogServer extends MockSyslogServer {

    public static final int DEFAULT_PORT = 6514;
    public static final String DEFAULT_HOST = "localhost";
    public static final TLSSyslogMessageFormat DEFAULT_MESSAGE_FORMAT = TLSSyslogMessageFormat.SYSLOG;
    private static final int SOCKET_BACKLOG = 1;

    private SSLServerSocket serverSocket;
    private SSLSocket clientSocket;
    private InetAddress serverAddress;
    private List<String> messageList = new ArrayList<String>();
    private TLSSyslogInputStreamReaderBase syslogReader;

    private TLSSyslogMessageFormat messageFormat = TLSSyslogMessageFormat.SYSLOG;
    private int serverPort;
    private boolean finished = false;

    public MockTLSSyslogServer(int loopLen, int port, TLSSyslogMessageFormat format) {
        super(loopLen, port);
        this.serverPort = port;
        this.messageFormat = format;
    }

    public MockTLSSyslogServer(int loopLen, int port)  {
        this(loopLen, port, DEFAULT_MESSAGE_FORMAT);
    }

    @Override
    public void run() {
        try {
            initSockets();
            waitForConnection();
            processFrames();
        } catch (Exception se) {
            se.printStackTrace();
        } finally {
            closeSockets();
        }
        finished = true;
    }

    private void initSockets() throws IOException {
        SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(serverPort);
    }

    private void waitForConnection() throws IOException {
        clientSocket =  (SSLSocket) serverSocket.accept();
        InputStream clientSocketInputStream = clientSocket.getInputStream();
        syslogReader = createTLSSyslogReader(clientSocketInputStream);
    }

    private TLSSyslogInputStreamReaderBase createTLSSyslogReader(InputStream inputStream) {
        switch (messageFormat) {
            case SYSLOG:
                return new TLSSyslogInputStreamReader(inputStream);
            case LEGACY_BSD:
                return new LegacyBSDTLSSyslogInputStreamReader(inputStream);
            default:
                return null;
        }
    }

    private void closeSockets() {
        if(clientSocket != null) {
            try {
                clientSocket.close();
            }
            catch(Exception e) {}
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (Exception e) {
            }
        }
    }

    private void processFrames() throws IOException {
        try {
            int count = 0;
            while (true) {
                String message = "";
                message = syslogReader.read();
                messageList.add(message);
                count++;
                if (isEndOfMessages(count))
                    break;
            }
        }
        catch(Exception e) {
            throw new IOException(e);
        }
        finished = true;
        return;
    }

    private boolean isEndOfMessages(int count) {
        return count == loopLen;
    }

    public boolean isFinished() {
        return finished;
    }

    public List<String> getMessageList() {
        return messageList;
    }

    public TLSSyslogMessageFormat getMessageFormat() {
        return messageFormat;
    }

    public void setMessageFormat(TLSSyslogMessageFormat messageFormat) {
        this.messageFormat = messageFormat;
    }
}
