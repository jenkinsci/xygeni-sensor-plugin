&emsp;&emsp; ![Xygeni Logo](docs/images/xygenilogo.png)

# Protect the integrity and security of your software assets, pipelines and infrastructure

[Xygeni](https://xygeni.io/?utm_source=jenkins&utm_medium=marketplace)  seamlessly evaluates the security posture of every asset of your Software Development Life Cycle (SDLC). It offers automated asset discovery and a comprehensive inventory, ensuring total transparency over your software projects. Cataloguing all artefacts, resources, and dependencies aids in making informed decisions for asset protection and implementing preventive and mitigative strategies.

Stay proactive with Xygeni's integrated anomaly detection. This feature safeguards your business operations by identifying unusual patterns that indicate emerging threats. Our code tampering prevention ensures the integrity of critical pipelines and files, and enforces security and build procedures. Additionally, Xygeni allows you to proactively identify risky or suspicious user actions, providing automated real-time alerts.


## Global and project security posture

Assess the security posture of all of the components in your SDLC effortlessly using the automatic inventory and assessment capabilities of Xygeni.

![Security Posture](docs/images/xygeni-sec-posture.png)


## Prevention and remediation

Systematically prevent and remediate risks everywhere in the Software Supply Chain, including open-source packages, pipelines, artefacts, runtime assets and infrastructure.

![docs/images/xygeni-prevention.png](docs/images/xygeni-prevention.png)


## Attack Detection
Integrating anomaly detection is a proactive measure to safeguard your business operations by identifying unusual patterns indicating emerging threats.

![docs/images/xygeni-unusual-activity.png](docs/images/xygeni-unusual-activity.png)


# Key features
Xygeni streamlines security processes, improves collaboration, and provides detailed reporting for effective software supply chain security management. It offers a range of essential features, including:

- **Identifying and tracking all components in software projects** to enhance security visibility and control.
- **Continuously scanning and assessing components and dependencies** for vulnerabilities and anomalies.
- **Prioritizing and supporting teams in efficiently remediating** software supply chain issues. Offering remediation support capabilities and **integration with ticketing tools**.
- Offering **advanced reporting capabilities** for tracking and monitoring changes and progress.


# Pre-Requisites
Please note that to utilize this plugin, a license of Xygeni Platform is required. You can easily request your license on our contact page. Dive in and experience the next level of efficiency!

# Contact us
Get in touch today! [Book a demo](https://xygeni.io/book-a-demo?utm_source=jenkins&utm_medium=marketplace) and let us know how we can help you.


Table of contents
=================

* [Installing the plugin](#installing-the-plugin)
* [Xygeni Sensor](#xygeni-sensor-unusual-activity)
* [Running Xygeni Salt Attestation Provenance Step](#xygenisalt-step)


# Installing the plugin

### Requirements:
- Java >= 11
- Jenkins >= 2.387.2

### 1. Set up credentials
- In Jenkins, navigate to `Manage Jenkins > Manage Credentials > System > Global Credentials > Add Credentials`
    * Select `Secret text` as Kind and fill out required information
  ![Global Credentials](docs/images/global_credentials.png)

### 2. Configure Xygeni Sensor plugin settings 
- In Jenkins, navigate to `Manage Jenkins > Configure System > Xygeni Sensor`,
    * Set the `Xygeni Token Credential ID` (generated above).
    * Set the `Xygeni API Url` (https://api.xygeni.io).
    * Optionally click the `TestToken` button to check url connection and token validity.
    * Click the `Save` button. 
    ![Xygeni Configuration](docs/images/xygeni-config.png)



# Xygeni Sensor Unusual Activity

By installing the Xygeni sensor, user activities and actions will be monitored and suspicious events will be sent to the Xygeni server for analysis.


# XygeniSalt Step

## Use Xygeni Salt Step to generate Attestation Provenance

The Xygeni Sensor plugin add a Build Step to generate [SLSA provenance attestations](https://slsa.dev/provenance/).
This step command will generate an slsa attestation provenance ``xygeni-salt-attestation.json`` in SLSA format and upload attestation to ``salt.xygeni.io`` server.

## Job configuration - Freestyle project

The plugin provides a ```Post-build action``` which will generate SLSA provenace attestations.

**Artifact Path Filter**: Specifies the artifacts to include.

![xygenisalt-provenance.png](docs/images/xygenisalt-provenance.png)

**Manually defined Subject Attestation**: Specifies one or more name and content to include (a string, file or image).

![xygenisalt-manual.png](docs/images/xygenisalt-manual.png)

**Signer configuration**: Add reference or enviroment variable to configure the key, public key and password. Optionally a certificate could be added to avoid a round to verification.

![xygenisalt-signer.png](docs/images/xygenisalt-signer.png)

## Job configuration - Pipeline project

In order to use the plugin with the descriptive pipeline syntax, the following snippet can be added:

```
...
post {
    success {
        xygeniSalt artifactFilter: '**', subjects: [[name:'image', image:'index.docker.io/my_org/my_image:latest']], key:'my.key', publicKey:'mypub.pem', keyPassword:'passw', kpiFormat: 'x509'
    }
}
...
```

