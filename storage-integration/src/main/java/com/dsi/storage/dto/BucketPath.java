package com.dsi.storage.dto;

/**
 * Represents a path structure used in storage operations.
 *
 * This record is used to encapsulate:
 * - `baseBucket`: The name of the base bucket in the storage system.
 * - `remainingPath`: The path remaining after the base bucket, which could include directory buckets and/or filenames.
 *
 * Example:
 * For a path "my-bucket/folder1/folder2/file.txt":
 * - `baseBucket` would be "my-bucket"
 * - `remainingPath` would be "folder1/folder2/file.txt"
 *
 * This structure helps in organizing and processing storage paths by clearly separating the bucket name from the rest of the path.
 */
public record BucketPath(String baseBucket, String remainingPath) {
}
