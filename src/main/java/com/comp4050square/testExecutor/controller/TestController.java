package com.comp4050square.testExecutor.controller;

import com.amazonaws.AmazonServiceException;
import com.comp4050square.testExecutor.clients.S3Client;
import com.comp4050square.testExecutor.parser.ProcessingToolsParser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;



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
            // Getting list of files inside the projects
            Set<String> projectFiles = s3Client.listObjects(testDetails.s3KeyProjectFile);
            Set<String> projectPaths = new HashSet<>();

            for(String filePath : projectFiles) {

                File file = new File(filePath);
                projectPaths.add(file.getParentFile().getAbsolutePath());
                
            }

            // Storing the files locally
            for (String projectPath : projectPaths) {

                // Making files in local directory and copying from s3 to local files
                String projectFilePath = "/tmp/" + projectPath;
//                System.out.println(projectFilePath);
                s3Client.downloadFile(projectPath, projectFilePath);

                // TODO: need to move Main.pde into a directory called Main

                // Parsing the project file and creating the corresponding java file
                ProcessingToolsParser parser = new ProcessingToolsParser();
                parser.parse(projectFilePath);
            }

            // Return file content
            return readFile("/tmp/tmpTest.txt");
        } catch (AmazonServiceException | IOException e) {
            System.err.println(e.getMessage());

            System.out.println("Failed to save file");

            return "500 BAD";
        }
    }
}

