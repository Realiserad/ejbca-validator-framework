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

package org.ejbca.validator.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.ejbca.validator.exception.ModuleCreationException;
import org.ejbca.validator.extraction.X509CertificateDataExtractor;
import org.ejbca.validator.extraction.data.CertificateData;

import com.google.common.base.Optional;

public class ValidatorModuleFactory {
    private final X509CertificateDataExtractor certificateDataExtractor;

    public ValidatorModuleFactory(final X509CertificateDataExtractor certificateDataExtractor) {
        this.certificateDataExtractor = certificateDataExtractor;
    }

    public List<ModuleExecutor> fromCommandLine(final List<String> args) {
        final List<ModuleExecutor> moduleExecutors = new ArrayList<>();
        for (final String arg : args) {
            final String[] moduleAndParts = arg.split("=");
            if (moduleAndParts.length != 2) {
                throw new ModuleCreationException(
                        String.format("Malformed module specification '%s'. Missing delimiter '='.", arg));
            }
            final String module = moduleAndParts[0];
            final String[] parts = moduleAndParts[1].split(",");
            if (!module.startsWith("+") && !module.startsWith("-")) {
                throw new ModuleCreationException(
                        String.format("Malformed module specification '%s'. Missing mode -/+.", arg));
            }
            if (parts.length == 0) {
                throw new ModuleCreationException(
                        String.format("Malformed module specification '%s'. No data to validate.", arg));
            }
            final boolean okIsFailure = module.startsWith("-");
            final String moduleName = module.substring(1);
            final Optional<ModuleExecutorBuilder> moduleExecutorBuilder = ValidatorModule.create(moduleName);
            if (!moduleExecutorBuilder.isPresent()) {
                throw new ModuleCreationException(
                        String.format("No module with name '%s' could be found.", moduleName));
            }
            final ModuleExecutor moduleExecutor = moduleExecutorBuilder.get()
                .setModuleName(moduleName)
                .okIsFailure(okIsFailure)
                .setDataToValidate(extractPartsFromCertificate(parts))
                .build();
            moduleExecutors.add(moduleExecutor);
        }
        return moduleExecutors;
    }

    private List<CertificateData> extractPartsFromCertificate(final String[] parts) {
        return Arrays.asList(parts)
                .stream()
                .map(part -> certificateDataExtractor.extractData(part))
                .collect(Collectors.toList());
    }
}
