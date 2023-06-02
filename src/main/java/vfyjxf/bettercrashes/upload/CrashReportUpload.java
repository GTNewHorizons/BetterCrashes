package vfyjxf.bettercrashes.upload;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import vfyjxf.bettercrashes.BetterCrashes;
import vfyjxf.bettercrashes.BetterCrashesConfig;

public class CrashReportUpload {

    private static final Map<String, IUploadService> services = new HashMap<>();
    private static final String DEFAULT_SERVICE = "mclo.gs";

    static {
        services.put("mclo.gs", new MclogsUploadService());
    }

    public static URL uploadCrashReport(String crashReport) throws IOException {
        String service = BetterCrashesConfig.crashLogPasteService;
        IUploadService selectedService = services.get(service);
        if (selectedService == null) {
            BetterCrashes.logger.warn(
                    String.format("Unknown upload service \"%s\", falling back to \"%s\"", service, DEFAULT_SERVICE));
            selectedService = services.get(DEFAULT_SERVICE);
        }

        return selectedService.upload(crashReport);
    }
}
