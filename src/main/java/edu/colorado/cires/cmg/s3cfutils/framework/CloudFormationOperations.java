package edu.colorado.cires.cmg.s3cfutils.framework;

import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.Output;
import java.util.List;

/**
 * Operations for interaction between cloud formation templates and stacks
 */
public interface CloudFormationOperations {

    /**
     * Deletes a stack and waits for completion
     * @param stackName the name of the stack
     */
    void deleteStackAndWait(String stackName);

    /**
     * Creates a stack from a template body and waits for completion
     * @param stackName the name of the stack
     * @param templateBody content of CloudFormation template
     * @param parameters a list of {@link ParameterKeyValue} for the template
     */
    void createStackWithBodyAndWait(String stackName, String templateBody, List<ParameterKeyValue> parameters);

    /**
     * Creates a stack with S3 bucket url and waits for completion
     * @param stackName the name of the stack
     * @param templateUrl S3 url to the template
     * @param parameters a list of {@link ParameterKeyValue} for the template
     */
    void createStackWithUrlAndWait(String stackName, String templateUrl, List<ParameterKeyValue> parameters);

    /**
     * Updates a stack with S3 bucket url and waits for completion
     * @param stackName the name of the stack
     * @param templateUrl S3 url to the updated template
     * @param parameters a list of {@link ParameterKeyValue} for the updated template
     */
    void updateStackWithUrlAndWait(String stackName, String templateUrl, List<ParameterKeyValue> parameters);

    /**
     * Returns true if a stack with a given name exists
     * @param stackName the name of the stack
     * @return true if the stack exists
     */
    boolean stackExists(String stackName);

    /**
     * Gets template outputs for a given stack
     * @param request {@link DescribeStacksRequest} containing the stack name
     * @return List of {@link Output} if outputs were included in the template
     */
    List<Output> getStackOutputs(DescribeStacksRequest request);

}
