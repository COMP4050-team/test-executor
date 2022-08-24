package com.comp4050square.testExecutor.controller;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TestParser {
    String path = "/tmp/projects/sketch_220822a.pde";
    String dest = path.replace("projects", "content");
//    dest = dest.replace(".pde", ".java");

    File file = new File(path);
//    String fileContent = Files.readString(Path.of(path), StandardCharsets.US_ASCII);
//    CtClass l = Launcher.parseClass("class Processing {" + fileContent + "}");

    String fileName = "";
    String globalVariable = "";
//    String drawBlock = String.valueOf(l.getMethod("draw"));
//    String setUpBlock = String.valueOf(l.getMethod("setup"));
    String otherBlocks = "";

        /*
        Getting the fileName from the path using spliter
         */



    /*
    Getting all global variables
     */
//    List fields = l.getFields();
//        for(int i = 0; i < fields.size(); i++){
//        globalVariable += fields.get(i);
//    }

    /*
    Getting all the other methods
     */
//    ArrayList method = new ArrayList<>(l.getMethods());
//        method.remove(0);
//        method.remove(method.size()-1);
//        for(Object x : method){
//        otherBlocks+= x;
//        otherBlocks+= "\n";
}

