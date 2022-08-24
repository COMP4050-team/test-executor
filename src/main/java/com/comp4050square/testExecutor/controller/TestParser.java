package com.comp4050square.testExecutor.controller;

import spoon.Launcher;
import spoon.reflect.declaration.CtClass;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TestParser {

    private String path;
    private String fileName;
    private String globalVariable;
    private String drawBlock;
    private String setUpBlock;
    private String otherBlocks;

    TestParser(String p){
        this.path = p;
    }

    public void readFile() throws IOException {

        //Read the file in the given path and create a CtClass to read the content
        File file = new File(path);
        String fileContent = Files.readString(Path.of(path), StandardCharsets.US_ASCII);
        CtClass l = Launcher.parseClass("class Processing {" + fileContent + "}");

        //Store draw and setup block in corresponding string
        drawBlock = String.valueOf(l.getMethod("draw"));
        setUpBlock = String.valueOf(l.getMethod("setup"));

        //Getting the fileName from the path using spliter
        String[] temp = path.split("/");
        fileName = temp[temp.length-1].replace(".pde", "");

        //Getting global variables
        List fields = l.getFields();
        for (int i = 0; i < fields.size(); i++) {
            globalVariable += fields.get(i);
        }

        //Getting all the other methods
        ArrayList method = new ArrayList<>(l.getMethods());
        method.remove(0);
        method.remove(method.size() - 1);
        for (Object x : method) {
            otherBlocks += x;
            otherBlocks += "\n";
        }

        //removing unnecessary null and replacing void to public void
        globalVariable = globalVariable.replaceFirst("null", "");
        otherBlocks = otherBlocks.replaceFirst("null", "");
        otherBlocks = otherBlocks.replace("void", "public void");
    }

    public void createFile() throws IOException {
        //Produce destination path
        String des = path.replace("projects", "content");
        des = des.replace(".pde", ".java");

        //Get the size variable and remove it from the setUpBlock
        String[] temp = setUpBlock.split("\n");
        String sizeVariable = temp[1];
        setUpBlock = setUpBlock.replace(sizeVariable, "");

        FileWriter outFile = new FileWriter(des);
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
                        String.format("%s", globalVariable) +
                        "\n" +
                        String.format("public %s\n", setUpBlock) +
                        "\n" +
                        String.format("public %s\n", drawBlock) +
                        "\n" +
                        String.format("%s", otherBlocks) +
                        String.format("  public void settings() {%s}\n", sizeVariable) +
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
