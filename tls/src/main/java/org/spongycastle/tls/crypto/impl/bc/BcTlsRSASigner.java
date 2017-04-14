package org.spongycastle.tls.crypto.impl.bc;

import java.io.IOException;

import org.spongycastle.crypto.CryptoException;
import org.spongycastle.crypto.Signer;
import org.spongycastle.crypto.digests.NullDigest;
import org.spongycastle.crypto.encodings.PKCS1Encoding;
import org.spongycastle.crypto.engines.RSABlindedEngine;
import org.spongycastle.crypto.params.AsymmetricKeyParameter;
import org.spongycastle.crypto.params.ParametersWithRandom;
import org.spongycastle.crypto.signers.GenericSigner;
import org.spongycastle.crypto.signers.RSADigestSigner;
import org.spongycastle.tls.AlertDescription;
import org.spongycastle.tls.SignatureAlgorithm;
import org.spongycastle.tls.SignatureAndHashAlgorithm;
import org.spongycastle.tls.TlsFatalAlert;
import org.spongycastle.tls.TlsUtils;
import org.spongycastle.tls.crypto.TlsSigner;

/**
 * Operator supporting the generation of RSA signatures using the BC light-weight API.
 */
public class BcTlsRSASigner
    implements TlsSigner
{
    private final AsymmetricKeyParameter privateKey;
    private final BcTlsCrypto crypto;

    public BcTlsRSASigner(BcTlsCrypto crypto, AsymmetricKeyParameter privateKey)
    {
        this.crypto = crypto;
        if (privateKey == null)
        {
            throw new IllegalArgumentException("'privateKey' cannot be null");
        }
        if (!privateKey.isPrivate())
        {
            throw new IllegalArgumentException("'privateKey' must be private");
        }

        this.privateKey = privateKey;
    }

    public byte[] generateRawSignature(SignatureAndHashAlgorithm algorithm,
                                       byte[] hash) throws IOException
    {
        Signer signer;
        if (algorithm != null)
        {
            if (algorithm.getSignature() != SignatureAlgorithm.rsa)
            {
                throw new IllegalStateException();
            }

            /*
             * RFC 5246 4.7. In RSA signing, the opaque vector contains the signature generated
             * using the RSASSA-PKCS1-v1_5 signature scheme defined in [PKCS1].
             */
            signer = new RSADigestSigner(new NullDigest(), TlsUtils.getOIDForHashAlgorithm(algorithm.getHash()));
        }
        else
        {
            /*
             * RFC 5246 4.7. Note that earlier versions of TLS used a different RSA signature scheme
             * that did not include a DigestInfo encoding.
             */
            signer = new GenericSigner(new PKCS1Encoding(new RSABlindedEngine()), new NullDigest());
        }
        signer.init(true, new ParametersWithRandom(privateKey, crypto.getSecureRandom()));
        signer.update(hash, 0, hash.length);
        try
        {
            return signer.generateSignature();
        }
        catch (CryptoException e)
        {
            throw new TlsFatalAlert(AlertDescription.internal_error, e);
        }
    }
}
