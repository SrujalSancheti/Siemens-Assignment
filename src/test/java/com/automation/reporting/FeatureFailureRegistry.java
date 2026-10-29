package com.automation.reporting;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class FeatureFailureRegistry {
    private static final Set<URI> FAILED_FEATURES = ConcurrentHashMap.newKeySet();

    private FeatureFailureRegistry() {
    }

    public static void clear() {
        FAILED_FEATURES.clear();
    }

    public static void markFailed(URI featureUri) {
        FAILED_FEATURES.add(featureUri);
    }

    public static boolean hasFailed(URI featureUri) {
        return FAILED_FEATURES.contains(featureUri);
    }
}
