# What is cert-validator?

External Command Certificate Validators was introduced in EJBCA Enterprise 6.11 and runs on either a certificate or pre-certificate object and calls a script on the local filesystem.

cert-validator is a framework for creating your own Java-based certificate validator which can be invoked by EJBCA.

For more information about External Command Certificate Validators in EJBCA, consult the [EJBCA documentation](https://www.ejbca.org/docs/Post_Processing_Validators.html).

# How does it work?
The validator reads the certificate in PEM format from stdin, extracts data from the certificate and performs validation on the extracted data using the defined modules. The exit status of the execution determines if the validation succeeded or not, where a non-zero exit status means a validation failure occurred in one of the modules.

# How do I use it?

## Check Java version
cert-validator requires Java 8 or later
```
> java -version
openjdk version "1.8.0_171"
OpenJDK Runtime Environment (build 1.8.0_171-8u171-b11-0ubuntu0.16.04.1-b11)
OpenJDK 64-Bit Server VM (build 25.171-b11, mixed mode)
```

## Compile
cert-validator can be built as a standalone jar file using Gradle

```
gradle clean shadowJar
```
## Run it
The syntax for running cert-validator on the command line is

```
java -jar cert-validator.jar type {-|+}moduleName=part[,part...]...
```

Where `type` is telling the certificate validator what kind of certificate it should expect. Currently only the `x509`option is supported.

`moduleName` is the name of the validation module and should be one of the following
* `isHostname` Checks if the data is a valid hostname. The validation is performed using [Guava's InternetDomainName implementation](https://google.github.io/guava/releases/20.0/api/docs/com/google/common/net/InternetDomainName.html).

You can specify any number of modules. The behaviour of a module can be toggled using a plus or minus sign in front of the module name, i.e. `-module` means the module should fail if the data was validated successfully. Conversely `+module` means the module should succeed if the data was validated successfully.

`part` specifies the part of the certificate to perform validation on and should be one of the following:
 * `CN` The Common Name of the Distinguished Name
 
You can specify more than one part of the certificate using a comma-separated list. The module will fail if the part is missing from the certificate during validation. 

## Examples

A validation of an X.509 certificate which succeeds iff the Common Name is present and NOT a valid hostname

> java -jar cert-validator.jar x509 -isHostname=CN

# Use cert-validator with EJBCA

## Check EJBCA version
Make sure you're running EJBCA 6.11 or later.

## Deploy cert-validator
Download the latest version of cert-validator and put it in the folder on the filesystem. For the sake of example, we'll use the path `opt/wildfly/scripts/` as an example.

Create a script which will be invoked by EJBCA
```
cd /opt/wildfly/scripts
echo '#!/bin/bash' > cert-validator.sh
echo 'java -jar /opt/wildfly/scripts/cert-validator.jar "$@"' >> cert-validator.sh
```
Change owner of cert-validator.jar to wildfly user and make the script executable
```
cd /opt/wildfly/scripts
sudo chown wildfly:wildfly cert-validator*
sudo chmod +x cert-validator.sh
```

## Enable external scripts
Open EJBCA admin web in a browser, log in as CA Administrator and navigate to `System Configuration -> External Scripts`. Check "Validate" and "Use the whitelist below". In the textbox type `/opt/wildfly/scripts/cert-validator.sh`and click on "Validate" to make sure the script is available and can be read by EJBCA. Finally click on "Save" to save the configuration. 

![External Scripts Configuration](https://image.ibb.co/cvjByJ/Screenshot_from_2018_05_27_23_46_09.png "Enable External Scripts in EJBCA")

## Create an External Command Certificate Validator
As an example we are creating a validator which ensures that the Common Name of the certificate is a valid hostname. Navigate to `Validators`and add a new validator called `Hostname Validator`. Click on the `Edit`button to edit your new validator. Make the following adjustments:

1. **Validator Type** should be `External Command Certificate Validator` 
2. **Full pathname of script** should be set to `/opt/wildfly/scripts/cert-validator.sh x509 %cert% +isHostname=CN`
3. **Issuance phase** should be set to `Certificate Validation`
4. Select the **Certificate Profiles** you want the validator to act on, or check **Apply for all certificate profiles** if you want to use the validator for all certificate profiles. 

In the **Test Certificate Path** section, click `Browse...` and upload a dummy certificate.

Click on **Test Command** to test the validator and click on **Save**.

![External Command Certificate Validator Configuration](https://image.ibb.co/gAYNoJ/screenshot3.png "Configuration of an External Command Certificate Validator for hostname validation")

## Enable the validator in your CA
Navigate to ``Certification Authorities``, select your CA in the list of CAs and click **Edit**.

Select **Hostname Validator** in the **Other data -> Validators** section and click **Save**.

# Configure logging
Log4j is used for logging. The logging configuration provided in the repository logs on DEBUG level to stdout. If you want to customise logging, you can edit the file `log4j2.xml` located in `src/main/resources`.

Here is an example of what the default log output looks like:
> 
