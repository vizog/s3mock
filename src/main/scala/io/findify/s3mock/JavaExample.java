package io.findify.s3mock;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.UploadPartRequest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shutty on 8/12/16.
 */
public class JavaExample {


    public static void main(String[] args) {
        S3Mock api = S3Mock.create(8002, "/tmp/s3");
        api.start();

        AmazonS3 client = AmazonS3ClientBuilder
                .standard()
                .withPathStyleAccessEnabled(true)
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:8002", "us-east-1"))
                .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
                .build();
        String bucketName = "document-bucket";
        client.createBucket(bucketName);





        List<PartETag> partETags = new ArrayList<>();

// Step 1: Initialize.
        String key = "urn%3Aoasis%3Anames%3Aspecification%3Aubl%3Aschema%3Axsd%3AInvoice-2/versions/2.1";
//        String key = "urn%3Aoasis%3Anames%3Aspecification%3Aubl%3Aschema%3Axsd%3AInvoice-2/versions/2.1";
        InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(
            bucketName, key);
        InitiateMultipartUploadResult initResponse =
            client.initiateMultipartUpload(initRequest);

        String filePath = "/Users/vid/Tradeshift/document-core/document-core-server/src/test/resources/bigubl.xml";
        File file = new File(filePath);
        long contentLength = file.length();
        long partSize = 5 * 1024 * 1024; // Set part size to 5 MB.

        try {
            // Step 2: Upload parts.
            long filePosition = 0;
            for (int i = 1; filePosition < contentLength; i++) {
                // Last part can be less than 5 MB. Adjust part size.
                partSize = Math.min(partSize, (contentLength - filePosition));

                // Create request to upload a part.
                UploadPartRequest uploadRequest = new UploadPartRequest()
                    .withBucketName(bucketName).withKey(key)
                    .withUploadId(initResponse.getUploadId()).withPartNumber(i)
                    .withFileOffset(filePosition)
                    .withFile(file)
                    .withPartSize(partSize);

                // Upload part and add response to our list.
                partETags.add(client.uploadPart(uploadRequest).getPartETag());

                filePosition += partSize;
            }

            // Step 3: Complete.
            CompleteMultipartUploadRequest compRequest = new
                CompleteMultipartUploadRequest(bucketName,
                key,
                initResponse.getUploadId(),
                partETags);
            System.out.println("etags: -------------------");
            System.out.println(partETags);

            CompleteMultipartUploadResult result = client.completeMultipartUpload(compRequest);
            System.out.println("result location: " + result.getLocation());
        } catch (Exception e) {
            e.printStackTrace();
            client.abortMultipartUpload(new AbortMultipartUploadRequest(
                bucketName, key, initResponse.getUploadId()));
        }
















//        client.putObject(bucketName, key, "contents");
        try {
            S3Object content = client.getObject(bucketName, key);
            S3ObjectInputStream objectContent = content.getObjectContent();
//            System.out.println(getStringFromInputStream(objectContent));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            api.stop();

        }
        api.stop();
        Runtime.getRuntime().exit(1);
    }

    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }



}
