package com.devsda.tools.azureswissknife.services;

import com.devsda.tools.azureswissknife.exceptions.InvalidCredentialsException;
import com.devsda.tools.azureswissknife.util.ImageUtil;
import com.microsoft.azure.storage.blob.*;
import com.microsoft.azure.storage.blob.models.BlockBlobUploadResponse;
import com.microsoft.azure.storage.blob.models.ContainerCreateResponse;
import com.microsoft.rest.v2.http.HttpPipeline;
import io.reactivex.Flowable;
import io.reactivex.Single;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.time.OffsetDateTime;
import java.util.Locale;

public class BlobService {

    private ServiceURL getServiceInstance(String accountName, String accountKey) throws InvalidKeyException, MalformedURLException {


        // Use your Storage account's name and key to create a credential object; this is used to access your account.
        SharedKeyCredentials credential = new SharedKeyCredentials(accountName, accountKey);

        /*
        Create a request pipeline that is used to process HTTP(S) requests and responses. It requires your accont
        credentials. In more advanced scenarios, you can configure telemetry, retry policies, logging, and other
        options. Also you can configure multiple pipelines for different scenarios.
         */
        HttpPipeline pipeline = StorageURL.createPipeline(credential, new PipelineOptions());

        /*
        From the Azure portal, get your Storage account blob service URL endpoint.
        The URL typically looks like this:
         */
        URL u = new URL(String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName));

        // Create a ServiceURL objet that wraps the service URL and a request pipeline.
        ServiceURL serviceURL = new ServiceURL(u, pipeline);

        return serviceURL;

    }

    public void uploadBlob(String accountName, String accountKey, String containerName, String blobName, String imageLocation) throws IOException {

        ServiceURL serviceURL = null;

        try {
            serviceURL = getServiceInstance(accountName, accountKey);
        } catch(InvalidKeyException | MalformedURLException e) {
            throw new InvalidCredentialsException("Invalid Credentails of Azure Storage Account", e);
        }

        ContainerURL containerURL = serviceURL.createContainerURL(containerName);

        BlockBlobURL blobURL = containerURL.createBlockBlobURL(blobName);

        ByteBuffer byteBuffer = ImageUtil.getByteBufferOfImage(imageLocation);
        Single<BlockBlobUploadResponse> blockBlobUploadResponseSingle = blobURL.upload(Flowable.just(byteBuffer), byteBuffer.capacity(),
                null, null, null, null);

        BlockBlobUploadResponse blockBlobUploadResponse = blockBlobUploadResponseSingle.blockingGet();

    }

    public void downloadBlob(String accountName, String accountKey, String containerName, String blobName) {

        ServiceURL serviceURL = null;

        try {
            serviceURL = getServiceInstance(accountName, accountKey);
        } catch(InvalidKeyException | MalformedURLException e) {
            throw new InvalidCredentialsException("Invalid Credentails of Azure Storage Account", e);
        }

        ContainerURL containerURL = serviceURL.createContainerURL(containerName);

        /*
        Create a URL that references a to-be-created blob in your Azure Storage account's container.
        This returns a BlockBlobURL object that wraps the blob's URl and a request pipeline
        (inherited from containerURL). Note that blob names can be mixed case.
         */
        BlockBlobURL blobURL = containerURL.createBlockBlobURL(blobName);

        Single<DownloadResponse> downloadResponseSingle = blobURL.download(null, null, false, null);

        DownloadResponse downloadResponse = downloadResponseSingle.blockingGet();


//        // Create the container on the service (with no metadata and no public access)
//        containerURL.create(null, null, null)
//                .flatMap(containersCreateResponse ->
//                        /*
//                         Create the blob with string (plain text) content.
//                         NOTE: It is imperative that the provided length matches the actual length exactly.
//                         */
//                        blobURL.upload(Flowable.just(ByteBuffer.wrap(data.getBytes())), data.length(),
//                                null, null, null, null))
//                .flatMap(blobsDownloadResponse ->
//                        // Download the blob's content.
//                        blobURL.download(null, null, false, null))
//                .flatMap(blobsDownloadResponse ->
//                        // Verify that the blob data round-tripped correctly.
//                        FlowableUtil.collectBytesInBuffer(blobsDownloadResponse.body(null))
//                                .doOnSuccess(byteBuffer -> {
//                                    if (byteBuffer.compareTo(ByteBuffer.wrap(data.getBytes())) != 0) {
//                                        throw new Exception("The downloaded data does not match the uploaded data.");
//                                    }
//                                }))
//                .flatMap(byteBuffer ->
//                        // Delete the blob we created earlier.
//                        blobURL.delete(null, null, null))
//                .flatMap(blobsDeleteResponse ->
//                        // Delete the container we created earlier.
//                        containerURL.delete(null, null))
                /*
                This will synchronize all the above operations. This is strongly discouraged for use in production as
                it eliminates the benefits of asynchronous IO. We use it here to enable the sample to complete and
                demonstrate its effectiveness.
                 */
//                .blockingGet();
    }

    public void createContainer(String accountName, String accountKey, String containerName) {

        ServiceURL serviceURL = null;

        try {
            serviceURL = getServiceInstance(accountName, accountKey);
        } catch(InvalidKeyException | MalformedURLException e) {
            throw new InvalidCredentialsException("Invalid Credentails of Azure Storage Account", e);
        }

        /*
        Create a URL that references a to-be-created container in your Azure Storage account. This returns a
        ContainerURL object that wraps the container's URL and a request pipeline (inherited from serviceURL).
        Note that container names require lowercase.
         */
        ContainerURL containerURL = serviceURL.createContainerURL(containerName);

        Single<ContainerCreateResponse> containerCreateResponseSingle = containerURL.create(null, null, null);

        ContainerCreateResponse containerCreateResponse = containerCreateResponseSingle.blockingGet();

    }

    public String generatePublicURL(String accountName, String accountKey, String containerName, String blobName) throws InvalidKeyException  {

        ServiceURL serviceURL = null;

        try {
            serviceURL = getServiceInstance(accountName, accountKey);
        } catch(InvalidKeyException | MalformedURLException e) {
            throw new InvalidCredentialsException("Invalid Credentails of Azure Storage Account", e);
        }

        ContainerURL containerURL = serviceURL.createContainerURL(containerName);

        /*
        Create a URL that references a to-be-created blob in your Azure Storage account's container.
        This returns a BlockBlobURL object that wraps the blob's URl and a request pipeline
        (inherited from containerURL). Note that blob names can be mixed case.
         */
        BlockBlobURL blobURL = containerURL.createBlockBlobURL(blobName);
        /*
        Set the desired SAS signature values and sign them with the shared key credentials to get the SAS query
        parameters.
         */

        // Use your Storage account's name and key to create a credential object; this is required to sign a SAS.
        SharedKeyCredentials credential = new SharedKeyCredentials(accountName, accountKey);

        AccountSASSignatureValues values = new AccountSASSignatureValues();
        values.withProtocol(SASProtocol.HTTPS_ONLY) // Users MUST use HTTPS (not HTTP).
                .withExpiryTime(OffsetDateTime.now().plusDays(2)); // 2 days before expiration.

        AccountSASPermission permission = new AccountSASPermission()
                .withRead(true)
                .withList(true);
        values.withPermissions(permission.toString());

        AccountSASService service = new AccountSASService()
                .withBlob(true);
        values.withServices(service.toString());

        AccountSASResourceType resourceType = new AccountSASResourceType()
                .withContainer(true)
                .withObject(true);
        values.withResourceTypes(resourceType.toString());

        SASQueryParameters params = values.generateSASQueryParameters(credential);

        // Calling encode will generate the query string.
        String encodedParams = params.encode();

        String urlToSendToSomeone = String.format(Locale.ROOT, "%s%s",
                blobURL.toString(), encodedParams);

        return urlToSendToSomeone;

    }
}
