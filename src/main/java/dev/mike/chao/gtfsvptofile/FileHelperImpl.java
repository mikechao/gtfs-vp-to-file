package dev.mike.chao.gtfsvptofile;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class FileHelperImpl implements FileHelper {

  private final String filePath;
  private boolean fileExists = false;
  private Path path;
  private File file;

  @PostConstruct
  public void init() {
    path = createPath();
    if (path != null) {
      file = path.toFile();
      fileExists = true;
    } else {
      log.error("File at {} not created because Path was not created", filePath);
    }
  }

  @Override
  public Path getPath() {
    return path;
  }

  @Override
  public File getFile() {
    return file;
  }

  @Override
  public boolean isFileExist() {
    return fileExists;
  }

  private Path createPath() {
    Path p = null;
    try {
      if (filePath.equals(GtfsVpToFileConfig.TEMP)) {
        p = Files.createTempFile("vps", ".txt");
      } else {
        p = Path.of(filePath);
        Files.createFile(p);
      }
    } catch (FileAlreadyExistsException fe) {
      log.warn("File at {} already exists. New results will be appended", filePath);
      return p;
    } catch (Exception e) {
      log.error("Error creating path at {}", filePath, e);
    }
    return p;
  }
}
