package edu.colorado.cires.cmg.s3cfutils.operations;

import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.colorado.cires.cmg.s3cfutils.framework.CloudFormationOperations;
import edu.colorado.cires.cmg.s3cfutils.framework.CloudFormationOperationsImpl;
import edu.colorado.cires.cmg.s3cfutils.framework.ObjectMapperCreator;
import edu.colorado.cires.cmg.s3cfutils.framework.S3Operations;
import edu.colorado.cires.cmg.s3cfutils.framework.S3OperationsImpl;
import edu.colorado.cires.cmg.s3cfutils.framework.StackContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * CloudFormation stack operations from command line parameters
 */
public class StackOperations {


  private static final Logger LOGGER = LoggerFactory.getLogger(StackOperations.class);

  private static final CloudFormationOperations cf = new CloudFormationOperationsImpl(AmazonCloudFormationClientBuilder.defaultClient());
  private static final S3Operations s3 = new S3OperationsImpl(AmazonS3ClientBuilder.defaultClient());
  private static final ObjectMapper objectMapper = ObjectMapperCreator.create();

  public static void main(String[] args) {

    LOGGER.info("{}", Arrays.toString(args));

    switch (args[0]) {
      case "create-stack": {
        String version = args[1].trim();
        String cfBaseDir = args[2].trim();
        String baseDir = args[3].trim();
        String cfPrefix = args[4].trim();
        String deploymentParamsPath = args[5].trim();
        String stackParamsPath = args[6].trim();
        String applicationStackFileName = args[7].trim();
        boolean writeStackOutputs = Boolean.parseBoolean(args[8]);
        new CreateStack(cf, s3, objectMapper).run(version, cfBaseDir, baseDir,
                cfPrefix, deploymentParamsPath, stackParamsPath, applicationStackFileName, writeStackOutputs);
      }
      break;
      case "delete-stack":
        String applicationStackName = args[1].trim();
        String deploymentStackName = args[2].trim();
        String deploymentBucketName = args[3].trim();
        StackContext stackContext = StackContext.Builder.configure()
            .withStackName(applicationStackName)
            .withDeploymentStackName(deploymentStackName)
            .withDeploymentBucketName(deploymentBucketName)
            .build();
        new DeleteStack(cf, s3).run(stackContext, false);
        break;
      case "delete-stored-stack":
        new DeleteStack(cf, s3).run(args[1].trim(), false);
        break;
      case "update-stack":
        String version = args[1].trim();
        applicationStackName = args[2].trim();
        String applicationStackFileName = args[3].trim();
        deploymentStackName = args[4].trim();
        String cfBaseDir = args[5].trim();
        String cfPrefix = args[6].trim();
        String baseDir = args[7].trim();
        String deploymentParamsPath = args[8].trim();
        String stackParamsPath = args[9].trim();
        boolean writeStackOutputs = Boolean.parseBoolean(args[10]);
        new UpdateStack(cf, s3, objectMapper).run(version, applicationStackName, applicationStackFileName, deploymentStackName, cfBaseDir, cfPrefix, baseDir, deploymentParamsPath, stackParamsPath, writeStackOutputs);
        break;
      default:
        throw new RuntimeException("Invalid command '" + args[0] + "'");
    }

  }

}
