package org.jmxtrans.embedded.util.ssl;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.ssl.TrustStrategy;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Slf4j
public class LoggingTrustStrategy implements TrustStrategy {

    private static final CertificateLoggingHelper certificateLoggingHelper = new CertificateLoggingHelper();

    private final TrustStrategy delegate;

    public LoggingTrustStrategy(TrustStrategy delegate) {
        super();
        this.delegate = delegate;
        log.info("New instance created : " + getClass().getSimpleName());
    }

    @Override
    public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("isTrusted() : ");
        logBuilder.append("authType = ").append(authType);
        logBuilder.append(", chain.length = ").append(chain.length);
        if (chain.length > 0) {
            logBuilder.append(", ");
            for (int i = 0; i < chain.length; i++) {
                X509Certificate cert = chain[i];
                logBuilder.append("chain[").append(i).append("] = {");
                certificateLoggingHelper.appendCertificateDescription(logBuilder, cert);
                logBuilder.append("}");
                if (i + 1 < chain.length) {
                    logBuilder.append(", ");
                }
            }
        }

        // should always return false so that the TrustManager is queried
        // because we have no authority on whether or not the Certificate should be trusted without querying the underlying TrustManager
        // the result is returned by the delegate
        boolean trusted = false;
        try {
            trusted = delegate.isTrusted(chain, authType);
            return trusted;
        } finally {
            logBuilder.append(" : result : trusted = ").append(trusted);
            log.info(logBuilder.toString());
        }
    }

}
