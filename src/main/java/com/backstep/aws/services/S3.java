package com.backstep.aws.services;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.ObjectMetadataProvider;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;

public class S3 {
  private String mountPath;
  private String bucketName;
  Properties properties;

  public S3(Properties properties) {
    this.mountPath = properties.get("AWS_MOUNT_PATH").toString();
    this.bucketName = properties.get("AWS_BUCKET_NAME").toString();
    this.properties = properties;
  }

  public void backup() throws AmazonClientException {
    AWSConnection aws = new AWSConnection(properties);
    TransferManager tm = aws.getS3TransferManager();

    try {
      createBucket(tm, bucketName);
    } catch (Exception e) {
      throw new AmazonClientException(e.getMessage());
    }

    ObjectMetadata metadata = new ObjectMetadata();
    metadata.addUserMetadata("TIMESTAMP", String.valueOf(System.currentTimeMillis()));
    metadata.addUserMetadata("MOUNT_PATH", mountPath.toString());

    File uploadDir = new File(mountPath);
    String[] fileList = uploadDir.list();

    for (String filePath : fileList) {
      final File file = new File(mountPath, filePath);

      String backupPath = file.getAbsolutePath();
      if (file.isFile()) {
        System.out.println("Uploading file " + backupPath + " to S3");
        try {
          Upload uploadFile =
              tm.upload(bucketName, backupPath, new FileInputStream(file), metadata);

          while (uploadFile.isDone() == false) {
            Double progress = uploadFile.getProgress().getPercentTransferred();
            String percent = progress.intValue() + "%";

            System.out.println("Upload percent for file " + file.getAbsolutePath() + " to bucket "
                + bucketName + " is " + percent);

            Thread.sleep(1000);
          }

          System.out.println("Upload of file " + file.getAbsolutePath() + " to S3 bucket "
              + bucketName + " completed successfully");
        } catch (Exception e) {
          throw new AmazonClientException(e.getMessage());
        }

      } else if (file.isDirectory()) {
        System.out.println("Uploading directory " + file.getAbsolutePath() + " to S3 bucket "
            + bucketName);

        try {
          ObjectMetadataProvider metadataProvider = new ObjectMetadataProvider() {
            public void provideObjectMetadata(File file, ObjectMetadata metadata) {
              metadata.addUserMetadata("TIMESTAMP", String.valueOf(System.currentTimeMillis()));
              metadata.addUserMetadata("MOUNT_PATH", mountPath.toString());
            }
          };

          MultipleFileUpload uploadDirectory =
              tm.uploadDirectory(bucketName, backupPath, file, true, metadataProvider);

          while (uploadDirectory.isDone() == false) {
            Double progress = uploadDirectory.getProgress().getPercentTransferred();
            String percent = progress.intValue() + "%";

            System.out.println("Upload percent for directory " + file.getAbsolutePath()
                + " to bucket " + bucketName + " is " + percent);

            Thread.sleep(1000);
          }

          System.out.println("Upload of file " + file.getAbsolutePath() + " to S3 bucket "
              + bucketName + " completed successfully");
        } catch (Exception e) {
          throw new AmazonClientException(e.getMessage());
        }
      }
    }
  }

  private static void createBucket(TransferManager tm, String bucketName)
      throws AmazonClientException {

    if (tm.getAmazonS3Client().doesBucketExist(bucketName) == false) {
      try {
        tm.getAmazonS3Client().createBucket(bucketName);
      } catch (AmazonClientException e) {
        throw new AmazonClientException(e.getMessage());
      }
    }
  }
}
