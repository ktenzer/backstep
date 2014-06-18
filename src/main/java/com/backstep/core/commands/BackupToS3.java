package com.backstep.core.commands;

import java.util.Properties;

import com.backstep.aws.services.S3;
import com.backstep.core.util.FileManager;

public class BackupToS3 {
  private final static FileManager fileManager = new FileManager();

  public static void main(String[] args) throws Exception {
    Properties properties = fileManager.getProperties();
    S3 s3 = new S3(properties);

    s3.backup();
  }
}
