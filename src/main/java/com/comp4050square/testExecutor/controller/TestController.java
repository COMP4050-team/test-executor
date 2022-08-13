package com.comp4050square.testExecutor.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;

@RestController
public class TestController {
    String bucketName = "uploads-76078f4";

     static class TestDetails {
        String s3Key;


         public String getS3Key() {
             return s3Key;
         }

         public void setS3Key(String s3Key) {
             this.s3Key = s3Key;
         }
     }

    @PostMapping("/")
    public String runTest(@RequestBody TestDetails testDetails) {
        System.out.println("Running test");

        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().build();

        try {
            File outFile = new File("/tmp/tmpfile.txt");

            System.out.println("Downloading file");

            ObjectMetadata metadata = s3.getObject(new GetObjectRequest(bucketName, testDetails.s3Key), outFile);
            if (metadata == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Key does not exist");
            }

            System.out.println("Saved file");

            try (BufferedReader br = new BufferedReader(new FileReader("/tmp/tmpfile.txt"))) {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                return sb.toString();
            }
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());

            System.out.println("Failed to save file");

            return "500 BAD";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
