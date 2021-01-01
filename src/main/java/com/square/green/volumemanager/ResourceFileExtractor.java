package com.square.green.volumemanager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ResourceFileExtractor {

  private ResourceFileExtractor(){

  }

  public static URI getJarUri() throws URISyntaxException {
    final ProtectionDomain domain;
    final CodeSource source;
    final URL url;
    final URI uri;

    domain = ResourceFileExtractor.class.getProtectionDomain();
    source = domain.getCodeSource();
    url = source.getLocation();
    uri = url.toURI();

    return (uri);
  }

  public static URI getFile(final String fileName) throws IOException, URISyntaxException {
    return getFile(getJarUri(), fileName);
  }

  public static URI getFile(final URI where, final String fileName) throws IOException {

    final URI fileUri;
    final File location = new File(where);
    if (location.isDirectory()) {
      fileUri = URI.create(where.toString() + fileName);
    } else {
      try (final ZipFile zipFile = new ZipFile(location)) {
        fileUri = extract(zipFile, fileName);
      }
    }

    return fileUri;
  }

  private static URI extract(final ZipFile zipFile, final String fileName) throws IOException {
    final File tempFile;
    final ZipEntry entry;

    tempFile = File.createTempFile(fileName, Long.toString(System.currentTimeMillis()));
    tempFile.deleteOnExit();
    entry = zipFile.getEntry(fileName);

    if (entry == null) {
      throw new FileNotFoundException(
          "cannot find file: " + fileName + " in archive: " + zipFile.getName());
    }

    try (InputStream zipStream = zipFile.getInputStream(entry)) {
      final byte[] buf;
      int i;

      try (OutputStream fileStream = new FileOutputStream(tempFile)) {
        buf = new byte[1024];
        while ((i = zipStream.read(buf)) != -1) {
          fileStream.write(buf, 0, i);
        }
      }
    }
    return (tempFile.toURI());
  }
}
