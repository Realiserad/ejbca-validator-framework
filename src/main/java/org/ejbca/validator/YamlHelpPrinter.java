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

import java.util.LinkedHashMap;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class YamlHelpPrinter {
    private static final String helpLink = "https://github.com";

    public void printSupportedOperations(final String type) {
        if ("x509".equals(type)) {
            final Yaml yaml = getYaml();
            final LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("supported", "true");
            map.put("recognizedType", "X.509");
            map.put("supportedModules", ImmutableList.builder()
                    .add(ImmutableMap.of(
                            "moduleName", "isHostname",
                            "description", "Checks if the data is a valid hostname"))
                    .build());
            map.put("supportedParts", ImmutableList.builder()
                    .add(ImmutableMap.of(
                            "partOfCertificate", "CN",
                            "description", "The Common Name of the certificate"))
                    .build());
            map.put("helpLink", helpLink);
            System.out.println(yaml.dump(map));
        } else {
            final Yaml yaml = getYaml();
            final LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("supported", "false");
            map.put("helpLink", helpLink);
            System.out.println(yaml.dump(map));
        }

    }

    public void printSupportedTypes() {
        final Yaml yaml = getYaml();
        final LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("supportedTypes", ImmutableList.builder()
                .add("x509")
                .build());
        map.put("helpLink", helpLink);
        System.out.println(yaml.dump(map));
    }

    private Yaml getYaml() {
        final Representer representer = new Representer();
        final DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        return new Yaml(representer, options);
    }
}
