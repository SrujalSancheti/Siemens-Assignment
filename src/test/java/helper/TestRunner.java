package helper;

import org.testng.annotations.Listeners;

import com.automation.reporting.FeatureFailureListener;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@Listeners(FeatureFailureListener.class)
@CucumberOptions(
        features = "src/test/resources/features",
        tags = "@ui",
        glue = {"com.automation.stepdefinitions"},
        plugin = {
                "pretty",
                "html:target/cucumber-reports/cucumber.html",
                "json:target/cucumber-reports/cucumber.json",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
                "com.automation.reporting.CucumberStepLoggingPlugin",
                "com.automation.reporting.CucumberPdfReportPlugin"
        },
        monochrome = true,
        publish = false
)
public class TestRunner extends AbstractTestNGCucumberTests {
}
