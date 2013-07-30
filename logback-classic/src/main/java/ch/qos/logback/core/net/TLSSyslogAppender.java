package ch.qos.logback.core.net;

import ch.qos.logback.classic.net.SyslogAppender;
import ch.qos.logback.core.net.TLSSyslogMessageFormat;
import ch.qos.logback.core.net.TLSSyslogOutputStream;
import ch.qos.logback.core.net.SyslogConstants;
import ch.qos.logback.core.net.SyslogOutputStream;
import ch.qos.logback.core.net.ssl.ConfigurableSSLSocketFactory;
import ch.qos.logback.core.net.ssl.SSLComponent;
import ch.qos.logback.core.net.ssl.SSLConfiguration;
import ch.qos.logback.core.net.ssl.SSLParametersConfiguration;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

public class TLSSyslogAppender extends SyslogAppender implements SSLComponent {

    public static final TLSSyslogMessageFormat DEFAULT_MESSAGE_FORMAT = TLSSyslogMessageFormat.SYSLOG;
    public static final int DEFAULT_PORT = 6514;

    private SSLConfiguration ssl;
    private SSLSocket clientSocket;
    private int tls_port = SyslogConstants.TLS_SYSLOG_PORT;
    private TLSSyslogMessageFormat messageFormat = DEFAULT_MESSAGE_FORMAT;


    public TLSSyslogAppender() {
        setPort(DEFAULT_PORT);
    }

    public TLSSyslogAppender(TLSSyslogMessageFormat format) {
        this();
        this.messageFormat = format;
    }

    @Override
    public void start() {
        boolean gotException = false;
        try {
            SSLContext sslContext = getSsl().createContext(this);
            SSLParametersConfiguration parameters = getSsl().getParameters();
            parameters.setContext(getContext());
            SocketFactory socketFactory = new ConfigurableSSLSocketFactory(parameters, sslContext.getSocketFactory());
            clientSocket = (SSLSocket) socketFactory.createSocket(getSyslogHost(), getPort());
        }
        catch (Exception e) {
            gotException = true;
            addError("Error starting TLSSyslogAppender" , e);
        }
        if (!gotException)
            super.start();
    }

    @Override
    public SyslogOutputStream createOutputStream() throws SocketException, UnknownHostException {
        try {
            return new TLSSyslogOutputStream(clientSocket, messageFormat);
        } catch (IOException e) {
            addError("Failed to create TLSOutputStream", e);
            throw new SocketException();
        }
    }

    public void setMessageFormat(String format) {
        if("SYSLOG".equalsIgnoreCase(format))
            messageFormat = TLSSyslogMessageFormat.SYSLOG;
        else if ("LEGACY_BSD".equalsIgnoreCase(format))
            messageFormat = TLSSyslogMessageFormat.LEGACY_BSD;
        else
            messageFormat = DEFAULT_MESSAGE_FORMAT;
    }

    public String getMessageFormat() {
        return messageFormat.toString();
    }

    public void setMessageFormat(TLSSyslogMessageFormat messageFormat) {
        this.messageFormat = messageFormat;
    }

    /**
     * Gets the SSL configuration.
     * @return SSL configuration; if no configuration has been set, a
     *    default configuration is returned
     */
    public SSLConfiguration getSsl() {
        if (ssl == null) {
            ssl = new SSLConfiguration();
        }
        return ssl;
    }

    /**
     * Sets the SSL configuration.
     * @param ssl the SSL configuration to set
     */
    public void setSsl(SSLConfiguration ssl) {
        this.ssl = ssl;
    }
}
