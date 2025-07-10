package com.backend.hormonalcare.medicalRecord.application.internal.outboundservices.acl;

import okhttp3.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component("medicalRecordSupabaseStorageService")
public class SupabaseStorageServiceMedicalExam {
    private final OkHttpClient client = new OkHttpClient();
    private final SupabasePropertiesMedicalExam properties;

    public SupabaseStorageServiceMedicalExam(SupabasePropertiesMedicalExam properties) {
        this.properties = properties;
    }

    /**
     * Upload file asynchronously to Supabase storage
     * This method is annotated with @Async to offload the file processing and
     * upload task
     * from the main request thread, improving response times for the client.
     *
     * @param fileData         The byte array containing the file data
     * @param originalFileName The original name of the file
     * @return CompletableFuture with the public URL of the uploaded file
     * @throws IOException If there's an error during file upload
     */
    @Async("taskExecutor")
    public CompletableFuture<String> uploadFile(byte[] fileData, String originalFileName) throws IOException {
        final long MAX_FILE_SIZE = 2 * 1024 * 1024;
        if (fileData.length > MAX_FILE_SIZE) {
            throw new IOException("File is too large. Maximum allowed size is 2MB.");
        }

        String uniqueFileName = "medical-exam/" + UUID.randomUUID() + "-"
                + originalFileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");

        String contentType;
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.') + 1).toLowerCase();
        switch (fileExtension) {
            case "jpg":
                contentType = "image/jpeg";
                break;
            case "png":
                contentType = "image/png";
                break;
            case "pdf":
                contentType = "application/pdf";
                break;
            case "doc":
                contentType = "application/msword";
                break;
            case "docx":
                contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                break;
            case "ppt":
                contentType = "application/vnd.ms-powerpoint";
                break;
            case "pptx":
                contentType = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
                break;
            case "xls":
                contentType = "application/vnd.ms-excel";
                break;
            case "xlsx":
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                break;
            default:
                throw new IOException("Unsupported file type: " + fileExtension);
        }

        RequestBody requestBody = RequestBody.create(fileData, MediaType.parse(contentType));

        Request request = new Request.Builder()
                .url(properties.getUrl() + "/storage/v1/object/" + properties.getBucket() + "/" + uniqueFileName)
                .addHeader("apikey", properties.getKey())
                .addHeader("Authorization", "Bearer " + properties.getKey())
                .put(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to upload file: " + response);
            }
        }

        String publicUrl = properties.getUrl() + "/storage/v1/object/public/" + properties.getBucket() + "/"
                + uniqueFileName;
        return CompletableFuture.completedFuture(publicUrl);
    }

    /**
     * Delete file asynchronously from Supabase storage
     * This method is annotated with @Async to offload the file deletion task
     * from the main request thread, improving response times for the client.
     *
     * @param filePath The path to the file to delete
     * @return CompletableFuture<Boolean> indicating success or failure
     * @throws IOException If there's an error during file deletion
     */
    @Async("taskExecutor")
    public CompletableFuture<Boolean> deleteFile(String filePath) throws IOException {
        Request request = new Request.Builder()
                .url(properties.getUrl() + "/storage/v1/object/" + properties.getBucket() + "/" + filePath)
                .addHeader("apikey", properties.getKey())
                .addHeader("Authorization", "Bearer " + properties.getKey())
                .delete()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to delete file: " + response);
            }
            return CompletableFuture.completedFuture(true);
        }
    }

    public SupabasePropertiesMedicalExam getProperties() {
        return properties;
    }

}
