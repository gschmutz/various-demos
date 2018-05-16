
package com.hortonworks.labutils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyParser {

  private static final Logger LOG = LoggerFactory.getLogger(PropertyParser.class);

  public Properties props = new Properties();
  private String propFileName;

  public PropertyParser(String propFileName) {
    this.propFileName = propFileName;
  }

  public String getPropFileName() {
    return propFileName;
  }

  public void setPropFileName(String propFileName) {
    this.propFileName = propFileName;
  }

  public String getProperty(String key) {
    return props.get(key).toString();
  }

  public void parsePropsFile() throws IOException {

    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

    try {
      if (null != inputStream) {
        props.load(inputStream);
      } else {
        throw new IOException("Could not load property file from the resources directory, trying local");
      }
    } catch(IOException e) {
      LOG.error(e.getMessage());
      e.printStackTrace();
      try {
        inputStream = new FileInputStream(new File(propFileName).getAbsolutePath());
        props.load(inputStream);
      } catch (IOException ex) {
        LOG.error("Could not load property file at " + new File(propFileName).getAbsolutePath());
        ex.printStackTrace();
        throw ex;
      }
    }
  }
}
