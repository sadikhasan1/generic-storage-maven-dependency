package com.dsi.storage.util;

import com.dsi.storage.dto.BucketObject;
import org.apache.tika.Tika;
//import org.jetbrains.annotations.NotNull;
//import org.primefaces.model.file.UploadedFile;
//import org.springframework.core.io.ByteArrayResource;
//import org.springframework.core.io.InputStreamResource;
//import org.springframework.core.io.Resource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.ByteArrayInputStream;
//import java.nio.file.Files;
//import java.nio.file.Path;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class FileUtils {

    private static final Tika tika = new Tika();

//    public static UploadedFile createUploadedFile(byte[] fileData, String fileName, String contentType) {
//        return new UploadedFile() {
//            @Override
//            public InputStream getInputStream() throws IOException {
//                return new ByteArrayInputStream(fileData);
//            }
//
//            @Override
//            public byte[] getContent() {
//                return fileData;
//            }
//
//            @Override
//            public String getFileName() {
//                return fileName;
//            }
//
//            @Override
//            public String getWebkitRelativePath() {
//                return "";
//            }
//
//            @Override
//            public String getContentType() {
//                return contentType;
//            }
//
//            @Override
//            public long getSize() {
//                return fileData.length;
//            }
//
//            @Override
//            public void write(String filePath) throws Exception {
//                Path path = Path.of(filePath);
//                Files.write(path, fileData);
//            }
//
//            @Override
//            public void delete() throws IOException {
//            }
//        };
//    }
//
//    public static MultipartFile createMultipartFile(byte[] fileData, String fileName, String contentType) {
//        return new MultipartFile() {
//            @NotNull
//            @Override
//            public String getName() {
//                return fileName;
//            }
//
//            @Override
//            public String getOriginalFilename() {
//                return fileName;
//            }
//
//            @Override
//            public String getContentType() {
//                return contentType;
//            }
//
//            @Override
//            public boolean isEmpty() {
//                return fileData.length == 0;
//            }
//
//            @Override
//            public long getSize() {
//                return fileData.length;
//            }
//
//            @NotNull
//            @Override
//            public byte[] getBytes() throws IOException {
//                return fileData;
//            }
//
//            @NotNull
//            @Override
//            public InputStream getInputStream() throws IOException {
//                return new ByteArrayInputStream(fileData);
//            }
//
//            @Override
//            public void transferTo(@NotNull java.io.File dest) throws IOException, IllegalStateException {
//                try (InputStream in = getInputStream()) {
//                    Files.copy(in, dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
//                }
//            }
//        };
//    }
//
//    public static InputStreamResource createInputStreamResource(byte[] fileData, String fileName, String contentType) {
//        InputStream inputStream = new ByteArrayInputStream(fileData);
//        return new InputStreamResource(inputStream) {
//            @NotNull
//            @Override
//            public String getDescription() {
//                return fileName + " (" + contentType + ")";
//            }
//        };
//    }
//
//    public static ResponseEntity<InputStreamResource> createResponseEntityForInputStreamResource(byte[] fileData, String fileName, String contentType) {
//        InputStreamResource resource = createInputStreamResource(fileData, fileName, contentType);
//        HttpHeaders headers = new HttpHeaders();
//        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + getOriginalFileName(fileName));
//        headers.add(HttpHeaders.CONTENT_TYPE, contentType);
//        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
//    }
//
//
//
//    public static UploadedFile convertToUploadedFile(InputStream inputStream, String fileName) throws IOException {
//        byte[] fileData = readInputStreamToByteArray(inputStream);
//        String contentType = detectMimeType(fileData);
//        return createUploadedFile(fileData, getOriginalFileName(fileName), contentType);
//    }
//
//    public static MultipartFile convertToMultipartFile(InputStream inputStream, String fileName) throws IOException {
//        byte[] fileData = readInputStreamToByteArray(inputStream);
//        String contentType = detectMimeType(fileData);
//        return createMultipartFile(fileData, getOriginalFileName(fileName), contentType);
//    }
//
//    public static InputStreamResource convertToInputStreamResource(InputStream inputStream, String fileName) throws IOException {
//        byte[] fileData = readInputStreamToByteArray(inputStream);
//        String contentType = detectMimeType(fileData);
//        return createInputStreamResource(fileData, getOriginalFileName(fileName), contentType);
//    }
//
//    public static ResponseEntity<InputStreamResource> convertToResponseEntityForInputStreamResource(InputStream inputStream, String fileName) throws IOException {
//        byte[] fileData = readInputStreamToByteArray(inputStream);
//        String contentType = detectMimeType(fileData);
//        return createResponseEntityForInputStreamResource(fileData, getOriginalFileName(fileName), contentType);
//    }
//
//    public static ResponseEntity<Resource> convertToResponseEntityForResource(InputStream inputStream, String fileName, String contentType) throws IOException {
//        byte[] fileData = readInputStreamToByteArray(inputStream);
//        return createResponseEntityForResource(fileData, getOriginalFileName(fileName), contentType);
//    }


    public static byte[] readInputStreamToByteArray(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }

    public static String detectMimeType(byte[] fileData) {
        return tika.detect(fileData);
    }

//    public static ByteArrayResource createByteArrayResource(byte[] fileData, String fileName, String contentType) {
//        return new ByteArrayResource(fileData) {
//            @NotNull
//            @Override
//            public String getDescription() {
//                return fileName + " (" + contentType + ")";
//            }
//        };
//    }
//
//    public static ResponseEntity<Resource> createResponseEntityForResource(byte[] fileData, String fileName, String contentType) {
//        ByteArrayResource resource = createByteArrayResource(fileData, fileName, contentType);
//        HttpHeaders headers = new HttpHeaders();
//        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + getOriginalFileName(fileName));
//        headers.add(HttpHeaders.CONTENT_TYPE, contentType);
//        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
//    }
//
//    public static ResponseEntity<Resource> convertToResponseEntityForResource(InputStream inputStream, String fileName) throws IOException {
//        byte[] fileData = readInputStreamToByteArray(inputStream);
//        String contentType = detectMimeType(fileData);
//        return createResponseEntityForResource(fileData, fileName, contentType);
//    }

    public static String getBaseBucketName(String bucketName) {
        String[] parts = bucketName.split("/");
        return parts[0];
    }

    public static String appendUUIDToFilename(String bucketName, String objectName) {
        // Generate a unique identifier
        String uuid = UUID.randomUUID().toString();

        // Extract the part of the bucketName after the first slash if it exists
        String bucketPrefix = bucketName.contains("/")
                ? bucketName.substring(bucketName.indexOf('/') + 1)
                : "";

        // Process objectName to handle extension
        int dotIndex = objectName.lastIndexOf('.');
        String baseName = dotIndex == -1 ? objectName : objectName.substring(0, dotIndex);
        String extension = dotIndex == -1 ? "" : objectName.substring(dotIndex);

        // Construct new file name with UUID and optional bucketPrefix
        String newFileName;
        if (bucketPrefix.isEmpty()) {
            newFileName = baseName + '-' + uuid + extension;
        } else {
            newFileName = bucketPrefix + "/" + baseName + '-' + uuid + extension;
        }

        // Normalize the file path to remove double slashes and ensure single slashes
        newFileName = newFileName.replaceAll("/+", "/");

        // Ensure the file name does not start with a slash
        if (newFileName.startsWith("/")) {
            newFileName = newFileName.substring(1);
        }

        return newFileName;
    }

    public static String appendUUIDToFilename(String objectName) {
        String uuid = UUID.randomUUID().toString();
        int dotIndex = objectName.lastIndexOf('.');
        if (dotIndex == -1) {
            return objectName + '-' + uuid;
        }
        String baseName = objectName.substring(0, dotIndex);
        String extension = objectName.substring(dotIndex);
        return baseName + '-' + uuid + extension;
    }

    public static BucketObject extractBucketAndObjectName(String filePath) {
        int firstSlashIndex = filePath.indexOf('/');

        // Check if "/" exists in the filePath
        if (firstSlashIndex != -1) {
            // Extract bucketName and objectName
            String bucketName = filePath.substring(0, firstSlashIndex);
            String objectName = filePath.substring(firstSlashIndex + 1);

            return new BucketObject(bucketName, objectName);
        } else {
            // Handle case where "/" is not found (return null or handle differently)
            return null;
        }
    }

    public static String getOriginalFileName(String objectName) {
        int lastSlashIndex = objectName.lastIndexOf('/');

        // Check if "/" exists in the filePath
        if (lastSlashIndex != -1) {
            // Extract and return the file name
            return objectName.substring(lastSlashIndex + 1);
        } else {
            // Handle case where "/" is not found (return the whole string or handle differently)
            return objectName;
        }
    }

    public static void validateNotEmpty(String... params) {
        StringBuilder errorMessage = new StringBuilder("The following parameters are invalid: ");
        boolean hasInvalidParams = false;

        for (int i = 0; i < params.length; i++) {
            if (params[i] == null || params[i].isEmpty()) {
                if (hasInvalidParams) {
                    errorMessage.append(", ");
                }
                errorMessage.append("Parameter ").append(i + 1);
                hasInvalidParams = true;
            }
        }

        if (hasInvalidParams) {
            throw new IllegalArgumentException(errorMessage.toString());
        }
    }

}