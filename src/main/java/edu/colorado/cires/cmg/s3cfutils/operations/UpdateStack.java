package edu.colorado.cires.cmg.s3cfutils.operations;

import static edu.colorado.cires.cmg.s3cfutils.operations.OperationUtils.writeOutputsToFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.colorado.cires.cmg.s3cfutils.framework.CloudFormationOperations;
import edu.colorado.cires.cmg.s3cfutils.framework.ParameterKeyValue;
import edu.colorado.cires.cmg.s3cfutils.framework.S3Operations;
import edu.colorado.cires.cmg.s3cfutils.framework.StackContext;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateStack {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateStack.class);

  private static final com.fasterxml.jackson.core.type.TypeReference<List<ParameterKeyValue>> LIST_PKV = new TypeReference<List<ParameterKeyValue>>() {
  };

  private final CloudFormationOperations cf;
  private final S3Operations s3;
  private final ObjectMapper objectMapper;

  public UpdateStack(CloudFormationOperations cf, S3Operations s3, ObjectMapper objectMapper) {
    this.cf = cf;
    this.s3 = s3;
    this.objectMapper = objectMapper;
  }

  public void run(
      String version,
      String applicationStackName,
      String applicationStackFileName,
      String deploymentStackName,
      String cfBaseDir,
      String cfPrefix,
      String baseDir,
      String deploymentParamsPath,
      String stackParamsPath,
      boolean writeStackOutputs
  ) {


    List<ParameterKeyValue> deploymentParameters = getParameters(Paths.get(deploymentParamsPath));
    List<ParameterKeyValue> stackParameters = getParameters(Paths.get(stackParamsPath));

    String deploymentStackPrefix = getParamValue(deploymentParameters, ParameterConsts.StackPrefix.name());
    String applicationStackPrefix = getParamValue(stackParameters, ParameterConsts.StackPrefix.name());

    if (!deploymentStackPrefix.equals(applicationStackPrefix)) {
      throw new IllegalStateException(String.format("%s must be the same in deployment and stack parameters", ParameterConsts.StackPrefix.name()));
    }

    String deploymentBucketName = getParamValue(deploymentParameters, ParameterConsts.DeploymentBucketName.name());

    StackContext stackContext = StackContext.Builder.configure()
        .withDeploymentBucketName(deploymentBucketName)
        .withDeploymentStackName(deploymentStackName)
        .withStackPrefix(deploymentStackPrefix)
        .withStackName(applicationStackName)
        .build();

    LOGGER.info("Updating AWS Resources: {}", applicationStackName);

    OperationUtils.createOrUpdateStack(
        cf,
        s3,
        stackContext,
        cfBaseDir,
        version,
        deploymentParameters,
        stackParameters,
        cfPrefix,
        applicationStackFileName
    );

    LOGGER.info("Done Updating AWS Resources: {}", applicationStackName);

    if (writeStackOutputs) {
      writeOutputsToFile(cf, Paths.get(baseDir).resolve("target"), applicationStackName, stackContext);
    }

  }

  private String getParamValue(List<ParameterKeyValue> parameters, String key) {
    return parameters.stream()
        .filter(pkv -> pkv.getParameterKey().equals(key))
        .map(ParameterKeyValue::getParameterValue)
        .findFirst().orElse(null);
  }

  private List<ParameterKeyValue> getParameters(Path path) {
    try {
      return objectMapper.readValue(path.toFile(), LIST_PKV);
    } catch (IOException e) {
      throw new RuntimeException("Unable to read parameters", e);
    }
  }

  enum ParameterConsts {
    StackPrefix,
    DeploymentBucketName,
  }
}
