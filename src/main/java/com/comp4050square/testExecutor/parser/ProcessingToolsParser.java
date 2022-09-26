package com.comp4050square.testExecutor.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class ProcessingToolsParser {

    private String readStream(InputStream stream) {
        StringBuilder result = new StringBuilder();

        Scanner scanner = new Scanner(stream).useDelimiter("\\n");

        while (scanner.hasNext()) {
            result.append(scanner.next());
        }

        return result.toString();
    }

    public Boolean parse(String path) throws IOException {
        boolean compileError = false;
        String[] pathList = path.split("/");
        String outPath = String.format("/tmp/out/%s/%s", pathList[pathList.length - 2], pathList[pathList.length - 1]);

        Process process = Runtime.getRuntime().exec(new String[]{"/usr/local/bin/processing-java", "--force", "--sketch=" + path, "--output=" + outPath, "--build"});

        InputStream serrStream = process.getErrorStream();

        String errString = readStream(serrStream);

        if (errString.length() != 0) {
            compileError = true;
        }

        return !compileError;
    }
}
