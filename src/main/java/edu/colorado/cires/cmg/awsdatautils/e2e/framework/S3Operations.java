package edu.colorado.cires.cmg.awsdatautils.e2e.framework;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public interface S3Operations {
    void copyObject(String sourceBucket, String sourceKey, String targetBucket, String targetKey);

    List<String> deleteObjects(String bucket, List<String> keys);

    boolean doesObjectExist(String bucket, String key);

    List<String> listObjects(String bucket, String prefix);

    List<String> listObjects(String bucket);

    void uploadDirectoryToBucket(Path dir, String bucket);

    void upload(Path source, String targetBucket, String targetKey);

    void download(String sourceBucket, String sourceKey, Path target);

    default void moveObject(String sourceBucket, String sourceKey, String targetBucket, String targetKey) {
        this.copyObject(sourceBucket, sourceKey, targetBucket, targetKey);
        this.deleteObjects(sourceBucket, Collections.singletonList(sourceKey));
    }
}
