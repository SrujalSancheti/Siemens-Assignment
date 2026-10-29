package com.automation.reporting;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestSourceRead;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

public final class CucumberPdfReportPlugin implements ConcurrentEventListener {
    private static final Path REPORT_DIRECTORY = Path.of("test-results");
    private static final DateTimeFormatter RUN_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    private final Map<URI, FeatureReport> features = new LinkedHashMap<>();
    private final Map<URI, String> descriptions = new LinkedHashMap<>();
    private final Map<UUID, ScenarioReport> scenarios = new LinkedHashMap<>();
    private final Map<UUID, StepReport> runningSteps = new LinkedHashMap<>();
    private final String runId = LocalDateTime.now().format(RUN_ID_FORMAT);

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, this::onSourceRead);
        publisher.registerHandlerFor(TestCaseStarted.class, this::onTestCaseStarted);
        publisher.registerHandlerFor(TestStepStarted.class, this::onTestStepStarted);
        publisher.registerHandlerFor(TestStepFinished.class, this::onTestStepFinished);
        publisher.registerHandlerFor(TestCaseFinished.class, this::onTestCaseFinished);
        publisher.registerHandlerFor(TestRunFinished.class, event -> writeReports());
    }

    private synchronized void onSourceRead(TestSourceRead event) {
        descriptions.put(event.getUri(), extractDescription(event.getSource()));
    }

    private synchronized void onTestCaseStarted(TestCaseStarted event) {
        TestCase testCase = event.getTestCase();
        FeatureReport feature = features.computeIfAbsent(testCase.getUri(),
                uri -> new FeatureReport(featureName(uri), descriptions.getOrDefault(uri, "")));
        ScenarioReport scenario = new ScenarioReport(testCase.getName());
        feature.scenarios().add(scenario);
        scenarios.put(testCase.getId(), scenario);
    }

    private synchronized void onTestStepStarted(TestStepStarted event) {
        if (event.getTestStep() instanceof PickleStepTestStep step) {
            runningSteps.put(event.getTestCase().getId(), new StepReport(step.getStepText()));
        }
    }

    private synchronized void onTestStepFinished(TestStepFinished event) {
        if (!(event.getTestStep() instanceof PickleStepTestStep step)) {
            return;
        }
        ScenarioReport scenario = scenarios.get(event.getTestCase().getId());
        if (scenario == null) {
            return;
        }
        Result result = event.getResult();
        StepReport report = runningSteps.remove(event.getTestCase().getId());
        if (report == null) {
            report = new StepReport(step.getStepText());
        }
        report.setStatus(result.getStatus());
        report.setErrorMessage(errorMessage(result));
        report.setDurationMillis(result.getDuration().toMillis());
        scenario.steps().add(report);
    }

    private synchronized void onTestCaseFinished(TestCaseFinished event) {
        ScenarioReport scenario = scenarios.get(event.getTestCase().getId());
        if (scenario != null) {
            scenario.setStatus(event.getResult().getStatus());
        }
    }

    private void writeReports() {
        synchronized (this) {
            try {
                Files.createDirectories(REPORT_DIRECTORY);
                for (FeatureReport feature : features.values()) {
                    writeReport(feature);
                }
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to create PDF reports in " + REPORT_DIRECTORY, exception);
            }
        }
    }

    private void writeReport(FeatureReport feature) throws IOException {
        Path output = REPORT_DIRECTORY.resolve(fileName(feature.name()) + "_" + runId + ".pdf");
        try (PDDocument document = new PDDocument()) {
            PdfWriter writer = new PdfWriter(document);
            Counts scenarioCounts = counts(feature.scenarios().stream().map(ScenarioReport::status).toList());
            List<Status> stepStatuses = feature.scenarios().stream()
                    .flatMap(scenario -> scenario.steps().stream())
                    .map(StepReport::status)
                    .toList();
            Counts stepCounts = counts(stepStatuses);
            writer.writeHeader();
            writer.writeTable(
                    List.of("Report information", "Value"),
                    List.of(
                            List.of("Feature", feature.name()),
                            List.of("Description", feature.description().isBlank() ? "-" : feature.description()),
                            List.of("Run ID", runId),
                            List.of("Generated at",
                                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))),
                    new float[]{132, 396});
            writer.writePieCharts("Test case outcomes", scenarioCounts, "Step outcomes", stepCounts);
            writer.writeSection("Execution summary");
            writer.writeTable(
                    List.of("Metric", "Run", "Passed", "Failed", "Skipped"),
                    List.of(
                            List.of("Test cases", value(scenarioCounts.run()), value(scenarioCounts.passed()),
                                    value(scenarioCounts.failed()), value(scenarioCounts.skipped())),
                            List.of("Steps", value(stepCounts.run()), value(stepCounts.passed()),
                                    value(stepCounts.failed()), value(stepCounts.skipped()))),
                    new float[]{190, 84.5f, 84.5f, 84.5f, 84.5f});

            for (ScenarioReport scenario : feature.scenarios()) {
                writer.writeSection("Scenario details");
                writer.writeTable(
                        List.of("Scenario", "Result"),
                        List.of(List.of(scenario.name(), displayStatus(scenario.status()))),
                        new float[]{430, 98});
                writer.writeTable(
                        List.of("#", "Step", "Result", "Duration", "Failure details"),
                        stepRows(scenario.steps()),
                        new float[]{28, 205, 62, 58, 175});
            }
            writer.close();
            document.save(output.toFile());
        }
        System.out.printf("[PDF REPORT] %s%n", output.toAbsolutePath());
    }

    private static List<List<String>> stepRows(List<StepReport> steps) {
        List<List<String>> rows = new ArrayList<>();
        for (int index = 0; index < steps.size(); index++) {
            StepReport step = steps.get(index);
            rows.add(List.of(
                    value(index + 1),
                    step.text(),
                    displayStatus(step.status()),
                    step.durationMillis() + " ms",
                    step.errorMessage().isBlank() ? "-" : step.errorMessage()));
        }
        return rows;
    }

    private static String value(int number) {
        return Integer.toString(number);
    }

    private static Counts counts(List<Status> statuses) {
        int passed = 0;
        int failed = 0;
        int skipped = 0;
        for (Status status : statuses) {
            if (status == Status.PASSED) {
                passed++;
            } else if (status == Status.SKIPPED) {
                skipped++;
            } else {
                failed++;
            }
        }
        return new Counts(statuses.size(), passed, failed, skipped);
    }

    private static String displayStatus(Status status) {
        return status == null ? "NOT FINISHED" : status.name();
    }

    private static String errorMessage(Result result) {
        Throwable error = result.getError();
        return error == null || error.getMessage() == null
                ? ""
                : error.getMessage().replaceAll("\\s+", " ").trim();
    }

    private static String featureName(URI uri) {
        String path = uri.getPath();
        String filename = path == null ? uri.toString() : path.substring(path.lastIndexOf('/') + 1);
        return filename.endsWith(".feature")
                ? filename.substring(0, filename.length() - ".feature".length())
                : filename;
    }

    private static String fileName(String name) {
        return name.replaceAll("[^A-Za-z0-9._-]+", "_");
    }

    private static String extractDescription(String source) {
        StringBuilder description = new StringBuilder();
        boolean foundFeature = false;
        for (String line : source.split("\\R")) {
            String trimmed = line.trim();
            if (!foundFeature) {
                if (trimmed.startsWith("Feature:")) {
                    description.append(trimmed.substring("Feature:".length()).trim());
                    foundFeature = true;
                }
                continue;
            }
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.startsWith("@")
                    || trimmed.startsWith("Background:")
                    || trimmed.startsWith("Scenario:")
                    || trimmed.startsWith("Scenario Outline:")
                    || trimmed.startsWith("Rule:")) {
                break;
            }
            if (description.length() > 0) {
                description.append(' ');
            }
            description.append(trimmed);
        }
        return description.toString();
    }

    private record Counts(int run, int passed, int failed, int skipped) {
    }

    private record FeatureReport(String name, String description, List<ScenarioReport> scenarios) {
        private FeatureReport(String name, String description) {
            this(name, description, new ArrayList<>());
        }
    }

    private static final class ScenarioReport {
        private final String name;
        private final List<StepReport> steps = new ArrayList<>();
        private Status status;

        private ScenarioReport(String name) {
            this.name = name;
        }

        private String name() {
            return name;
        }

        private List<StepReport> steps() {
            return steps;
        }

        private Status status() {
            return status;
        }

        private void setStatus(Status status) {
            this.status = status;
        }
    }

    private static final class StepReport {
        private final String text;
        private Status status;
        private String errorMessage = "";
        private long durationMillis;

        private StepReport(String text) {
            this.text = text;
        }

        private String text() {
            return text;
        }

        private Status status() {
            return status;
        }

        private String errorMessage() {
            return errorMessage;
        }

        private long durationMillis() {
            return durationMillis;
        }

        private void setStatus(Status status) {
            this.status = status;
        }

        private void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        private void setDurationMillis(long durationMillis) {
            this.durationMillis = durationMillis;
        }
    }

    private static final class PdfWriter {
        private static final float PAGE_WIDTH = PDRectangle.LETTER.getWidth();
        private static final float PAGE_HEIGHT = PDRectangle.LETTER.getHeight();
        private static final float LEFT = 42;
        private static final float TABLE_WIDTH = PAGE_WIDTH - LEFT * 2;
        private static final float FONT_SIZE = 8;
        private static final float LINE_HEIGHT = 10;

        private final PDDocument document;
        private PDPageContentStream content;
        private float y;

        private PdfWriter(PDDocument document) throws IOException {
            this.document = document;
            newPage();
        }

        private void writeHeader() throws IOException {
            content.setNonStrokingColor(31, 78, 121);
            content.addRect(0, PAGE_HEIGHT - 82, PAGE_WIDTH, 82);
            content.fill();
            drawText("AUTOMATION TEST REPORT", LEFT, PAGE_HEIGHT - 34, 17, true, 255, 255, 255);
            y = PAGE_HEIGHT - 101;
        }

        private void writePieCharts(String firstTitle, Counts first, String secondTitle, Counts second)
                throws IOException {
            float chartY = y - 28;
            drawPie(firstTitle, first, 150, chartY, 43);
            drawPie(secondTitle, second, PAGE_WIDTH - 192, chartY, 43);
            y = chartY - 112;
        }

        private void writeSection(String title) throws IOException {
            ensureSpace(28);
            y -= 13;
            content.setNonStrokingColor(31, 78, 121);
            content.addRect(LEFT, y - 5, TABLE_WIDTH, 20);
            content.fill();
            drawText(title, LEFT + 8, y + 1, 10, true, 255, 255, 255);
            y -= 23;
        }

        private void writeTable(List<String> headers, List<List<String>> rows, float[] widths) throws IOException {
            if (widths.length != headers.size()
                    || rows.stream().anyMatch(row -> row.size() != headers.size())) {
                throw new IllegalArgumentException("Table column widths, headers, and rows must match.");
            }
            drawTableRow(headers, widths, true);
            for (List<String> row : rows) {
                drawTableRow(row, widths, false);
            }
            y -= 8;
        }

        private void drawTableRow(List<String> cells, float[] widths, boolean header) throws IOException {
            List<List<String>> wrapped = new ArrayList<>();
            int maxLines = 1;
            for (int index = 0; index < cells.size(); index++) {
                List<String> lines = wrap(cells.get(index), widths[index] - 12);
                wrapped.add(lines);
                maxLines = Math.max(maxLines, lines.size());
            }
            float rowHeight = Math.max(22, maxLines * LINE_HEIGHT + 10);
            ensureSpace(rowHeight);
            float x = LEFT;
            for (int index = 0; index < cells.size(); index++) {
                if (header) {
                    content.setNonStrokingColor(224, 232, 240);
                } else {
                    content.setNonStrokingColor((index % 2 == 0) ? 250 : 255, 252, 248);
                }
                content.setStrokingColor(190, 198, 207);
                content.addRect(x, y - rowHeight, widths[index], rowHeight);
                content.fillAndStroke();
                float textY = y - 13;
                for (String line : wrapped.get(index)) {
                    String status = header ? "" : cells.get(index);
                    if (index == 2 && ("PASSED".equals(status) || "FAILED".equals(status)
                            || "SKIPPED".equals(status))) {
                        setStatusTextColor(status);
                    } else {
                        content.setNonStrokingColor(40, 47, 55);
                    }
                    drawText(line, x + 6, textY, FONT_SIZE, header, null, null, null);
                    textY -= LINE_HEIGHT;
                }
                x += widths[index];
            }
            y -= rowHeight;
        }

        private void drawPie(String title, Counts counts, float centerX, float centerY, float radius)
                throws IOException {
            drawText(title, centerX - 70, centerY + radius + 16, 10, true, 40, 47, 55);
            int[] values = {counts.passed(), counts.failed(), counts.skipped()};
            String[] labels = {"Passed", "Failed", "Skipped"};
            int[][] colors = {{46, 125, 50}, {198, 40, 40}, {130, 130, 130}};
            int total = counts.run();
            if (total == 0) {
                content.setNonStrokingColor(225, 229, 233);
                content.addRect(centerX - radius, centerY - radius, radius * 2, radius * 2);
                content.fill();
            } else {
                float angle = 90;
                for (int index = 0; index < values.length; index++) {
                    if (values[index] == 0) {
                        continue;
                    }
                    float sweep = values[index] * 360f / total;
                    content.setNonStrokingColor(colors[index][0], colors[index][1], colors[index][2]);
                    drawSector(centerX, centerY, radius, angle, sweep);
                    angle += sweep;
                }
            }
            float legendY = centerY + 17;
            for (int index = 0; index < labels.length; index++) {
                content.setNonStrokingColor(colors[index][0], colors[index][1], colors[index][2]);
                content.addRect(centerX + radius + 12, legendY - 2, 7, 7);
                content.fill();
                content.setNonStrokingColor(40, 47, 55);
                drawText(labels[index] + ": " + values[index], centerX + radius + 24, legendY, 7, false,
                        null, null, null);
                legendY -= 13;
            }
        }

        private void drawSector(float centerX, float centerY, float radius, float startAngle, float sweep)
                throws IOException {
            content.moveTo(centerX, centerY);
            double start = Math.toRadians(startAngle);
            content.lineTo(centerX + (float) Math.cos(start) * radius,
                    centerY + (float) Math.sin(start) * radius);
            int segments = Math.max(1, (int) Math.ceil(sweep / 90));
            double segmentSweep = Math.toRadians(sweep / segments);
            double angle = start;
            for (int index = 0; index < segments; index++) {
                double next = angle + segmentSweep;
                double factor = 4.0 / 3.0 * Math.tan(segmentSweep / 4.0);
                float x1 = centerX + (float) (radius * (Math.cos(angle) - factor * Math.sin(angle)));
                float y1 = centerY + (float) (radius * (Math.sin(angle) + factor * Math.cos(angle)));
                float x2 = centerX + (float) (radius * (Math.cos(next) + factor * Math.sin(next)));
                float y2 = centerY + (float) (radius * (Math.sin(next) - factor * Math.cos(next)));
                float x3 = centerX + (float) (radius * Math.cos(next));
                float y3 = centerY + (float) (radius * Math.sin(next));
                content.curveTo(x1, y1, x2, y2, x3, y3);
                angle = next;
            }
            content.closePath();
            content.fill();
        }

        private void setStatusTextColor(String status) throws IOException {
            if ("PASSED".equals(status)) {
                content.setNonStrokingColor(46, 125, 50);
            } else if ("FAILED".equals(status)) {
                content.setNonStrokingColor(198, 40, 40);
            } else {
                content.setNonStrokingColor(130, 130, 130);
            }
        }

        private void drawText(String text, float x, float y, float size, boolean bold,
                              Integer red, Integer green, Integer blue) throws IOException {
            if (red != null) {
                content.setNonStrokingColor(red, green, blue);
            } else {
                content.setNonStrokingColor(40, 47, 55);
            }
            content.beginText();
            content.setFont(bold ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA, size);
            content.newLineAtOffset(x, y);
            content.showText(sanitizeText(text));
            content.endText();
        }

        private static String sanitizeText(String text) {
            return text.replaceAll("[^\\x20-\\x7E]", "?");
        }

        private static List<String> wrap(String text, float maxWidth) throws IOException {
            List<String> lines = new ArrayList<>();
            StringBuilder line = new StringBuilder();
            PDType1Font font = PDType1Font.HELVETICA;
            for (String word : sanitizeText(text).split("\\s+")) {
                String candidate = line.length() == 0 ? word : line + " " + word;
                if (font.getStringWidth(candidate) * FONT_SIZE / 1000 > maxWidth && line.length() > 0) {
                    lines.add(line.toString());
                    line.setLength(0);
                    line.append(word);
                } else {
                    if (line.length() > 0) {
                        line.append(' ');
                    }
                    line.append(word);
                }
            }
            if (line.length() > 0 || lines.isEmpty()) {
                lines.add(line.toString());
            }
            return lines;
        }

        private void ensureSpace(float height) throws IOException {
            if (y - height < 42) {
                newPage();
            }
        }

        private void close() throws IOException {
            closePage();
        }

        private void closePage() throws IOException {
            if (content != null) {
                content.close();
                content = null;
            }
        }

        private void newPage() throws IOException {
            closePage();
            PDPage page = new PDPage();
            document.addPage(page);
            content = new PDPageContentStream(document, page);
            y = page.getMediaBox().getHeight() - 50;
        }
    }
}
