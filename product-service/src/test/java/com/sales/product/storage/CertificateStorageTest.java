package com.sales.product.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Testcontainers
public class CertificateStorageTest {
    private static final String BUCKET = "test-certificates";

    @Container
    static LocalStackContainer localstack =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:3"));

    private S3Client s3Client;
    private CertificateStorage storage;

    @BeforeEach
    void setUp() {
        var credentials = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey()));

        var serviceConfig = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();

        s3Client = S3Client.builder()
                .endpointOverride(localstack.getEndpoint())
                .region(Region.of(localstack.getRegion()))
                .credentialsProvider(credentials)
                .serviceConfiguration(serviceConfig)
                .build();

        S3Presigner presigner = S3Presigner.builder()
                .endpointOverride(localstack.getEndpoint())
                .region(Region.of(localstack.getRegion()))
                .credentialsProvider(credentials)
                .serviceConfiguration(serviceConfig)
                .build();

        s3Client.createBucket(CreateBucketRequest.builder().bucket(BUCKET).build());

        storage = new CertificateStorage(s3Client, presigner, BUCKET);
    }

    @Test
    void uploadStoresFileInBucket() {

        byte[] content = "содержимое сертификата".getBytes(StandardCharsets.UTF_8);

        MockMultipartFile file = new MockMultipartFile(
                "file",                    // имя поля формы
                "cert.pdf",                // имя файла
                "application/pdf",         // MIME-тип
                 content);                  // содержимое, byte[]

        String key = storage.upload(42L, file);

        byte[] stored = s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                        .bucket(BUCKET)
                        .key(key)
                        .build()
        ).asByteArray();

        assertThat(stored).isEqualTo(content);

    }

}
