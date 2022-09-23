package com.comp4050square.testExecutor.controller;

import com.amazonaws.AmazonServiceException;
import com.comp4050square.testExecutor.clients.S3Client;
import com.comp4050square.testExecutor.parser.ProcessingToolsParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.*;


@RestController
public class TestController {

    static class TestDetails {
        String s3KeyTestFile;
        String s3KeyProjectFile;

        public String getS3KeyTestFile() {
            return s3KeyTestFile;
        }

        public void setS3KeyTestFile(String s3KeyTestFile) {
            this.s3KeyTestFile = s3KeyTestFile;
        }

        public String getS3KeyProjectFileKeyTestFile() {
            return s3KeyProjectFile;
        }

        public void setS3KeyProjectFile(String s3KeyProjectFile) {
            this.s3KeyProjectFile = s3KeyProjectFile;
        }
    }
    String bucketName = "uploads-76078f4";

    private String readFile(String filePath) throws IOException {
        if (filePath == null) {
            return "";
        }

        BufferedReader br = new BufferedReader(new FileReader(filePath));
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();

        while (line != null) {
            sb.append(line);
            sb.append(System.lineSeparator());
            line = br.readLine();
        }

        return sb.toString();
    }


    @PostMapping("/")
    public String runTest(@RequestBody TestDetails testDetails) {

        final S3Client s3Client = new S3Client(bucketName);

        try {
            // Storing the test file
            s3Client.downloadFile(testDetails.s3KeyTestFile, "/tmp/tmpTest.txt");

            // Getting list of files inside the projects
            Set<String> s3ProjectFiles = s3Client.listObjects(testDetails.s3KeyProjectFile);

            // Storing the files locally
            for (String s3FilePath : s3ProjectFiles) {
                // Making files in local directory and copying from s3 to local files
                String projectFilePath = "/tmp/" + s3FilePath;
                s3Client.downloadFile(s3FilePath, projectFilePath);
            }

            Set<String> projectPaths = new HashSet<>();
            for(String filePath : s3ProjectFiles) {
                File file = new File("/tmp/" + filePath);
                projectPaths.add(file.getParentFile().getAbsolutePath());
            }

            // Creating JSON Object and Array
            JSONObject obj = new JSONObject();
            JSONArray arr = new JSONArray();

            // Storing the files locally
            for (String localProjectPath : projectPaths) {

               Map<String, String> studentResults = new LinkedHashMap<>();

                // Retrieving SID and Student String
                String[] projectList = localProjectPath.split("/");
                String[] studentDetails = projectList[projectList.length-2].split("_");

                // Adding SID to Map
                studentResults.put("SID", studentDetails[0]);

                // Adding Name to Map
                studentResults.put("Name", studentDetails[1] + " " +studentDetails[2]);

                // TODO: Check the project structure is correct - i.e. Main.pde in a directory called Main

                // Parsing the project file and creating the corresponding java file
                ProcessingToolsParser parser = new ProcessingToolsParser();
                Boolean testPass = parser.parse(localProjectPath);
                if (testPass)
                    studentResults.put("Test", "Passed");
                else
                    studentResults.put("Test", "Failed");

                // Adding map to JSONArray and clearing the map
                arr.put(studentResults);
            }

            // Populating JSON Object
            obj.put("rows", arr);

            // Creating JSON File and writing the JSONObject into it
            File jsonFile = new File("/tmp/result.json");
            try {
                FileWriter writer = new FileWriter(jsonFile);
                writer.write(obj.toString());
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Uploading Result to S3
            String[] uploadPathList = testDetails.s3KeyProjectFile.split("/");
            String uploadPath = String.format("%s/%s/Results/result.json", uploadPathList[0], uploadPathList[1]);
            s3Client.uploadFile(uploadPath, jsonFile);

            // Return file content
            return readFile("/tmp/tmpTest.txt");
        } catch (AmazonServiceException | IOException e) {
            System.err.println(e.getMessage());

            System.out.println("Failed to save file");

            return "500 BAD";
        }
    }
}

