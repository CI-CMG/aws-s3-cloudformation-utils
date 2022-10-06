package edu.colorado.cires.cmg.s3cfutils.operations;

import edu.colorado.cires.cmg.s3cfutils.framework.CloudFormationOperations;
import edu.colorado.cires.cmg.s3cfutils.framework.ITUtils;
import edu.colorado.cires.cmg.s3cfutils.framework.S3Operations;
import edu.colorado.cires.cmg.s3cfutils.framework.StackContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utilities for deleting a CloudFormation stack
 */
public class DeleteStack {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteStack.class);

  private final CloudFormationOperations cf;
  private final S3Operations s3;

  public DeleteStack(CloudFormationOperations cf, S3Operations s3) {
    this.cf = cf;
    this.s3 = s3;
  }

  /**
   * Empties an S3 bucket
   * @param bucket the bucket name
   */
  private void emptyBucket(String bucket) {
    try {
      OperationUtils.emptyBucket(s3, bucket);
    } catch (Exception e) {
      LOGGER.warn("Unable to empty bucket '{}'", bucket, e);
    }
  }

  /**
   * Empties deployment bucket, deletes deployment stack and deletes application stack
   * @param baseDir the project base directory
   */
  public void run(String baseDir, boolean deploymentOnly) {
    Path targetDir = Paths.get(baseDir).resolve("target");
    String id = ITUtils.readId(targetDir);
    LOGGER.info("Deleting AWS Test Resources: {}", id);
    StackContext stackContext = StackContext.Builder.configureTest(id).build();
    emptyBucket(stackContext.getDeploymentBucketName());
    if (!deploymentOnly) {
      deleteStack(stackContext.getStackName());
    }
    deleteStack(stackContext.getDeploymentStackName());

    LOGGER.info("Done Deleting AWS Test Resources: {}", id);
  }

  /**
   * Deletes a stack and waits for completion
   * @param stackName the stack name
   */
  private void deleteStack(String stackName) {

    LOGGER.info("Deleting Stack: {}", stackName);

    try {
      cf.deleteStackAndWait(stackName);
    } catch (Exception e) {
      LOGGER.warn("Unable to delete stack '{}'", stackName, e);
    }

    LOGGER.info("Done Deleting Stack: {}", stackName);
  }




}
