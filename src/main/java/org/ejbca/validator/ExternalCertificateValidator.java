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

package org.ejbca.validator;

import java.math.BigInteger;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ejbca.validator.exception.CertificateExtractionException;
import org.ejbca.validator.exception.ModuleCreationException;
import org.ejbca.validator.extraction.X509CertificateDataExtractor;
import org.ejbca.validator.module.ModuleExecutor;
import org.ejbca.validator.module.ValidatorModuleFactory;

/**
 * Implements a flexible certificate validator which can be invoked on the
 * command line. This makes it possible to use it as an "External Command
 * Certificate Validator" in EJBCA. The validator reads the certificate in PEM
 * format from the command line, extracts parts of the certificate and performs
 * validation on the extracted data using the defined modules. The exit status
 * of the execution determines if the validator succeeded or not, where a
 * non-zero exit status means a validation failure occurred in one of the
 * modules.
 * <p>
 * Usage:
 * <code>java -jar cert-validator.jar [<strong>type</strong>...] {-|+}<strong>module</strong>=<strong>part</strong>[,<strong>part</strong>]...</code>
 * <p>
 * <code>type</code> should be one of the following
 * <ul>
 * <li><code>x509</code> Perform validation of an X.509 certificate</li>
 * </ul>
 * <p>
 * <code>module</code> should be one of the following
 * <ul>
 * <li><code>isHostname</code> Checks if the data is a valid hostname.</li>
 * </ul>
 * <p>
 * The behaviour of a module can be toggled using a plus or minus sign in front
 * of the module name, i.e. -module means the module should fail if the data was
 * validated successfully and +module means the module should succeed if the
 * data was validated successfully.
 * <p>
 * <code>part</code> specifies the part of the certificate to validate and
 * should be one of the following:
 * <ul>
 * <li><code>CN</code> The Common Name of the Distinguished Name
 * </ul>
 * <p>
 * <h1>Examples:</h1>
 * <p>
 * A validation of an X.509 certificate which succeeds iff the Common Name is
 * present and NOT a valid hostname</br>
 * <code>java -jar cert-validator.jar x509 -isHostname=CN</code>
 */
public class ExternalCertificateValidator {
    private static final Logger log = LogManager.getLogger();
    private final List<ModuleExecutor> moduleExecutors;

    public static void main(final String[] args) {
        log.info("Starting External Certificate Validator");
        final List<String> argsList = Arrays.asList(args);

        if (argsList.isEmpty()) {
            new YamlHelpPrinter().printSupportedTypes();
            System.exit(1);
        }

        final String type = argsList.get(0);
        if (argsList.size() == 1) {
            new YamlHelpPrinter().printSupportedOperations(type);
            System.exit(1);
        }

        if ("x509".equals(type)) {
            validateX509Certificate(argsList.stream().skip(1).collect(Collectors.toList()));
        } else {
            log.error("Requested validation of unsupported certificate type {}.");
            System.exit(1);
        }
    }

    private static void validateX509Certificate(final List<String> argsList) {
        try {
            final X509Certificate certificate = new CertificateLoader().loadX509CertificateFromPem();
            final BigInteger serialNumber = certificate.getSerialNumber();
            log.info("Loaded certificate with serial number {}", serialNumber);
            final ValidatorModuleFactory validatorModuleFactory = new ValidatorModuleFactory(
                    new X509CertificateDataExtractor(certificate));
            final List<ModuleExecutor> moduleExecutors = validatorModuleFactory.fromCommandLine(argsList);
            final boolean validationOk = new ExternalCertificateValidator(moduleExecutors).run();
            if (!validationOk) {
                log.info("Certificate with serial number {} failed validation.", serialNumber);
                System.exit(-1);
            } else {
                log.info("Certificate with serial number {} passed validation.", serialNumber);
                System.exit(0);
            }
        } catch (final CertificateException e) {
            log.error(e.getMessage());
            System.exit(2);
        } catch (final ModuleCreationException e) {
            log.error(e.getMessage());
            System.exit(3);
        } catch (final CertificateExtractionException e) {
            log.error(e.getMessage());
            System.exit(4);
        }
    }

    public ExternalCertificateValidator(final List<ModuleExecutor> moduleExecutors) {
        this.moduleExecutors = moduleExecutors;
    }

    public boolean run() {
        return moduleExecutors.stream()
                .map(moduleExecutor -> moduleExecutor.execute())
                .allMatch(result -> result == true);
    }
}
