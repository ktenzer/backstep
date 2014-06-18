package com.backstep.aws.services;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.CreateVaultRequest;
import com.amazonaws.services.glacier.model.CreateVaultResult;
import com.amazonaws.services.glacier.model.DescribeVaultOutput;
import com.amazonaws.services.glacier.model.ListVaultsRequest;
import com.amazonaws.services.glacier.model.ListVaultsResult;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.glacier.transfer.UploadResult;

public class Glacier {
  private String region;
  private String vaultName;
  private String archiveFilePath;
  Properties properties;
  public String partSize = "1048576";

  public Glacier(Properties properties) {
    this.region = properties.get("AWS_GLACIER_REGION").toString();
    this.vaultName = properties.get("AWS_GLACIER_VAULT_NAME").toString();
    this.archiveFilePath = properties.get("AWS_GLACIER_ARCHIVE_FILE_PATH").toString();
    this.properties = properties;
  }

  public void archive() throws AmazonClientException {
    File archiveName = new File(archiveFilePath);
    String archiveDescription = "Glacier backup using backupId " + new Date();
    UploadResult result = null;

    AWSConnection aws = new AWSConnection(properties);
    AmazonGlacierClient glacierClient = aws.getGlacierClient(region);
    ArchiveTransferManager atm = aws.getGlacierTransferManager(glacierClient);

    if (!existsVault()) {
      this.createVault();
    }

    try {
      System.out.println("Uploading archive file " + archiveName.getAbsolutePath()
          + " to Glacier vault " + vaultName);
      result = atm.upload(vaultName, archiveDescription, archiveName);
    } catch (Exception e) {
      throw new AmazonClientException(e.getMessage());
    }

    System.out.println("Archive ID is " + result.getArchiveId());
    System.out.println("Uploading archive file " + archiveName.getAbsolutePath()
        + " to Glacier vault " + vaultName + " completed successfully");
  }

  private void createVault() throws AmazonClientException {
    System.out.println("Creating Glacier vault " + vaultName);

    try {
      AWSConnection aws = new AWSConnection(properties);
      AmazonGlacierClient glacierClient = aws.getGlacierClient(region);

      CreateVaultRequest request = new CreateVaultRequest().withVaultName(vaultName);
      CreateVaultResult result = glacierClient.createVault(request);

      System.out.println("Glacier vault URI is " + result.getLocation());
    } catch (Exception e) {
      throw new AmazonClientException(e.getMessage());
    }

    System.out.println("Creating Glacier vault " + vaultName + " completed successfully");
  }

  private boolean existsVault() throws AmazonClientException {
    try {
      AWSConnection aws = new AWSConnection(properties);
      AmazonGlacierClient glacierClient = aws.getGlacierClient(region);

      ListVaultsRequest request = new ListVaultsRequest().withLimit("5").withMarker(null);
      ListVaultsResult listVaultsResult = glacierClient.listVaults(request);

      List<DescribeVaultOutput> vaultList = listVaultsResult.getVaultList();

      for (DescribeVaultOutput vault : vaultList) {
        if (vault.getVaultName().equals(vaultName)) {
          System.out.println("Glacier vault " + vaultName + " exists, using vault");

          System.out.println(String.format("\nCreationDate: " + vault.getCreationDate()
              + "\nLastInventoryDate: " + vault.getLastInventoryDate() + "\nNumberOfArchives: "
              + vault.getNumberOfArchives() + "\nSizeInBytes: " + vault.getSizeInBytes()
              + "\nVaultARN: " + vault.getVaultARN() + "\nVaultName: " + vault.getVaultName()));

          return true;
        }
      }
    } catch (Exception e) {
      throw new AmazonClientException(e.getMessage());
    }

    return false;
  }
}
