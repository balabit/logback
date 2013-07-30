package ch.qos.logback.core.net;

import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.testUtil.TLSSyslogTestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class TLSSyslogOutputStreamTest {

    class SSLServer extends Thread {

        private int port;
        private SSLServerSocket socket;
        private TLSSyslogInputStreamReaderBase reader;

        public SSLServer(int port) throws IOException {
            this.port = port;
            socket = (SSLServerSocket) socketFactory.createServerSocket(port);
        }

        @Override
        public void run() {
            try {
                waitForConnection();
                receiveMessages();
            } catch (IOException e) {
                Assert.assertTrue(false);
            }
        }

        public void interrupt() {
            super.interrupt();
        }

        private void initReader(InputStream is) {
            switch (messageFormat) {
                case SYSLOG:
                    reader = new TLSSyslogInputStreamReader(is);
                    break;
                case LEGACY_BSD:
                    reader = new LegacyBSDTLSSyslogInputStreamReader(is);
                    break;
            }
        }

        private void waitForConnection() throws IOException {
            clientSocket = (SSLSocket) socket.accept();
            initReader(clientSocket.getInputStream());
        }

        private void receiveMessages() throws IOException {
            while(true) {
                try {
                    receivedMessage = reader.read();
                }
                catch(EOFException e) {
                    return;
                }
            }
        }
    }

    private static final int MAX_FRAME_NUM = 1000;
    private static final int MAX_FRAME_SIZE = 8192;
    private static final int DEFAULT_PORT = 6514;

    private SSLServerSocketFactory socketFactory;
    private TLSSyslogOutputStream outputStream;
    private SSLSocket clientSocket;
    private SSLServer testServer;
    private DataInputStream inputStream;
    private TLSSyslogMessageFormat messageFormat = TLSSyslogMessageFormat.SYSLOG;
    private Map<String, String> systemSSLProperties;
    String receivedMessage = "";
    String sentMessage = "";
    int readBytes;
    private int port;

    public TLSSyslogOutputStreamTest() {
        systemSSLProperties = TLSSyslogTestUtil.getSystemSSLProperties();
        TLSSyslogTestUtil.setSystemSSLProperties();
        socketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
    }

    @Before
    public void setUp() throws IOException, InterruptedException {
        port = RandomUtil.getRandomServerPort();
        testServer = new SSLServer(port);
        testServer.start();
        Thread.sleep(100);
    }

    @After
    public void tearDown() {
        TLSSyslogTestUtil.restoreSystemSSLProperties(systemSSLProperties);
    }

    @Test
    public void sendOneMessageOverSyslogTLS() throws IOException {
        sendOneMessage(TLSSyslogMessageFormat.SYSLOG);
    }

    @Test
    public void sendOneMessageOverLegacyBSDTLS() throws IOException {
        sendOneMessage(TLSSyslogMessageFormat.LEGACY_BSD);
    }

    @Test
    public void sendMessagesOverSyslogTLS() throws IOException {
        sendMoreMessages(TLSSyslogMessageFormat.SYSLOG);
    }

    @Test
    public void sendMessagesOverLegacyBSDTLS() throws IOException {
        sendMoreMessages(TLSSyslogMessageFormat.LEGACY_BSD);
    }

    private void sendOneMessage(TLSSyslogMessageFormat format) throws IOException {
        setMessageFormat(format);
        sendOneMessage();
    }

    public void sendOneMessage() throws IOException {
        try {
            createOutputStream();
            sendAndReceiveOneMessage();
        }
        catch (EOFException e) {

        }
    }

    private void sendMoreMessages(TLSSyslogMessageFormat format) throws IOException {
        setMessageFormat(format);
        sendMoreMessages();
    }

    private void sendMoreMessages() throws IOException {
        try {
            createOutputStream();
            for (int i = 0; i < TLSSyslogTestUtil.getRandomInt(1000); i++)
                sendAndReceiveOneMessage();
        }
        catch (EOFException e) {

        }
    }

    // The RFC 5424 is not supported yet
    //@Test
    public void sendRFC5424MessageToSyslogNg() throws IOException {
        outputStream.close();
        String rfc5424CompatibleMessage = "<132>1 2013-07-15T14:30:03+02:00 10.160.1.90 StructuredDataProducer - TYPE [login@18372 IPAddr=\"127.0.0.1\" count=\"0\" encrypted=\"true\" hostName=\"gipsz-pc\" loginName=\"foo@bar\" password=\"PASSWORD\" timeStamp=\"2013-07-10 17:00:00.1234\"] MESSAGE";
        port = DEFAULT_PORT;
        setMessageFormat(TLSSyslogMessageFormat.SYSLOG);
        createOutputStream();
        outputStream.write(rfc5424CompatibleMessage.getBytes());
    }

    private void setMessageFormat(TLSSyslogMessageFormat format) {
        messageFormat = format;
    }

    private void createOutputStream() throws IOException {
        outputStream = new TLSSyslogOutputStream("localhost", port);
        outputStream.setMessageFormat(messageFormat);
    }

    private void sendAndReceiveOneMessage() throws IOException {
        sendRandomMessage();
        checkReceivedMessageEqualsWithSentMessage();
    }

    private void sendRandomMessage() throws IOException {
        String message = TLSSyslogTestUtil.createRandomString(TLSSyslogTestUtil.CHARSET);
        outputStream.write(message);
    }

    private boolean checkReceivedMessageEqualsWithSentMessage() {
        return sentMessage.equals(receivedMessage);
    }
}
