package com.automation.reporting;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;

public final class CucumberStepLoggingPlugin implements ConcurrentEventListener {
    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestRunStarted.class, event -> FeatureFailureRegistry.clear());
        publisher.registerHandlerFor(TestCaseStarted.class, this::onTestCaseStarted);
        publisher.registerHandlerFor(TestStepStarted.class, this::onTestStepStarted);
        publisher.registerHandlerFor(TestStepFinished.class, this::onTestStepFinished);
        publisher.registerHandlerFor(TestCaseFinished.class, this::onTestCaseFinished);
    }

    private void onTestCaseStarted(TestCaseStarted event) {
        TestCase testCase = event.getTestCase();
        System.out.printf("%n[TEST START] %s | %s%n", featureName(testCase), testCase.getName());
    }

    private void onTestStepStarted(TestStepStarted event) {
        if (event.getTestStep() instanceof PickleStepTestStep step) {
            System.out.printf("  [STEP START] %s%n", step.getStepText());
        }
    }

    private void onTestStepFinished(TestStepFinished event) {
        if (event.getTestStep() instanceof PickleStepTestStep step) {
            if (event.getResult().getStatus() == Status.FAILED) {
                FeatureFailureRegistry.markFailed(event.getTestCase().getUri());
            }
            System.out.printf("  [STEP %s] %s (%d ms)%s%n",
                    displayStatus(event.getResult().getStatus()),
                    step.getStepText(),
                    event.getResult().getDuration().toMillis(),
                    errorMessage(event.getResult()));
        }
    }

    private void onTestCaseFinished(TestCaseFinished event) {
        System.out.printf("[TEST %s] %s%n",
                displayStatus(event.getResult().getStatus()),
                event.getTestCase().getName());
    }

    private static String featureName(TestCase testCase) {
        String path = testCase.getUri().getPath();
        if (path == null) {
            return testCase.getUri().toString();
        }
        String filename = path.substring(path.lastIndexOf('/') + 1);
        return filename.endsWith(".feature")
                ? filename.substring(0, filename.length() - ".feature".length())
                : filename;
    }

    private static String displayStatus(Status status) {
        return status == null ? "NOT FINISHED" : status.name();
    }

    private static String errorMessage(Result result) {
        Throwable error = result.getError();
        if (error == null || error.getMessage() == null) {
            return "";
        }
        return " - " + error.getMessage().replaceAll("\\s+", " ").trim();
    }
}
