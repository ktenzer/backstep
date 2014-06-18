package com.backstep.core.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class FileManager {

  public Properties getProperties() throws IOException {
    String filePath;

    Properties properties = new Properties();
    try {
      filePath = this.getProviderPath() + "/src/main/resources/config.properties";
      properties.load(new FileInputStream(filePath));
    } catch (IOException e) {
      throw new IOException(e.getMessage());
    }

    return properties;
  }

  private String getProviderPath() throws IOException {
    String path = null;

    try {
      path = new java.io.File(".").getCanonicalPath();
    } catch (IOException e) {
      throw new IOException(e);
    }

    return path;
  }
}
