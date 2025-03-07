package com.aliyun.jenkins;

import com.aliyun.jenkins.utils.AliyunClientFactory;
import com.aliyun.jenkins.utils.StepUtils;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.oos.model.v20190601.ListExecutionsRequest;
import com.aliyuncs.oos.model.v20190601.ListExecutionsResponse;
import hudson.Extension;

import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * This class is a  pipeline step:
 * query oos template execution status by oos template execute id.
 */
public class OosStatusQueryStep extends Step {
    //oos template execute region.
    private final String region;
    //oos template execute id.
    private final String executeId;

    // getXXX functions are obtain data.
    public String getExecuteId() {
        return executeId;
    }

    public String getRegion() {
        return region;
    }

    // bind data
    @DataBoundConstructor
    public OosStatusQueryStep(String region, String executeId) {
        this.executeId = executeId;
        this.region = region;
    }

    // step execution point.
    @Override
    public StepExecution start(StepContext stepContext) throws Exception {
        return new OosStatusQueryStep.Execution(stepContext, this);
    }

    //plugin extension
    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return StepUtils.requiresDefault();
        }

        @Override
        public String getFunctionName() {
            return "oosStatusQuery";
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "query oos execute status";
        }
    }

    // specific behavior of execution and return oos template execute status.
    public static class Execution extends SynchronousNonBlockingStepExecution<String> {
        private static final long serialVersionUID = 175374543031461563L;
        private final transient OosStatusQueryStep step;

        protected Execution(@Nonnull StepContext context, OosStatusQueryStep step) {
            super(context);
            this.step = step;
        }

        /**
         * run include:
         * query oos template execute status by oos template execute id.
         */
        @Override
        protected String run() throws Exception {
            AliyunClientFactory instance = new AliyunClientFactory();
            instance.build(this.step.region);
            IAcsClient oosClient = instance.getOosClient();
            ListExecutionsRequest request = new ListExecutionsRequest();
            request.setExecutionId(this.step.executeId);
            String status = null;
            // avoid frequent queries.
            Thread.sleep(500);
            List<ListExecutionsResponse.Execution> executions = oosClient.getAcsResponse(request).getExecutions();
            if (executions.size() > 0) {
                status = executions.get(0).getStatus();
            } else {
                throw new Exception("oos executeId:" + this.step.executeId + " is not exist");
            }
            return status;
        }
    }

}
