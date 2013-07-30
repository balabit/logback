package ch.qos.logback.core.net;

import ch.qos.logback.core.net.ssl.TLSSyslogFrame;
import ch.qos.logback.core.testUtil.RandomUtil;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class TLSSyslogFrameTest {

    private static final String TESTMESSAGE = "The quick brown fox jumps over the lazy dog";

    @Test
    public void messageSetByConstructor() {
        TLSSyslogFrame frame = new TLSSyslogFrame(TESTMESSAGE);
        byte[] representation = frame.getBytes();
        byte[] expected = getByteRepresentation(TESTMESSAGE);
        Assert.assertTrue(Arrays.equals(representation, expected));
    }

    @Test
    public void messageSetBySetter() {
        TLSSyslogFrame frame = new TLSSyslogFrame("Some text");
        frame.setMessage(TESTMESSAGE);
        byte[] representation = frame.getBytes();
        byte[] expected = getByteRepresentation(TESTMESSAGE);
        Assert.assertTrue(Arrays.equals(representation, expected));
    }

    @Test
    public void checkGetBytes() {
        TLSSyslogFrame frame = new TLSSyslogFrame(TESTMESSAGE);
        byte[] representation = frame.getBytes();
        byte[] expected = getByteRepresentation(TESTMESSAGE);
        Assert.assertTrue(Arrays.equals(representation, expected));
    }

    private byte[] getByteRepresentation(String message) {
        String frame = message.length() + Character.toString(TLSSyslogFrame.SPACE) + message;
        byte[] representation = frame.getBytes();
        return representation;
    }

    @Test
    public void equals() {
        TLSSyslogFrame first = new TLSSyslogFrame("A message");
        TLSSyslogFrame second = new TLSSyslogFrame("A message");
        Assert.assertTrue(first.equals(second));
    }

    @Test
    public void notEquals() {
        TLSSyslogFrame first = new TLSSyslogFrame("A message");
        TLSSyslogFrame second = new TLSSyslogFrame("B message");
        Assert.assertFalse(first.equals(second));
    }
}
