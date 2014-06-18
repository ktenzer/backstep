package com.backstep.core.commands;

import java.util.Properties;

import com.backstep.aws.services.Glacier;
import com.backstep.core.util.FileManager;

public class ArchiveToGlacier {
  private final static FileManager fileManager = new FileManager();

  public static void main(String[] args) throws Exception {
    Properties properties = fileManager.getProperties();
    Glacier glacier = new Glacier(properties);

    glacier.archive();
  }
}
