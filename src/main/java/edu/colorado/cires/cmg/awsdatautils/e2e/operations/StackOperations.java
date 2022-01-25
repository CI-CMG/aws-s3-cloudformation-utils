package edu.colorado.cires.cmg.awsdatautils.e2e.operations;

import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.colorado.cires.cmg.awsdatautils.e2e.framework.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;


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
        String deploymentParamsName = args[5].trim();
        String stackParamsName = args[6].trim();
        String applicationStackName = args[7].trim();
        new CreateStack(cf, s3, objectMapper).run(version, cfBaseDir, baseDir,
                cfPrefix, deploymentParamsName, stackParamsName, applicationStackName);
      }
      break;
      case "delete-stack":
        new DeleteStack(cf, s3).run(args[1].trim());
        break;
      default:
        throw new RuntimeException("Invalid command '" + args[0] + "'");
    }

  }

}
