package com.backstep.aws.services;

import java.io.IOException;
import java.util.Properties;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sqs.AmazonSQSClient;

public class AWSConnection {

  private String accessKey;
  private String secretKey;

  public AWSConnection(Properties properties) {
    this.accessKey = properties.get("AWS_ACCESS_KEY").toString();
    this.secretKey = properties.get("AWS_SECRET_KEY").toString();
  }

  public TransferManager getS3TransferManager() throws AmazonClientException {
    TransferManager tm = null;

    try {
      AWSCredentials credentials = getAwsCredentials();
      tm = new TransferManager(credentials);
    } catch (IOException e) {
      throw new AmazonClientException(e.getMessage());
    }

    return tm;
  }

  public AmazonS3Client getS3Client() throws AmazonClientException {
    AmazonS3Client s3 = null;

    try {
      AWSCredentials credentials = getAwsCredentials();
      s3 = new AmazonS3Client(credentials);
    } catch (IOException e) {
      throw new AmazonClientException(e.getMessage());
    }

    return s3;
  }

  public ArchiveTransferManager getGlacierTransferManager(AmazonGlacierClient glacierClient)
      throws AmazonClientException {

    ArchiveTransferManager atm = null;
    try {
      AWSCredentials credentials = getAwsCredentials();

      atm = new ArchiveTransferManager(glacierClient, credentials);
    } catch (IOException e) {
      throw new AmazonClientException(e.getMessage());
    } catch (IllegalArgumentException e) {
      throw new AmazonClientException(e.getMessage());
    }

    return atm;
  }

  public AmazonGlacierClient getGlacierClient(String region) throws AmazonClientException {

    AmazonGlacierClient glacierClient = null;
    try {
      AWSCredentials credentials = getAwsCredentials();
      glacierClient = new AmazonGlacierClient(credentials);
      glacierClient.setEndpoint("https://glacier." + region + ".amazonaws.com");
    } catch (IOException e) {
      throw new AmazonClientException(e.getMessage());
    } catch (IllegalArgumentException e) {
      throw new AmazonClientException(e.getMessage());
    }

    return glacierClient;
  }

  public AmazonSQSClient getSQSClient(String region) throws AmazonClientException {

    AmazonSQSClient sqsClient = null;
    try {
      AWSCredentials credentials = getAwsCredentials();
      sqsClient = new AmazonSQSClient(credentials);
      sqsClient.setEndpoint("https://sqs." + region + ".amazonaws.com");
    } catch (IOException e) {
      throw new AmazonClientException(e.getMessage());
    } catch (IllegalArgumentException e) {
      throw new AmazonClientException(e.getMessage());
    }

    return sqsClient;
  }

  public AmazonSNSClient getSNSClient(String region) throws AmazonClientException {
    AmazonSNSClient snsClient = null;
    try {
      AWSCredentials credentials = getAwsCredentials();
      snsClient = new AmazonSNSClient(credentials);
      snsClient.setEndpoint("https://sns." + region + ".amazonaws.com");
    } catch (IOException e) {
      throw new AmazonClientException(e.getMessage());
    } catch (IllegalArgumentException e) {
      throw new AmazonClientException(e.getMessage());
    }

    return snsClient;
  }

  private AWSCredentials getAwsCredentials() throws IOException {
    AWSCredentials credentials;
    credentials = new BasicAWSCredentials(accessKey, secretKey);

    return credentials;
  }
}
