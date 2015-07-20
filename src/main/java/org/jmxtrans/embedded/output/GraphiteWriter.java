/*
 * Copyright (c) 2010-2013 the original author or authors
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */
package org.jmxtrans.embedded.output;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.jmxtrans.embedded.QueryResult;
import org.jmxtrans.embedded.util.net.HostAndPort;
import org.jmxtrans.embedded.util.net.SocketWriter;
import org.jmxtrans.embedded.util.net.ssl.TrustAllX509TrustManager;
import org.jmxtrans.embedded.util.pool.SocketWriterPoolFactory;
import org.jmxtrans.embedded.util.pool.UDPSocketWriterPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * <a href="http://graphite.readthedocs.org/">Graphite</a> implementation of the {@linkplain OutputWriter}.
 * <p/>
 * This implementation uses <a href="http://graphite.readthedocs.org/en/0.9.10/feeding-carbon.html#the-plaintext-protocol">
 * Carbon Plan Text protocol</a> over TCP/IP.
 * <p/>
 * Settings:
 * <ul>
 * <li>"host": hostname or ip address of the Graphite server. Mandatory</li>
 * <li>"port": listen port for the TCP Plain Text Protocol of the Graphite server.
 * Optional, default value: {@value #DEFAULT_GRAPHITE_SERVER_PORT}.</li>
 * <li>"namePrefix": prefix append to the metrics name.
 * Optional, default value: {@value #DEFAULT_NAME_PREFIX}.</li>
 * <li>"enabled": flag to enable/disable the writer. Optional, default value: {$code true}.</li>
 * <li>"graphite.socketConnectTimeoutInMillis": timeout for the socketConnect in millis.
 * Optional, default value: {@link SocketWriterPoolFactory#DEFAULT_SOCKET_CONNECT_TIMEOUT_IN_MILLIS}
 * </li>
 * </ul>
 *
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 * @author <a href="mailto:patrick.bruehlmann@gmail.com">Patrick Brühlmann</a>
 */
public class GraphiteWriter extends AbstractOutputWriter implements OutputWriter {

    public static final int DEFAULT_GRAPHITE_SERVER_PORT = 2003;

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private static final String PROTOCOL_TCP = "TCP";
    private static final String PROTOCOL_UDP = "UDP";

    public static final String DEFAULT_NAME_PREFIX = "servers.#hostname#.";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Metric path prefix. Ends with "." if not empty;
     */
    private String metricPathPrefix;

    private HostAndPort graphiteServerHostAndPort;

    private GenericKeyedObjectPool<HostAndPort, SocketWriter> socketWriterPool;

    /**
     * Load settings, initialize the {@link SocketWriter} pool and test the connection to the graphite server.
     * <p/>
     * a {@link Logger#warn(String)} message is emitted if the connection to the graphite server fails.
     */
    @Override
    public void start() {
        int port = getIntSetting(SETTING_PORT, DEFAULT_GRAPHITE_SERVER_PORT);
        String host = getStringSetting(SETTING_HOST);
        graphiteServerHostAndPort = new HostAndPort(host, port);

        logger.info("Start Graphite writer connected to '{}'...", graphiteServerHostAndPort);

        metricPathPrefix = getStringSetting(SETTING_NAME_PREFIX, DEFAULT_NAME_PREFIX);
        metricPathPrefix = getStrategy().resolveExpression(metricPathPrefix);
        if (!metricPathPrefix.isEmpty() && !metricPathPrefix.endsWith(".")) {
            metricPathPrefix = metricPathPrefix + ".";
        }

        GenericKeyedObjectPoolConfig config = new GenericKeyedObjectPoolConfig();
        config.setTestOnBorrow(getBooleanSetting("pool.testOnBorrow", true));
        config.setTestWhileIdle(getBooleanSetting("pool.testWhileIdle", true));
        config.setMaxTotal(getIntSetting("pool.maxActive", -1));
        config.setMaxIdlePerKey(getIntSetting("pool.maxIdle", -1));
        config.setMinEvictableIdleTimeMillis(getLongSetting("pool.minEvictableIdleTimeMillis", TimeUnit.MINUTES.toMillis(5)));
        config.setTimeBetweenEvictionRunsMillis(getLongSetting("pool.timeBetweenEvictionRunsMillis", TimeUnit.MINUTES.toMillis(5)));
        config.setJmxNameBase("org.jmxtrans.embedded:type=GenericKeyedObjectPool,writer=GraphiteWriter,name=");
        config.setJmxNamePrefix(graphiteServerHostAndPort.getHost() + "_" + graphiteServerHostAndPort.getPort());

        int socketConnectTimeoutInMillis = getIntSetting("graphite.socketConnectTimeoutInMillis", SocketWriterPoolFactory.DEFAULT_SOCKET_CONNECT_TIMEOUT_IN_MILLIS);

        String protocol = getStringSetting(SETTING_PROTOCOL, null);
        if (protocol != null && protocol.equalsIgnoreCase(PROTOCOL_UDP)) {
            socketWriterPool = new GenericKeyedObjectPool<HostAndPort, SocketWriter>(new UDPSocketWriterPoolFactory(UTF_8), config);
        } else {
            if (protocol == null) {
                // protocol not specified, use default one
                logger.info("Protocol unspecified, default protocol '{}' will be used.", PROTOCOL_TCP);
            } else if (PROTOCOL_TCP.equalsIgnoreCase(protocol) == false) {
                // unknown or protocol, use default one
                logger.warn("Unknown protocol specified '{}', default protocol '{}' will be used instead.", protocol, PROTOCOL_TCP);
            }
            boolean useTls = getBooleanSetting(SETTING_USE_TLS, false);
            final SocketFactory socketFactory;
            if (useTls) {
                boolean tlsInsecure = getBooleanSetting(SETTING_TLS_INSECURE, false);
                try {
                    SSLContext sslContext = SSLContext.getInstance("SSL");
                    if (tlsInsecure) {
                        sslContext.init(null, new TrustManager[]{new TrustAllX509TrustManager()}, null);
                    }
                    socketFactory = sslContext.getSocketFactory();
                } catch (NoSuchAlgorithmException e) {
                    // that should never occur
                    throw new RuntimeException("Unable to create the TLS SocketFactory", e);
                } catch (KeyManagementException e) {
                    // that should never occur
                    throw new RuntimeException("Unable to create the TLS SocketFactory", e);
                }
            } else {
                socketFactory = SocketFactory.getDefault();
            }
            socketWriterPool = new GenericKeyedObjectPool<HostAndPort, SocketWriter>(new SocketWriterPoolFactory(socketFactory, UTF_8, socketConnectTimeoutInMillis), config);
        }

        if (isEnabled()) {
            try {
                SocketWriter socketWriter = socketWriterPool.borrowObject(graphiteServerHostAndPort);
                socketWriterPool.returnObject(graphiteServerHostAndPort, socketWriter);
            } catch (Exception e) {
                logger.warn("Test Connection: FAILURE to connect to Graphite server '{}'", graphiteServerHostAndPort, e);
            }
        }
    }

    /**
     * Send given metrics to the Graphite server.
     */
    @Override
    public void write(Iterable<QueryResult> results) {
        logger.debug("Export to '{}' results {}", graphiteServerHostAndPort, results);
        SocketWriter socketWriter = null;
        try {
            socketWriter = socketWriterPool.borrowObject(graphiteServerHostAndPort);
            for (QueryResult result : results) {
                String msg = metricPathPrefix + result.getName() + " " + result.getValue() + " " + result.getEpoch(TimeUnit.SECONDS) + "\n";
                logger.debug("Export '{}'", msg);
                socketWriter.write(msg);
            }
            socketWriter.flush();
            socketWriterPool.returnObject(graphiteServerHostAndPort, socketWriter);
        } catch (Exception e) {
            logger.warn("Failure to send result to graphite server '{}' with {}", graphiteServerHostAndPort, socketWriter, e);
            if (socketWriter != null) {
                try {
                    socketWriterPool.invalidateObject(graphiteServerHostAndPort, socketWriter);
                } catch (Exception e2) {
                    logger.warn("Exception invalidating socketWriter connected to graphite server '{}': {}", graphiteServerHostAndPort, socketWriter, e2);
                }
            }
        }
    }

    /**
     * Close the {@link SocketWriter} pool.
     */
    @Override
    public void stop() throws Exception {
        logger.info("Stop GraphiteWriter connected to '{}' ...", graphiteServerHostAndPort);
        super.stop();
        socketWriterPool.close();
    }
}
