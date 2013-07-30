package ch.qos.logback.core.testUtil;

import ch.qos.logback.core.net.AbstractSocketAppenderTest;
import ch.qos.logback.core.net.TLSSyslogInputStreamReaderBase;
import ch.qos.logback.core.net.TLSSyslogMessageFormat;
import ch.qos.logback.core.net.ssl.TLSSyslogFrame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TLSSyslogTestUtil {
    public static final String ABC = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String NUMBERS = "0123456789";
    public static final String WHITESPACES = " \t\n";
    public static final String CHARSET = ABC + NUMBERS;

    public static final String LEGACY_BSD_SYSLOG_CHARSET = ABC + NUMBERS + " \t";
    public static final String SYSLOG_CHARSET = ABC + NUMBERS + WHITESPACES;

    public static final int MAX_FRAME_NUMBER = 567;

    public static ArrayList<String> generateMessages(int numberOfMessages) {
        return generateMessages(numberOfMessages, CHARSET);
    }

    public static ArrayList<String> generateMessages(int numberOfMessages, TLSSyslogMessageFormat format) {
        switch (format) {
            case SYSLOG:
                return generateMessages(numberOfMessages, SYSLOG_CHARSET);
            case LEGACY_BSD:
                return generateMessages(numberOfMessages, LEGACY_BSD_SYSLOG_CHARSET);
            default:
                throw new IllegalArgumentException();
        }

    }

    public static ArrayList<String> generateMessages(int numberOfMessages, String charSet) {
        ArrayList<String> messageList = new ArrayList<String>(numberOfMessages);
        for (int i = 0; i < numberOfMessages; i++) {
            String message = createRandomString(charSet);
            messageList.add(message);
        }
        return messageList;
    }

    public static ArrayList<TLSSyslogFrame> generateFrames(int numberOfFrames) {
        ArrayList<TLSSyslogFrame> frameList = new ArrayList<TLSSyslogFrame>(numberOfFrames);
        for (int i = 0; i < numberOfFrames; i++) {
            TLSSyslogFrame frame = createRandomFrame();
            frameList.add(frame);
        }
        return frameList;
    }

    public static TLSSyslogFrame createRandomFrame() {
        String message = createRandomString(CHARSET);
        return new TLSSyslogFrame(message);
    }

    public static String createRandomString(TLSSyslogMessageFormat format) {
        switch (format) {
            case SYSLOG:
                return createRandomString(SYSLOG_CHARSET);
            case LEGACY_BSD:
                return createRandomString(LEGACY_BSD_SYSLOG_CHARSET);
            default:
                throw new IllegalArgumentException();
        }
    }

    public static String createRandomString(String charset) {
        char[] chars = charset.toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < random.nextInt(10000); i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        String output = sb.toString();
        return output;
    }

    public static String createRandomMessage() {
        return createRandomString(CHARSET);
    }

    public static int getRandomInt(int max) {
        Random random = new Random();
        int n = random.nextInt(max);
        if (n < 0)
            return -n;
        return n;
    }

    public static Map<String, String> getSystemSSLProperties() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("javax.net.ssl.trustStore", System.getProperty("javax.net.ssl.trustStore"));
        properties.put("javax.net.ssl.trustStorePassword", System.getProperty("javax.net.ssl.trustStorePassword"));
        properties.put("javax.net.ssl.trustStoreType", System.getProperty("javax.net.ssl.trustStoreType"));
        properties.put("javax.net.ssl.keyStore", System.getProperty("javax.net.ssl.keyStore"));
        properties.put("javax.net.ssl.keyStorePassword", System.getProperty("javax.net.ssl.keyStorePassword"));
        properties.put("javax.net.ssl.keyStoreType", System.getProperty("javax.net.ssl.keyStoreType"));
        return properties;
    }

    public static void setSystemSSLProperties() {
        String RELATIVE_PATH = "../logback-core/src/test/resources/net/ssl/CaSigned/";
        String PATH = RELATIVE_PATH;

        System.setProperty("javax.net.ssl.trustStore", PATH + "truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        System.setProperty("javax.net.ssl.keyStore", PATH + "client-keystore.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
        System.setProperty("javax.net.ssl.keyStoreType", "JKS");
    }

    public static void restoreSystemSSLProperties(Map<String, String> properties) {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (entry.getValue() != null)
                System.setProperty(entry.getKey(), entry.getValue());
        }
    }
}
