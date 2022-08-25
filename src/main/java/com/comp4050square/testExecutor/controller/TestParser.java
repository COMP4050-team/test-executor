package com.comp4050square.testExecutor.controller;

import spoon.Launcher;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.support.reflect.code.CtInvocationImpl;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

public class TestParser {

    private final Path path;
    private String fileName;
    private ArrayList<CtField<?>> globalVariables;
    private CtMethod<?> drawBlock;
    private CtMethod<?> setUpBlock;
    private HashSet<CtMethod<?>> methods;

    public TestParser(String p) {
        this.path = Path.of(p);
    }

    private String getFilenameNoExtension(String fileName) {
        int lastPeriodPos = fileName.lastIndexOf('.');

        if (lastPeriodPos == -1) {
            return fileName;
        } else {
            return fileName.substring(0, lastPeriodPos);
        }
    }

    public void readFile() throws IOException {

        // Read the file in the given path and create a CtClass to read the content
        fileName = getFilenameNoExtension(path.getFileName().toString());

        String fileContent = Files.readString(path, StandardCharsets.US_ASCII);
        CtClass<?> l = Launcher.parseClass("class " + fileName + " {" + fileContent + "}");

        // Store draw and setup block in corresponding string
        drawBlock = l.getMethod("draw");
        setUpBlock = l.getMethod("setup");

        // Getting global variables
        globalVariables = new ArrayList<>(l.getFields());

        // Getting all the other methods
        methods = new HashSet<>(l.getMethods());

        // Remove setup
        methods.removeIf(ctMethod -> ctMethod.getSimpleName().equals("setup"));

        // Remove draw
        methods.removeIf(ctMethod -> ctMethod.getSimpleName().equals("draw"));

        // Set all methods to public
        methods.forEach(method -> method.setVisibility(ModifierKind.PUBLIC));
    }

    private String codeObjectListToString(Collection<?> col) {
        if (col.size() == 0) {
            return "";
        }

        return col.stream().map(Object::toString).collect(Collectors.joining("\n"));
    }

    public void createFile() throws IOException {
        // Get the size variable and remove it from the setUpBlock
        ArrayList<CtStatement> setupStatements = new ArrayList<>();
        CtStatement sizeStatement = null;
        for (CtStatement statement : setUpBlock.getBody().getStatements()) {
            if (statement instanceof CtInvocationImpl) {
                if (((CtInvocationImpl<?>) statement).getExecutable().getSimpleName().equals("size")) {
                    sizeStatement = statement;
                    continue;
                }
            }

            setupStatements.add(statement);
        }

        FileWriter outFile = new FileWriter(String.format("/tmp/%s.java", fileName));
        outFile.write(
                "import processing.core.*; \n" +
                        "import processing.data.*; \n" +
                        "import processing.event.*; \n" +
                        "import processing.opengl.*; \n" +
                        "\n" +
                        "import java.util.HashMap; \n" +
                        "import java.util.ArrayList; \n" +
                        "import java.io.File; \n" +
                        "import java.io.BufferedReader; \n" +
                        "import java.io.PrintWriter; \n" +
                        "import java.io.InputStream; \n" +
                        "import java.io.OutputStream; \n" +
                        "import java.io.IOException; \n" +
                        "\n" +
                        String.format("public class %s extends PApplet {\n", fileName) +
                        "\n" +
                        String.format("%s", codeObjectListToString(globalVariables)) +
                        "\n" +
                        String.format("public void setup() {%s}\n", codeObjectListToString(setupStatements)) +
                        "\n" +
                        String.format("public %s\n", drawBlock) +
                        "\n" +
                        String.format("%s", codeObjectListToString(methods)) +
                        String.format("public void settings() { %s; }\n", sizeStatement) +
                        "  static public void main(String[] passedArgs) {\n" +
                        String.format("    String[] appletArgs = new String[] { \"%s\" };\n", fileName) +
                        "    if (passedArgs != null) {\n" +
                        "      PApplet.main(concat(appletArgs, passedArgs));\n" +
                        "    } else {\n" +
                        "      PApplet.main(appletArgs);\n" +
                        "    }\n" +
                        "  }\n" +
                        "}\n"
        );
        outFile.close();
    }
}
