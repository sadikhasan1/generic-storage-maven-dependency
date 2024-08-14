package com.dsi.storage.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PathUtilTest {

    @Test
    void testSplitPathForUpload_NormalizationAndValidation() {
        String fullPath = "  my-bucket///folder1/folder2  /  ";
        String[] expected = {"my-bucket", "folder1", "folder2"};

        String[] result = PathUtil.splitPathForUpload(fullPath);

        assertArrayEquals(expected, result);
    }

    @Test
    void testSplitPathForUpload_InvalidPath() {
        String fullPath = "my-bucket/folder1/folder2--invalid";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            PathUtil.splitPathForUpload(fullPath);
        });

        assertEquals("Invalid directory path: my-bucket/folder1/folder2--invalid", exception.getMessage());
    }

    @Test
    void testSplitPathForUpload_EmptyPath() {
        String fullPath = "  ";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            PathUtil.splitPathForUpload(fullPath);
        });

        assertEquals("Invalid directory path: ", exception.getMessage());
    }

    @Test
    void testSplitPathForDownload_Normalization() {
        String fullPathWithFileId = "  /my-bucket///folder1/folder2/fileId  ";
        String[] expected = {"my-bucket", "folder1", "folder2", "fileId"};

        String[] result = PathUtil.splitPathForDownload(fullPathWithFileId);
        
        assertArrayEquals(expected, result);
    }

    @Test
    void testSplitPathForDownload_SingleSegment() {
        String fullPathWithFileId = "my-bucket";
        String[] expected = {"my-bucket"};

        String[] result = PathUtil.splitPathForDownload(fullPathWithFileId);
        
        assertArrayEquals(expected, result);
    }
}
