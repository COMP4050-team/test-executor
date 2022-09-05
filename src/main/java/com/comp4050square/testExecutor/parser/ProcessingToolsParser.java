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

    public void parse(String path) throws IOException {
        boolean compileError = false;

        // TODO: Fix this output path
        Process process = Runtime.getRuntime().exec(String.format("processing-java --force --sketch=%s --output=/tmp/out/%s --build", path, path));

        InputStream serrStream = process.getErrorStream();

        String errString = readStream(serrStream);

        if (errString.length() != 0) {
            compileError = true;
        }

        if (compileError) {
            System.out.println("finished with error");
        } else {
            System.out.println("finished");
        }
    }
}
