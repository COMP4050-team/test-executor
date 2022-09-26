package com.comp4050square.testExecutor.controller;

import com.amazonaws.AmazonServiceException;
import com.comp4050square.testExecutor.clients.S3Client;
import com.comp4050square.testExecutor.parser.ProcessingToolsParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;


@RestController
public class TestController {

    final String bucketName = "uploads-76078f4";

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
    @ResponseStatus
    public HttpStatus runTest(@RequestBody TestDetails testDetails) {

        final S3Client s3Client = new S3Client(bucketName);

        try {
            System.out.println("testDetails.s3KeyTestFile: "+testDetails.s3KeyTestFile);
            System.out.println("testDetails.s3KeyProjectile: "+testDetails.s3KeyProjectFile);
            // Storing the test file
            s3Client.downloadFile(testDetails.s3KeyTestFile, "/tmp/tmpTest.txt");

            // Getting list of files inside the projects
            Set<String> s3ProjectFiles = s3Client.listObjects(testDetails.s3KeyProjectFile);
            s3ProjectFiles = s3ProjectFiles.stream().filter(file -> file.endsWith(".pde")).collect(Collectors.toSet());
            System.out.println("s3ProjectFiles: "+s3ProjectFiles);

            // Storing the files locally
            for (String s3FilePath : s3ProjectFiles) {
                // Making files in local directory and copying from s3 to local files
                String projectFilePath = "/tmp/" + s3FilePath;
                s3Client.downloadFile(s3FilePath, projectFilePath);
            }

            Set<String> projectPaths = new HashSet<>();
            for (String filePath : s3ProjectFiles) {
                File file = new File("/tmp/" + filePath);
                projectPaths.add(file.getParentFile().getAbsolutePath());
            }

            // Creating JSON Object and Array
            JSONObject obj = new JSONObject();
            JSONArray arr = new JSONArray();
            ProcessingToolsParser parser = new ProcessingToolsParser();

            // Storing the files locally
            for (String localProjectPath : projectPaths) {
                System.out.println("localProjectPath: "+localProjectPath);

                Map<String, String> studentResults = new LinkedHashMap<>();

                // Retrieving SID and Student String
                String[] projectList = localProjectPath.split("/");
                String[] studentDetails = projectList[projectList.length - 2].split("_");
                System.out.println("projectList: "+ Arrays.toString(projectList));

                // Adding SID to Map
                if (studentDetails.length < 1) {
                    studentResults.put("SID", "Unknown");
                } else {
                    // TODO Regex validate
                    studentResults.put("SID", studentDetails[0]);
                }

                // Adding Name to Map
                if (studentDetails.length < 3) {
                    studentResults.put("Name", "Unknown");
                } else {
                    // TODO Regex validate
                    studentResults.put("Name", studentDetails[1] + " " + studentDetails[2]);
                }

                // TODO: Check the project structure is correct - i.e. Main.pde in a directory called Main

                // Parsing the project file and creating the corresponding java file
                Boolean testPass = parser.parse(localProjectPath);
                if (testPass) {
                    studentResults.put("Test", "Passed");
                } else {
                    studentResults.put("Test", "Failed");
                }

                // Adding map to JSONArray and clearing the map
                arr.put(studentResults);
            }

            // Populating JSON Object
            obj.put("rows", arr);

            System.out.println("writing json file");
            // Creating JSON File and writing the JSONObject into it
            File jsonFile = new File("/tmp/result.json");
            try {
                FileWriter writer = new FileWriter(jsonFile);
                writer.write(obj.toString());
                writer.close();
            } catch (IOException e) {
                return HttpStatus.INTERNAL_SERVER_ERROR;
            }
            System.out.println("wrote json file");

            System.out.println("uploading results to s3");
            // Uploading Result to S3
            String[] uploadPathList = testDetails.s3KeyProjectFile.split("/");
            String unitCode = uploadPathList[0];
            String assignmentName = uploadPathList[1];
            String uploadPath = String.format("%s/%s/Results/result.json", unitCode, assignmentName);
            s3Client.uploadFile(uploadPath, jsonFile);
            System.out.println("uploaded results to s3");

            // Return file content
            return HttpStatus.OK;
        } catch (AmazonServiceException e) {
            System.err.println(e.getMessage());

            System.out.println("Failed to save file");

            // TODO actually send a 500 here
            // TODO check whether this is our fault. If not send a 400
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    static class TestDetails {
        String s3KeyTestFile;
        String s3KeyProjectFile;

        public String getS3KeyTestFile() {
            // /<unit code>/<assignment name>/Tests/<filename>.java
            // eg. /COMP1000/A1/Tests/Test.java
            return s3KeyTestFile;
        }

        public void setS3KeyTestFile(String s3KeyTestFile) {
            this.s3KeyTestFile = s3KeyTestFile;
        }

        public String getS3KeyProjectFile() {
            // /<unit code>/<assignment name>/Projects/
            // eg. /COMP1000/A1/Projects/
            return s3KeyProjectFile;
        }

        public void setS3KeyProjectFile(String s3KeyProjectFile) {
            this.s3KeyProjectFile = s3KeyProjectFile;
        }
    }
}

