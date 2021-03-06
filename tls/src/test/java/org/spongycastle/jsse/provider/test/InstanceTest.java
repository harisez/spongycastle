package org.spongycastle.jsse.provider.test;

import java.security.Security;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import junit.framework.TestCase;
import org.spongycastle.jsse.provider.BouncyCastleJsseProvider;

public class InstanceTest
    extends TestCase
{
    protected void setUp()
    {
        Security.addProvider(new BouncyCastleJsseProvider());
    }

    protected void tearDown()
    {
        Security.removeProvider(BouncyCastleJsseProvider.PROVIDER_NAME);
    }

    public void testKeyManager()
        throws Exception
    {
        KeyManagerFactory.getInstance("PKIX", BouncyCastleJsseProvider.PROVIDER_NAME);
    }

    public void testTrustManager()
        throws Exception
    {
        TrustManagerFactory.getInstance("PKIX", BouncyCastleJsseProvider.PROVIDER_NAME);
    }

    public void testSSLContext()
        throws Exception
    {
        SSLContext.getInstance("TLS", BouncyCastleJsseProvider.PROVIDER_NAME);
    }
}
