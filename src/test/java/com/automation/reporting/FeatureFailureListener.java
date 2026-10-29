package com.automation.reporting;

import io.cucumber.testng.PickleWrapper;
import java.net.URI;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.SkipException;

public final class FeatureFailureListener implements IInvokedMethodListener {
    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        if (!method.isTestMethod() || !"runScenario".equals(method.getTestMethod().getMethodName())) {
            return;
        }
        Object[] parameters = testResult.getParameters();
        if (parameters.length == 0 || !(parameters[0] instanceof PickleWrapper pickleWrapper)) {
            return;
        }

        URI featureUri = pickleWrapper.getPickle().getUri();
        if (FeatureFailureRegistry.hasFailed(featureUri)) {
            String message = "Skipped scenario '" + pickleWrapper.getPickle().getName()
                    + "' because an earlier step failed in feature " + featureUri;
            System.out.println("[FEATURE SKIPPED] " + message);
            throw new SkipException(message);
        }
    }
}
