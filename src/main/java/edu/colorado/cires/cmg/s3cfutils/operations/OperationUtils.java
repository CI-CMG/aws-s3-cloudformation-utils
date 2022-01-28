package edu.colorado.cires.cmg.s3cfutils.operations;


import edu.colorado.cires.cmg.s3cfutils.framework.CloudFormationOperations;
import edu.colorado.cires.cmg.s3cfutils.framework.ParameterKeyValue;
import edu.colorado.cires.cmg.s3cfutils.framework.S3Operations;
import edu.colorado.cires.cmg.s3cfutils.framework.StackContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Utilities for CloudFormation stack operations
 */
public final class OperationUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(OperationUtils.class);

  /**
   * Creates a deployment stack and an application stack
   * @param cf {@link CloudFormationOperations} for interaction between cloud formation templates and stacks
   * @param s3 {@link S3Operations} for interaction with S3 objects
   * @param stackContext the uniquely identifying {@link StackContext} for the stacks
   * @param cfBaseDir the location of the module CloudFormation templates are located in
   * @param version the project version
   * @param deploymentParameters List of {@link ParameterKeyValue} for deployment stack template
   * @param stackParameters List of {@link ParameterKeyValue} for application stack template
   * @param cfPrefix the name of the module CloudFormation templates are located in
   * @param applicationStackName the application stack template file name
   */
  public static void createStacks(
      CloudFormationOperations cf,
      S3Operations s3,
      StackContext stackContext,
      String cfBaseDir,
      String version,
      List<ParameterKeyValue> deploymentParameters,
      List<ParameterKeyValue> stackParameters,
      String cfPrefix,
      String applicationStackName
      ) {

    Path cfTargetDir = Paths.get(cfBaseDir).resolve("target");
    String name = String.format("%s-%s", cfPrefix, version);
    Path bundle = cfTargetDir.resolve(String.format("%s.zip", name));
    Path bundleDir = cfTargetDir.resolve(name);

    if (!Files.exists(bundleDir)) {
      unzip(bundle, cfTargetDir);
    }

    if (!cf.stackExists(stackContext.getDeploymentStackName())) {
      createDeploymentStack(cf, bundleDir, stackContext, deploymentParameters);
    }

    hardSyncBucket(
        s3,
        bundleDir,
        stackContext.getDeploymentBucketName());

    if (!cf.stackExists(stackContext.getStackName())) {
      createStack(cf, stackContext, stackParameters, applicationStackName);
    } else {
      throw new UnsupportedOperationException("Stack already exists: " + stackContext.getStackName());
    }

  }

  /**
   * Creates a deployment stack
   * @param cf {@link CloudFormationOperations} for interaction between cloud formation templates and stacks
   * @param bundleDir location of zip file containing CloudFormation template bundle
   * @param stackContext the uniquely identifying {@link StackContext} for the stacks
   * @param parameters List of {@link ParameterKeyValue} for deployment stack template
   */
  public static void createDeploymentStack(CloudFormationOperations cf, Path bundleDir, StackContext stackContext,
      List<ParameterKeyValue> parameters) {

    String stackName = stackContext.getDeploymentStackName();

    LOGGER.info("Creating Stack: {}", stackName);

    try {
      cf.createStackWithBodyAndWait(
          stackName,
          FileUtils.readFileToString(bundleDir.resolve("deploy/deployment-stack.yaml").toFile(), StandardCharsets.UTF_8),
          parameters);
    } catch (IOException e) {
      throw new RuntimeException("Unable to create deployment stack", e);
    }

    LOGGER.info("Done Creating Stack: {}", stackName);
  }

  /**
   * Creates an application stack
   * @param cf {@link CloudFormationOperations} for interaction between cloud formation templates and stacks
   * @param stackContext the uniquely identifying {@link StackContext} for the stacks
   * @param parameters List of {@link ParameterKeyValue} for application stack template
   * @param applicationStackName the application stack template file name
   */
  public static void createStack(CloudFormationOperations cf, StackContext stackContext, List<ParameterKeyValue> parameters, String applicationStackName) {

    String stackName = stackContext.getStackName();

    LOGGER.info("Creating Stack: {}", stackName);

    cf.createStackWithUrlAndWait(
        stackName,
        String.format("https://s3.amazonaws.com/%s/stack/%s", stackContext.getDeploymentBucketName(), applicationStackName),
        parameters);

    LOGGER.info("Done Creating Stack: {}", stackName);
  }

  /**
   * Uploads CloudFormation templates to S3 bucket
   * @param s3 {@link S3Operations} for interaction with S3 objects
   * @param bundleDir location of zip file containing CloudFormation template bundle
   * @param bucketName the bucket name
   */
  public static void hardSyncBucket(
      S3Operations s3,
      Path bundleDir,
      String bucketName
      ) {

    emptyBucket(s3, bucketName);

    LOGGER.info("Syncing {} to S3 Bucket {}", bundleDir.toString(), bucketName);

    s3.uploadDirectoryToBucket(bundleDir, bucketName);

    LOGGER.info("Done Syncing {} to S3 Bucket {}", bundleDir.toString(), bucketName);
  }

  /**
   * Empties an S3 bucket
   * @param s3 {@link S3Operations} for interaction with S3 objects
   * @param bucketName the bucket name
   */
  public static void emptyBucket(S3Operations s3, String bucketName) {

    LOGGER.info("Emptying Bucket: {}", bucketName);

    s3.deleteObjects(bucketName, s3.listObjects(bucketName));

    try {
      s3.deleteObjects(bucketName, s3.listObjects(bucketName));
    } catch (Exception e) {
      LOGGER.warn("Unable to delete bucket '{}'", bucketName, e);
    }

    LOGGER.info("Done Emptying Bucket: {}", bucketName);

  }

  /**
   * Unzips CloudFormation templates
   * @param bundle location of zip file containing CloudFormation template bundle
   * @param targetDir location of target directory within the module containing CloudFormation templates
   */
  public static void unzip(Path bundle, Path targetDir) {

    LOGGER.info("Unzipping CloudFormation Bundle: {}", bundle.toString());

    try (ZipFile zipFile = new ZipFile(bundle.toFile())) {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        Path entryDestination = targetDir.resolve(entry.getName());
        if (entry.isDirectory()) {
          Files.createDirectories(entryDestination);
        } else {
          Path parent = entryDestination.getParent();
          if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
          }
          try (InputStream in = zipFile.getInputStream(entry);
              OutputStream out = Files.newOutputStream(entryDestination)) {
            IOUtils.copy(in, out);
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Unable to extract zip file", e);
    }

    LOGGER.info("Done Unzipping CloudFormation Bundle: {}", bundle.toString());
  }

  private OperationUtils() {

  }
}
