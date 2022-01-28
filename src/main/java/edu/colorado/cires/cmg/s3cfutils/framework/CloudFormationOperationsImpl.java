package edu.colorado.cires.cmg.s3cfutils.framework;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.ListStacksRequest;
import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.StackStatus;
import com.amazonaws.services.cloudformation.model.StackSummary;
import com.amazonaws.services.cloudformation.model.UpdateStackRequest;
import com.amazonaws.waiters.WaiterParameters;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Operations for interaction between cloud formation templates and stacks
 */
public class CloudFormationOperationsImpl implements CloudFormationOperations {
    private final AmazonCloudFormation cf;

    public CloudFormationOperationsImpl(AmazonCloudFormation cf) {
        this.cf = cf;
    }

    /**
     * Deletes a stack and waits for completion
     * @param stackName the name of the stack
     */
    public void deleteStackAndWait(String stackName) {
        this.cf.deleteStack((new DeleteStackRequest()).withStackName(stackName));
        this.cf.waiters().stackCreateComplete().run(new WaiterParameters((new DescribeStacksRequest()).withStackName(stackName)));
    }

    /**
     * Creates a stack from a template body and waits for completion
     * @param stackName the name of the stack
     * @param templateBody content of CloudFormation template
     * @param parameters a list of {@link ParameterKeyValue} for the template
     */
    public void createStackWithBodyAndWait(String stackName, String templateBody, List<ParameterKeyValue> parameters) {
        this.cf.createStack((new CreateStackRequest()).withStackName(stackName).withTemplateBody(templateBody).withParameters((Collection)parameters.stream().map(ParameterKeyValue::toParameter).collect(Collectors.toList())));
        this.cf.waiters().stackCreateComplete().run(new WaiterParameters((new DescribeStacksRequest()).withStackName(stackName)));
    }

    /**
     * Creates a stack with S3 bucket url and waits for completion
     * @param stackName the name of the stack
     * @param templateUrl S3 url to the template
     * @param parameters a list of {@link ParameterKeyValue} for the template
     */
    public void createStackWithUrlAndWait(String stackName, String templateUrl, List<ParameterKeyValue> parameters) {
        this.cf.createStack((new CreateStackRequest()).withStackName(stackName).withTemplateURL(templateUrl).withCapabilities(new Capability[]{Capability.CAPABILITY_IAM, Capability.CAPABILITY_AUTO_EXPAND}).withParameters((Collection)parameters.stream().map(ParameterKeyValue::toParameter).collect(Collectors.toList())));
        this.cf.waiters().stackCreateComplete().run(new WaiterParameters((new DescribeStacksRequest()).withStackName(stackName)));
    }

    /**
     * Returns true if a stack with a given name exists
     * @param stackName the name of the stack
     * @return true if the stack exists
     */
    public boolean stackExists(String stackName) {
        return (Boolean)this.cf.listStacks((new ListStacksRequest()).withStackStatusFilters(new StackStatus[]{StackStatus.CREATE_IN_PROGRESS, StackStatus.CREATE_COMPLETE, StackStatus.ROLLBACK_IN_PROGRESS, StackStatus.ROLLBACK_FAILED, StackStatus.ROLLBACK_COMPLETE, StackStatus.DELETE_FAILED, StackStatus.UPDATE_IN_PROGRESS, StackStatus.UPDATE_COMPLETE_CLEANUP_IN_PROGRESS, StackStatus.UPDATE_COMPLETE, StackStatus.UPDATE_ROLLBACK_IN_PROGRESS, StackStatus.UPDATE_ROLLBACK_FAILED, StackStatus.UPDATE_ROLLBACK_COMPLETE_CLEANUP_IN_PROGRESS, StackStatus.UPDATE_ROLLBACK_COMPLETE, StackStatus.REVIEW_IN_PROGRESS, StackStatus.IMPORT_IN_PROGRESS, StackStatus.IMPORT_COMPLETE, StackStatus.IMPORT_ROLLBACK_IN_PROGRESS, StackStatus.IMPORT_ROLLBACK_FAILED, StackStatus.IMPORT_ROLLBACK_COMPLETE})).getStackSummaries().stream().map(StackSummary::getStackName).filter((name) -> {
            return name.equals(stackName);
        }).findFirst().map((name) -> {
            return true;
        }).orElse(false);
    }

    public List<Output> getStackOutputs(DescribeStacksRequest request) {

        DescribeStacksResult result = cf.describeStacks(request);
        return result.getStacks().get(0).getOutputs();

    }

}

