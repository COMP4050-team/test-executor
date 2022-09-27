package com.comp4050square.testExecutor.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class ProcessingToolsParser {

    private String readStream(InputStream stream) {
        StringBuilder result = new StringBuilder();

        try (Scanner scanner = new Scanner(stream).useDelimiter("\\n")) {
            while (scanner.hasNext()) {
                result.append(scanner.next());
            }
        }

        return result.toString();
    }

    public Boolean parse(String path) {
        try {
            boolean compileError = false;
            String[] pathList = path.split("/");
            String outPath = String.format("/tmp/out/%s/%s", pathList[pathList.length - 2],
                    pathList[pathList.length - 1]);

            System.out.println("running processing-java");

            ProcessBuilder builder = new ProcessBuilder();
            builder.command("sh", "-c",
                    "/usr/local/bin/processing/processing-java",
                    "--force",
                    "--sketch=" + path + "/",
                    "--output=" + outPath,
                    "--build");
            Process process = builder.start();
            InputStream serrStream = process.getErrorStream();
            String errString = readStream(serrStream);
            System.out.println(errString);

            process.waitFor();
            if (errString.length() != 0) {
                compileError = true;
            }
            System.out.println("finished processing-java");

            return !compileError;
        } catch (IOException | InterruptedException e) {
            System.out.println("error running processing java");
            System.out.println(e);
            return false;
        }
    }
}
