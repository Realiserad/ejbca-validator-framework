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

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ejbca.validator.extraction.data.CertificateData;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

public class ModuleExecutor {
    private static final Logger log = LogManager.getLogger(ModuleExecutor.class);
    private final ValidatorModule module;
    private final List<CertificateData> dataToValidate;
    private final boolean okIsFailure;

    public ModuleExecutor(final ValidatorModule module, final List<CertificateData> dataToValidate,
            final boolean okIsFailure) {
        this.module = module;
        this.dataToValidate = dataToValidate;
        this.okIsFailure = okIsFailure;
    }

    public boolean execute() {
        for (final CertificateData certificateData : dataToValidate) {
            log.debug("Validating {} data '{}' in certificate using {} module", certificateData.getPartName(),
                    certificateData.getDataItems(), (okIsFailure ? "-" : "+") + module.getModuleName());
            final Map<String, Boolean> validationResults = module.validate(certificateData);
            log.debug("Validation results: {}{}", System.lineSeparator(), getYaml().dump(validationResults));
            final boolean validationOk = validationResults
                    .entrySet()
                    .stream()
                    .map(entry -> entry.getValue())
                    .allMatch(value -> value == true);
            if (validationOk == okIsFailure) {
                // Either okay is passed and validation passed or okay is failure and validation
                // failed
                return false;
            }
        }
        return true;
    }

    private Yaml getYaml() {
        final Representer representer = new Representer();
        final DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        return new Yaml(representer, options);
    }
}
