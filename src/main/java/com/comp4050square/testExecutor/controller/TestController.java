package com.comp4050square.testExecutor.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;

import java.io.File;

@RestController
public class TestController {
    String bucketName = "testbucket-1823787";

    static class TestDetails {
        String s3Key;

        public void setS3Key(String s3Key) {
            this.s3Key = s3Key;
        }

        public String getS3Key() {
            return s3Key;
        }
    }

    @PostMapping("/")
    public String runTest(@RequestBody TestDetails testDetails) {
        System.out.println("Running test");

        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.AP_SOUTHEAST_2).build();

        try {
            File outFile = new File("/tmp/tmpfile.txt");

            ObjectMetadata metadata = s3.getObject(new GetObjectRequest(bucketName, testDetails.s3Key), outFile);

            System.out.println("Saved file");

            return "200 OK";
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());

            System.out.println("Failed to save file");

            return "500 BAD";
        }
    }
}
