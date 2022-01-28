# aws-s3-cloudformation-utils

The aws-s3-cloudformation-utils project supports the creation and deletion of CloudFormation stacks using CloudFormation templates.

## Adding To Your Project

Add the following dependency to your Maven pom.xml

```xml
    <dependency>
      <groupId>io.github.ci-cmg</groupId>
      <artifactId>aws-s3-cloudformation-utils</artifactId>
      <version>1.0.0-SNAPSHOT</version>
    </dependency>
```

## Required Project Structure
***Note***: Project structure is only required if a user wants to stand up and delete CloudFormation stacks via the "operations" package.
Utilities used by this package such as S3Operations and CloudFormationOperations do not require a project structure.


The following is the required project structure for use of the "operations" package.

```
cloudformation-module
|                   |
|                   assembly
|                   |      |
|                   |      dist.xml
|                   |
|                   src
|                   | |
|                   | cloudformation
|                   |              |
|                   |              deploy
|                   |              |    |
|                   |              |    deployment-stack.yaml
|                   |              |
|                   |              stack
|                   |                  |
|                   |                  application-stack.yaml
|                   |            
|                   pom.xml
|
project-module
             |
             parameters
             |        |
             |        deployment-stack-parameters.json
             |        application-stack-parameters.json
             |
             pom.xml
```
***cloudformation-module:*** module containing CloudFormation templates
* assembly/dist.xml: contains instructions for module to be packaged as a zip file on install
* src/cloudformation:
  * deploy/deployment-stack.yaml: CloudFormation template for deployment stack. Needs at least an S3 bucket
  * stack/application-stack.yaml: CloudFormation template for application stack

***project-module:*** module containing aws-s3-cloudformation-utils dependency
* parameters:
  * deployment-stack-parameters.json: deployment stack parameters
  * application-stack-parameters.json: application stack parameters

## Workflow
This project is designed to receive a bundle of CloudFormation templates as a zip file. Once decompressed, this bundle 
is uploaded to an S3 bucket which is set up by the deployment stack. After this upload, the deployment stack's S3 bucket 
is utilized to provide the necessary templates for the application stack.

1. CloudFormation template bundle received as zip file
2. Unzip CloudFormation template bundle
3. Create S3 bucket from deployment stack template (along with rest of deployment stack)
4. Upload CloudFormation template bundle to deployment stack's S3 bucket
5. Use deployment stack's S3 bucket contents to create application stack
6. Utilize application and deployment stacks
7. Delete stacks

## Usage

### Java
#### Create deployment/application stacks
```java
CloudFormationOperations cf = new CloudFormationOperationsImpl(AmazonCloudFormationClientBuilder.defaultClient());
S3Operations s3 = new S3OperationsImpl(AmazonS3ClientBuilder.defaultClient());
ObjectMapper objectMapper = ObjectMapperCreator.create();

CreateStack createStack = new CreateStack(cf, s3, objectMapper);

createStack.run(
    version,
    cfBaseDir,
    baseDir,
    cfPrefix,
    deploymentParamsName,
    stackParamsName,
    applicationStackName,
    writeStackOutput
    );
```
***parameters***:
* version: your project version
* cfBaseDir: base directory of module containing CloudFormation templates
* baseDir: your project base directory
* cfPrefix: name of module containing CloudFormation templates
* deploymentParamsName: name of deployment stack parameters file
* stackParamsName: name of application stack parameters file
* applicationStackName: name of application stack CloudFormation template file
* writeStackOutput: optionally write the specified outputs from application stack template

***parameters*** (from project structure section):
* cfBaseDir: absolute/path/to/cloudformation-module
* baseDir: absolute/path/to/project-module
* cfPrefix: cloudformation-module
* deploymentParamsName: deployment-stack-parameters.json
* stackParamsName: application-stack-parameters.json
* applicationStackName: application-stack.yaml

#### Delete deployment/application stacks
```java
CloudFormationOperations cf = new CloudFormationOperationsImpl(AmazonCloudFormationClientBuilder.defaultClient());
S3Operations s3 = new S3OperationsImpl(AmazonS3ClientBuilder.defaultClient());

DeleteStack deleteStack = DeleteStack(cf, s3);
deleteStack.run(baseDir);
```
***parameters***:
* basedir: your project base directory

***parameters*** (from project structure section):
* baseDir: absolute/path/to/project-module


### Command Line via Maven

#### Setup
Command line integration can be achieved using the maven-exec-plugin in your pom.xml:

```xml
<plugin>
    <artifactId>exec-maven-plugin</artifactId>
    <groupId>org.codehaus.mojo</groupId>
    <version>3.0.0</version>
    <executions>
      <execution>
        <id>create-stack</id>
        <goals>
          <goal>java</goal>
        </goals>
        <configuration>
          <mainClass>edu.colorado.cires.cmg.s3cfutils.operations.StackOperations</mainClass>
          <arguments>
            <argument>create-stack</argument>
            <argument>${project.version}</argument>
            <argument>absolute/path/to/cloudformation-module</argument>
            <argument>${project.basedir}</argument>
            <argument>cloudformation-module</argument>
            <argument>deployment-stack-parameters.json</argument>
            <argument>application-stack-parameters.json</argument>
            <argument>application-stack.yaml</argument>
            <argument>false</argument>
          </arguments>
        </configuration>
      </execution>
      <execution>
        <id>delete-stack</id>
        <goals>
          <goal>java</goal>
        </goals>
        <configuration>
          <mainClass>edu.colorado.cires.cmg.s3cfutils.operations.StackOperations</mainClass>
          <arguments>
            <argument>delete-stack</argument>
            <argument>${project.basedir}</argument>
          </arguments>
        </configuration>
      </execution>
    </executions>
</plugin>
```

#### Create deployment/application stacks

```shell
mvn -Daws.profile=aws_profile -Daws.region=aws_region exec:java@create-stack
```

#### Delete deployment/application stacks

```shell
mvn -Daws.profile=aws_profile -Daws.region=aws_region exec:java@delete-stack
```

