package ch.qos.logback.core.net;

import ch.qos.logback.classic.net.SyslogAppenderTest;
import ch.qos.logback.classic.net.mock.MockTLSSyslogServer;
import ch.qos.logback.core.testUtil.TLSSyslogTestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;


@RunWith(value = Parameterized.class)
public class TLSSyslogAppenderTest extends SyslogAppenderTest {
    private TLSSyslogMessageFormat messageFormat;
    private Map<String, String> systemSSLProperties;

    public TLSSyslogAppenderTest(TLSSyslogMessageFormat format) {
        this.messageFormat = format;
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        systemSSLProperties = TLSSyslogTestUtil.getSystemSSLProperties();
        TLSSyslogTestUtil.setSystemSSLProperties();
    }

    @After
    public void tearDown() {
        TLSSyslogTestUtil.restoreSystemSSLProperties(systemSSLProperties);
    }

    @Override
    public TLSSyslogAppender createSyslogAppender() {
        return new TLSSyslogAppender(messageFormat);
    }

    @Override
    public MockTLSSyslogServer createMockServer(int expectedCount, int port) {
        return new MockTLSSyslogServer(expectedCount, port, messageFormat);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][] {{TLSSyslogMessageFormat.LEGACY_BSD}, { TLSSyslogMessageFormat.SYSLOG }};
        return Arrays.asList(data);
    }
}
