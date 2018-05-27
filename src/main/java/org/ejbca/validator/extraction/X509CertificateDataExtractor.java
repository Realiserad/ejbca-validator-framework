/***********************************************************************************************************
 * The MIT License                                                                                         *
 *                                                                                                         *
 * Copyright 2018 Bastian Fredriksson                                                                      *
 *                                                                                                         *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software           *
 * and associated documentation files (the "Software"), to deal in the Software without restriction,       *
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,   *
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,   *
 * subject to the following conditions:                                                                    *
 *                                                                                                         *
 * The above copyright notice and this permission notice shall be included in all copies or substantial    *
 * portions of the Software.                                                                               *
 *                                                                                                         *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT   *
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.     *
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, *
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE     *
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                                                  *
 ***********************************************************************************************************/

package org.ejbca.validator.extraction;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.ejbca.validator.exception.CertificateExtractionException;
import org.ejbca.validator.extraction.data.CertificateData;
import org.ejbca.validator.extraction.data.CommonNameCertificateData;

public class X509CertificateDataExtractor implements CertificateDataExtractor {
    private final JcaX509CertificateHolder certificateHolder;

    public X509CertificateDataExtractor(final X509Certificate certificate) throws CertificateEncodingException {
        this.certificateHolder = new JcaX509CertificateHolder(certificate);
    }

    @Override
    public CertificateData extractData(final String partName) {
        try {
            final CertificateData certificateData = performDataExtraction(partName);
            if (certificateData.getDataItems().size() == 0) {
                throw new CertificateExtractionException(
                        "Could not extract {}s from certificate. This part of the certificate is missing.");
            }
            return certificateData;
        } catch (final CertificateEncodingException e) {
            throw new CertificateExtractionException("An error occurred when decoding the certificate.", e);
        }
    }

    private CertificateData performDataExtraction(final String partName) throws CertificateEncodingException {
        if ("CN".equals(partName)) {
            return extractCommonNames();
        }
        throw new CertificateExtractionException(
                String.format("Extraction of certificate component '%s' is not supported.", partName));
    }

    private CertificateData extractCommonNames() {
        return new CommonNameCertificateData(certificateHolder);
    }
}
