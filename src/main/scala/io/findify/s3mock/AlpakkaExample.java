package io.findify.s3mock;

import akka.Done;
import akka.actor.ActorSystem;
import akka.actor.dsl.Creators;
import akka.http.javadsl.model.ContentType;
import akka.http.javadsl.model.ContentTypes;
import akka.stream.ActorMaterializer;
import akka.stream.IOResult;
import akka.stream.Materializer;
import akka.stream.alpakka.s3.S3Settings;
import akka.stream.alpakka.s3.javadsl.S3Client;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.StreamConverters;
import akka.util.ByteString;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import java.io.FileInputStream;
import java.util.concurrent.CompletionStage;

public class AlpakkaExample {

    private  S3Mock api;
    ActorSystem system = ActorSystem.create("alpakka");
    ActorMaterializer materializer = ActorMaterializer.create(system);

    S3Client s3Client = createS3Client(system);
    private String s3BucketName = "ts-documentcore-test-testing-eu";

    public AlpakkaExample() {
//        api = S3Mock.create(8002, "/tmp/s3");
         api = new S3Mock.Builder().withInMemoryBackend().withPort(8002).build();

        api.start();

        AmazonS3 client = AmazonS3ClientBuilder
            .standard()
            .withPathStyleAccessEnabled(true)
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:8002", "us-east-1"))
            .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
            .build();
        client.createBucket(s3BucketName);
    }

    private S3Client createS3Client(ActorSystem system) {
        return new S3Client(S3Settings.create(system), system, materializer);
    }

    Sink<ByteString, CompletionStage<Done>> upload(String key, ContentType actualContentType) {

        return s3Client.multipartUpload(s3BucketName, key, actualContentType)
            .mapMaterializedValue(c -> c.handle((result, error) -> {
                if (error != null) {
                    error.printStackTrace();
                    throw new RuntimeException(error);
                }
                return Done.getInstance();}
            ));
    }

    public void testUpload(String key, ContentType type) {
        Source<ByteString, CompletionStage<IOResult>> source = StreamConverters.fromInputStream(
            () -> new FileInputStream("/Users/vid/Tradeshift/document-core/document-core-server/src/test/resources/bigubl.xml"));

        Sink<ByteString, CompletionStage<Done>> sink = upload(key, type);


        source.toMat(sink, (m1,m2) -> m2)
            .run(materializer)
            .thenRun(() -> {
                System.out.println("done");
            });
    }

    public static void main(String[] args) {
        AlpakkaExample example = new AlpakkaExample();
        example.testUpload("keyWith%", ContentTypes.TEXT_XML_UTF8);
        Runtime.getRuntime().exit(1);

    }


}
