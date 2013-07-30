package ch.qos.logback.core.net;

import ch.qos.logback.classic.net.mock.MockTLSSyslogServer;
import ch.qos.logback.core.testUtil.TLSSyslogTestUtil;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.util.*;

import ch.qos.logback.core.testUtil.RandomUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(value = Parameterized.class)
public class MockTLSSyslogServerTest extends Thread {
    private static final String hostName = "localhost";
    private static final int MAX_NUMBER_OF_MESSAGES_TO_SEND = 10;

    private List<String> sentMessages;
    private List<String> receivedMessages;
    private TLSSyslogOutputStream outputStream;
    private MockTLSSyslogServer server;
    private TLSSyslogMessageFormat messageFormat = TLSSyslogMessageFormat.SYSLOG ;
    private int serverPort;
    private long WAIT_TIME_FOR_SERVER  = 100;
    private Map<String, String> systemSSLProperties;

    public MockTLSSyslogServerTest(TLSSyslogMessageFormat format) {
        messageFormat = format;
        systemSSLProperties = TLSSyslogTestUtil.getSystemSSLProperties();
        TLSSyslogTestUtil.setSystemSSLProperties();
        sentMessages = new ArrayList<String>(MAX_NUMBER_OF_MESSAGES_TO_SEND);
        receivedMessages = new ArrayList<String>(MAX_NUMBER_OF_MESSAGES_TO_SEND);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][] { { TLSSyslogMessageFormat.SYSLOG }, { TLSSyslogMessageFormat.LEGACY_BSD }};
        return Arrays.asList(data);
    }

    @Before
    public void setUp() throws IOException, InterruptedException {
        sentMessages.clear();
        receivedMessages.clear();
    }

    @After
    public void tearDown() {
        TLSSyslogTestUtil.restoreSystemSSLProperties(systemSSLProperties);
    }

    @Test
    public void connect() throws InterruptedException, IOException {
        initServer(0);
        connectToServer();
        Assert.assertTrue(true);
    }

    @Test
    public void receivedMessageNumberEqualsWithSentMessageNumber() throws InterruptedException, IOException {
        int numberOfMessages = 2;

        initServer(numberOfMessages);
        connectToServer();
        sendMessages(numberOfMessages);
        waitForMessages();
        getReceivedMessages();
        Assert.assertTrue(sentMessages.size() == receivedMessages.size());
    }

    @Test
    public void receivedMessagesAreEqualToSentMessages() throws IOException, InterruptedException {
        int numberOfMessages = TLSSyslogTestUtil.getRandomInt(MAX_NUMBER_OF_MESSAGES_TO_SEND);

        initServer(numberOfMessages);
        connectToServer();
        sendMessages(numberOfMessages);
        waitForMessages();
        getReceivedMessages();
        Assert.assertTrue(sentMessages.equals(receivedMessages));
    }

    @Test
    public void sendLargeMessage() throws IOException, InterruptedException {
        initServer(1);
        connectToServer();
        StringBuilder largeBuf = new StringBuilder();
        for (int i = 0; i < 2 * 1024 * 1024; i++) {
            largeBuf.append('a');
        }
        sendOneMessage(largeBuf.toString());
        waitForMessages();
        getReceivedMessages();
        Assert.assertTrue(sentMessages.equals(receivedMessages));
    }

    private void initServer(int loopLen) throws InterruptedException {
        this.serverPort = RandomUtil.getRandomServerPort();
        server = new MockTLSSyslogServer(loopLen, serverPort, messageFormat);
        server.start();
        Thread.sleep(WAIT_TIME_FOR_SERVER);
    }

    private void connectToServer() throws IOException {
        server.setMessageFormat(messageFormat);
        outputStream = new TLSSyslogOutputStream(serverPort);
        outputStream.setMessageFormat(messageFormat);
    }

    private void sendOneMessage(String message) throws IOException {
        outputStream.write(message.getBytes());
        outputStream.flush();
        sentMessages.add(message);
    }

    private void sendOneMessage() throws IOException {
        String sentMessage = TLSSyslogTestUtil.createRandomString(messageFormat);
        sendOneMessage(sentMessage);
    }

    private void sendMessages(int number) throws IOException {
        for (int i = 0; i < number; i++) {
            sendOneMessage();
        }
    }

    private int getReceivedMessageNumber() {
        List<String> messages = server.getMessageList();
        return messages.size();
    }

    private int getSentMessageNumber() {
        return sentMessages.size();
    }

    private void getReceivedMessages() {
        receivedMessages = server.getMessageList();
    }

    private void waitForMessages() throws InterruptedException, IOException {
        closeAndFlushStream();
        waitForReceiveAllMessages();
    }

    private void closeAndFlushStream() throws IOException {
        outputStream.flush();
        outputStream.close();
    }

    private void waitForReceiveAllMessages() throws InterruptedException {
        Thread.sleep(100);
    }
}
