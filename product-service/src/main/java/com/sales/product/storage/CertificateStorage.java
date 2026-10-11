package com.sales.product.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;

@Component
public class CertificateStorage {  // класс, который кладет файл в хранилище и выдает ссылку.

    private static final Logger log = LoggerFactory.getLogger(CertificateStorage.class);
    private static final Duration LINK_TTL = Duration.ofHours(1);

    private final S3Client s3Client;
    private final S3Presigner presigner;
    private final String bucket;

    public CertificateStorage(S3Client s3Client,
                              S3Presigner presigner,
                              @Value("${s3.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.presigner = presigner;
        this.bucket = bucket;
    }

    public String upload(Long productId, MultipartFile file) {
        String key = "certificates/product-%d/%s.pdf".formatted(productId, UUID.randomUUID());

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        try {
            s3Client.putObject(request,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать загружаемый файл", e);
        }

        log.info("Certificate uploaded: key={}, size={}", key, file.getSize());
        return key;
    }

    public URL generateDownloadLink(String key, String filename) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .responseContentDisposition("attachment; filename=\"%s\"".formatted(filename))
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(LINK_TTL)
                .getObjectRequest(getRequest)
                .build();

        return presigner.presignGetObject(presignRequest).url();
    }

}
