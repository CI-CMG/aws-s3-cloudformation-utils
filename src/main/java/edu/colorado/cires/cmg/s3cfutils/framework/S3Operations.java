package edu.colorado.cires.cmg.s3cfutils.framework;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * Operations for interaction with S3 objects
 */
public interface S3Operations {

    /**
     * Copies object from a source bucket to a target bucket
     * @param sourceBucket the source bucket name
     * @param sourceKey location of object within the source bucket
     * @param targetBucket the target bucket name
     * @param targetKey location of object within the target bucket
     */
    void copyObject(String sourceBucket, String sourceKey, String targetBucket, String targetKey);

    /**
     * Deletes objects from a bucket
     * @param bucket the bucket name
     * @param keys locations of objects to delete from the bucket
     * @return locations of objects deleted from the bucket
     */
    List<String> deleteObjects(String bucket, List<String> keys);

    /**
     * Returns true if a given object exists within a bucket
     * @param bucket the bucket name
     * @param key location of object within the bucket
     * @return true if object exists within the bucket
     */
    boolean doesObjectExist(String bucket, String key);

    /**
     * Lists objects matching a prefix within a bucket
     * @param bucket the bucket name
     * @param prefix the object prefix
     * @return objects matching the prefix within the bucket
     */
    List<String> listObjects(String bucket, String prefix);

    /**
     * Lists objects within a bucket
     * @param bucket the bucket name
     * @return objects within the bucket
     */
    List<String> listObjects(String bucket);

    /**
     * Uploads a directory to a bucket
     * @param dir the directory to upload to the bucket
     * @param bucket the bucket name
     */
    void uploadDirectoryToBucket(Path dir, String bucket);

    /**
     * Uploads an object to a bucket
     * @param source {@link Path} to source object
     * @param targetBucket the target bucket name
     * @param targetKey the location of the source object within the bucket
     */
    void upload(Path source, String targetBucket, String targetKey);

    /**
     * Downloads an object from a bucket
     * @param sourceBucket the source bucket name
     * @param sourceKey the location of the object within the source bucket
     * @param target {@link Path} to output object
     */
    void download(String sourceBucket, String sourceKey, Path target);

    /**
     * Moves an object between buckets
     * @param sourceBucket the source bucket name
     * @param sourceKey the location of the object within the source bucket
     * @param targetBucket the target bucket name
     * @param targetKey the location of the object within the target bucket
     */
    default void moveObject(String sourceBucket, String sourceKey, String targetBucket, String targetKey) {
        this.copyObject(sourceBucket, sourceKey, targetBucket, targetKey);
        this.deleteObjects(sourceBucket, Collections.singletonList(sourceKey));
    }
}
