package dev.mike.chao.gtfsvptofile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class TestFileHelperImpl {

  @Test
  public void testWithValidPath(@TempDir Path tempDir) {
    Path path = tempDir.resolve("test.txt");
    String stringPath = path.toString();
    FileHelperImpl helperImpl = new FileHelperImpl(stringPath);
    helperImpl.init();

    assertTrue(helperImpl.isFileExist(), () -> "The file should exist");
    Path actualPath = helperImpl.getPath();
    assertNotNull(actualPath, "getPath() should NOT return null for a valid path");
    assertEquals(path.toString(), actualPath.toString());

    File actualFile = helperImpl.getFile();
    assertNotNull(actualFile, "getFile() should not return null for a valid path");
  }

  @Test
  public void testWithInvalidPath(@TempDir Path tempDir) {
    String path = tempDir.resolve("").toString();
    FileHelperImpl helperImpl = new FileHelperImpl(path);

    try (MockedStatic<Path> pathMocked = Mockito.mockStatic(Path.class)) {
      pathMocked.when(() -> Path.of(path)).thenThrow(new RuntimeException("Test exception"));
      helperImpl.init();
    }

    assertFalse(helperImpl.isFileExist(), "The file should not exist for an invalid path");
    assertNull(helperImpl.getPath(), "getPath() should return null when Path.of throws exception");
    assertNull(helperImpl.getFile(), "getFile() should return null when Path.of throws exception");
  }

  @Test
  public void testWithExistingFile(@TempDir Path tempPath) throws IOException {
    Path path = tempPath.resolve("testExist.txt");
    Files.createFile(path);

    FileHelperImpl helperImpl = new FileHelperImpl(path.toString());
    helperImpl.init();

    assertTrue(helperImpl.isFileExist(), "The file should exist when it was already created before the class");
    assertEquals(path.toString(), helperImpl.getPath().toString());
  }

  @Test
  public void testWithTemp(@TempDir Path tempPath) {
    Path actualPath = tempPath.resolve("vps.txt");

    FileHelperImpl helperImpl = new FileHelperImpl(GtfsVpToFileConfig.TEMP);
    try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.createTempFile("vps", ".txt")).thenReturn(actualPath);
      helperImpl.init();
    }
    assertTrue(helperImpl.isFileExist(), "The file when we use the temp option");
    assertEquals(actualPath.toString(), helperImpl.getPath().toString());
  }
}
