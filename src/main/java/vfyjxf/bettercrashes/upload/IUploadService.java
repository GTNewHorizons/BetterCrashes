package vfyjxf.bettercrashes.upload;

import java.io.IOException;
import java.net.URL;

public interface IUploadService {

    /**
     * Upload crash report to pastebin service
     *
     * @param contents crash report
     * @return URL pointing to the uploaded crash report
     * @throws IOException
     */
    URL upload(String contents) throws IOException;
}
