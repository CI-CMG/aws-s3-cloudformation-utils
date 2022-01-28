package edu.colorado.cires.cmg.s3cfutils.operations;

import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Stack;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.colorado.cires.cmg.s3cfutils.framework.CloudFormationOperations;
import edu.colorado.cires.cmg.s3cfutils.framework.ParameterKeyValue;
import edu.colorado.cires.cmg.s3cfutils.framework.S3Operations;
import edu.colorado.cires.cmg.s3cfutils.framework.StackContext;
import java.io.OutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Utilities for creating a CloudFormation stack
 */
public class CreateStack {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateStack.class);

  private static final TypeReference<List<ParameterKeyValue>> LIST_PKV = new TypeReference<List<ParameterKeyValue>>() {
  };

  private final CloudFormationOperations cf;
  private final S3Operations s3;
  private final ObjectMapper objectMapper;

  public CreateStack(CloudFormationOperations cf, S3Operations s3, ObjectMapper objectMapper) {
    this.cf = cf;
    this.s3 = s3;
    this.objectMapper = objectMapper;
  }

  /**
   * Creates deployment and application stack
   * @param version the project version
   * @param cfBaseDir the location of the module CloudFormation templates are located in
   * @param baseDir the project base directory
   * @param cfPrefix the name of the module CloudFormation templates are located in
   * @param deploymentParamsName the deployment parameters file name
   * @param stackParamsName the stack parameters file name
   * @param applicationStackName the application stack template file name
   */
  public void run(String version, String cfBaseDir, String baseDir, String cfPrefix, String deploymentParamsName,
      String stackParamsName, String applicationStackName, boolean writeStackOutputs) {

    String id = String.format("test-%s", RandomStringUtils.random(8, true, true)).toLowerCase(Locale.ENGLISH);

    LOGGER.info("Creating AWS Test Resources: {}", id);

    StackContext stackContext = StackContext.Builder.configureTest(id).build();

    Path deployParams = Paths.get(baseDir).resolve("parameters").resolve(deploymentParamsName);
    Path params = Paths.get(baseDir).resolve("parameters").resolve(stackParamsName);
    List<ParameterKeyValue> deploymentParameters = getDeploymentParameters(deployParams, stackContext);
    List<ParameterKeyValue> stackParameters = getParameters(params, stackContext);

    Path targetDir = Paths.get(baseDir).resolve("target");
    writeIdFile(targetDir, id);

    OperationUtils.createStacks(
        cf,
        s3,
        stackContext,
        cfBaseDir,
        version,
        deploymentParameters,
        stackParameters,
        cfPrefix,
        applicationStackName
    );

    LOGGER.info("Done Creating AWS Test Resources: {}", id);

    if (writeStackOutputs) {

      writeOutputPropertiesFile(cf, targetDir, id, stackContext);

    }

  }

  private void writeOutputPropertiesFile(CloudFormationOperations cf, Path target, String id, StackContext stackContext) {

    LOGGER.info("Writing Stack Properties File: {}", id);

    try {

      Path file = target.resolve("test-stack.properties");
      Files.deleteIfExists(file);

      DescribeStacksRequest request = new DescribeStacksRequest().withStackName(stackContext.getStackName());

      List<Output> outputs = cf.getStackOutputs(request);

      for (Output output: outputs) {

        String property = String.format("%s=%s", output.getOutputKey(), output.getOutputValue()) + "\n";
        FileUtils.writeStringToFile(file.toFile(), property, StandardCharsets.UTF_8);

      }

    } catch (IOException e) {

      throw new RuntimeException("Unable to write properties file", e);

    }

    LOGGER.info("Done Writing Stack Properties File: {}", id);

  }


  /**
   * Writes stack id/prefix to text file
   * @param target the maven target directory path
   * @param id the unique stack id/prefix
   */
  private void writeIdFile(Path target, String id) {

    LOGGER.info("Writing ID File: {}", id);

    try {
      Path file = target.resolve("test-id.txt");
      Files.deleteIfExists(file);
      FileUtils.writeStringToFile(file.toFile(), id, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException("Unable to write id file", e);
    }

    LOGGER.info("Done Writing ID File: {}", id);
  }

  /**
   * Gets parameters for deployment stack
   * @param deployParams the deployment parameter file name
   * @param stackContext the uniquely identifying {@link StackContext} for the stacks
   * @return List of {@link ParameterKeyValue} for deployment stack template
   */
  private List<ParameterKeyValue> getDeploymentParameters(Path deployParams, StackContext stackContext) {
    try {
      List<ParameterKeyValue> kvs = objectMapper.readValue(deployParams.toFile(), LIST_PKV);
      kvs.addAll(getSystemProps("CFD_"));
      kvs.add(new ParameterKeyValue("StackPrefix", stackContext.getStackPrefix()));
      kvs.add(new ParameterKeyValue("DeploymentBucketName", stackContext.getDeploymentBucketName()));
      return kvs;
    } catch (IOException e) {
      throw new RuntimeException("Unable to read deployment parameters", e);
    }
  }

  /**
   * Gets system properties matching a given prefix
   * @param prefix the system property prefix
   * @return List of {@link ParameterKeyValue} that match the system property prefix
   */
  private List<ParameterKeyValue> getSystemProps(String prefix) {
    List<ParameterKeyValue> keyValues = new ArrayList<>();
    System.getProperties().forEach((k,v) -> {
      if(k instanceof String && v instanceof String) {
        String key = (String) k;
        if(key.startsWith(prefix)) {
          key = key.replaceFirst(prefix, "");
          keyValues.add(new ParameterKeyValue(key, (String)v));
        }
      }
    });
    return keyValues;
  }
   /**
   * Gets parameters for application stack
   * @param paramsPath the application stack parameter file name
   * @param stackContext the uniquely identifying {@link StackContext} for the stacks
   * @return List of {@link ParameterKeyValue} for application stack template
   */
  private List<ParameterKeyValue> getParameters(Path paramsPath, StackContext stackContext) {
    try {
      List<ParameterKeyValue> kvs = objectMapper.readValue(paramsPath.toFile(), LIST_PKV);
      kvs.addAll(getSystemProps("CF_"));
      kvs.add(new ParameterKeyValue("StackPrefix", stackContext.getStackPrefix()));
      return kvs;
    } catch (IOException e) {
      throw new RuntimeException("Unable to read parameters", e);
    }
  }

}
