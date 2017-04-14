package org.spongycastle.crypto.tls.test;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;

import org.spongycastle.asn1.x509.Certificate;
import org.spongycastle.crypto.tls.AlertDescription;
import org.spongycastle.crypto.tls.AlertLevel;
import org.spongycastle.crypto.tls.CertificateRequest;
import org.spongycastle.crypto.tls.CipherSuite;
import org.spongycastle.crypto.tls.ClientCertificateType;
import org.spongycastle.crypto.tls.DefaultTlsServer;
import org.spongycastle.crypto.tls.ProtocolVersion;
import org.spongycastle.crypto.tls.SignatureAlgorithm;
import org.spongycastle.crypto.tls.TlsEncryptionCredentials;
import org.spongycastle.crypto.tls.TlsSignerCredentials;
import org.spongycastle.crypto.tls.TlsUtils;
import org.spongycastle.util.Arrays;

class MockTlsServer
    extends DefaultTlsServer
{
    public void notifyAlertRaised(short alertLevel, short alertDescription, String message, Throwable cause)
    {
        PrintStream out = (alertLevel == AlertLevel.fatal) ? System.err : System.out;
        out.println("TLS server raised alert: " + AlertLevel.getText(alertLevel)
            + ", " + AlertDescription.getText(alertDescription));
        if (message != null)
        {
            out.println("> " + message);
        }
        if (cause != null)
        {
            cause.printStackTrace(out);
        }
    }

    public void notifyAlertReceived(short alertLevel, short alertDescription)
    {
        PrintStream out = (alertLevel == AlertLevel.fatal) ? System.err : System.out;
        out.println("TLS server received alert: " + AlertLevel.getText(alertLevel)
            + ", " + AlertDescription.getText(alertDescription));
    }

    protected int[] getCipherSuites()
    {
        return Arrays.concatenate(super.getCipherSuites(),
            new int[]
            {
                CipherSuite.DRAFT_TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256,
            });
    }

    protected ProtocolVersion getMaximumVersion()
    {
        return ProtocolVersion.TLSv12;
    }

    public ProtocolVersion getServerVersion() throws IOException
    {
        ProtocolVersion serverVersion = super.getServerVersion();

        System.out.println("TLS server negotiated " + serverVersion);

        return serverVersion;
    }

    public CertificateRequest getCertificateRequest() throws IOException
    {
        short[] certificateTypes = new short[]{ ClientCertificateType.rsa_sign,
            ClientCertificateType.dss_sign, ClientCertificateType.ecdsa_sign };

        Vector serverSigAlgs = null;
        if (TlsUtils.isSignatureAlgorithmsExtensionAllowed(serverVersion))
        {
            serverSigAlgs = TlsUtils.getDefaultSupportedSignatureAlgorithms();
        }

        Vector certificateAuthorities = new Vector();
        certificateAuthorities.addElement(TlsTestUtils.loadCertificateResource("x509-ca.pem").getSubject());

        return new CertificateRequest(certificateTypes, serverSigAlgs, certificateAuthorities);
    }

    public void notifyClientCertificate(org.spongycastle.crypto.tls.Certificate clientCertificate)
        throws IOException
    {
        Certificate[] chain = clientCertificate.getCertificateList();
        System.out.println("TLS server received client certificate chain of length " + chain.length);
        for (int i = 0; i != chain.length; i++)
        {
            Certificate entry = chain[i];
            // TODO Create fingerprint based on certificate signature algorithm digest
            System.out.println("    fingerprint:SHA-256 " + TlsTestUtils.fingerprint(entry) + " ("
                + entry.getSubject() + ")");
        }
    }

    protected TlsEncryptionCredentials getRSAEncryptionCredentials()
        throws IOException
    {
        return TlsTestUtils.loadEncryptionCredentials(context, new String[]{"x509-server.pem", "x509-ca.pem"},
            "x509-server-key.pem");
    }

    protected TlsSignerCredentials getRSASignerCredentials() throws IOException
    {
        return TlsTestUtils.loadSignerCredentials(context, supportedSignatureAlgorithms, SignatureAlgorithm.rsa,
            "x509-server.pem", "x509-server-key.pem");
    }
}