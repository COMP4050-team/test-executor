package com.comp4050square.testExecutor;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

public class S3Client {

    private final String bucketName;
    private final AmazonS3 client;

    public S3Client(String bucketName) {
        this.bucketName = bucketName;

        client = AmazonS3ClientBuilder.standard().build();
    }

    public void downloadFile(String s3Key, String downloadPath) throws IllegalArgumentException {
        File outFile = new File(downloadPath);

        ObjectMetadata metadata = client.getObject(new GetObjectRequest(bucketName, s3Key), outFile);

        if (metadata == null) {
            throw new IllegalArgumentException("Test Key does not exist");
        }
    }

    public Set<String> listObjects(String s3Prefix) {
        ListObjectsV2Request req = new ListObjectsV2Request()
                .withBucketName(bucketName)
                .withPrefix(s3Prefix)
                .withDelimiter("/");

        ListObjectsV2Result listResult = client.listObjectsV2(req);

        return listResult.getObjectSummaries().stream().map(S3ObjectSummary::getKey).collect(Collectors.toSet());
    }
}
