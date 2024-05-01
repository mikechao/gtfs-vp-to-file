package dev.mike.chao.gtfsvptofile;

import java.io.File;
import java.nio.file.Path;

public interface FileHelper {

  public Path getPath();

  public File getFile();

  public boolean isFileExist();
}
