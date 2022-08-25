package com.comp4050square.testExecutor.parser;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class TestParserTest {
    @Test
    void readFile() throws IOException {
        String path = "src/test/resources/processing.pde";
        File file = new File(path);
        String testFilePath = file.getAbsolutePath();

        TestParser testParser = new TestParser(testFilePath);

        testParser.readFile();
    }

    @Test
    void createFile() throws IOException {
        String path = "src/test/resources/processing.pde";
        File file = new File(path);
        String testFilePath = file.getAbsolutePath();

        TestParser testParser = new TestParser(testFilePath);

        testParser.readFile();
        testParser.createFile();
    }
}