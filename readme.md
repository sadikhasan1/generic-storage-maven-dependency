
# Company Maven Repository Guide

This document provides instructions for deploying Maven packages to our company Nexus repository and consuming these packages in your projects.

## Repository Information

- **URL**: https://sonatype.innovatorslab.net/

## Prerequisites

1. **Maven**: Ensure Maven is installed on your system.
2. **Nexus Credentials**: Obtain your Nexus repository username and password from the system administrator.

## Setting Up Your Maven Environment

To deploy artifacts to the Nexus repository, you need to configure your Maven settings.

### 1. Configure `settings.xml`

Edit the `settings.xml` file located in your Maven configuration directory (`~/.m2/settings.xml`). Add the following configuration to include your Nexus credentials:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>nexus-releases</id>
            <username>your-username</username>
            <password>your-password</password>
        </server>
        <server>
            <id>nexus-snapshots</id>
            <username>your-username</username>
            <password>your-password</password>
        </server>
    </servers>
</settings>
```

Replace `your-username` and `your-password` with your actual Nexus credentials.

## Deploying Artifacts

### 1. Configure Your Project's `pom.xml`

Add the following `distributionManagement` section to your `pom.xml`:

```xml
<distributionManagement>
  <repository>
    <id>nexus-releases</id>
    <url>https://sonatype.innovatorslab.net/repository/maven-releases/</url>
  </repository>
  <snapshotRepository>
    <id>nexus-snapshots</id>
    <url>https://sonatype.innovatorslab.net/repository/maven-snapshots/</url>
  </snapshotRepository>
</distributionManagement>
```

### 2. Deploy Your Artifact

To deploy your Maven project, run the following command:

```sh
mvn deploy
```

This command will package and deploy your artifact to the Nexus repository.

## Consuming Artifacts

To use artifacts from the Nexus repository in your projects, configure your `pom.xml` or `build.gradle` as follows:

### Maven Project

#### 1. Add Repository

Add the repository configuration to your `pom.xml`:

```xml
<repositories>
  <repository>
    <id>nexus-releases</id>
    <url>https://sonatype.innovatorslab.net/repository/maven-releases/</url>
  </repository>
</repositories>
```

#### 2. Add Dependency

Add the dependency to your `pom.xml`:

```xml
<dependencies>
  <dependency>
    <groupId>com.example</groupId>
    <artifactId>your-artifact-id</artifactId>
    <version>1.0.0</version>
  </dependency>
</dependencies>
```

### Gradle Project

#### 1. Add Repository

Add the repository configuration to your `build.gradle`:

```groovy
repositories {
    maven {
        url 'https://sonatype.innovatorslab.net/repository/maven-releases/'
    }
}
```

#### 2. Add Dependency

Add the dependency to your `build.gradle`:

```groovy
dependencies {
    implementation 'com.example:your-artifact-id:1.0.0'
}
```

## Troubleshooting

### Common Issues

- **Authentication Errors**: Ensure your credentials in `settings.xml` are correct.
- **Repository Not Found**: Verify the repository URL in your `pom.xml` or `build.gradle`.

### Support

If you encounter any issues, please contact the system administrator or consult the [Nexus Repository Manager documentation](https://help.sonatype.com/repomanager3).

## Additional Resources

- [Maven Central](https://search.maven.org/)
- [Gradle Documentation](https://docs.gradle.org/)

---

This guide should help you set up, deploy, and consume artifacts from our Nexus repository. Happy coding!
