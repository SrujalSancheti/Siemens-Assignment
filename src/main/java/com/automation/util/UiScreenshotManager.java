package com.automation.util;

import com.microsoft.playwright.Page;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class UiScreenshotManager {
    private static final Path SCREENSHOT_ROOT = Path.of("test-output");
    private static final DateTimeFormatter RUN_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
    private static final ThreadLocal<ScenarioScreenshots> CURRENT = new ThreadLocal<>();
    private static String runId;

    private UiScreenshotManager() {
    }

    public static synchronized void start(String featureName, String scenarioName, String scenarioId) {
        if (runId == null) {
            runId = LocalDateTime.now().format(RUN_ID_FORMAT);
        }
        String safeFeatureName = sanitize(featureName);
        String safeScenarioName = scenarioName.replaceAll("[^A-Za-z0-9._-]+", "_");
        String uniqueId = UUID.nameUUIDFromBytes(scenarioId.getBytes()).toString().substring(0, 8);
        Path featureDirectory = SCREENSHOT_ROOT.resolve("screenshots_" + runId).resolve(safeFeatureName);
        CURRENT.set(new ScenarioScreenshots(featureDirectory, safeScenarioName + "_" + uniqueId));
    }

    public static void capturePre(Page page) {
        ScenarioScreenshots screenshots = CURRENT.get();
        if (screenshots != null && !screenshots.preCaptured()) {
            save(page, screenshots.prePath());
            screenshots.markPreCaptured();
        }
    }

    public static void capturePost(Page page) {
        ScenarioScreenshots screenshots = CURRENT.get();
        if (screenshots != null) {
            if (!screenshots.preCaptured()) {
                capturePre(page);
            }
            save(page, screenshots.postPath());
        }
    }

    public static byte[] screenshotBytes(Page page) {
        return page.screenshot(new Page.ScreenshotOptions().setTimeout(10_000));
    }

    public static void clear() {
        CURRENT.remove();
    }

    private static void save(Page page, Path path) {
        try {
            Files.createDirectories(path.getParent());
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(path)
                    .setFullPage(true)
                    .setTimeout(10_000));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create the UI screenshot directory.", exception);
        }
    }

    private static String sanitize(String name) {
        return name.replaceAll("[^A-Za-z0-9._-]+", "_");
    }

    private static final class ScenarioScreenshots {
        private final Path directory;
        private final String baseName;
        private boolean preCaptured;

        private ScenarioScreenshots(Path directory, String baseName) {
            this.directory = directory;
            this.baseName = baseName;
        }

        private Path prePath() {
            return directory.resolve(baseName + "_pre.png");
        }

        private Path postPath() {
            return directory.resolve(baseName + "_post.png");
        }

        private boolean preCaptured() {
            return preCaptured;
        }

        private void markPreCaptured() {
            preCaptured = true;
        }
    }
}
