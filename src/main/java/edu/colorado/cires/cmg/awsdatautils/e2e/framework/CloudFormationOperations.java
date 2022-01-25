package edu.colorado.cires.cmg.awsdatautils.e2e.framework;

import java.util.List;

/**
 *
 */
public interface CloudFormationOperations {

    /**
     * Deletes a stack and waits for completion
     * @param stackName the name of the stack
     */
    void deleteStackAndWait(String stackName);

    /**
     * Creates a stack from filesystem and waits for completion
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
     * Returns true if a stack with a given name exists
     * @param stackName the name of the stack
     * @return true if the stack exists
     */
    boolean stackExists(String stackName);
}
