package com.devvsda.tools.azureswissknife.services;

import com.devsda.tools.azureswissknife.services.BlobService;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class BlobServiceTest {


    private static BlobService blobService;

    @BeforeClass
    public static void setup() {
        blobService = new BlobService();
    }


    @Test
    public void createContainerTest() {
        String accountName = "cricket2";
        String accountKey = "KEY";

        String containerName = "CONTAINER_NAME";

        blobService.createContainer(accountName, accountKey, containerName);
    }

    @Test
    public void uploadBlobTest() throws IOException {
        String accountName = "cricket2";
        String accountKey = "KEY";

        String containerName = "CONTAINER_NAME";
        String blockName = "BLOB_NAME";

        String imageLocation = "IMAGE_LOCATION";

        blobService.uploadBlob(accountName, accountKey, containerName, blockName, imageLocation);
    }
}
