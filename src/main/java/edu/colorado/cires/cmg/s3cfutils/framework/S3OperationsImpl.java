package edu.colorado.cires.cmg.s3cfutils.framework;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;
import com.amazonaws.services.s3.model.DeleteObjectsResult.DeletedObject;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import edu.colorado.cires.cmg.s3out.DefaultContentTypeResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Operations for interaction with S3 objects
 */
public class S3OperationsImpl implements S3Operations {
    private final AmazonS3 s3;

    public S3OperationsImpl(AmazonS3 s3) {
        this.s3 = s3;
    }

    /**
     * Copies object from a source bucket to a target bucket
     * @param sourceBucket the source bucket name
     * @param sourceKey location of object within the source bucket
     * @param targetBucket the target bucket name
     * @param targetKey location of object within the target bucket
     */
    public void copyObject(String sourceBucket, String sourceKey, String targetBucket, String targetKey) {
        this.s3.copyObject(sourceBucket, sourceKey, targetBucket, targetKey);
    }

    /**
     * Deletes objects from a bucket
     * @param bucket the bucket name
     * @param keys locations of objects to delete from the bucket
     * @return locations of objects deleted from the bucket
     */
    public List<String> deleteObjects(String bucket, List<String> keys) {
        List<KeyVersion> keysToDelete = new ArrayList();
        Iterator var4 = keys.iterator();

        while(var4.hasNext()) {
            String key = (String)var4.next();
            keysToDelete.add(new KeyVersion(key));
        }

        List<String> deletedObjects = new ArrayList();
        if (!keysToDelete.isEmpty()) {
            DeleteObjectsResult deletedObjectsResult = this.s3.deleteObjects((new DeleteObjectsRequest(bucket)).withKeys(keysToDelete));
            Iterator var6 = deletedObjectsResult.getDeletedObjects().iterator();

            while(var6.hasNext()) {
                DeletedObject deletedObject = (DeletedObject)var6.next();
                deletedObjects.add(deletedObject.getKey());
            }
        }

        return deletedObjects;
    }

    /**
     * Returns true if a given object exists within a bucket
     * @param bucket the bucket name
     * @param key location of object within the bucket
     * @return true if object exists within the bucket
     */
    public boolean doesObjectExist(String bucket, String key) {
        return this.s3.doesObjectExist(bucket, key);
    }

    /**
     * Lists objects matching a prefix within a bucket
     * @param bucket the bucket name
     * @param prefix the object prefix
     * @return objects matching the prefix within the bucket
     */
    public List<String> listObjects(String bucket, String prefix) {
        List<String> keys = new LinkedList();
        ObjectListing objectListing = this.s3.listObjects(bucket, prefix);

        while(true) {
            Iterator objIter = objectListing.getObjectSummaries().iterator();

            while(objIter.hasNext()) {
                keys.add(((S3ObjectSummary)objIter.next()).getKey());
            }

            if (!objectListing.isTruncated()) {
                return keys;
            }

            objectListing = this.s3.listNextBatchOfObjects(objectListing);
        }
    }

    /**
     * Lists objects within a bucket
     * @param bucket the bucket name
     * @return objects within the bucket
     */
    public List<String> listObjects(String bucket) {
        return this.listObjects(bucket, (String)null);
    }

    /**
     * Uploads on object to a bucket
     * @param source {@link Path} to source object
     * @param targetBucket the target bucket name
     * @param targetKey the location of the source object within the bucket
     */
    public void upload(Path source, String targetBucket, String targetKey) {
        TransferManager transferManager = TransferManagerBuilder.standard().withS3Client(this.s3).build();

        try {
            Upload upload = transferManager.upload(targetBucket, targetKey, source.toFile());
            upload.waitForCompletion();
        } catch (InterruptedException var9) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Unable to copy file to bucket", var9);
        } finally {
            transferManager.shutdownNow(false);
        }

    }

    /**
     * Creates a parent directory
     * @param target the new parent directory
     */
    private void createParent(Path target) {
        Path parent = target.getParent();
        if (parent != null && !Files.exists(parent, new LinkOption[0])) {
            try {
                Files.createDirectories(parent);
            } catch (IOException var4) {
                throw new RuntimeException("Unable to create directory: " + parent.toAbsolutePath().toString());
            }
        }

    }

    /**
     * Downloads on object from a bucket
     * @param sourceBucket the source bucket name
     * @param sourceKey the location of the object within the source bucket
     * @param target {@link Path} to output object
     */
    public void download(String sourceBucket, String sourceKey, Path target) {
        this.createParent(target);
        TransferManager transferManager = TransferManagerBuilder.standard().withS3Client(this.s3).build();

        try {
            Download download = transferManager.download(sourceBucket, sourceKey, target.toFile());
            download.waitForCompletion();
        } catch (InterruptedException var9) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Unable to download file", var9);
        } finally {
            transferManager.shutdownNow(false);
        }

    }

    /**
     * Uploads a directory to a bucket
     * @param dir the directory to upload to the bucket
     * @param bucket the bucket name
     */
    public void uploadDirectoryToBucket(Path dir, String bucket) {
        TransferManager transferManager = TransferManagerBuilder.standard().withS3Client(this.s3).build();

        try {
            MultipleFileUpload upload = transferManager.uploadDirectory(bucket, "", dir.toFile(), true, (file, objectMetadata) -> {
                Optional<String> maybeContentType = new DefaultContentTypeResolver().resolveContentType(file.getName());
                Objects.requireNonNull(objectMetadata);
                maybeContentType.ifPresent(objectMetadata::setContentType);
            });
            upload.waitForCompletion();
        } catch (InterruptedException var8) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Unable to sync files to bucket", var8);
        } finally {
            transferManager.shutdownNow(false);
        }

    }
}

