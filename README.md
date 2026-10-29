# Sauce Demo UI automation

Java 17+, Maven, Playwright for Java, Cucumber, and TestNG are used to test Sauce Demo. All reusable framework utilities are under `src/main/java/com/automation/util`. UI locators are XPath entries in page-specific JSON files under `src/main/resources/locators`. The shared Cucumber step definitions are in `src/test/java/com/automation/stepdefinitions/Playwright_CommonMethods.java`, and the TestNG runner is in `src/test/java/helper/TestRunner.java`.

UI page interactions are grouped into small page classes under `src/main/java/com/automation/ui/pages` (login, inventory, cart, and checkout). UI validations use Playwright's Java assertion API, which retries locator/page conditions until they pass or the assertion timeout expires. TestNG assertions remain in the API step definitions; TestNG is also used to run Cucumber scenarios.

Features live under `src/test/resources/features/ui`. UI test inputs are stored in `src/test/resources/testdata/UiTestData.xlsx`; API inputs are stored separately in `src/test/resources/testdata/ApiTestData.xlsx`. Both workbooks use a `TestData` worksheet, a first-column row key, and column headers for field names. Keep credentials, booking payloads, and other API-only data in the API workbook; keep browser credentials and checkout/product data in the UI workbook.

## API automation

Rest Assured API tests for the [Restful Booker API](https://restful-booker.herokuapp.com/apidoc/index.html) share the same Cucumber/TestNG runner and use their own workbook through `ApiExcelUtility`. The API layer follows a service-oriented structure inspired by the linked [RestAssured-Automation-Framework](https://github.com/SrujalSancheti/RestAssured-Automation-Framework): `BaseService` provides common Rest Assured request setup, endpoint-specific services implement authentication, bookings, and health calls, request/response models represent API payloads, and a filter logs request method/URI and response status without logging credentials or tokens. API scenarios and step definitions live under `src/test/resources/features/api` and `src/test/java/com/automation/stepdefinitions/api`.

The API coverage includes health, booking-list retrieval, authentication success and invalid credential combinations, unknown IDs, all 15 combinations of the documented booking search filters, valid booking payload variants, and a create/read/replace/partial-update/delete lifecycle. Stateful scenarios create and clean up only their own bookings. Run only read/auth API scenarios with `mvn test '-Dcucumber.filter.tags=@api and not @stateful'`; run all API scenarios with `mvn test '-Dcucumber.filter.tags=@api'`. Set `-DapiBaseUrl=https://restful-booker.herokuapp.com` to override the API host. The public demo service returned HTTP 418 for booking creation during this integration; stateful scenarios intentionally report that response rather than treating it as success.

## Select tests by tag

The shared [TestRunner](./src/test/java/helper/TestRunner.java) runs scenarios tagged `@ui` or `@api` by default. UI features also have `@login` and `@shopping` tags; API features have `@apiHealthCheck`, `@apiAuthentication`, and `@apiBookings` tags. To select a group in the runner itself, change its `tags` expression, for example to `@login`, `@shopping`, or `@apiBookings`. Alternatively, override the runner filter at the command line:

```powershell
mvn test '-Dcucumber.filter.tags=@login'
mvn test '-Dcucumber.filter.tags=@shopping'
mvn test '-Dcucumber.filter.tags=@apiAuthentication'
mvn test '-Dcucumber.filter.tags=@apiBookings and not @stateful'
```

## Logs, Allure reports, and UI screenshots

The Cucumber runner logs each feature step to the console as it starts and finishes, including its status and duration. It also creates a timestamped PDF for each feature directly under the root-level `test-results` folder whenever a test run completes. Each PDF starts with scenario and step outcome pie charts, followed by tabular run metadata, totals, scenario results, and step details including status, duration, and failure information. Allure stores test results in a timestamped `test-results/allure-results-<date-time>` folder at the project root. Run the full Maven verification lifecycle to generate the browsable HTML report in `test-results/allure-report-<date-time>`:

```powershell
mvn verify
```

When a feature step fails, Cucumber skips the remaining steps in that scenario. The runner also skips any later scenarios in the same feature and proceeds to the next selected feature. Other selected features continue to run.

For UI scenarios tagged `@ui`, pre- and post-run screenshots are saved under `test-output/screenshots_<date-time>/<feature-name>` at the project root. The pre screenshot is captured after the initial Sauce Demo page navigation; the post screenshot is captured after the scenario steps finish. Failure screenshots are also attached to the Allure report.

## Setup

Install the Maven dependencies and Playwright Chromium browser:

```powershell
mvn test-compile
mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium" -Dexec.classpathScope=test
```

Install `firefox` or `webkit` instead if you intend to run those browser options.

## Run

Run all scenarios headlessly in Chromium:

```powershell
mvn test
```

Override browser, headless mode, or base URLs with Maven properties:

```powershell
mvn test -Dbrowser=firefox -Dheadless=false
mvn test -DbaseUrl=https://www.saucedemo.com
mvn test -DapiBaseUrl=https://restful-booker.herokuapp.com
```

Supported browser values are `chromium`, `firefox`, and `webkit`. Use `-Dbrowser=firefox` or `-Dbrowser=webkit` with the features' `"configured"` browser setting.
