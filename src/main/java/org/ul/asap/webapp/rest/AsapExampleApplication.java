package org.ul.asap.webapp.rest;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.secnod.shiro.jaxrs.ShiroExceptionMapper;
import org.secnod.shiro.jersey.AuthInjectionBinder;
import org.secnod.shiro.jersey.AuthorizationFilterFeature;
import org.secnod.shiro.jersey.SubjectFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * An example JAX-RS application using Apache Shiro.
 */
public class AsapExampleApplication extends ResourceConfig {

    public AsapExampleApplication() {
        super();
        register(new AuthorizationFilterFeature());
        register(new SubjectFactory());
        register(new AuthInjectionBinder());
        register(new ShiroExceptionMapper());

        register(MultiPartFeature.class);

        try {
            turnOffHostnameVerifier();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // register your REST service
        register(new SwitchService());

        register(new SubjectAuthResource());
    }

    // turn off HTTPS host key checking
    private static void turnOffHostnameVerifier() throws Exception {
        TrustManager[] tam = { new X509TrustManager() {
            @Override public void checkClientTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString) throws CertificateException {}
            @Override public void checkServerTrusted( X509Certificate[] paramArrayOfX509Certificate, String paramString) throws CertificateException {}
            @Override public X509Certificate[] getAcceptedIssuers() { return null; } } };
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, tam, new SecureRandom());
        javax.net.ssl.SSLSocketFactory ssf = ctx.getSocketFactory();
        HttpsURLConnection.setDefaultSSLSocketFactory(ssf);

        HttpsURLConnection.setDefaultHostnameVerifier( org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    }
}
