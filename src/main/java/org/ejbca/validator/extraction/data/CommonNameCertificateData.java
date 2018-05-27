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

package org.ejbca.validator.extraction.data;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

public class CommonNameCertificateData implements CertificateData {
    private final JcaX509CertificateHolder certificateHolder;

    public CommonNameCertificateData(final JcaX509CertificateHolder certificateHolder) {
        this.certificateHolder = certificateHolder;
    }

    @Override
    public String getPartName() {
        return "CN";
    }

    @Override
    public List<String> getDataItems() {
        final X500Name x500name = certificateHolder.getSubject();
        final List<String> commonNames = Arrays.asList(x500name.getRDNs(BCStyle.CN)[0].getTypesAndValues())
                .stream()
                .map(cn -> IETFUtils.valueToString(cn.getValue()))
                .collect(Collectors.toList());
        return commonNames;
    }
};
