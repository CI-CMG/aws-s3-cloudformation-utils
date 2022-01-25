package edu.colorado.cires.cmg.awsdatautils.e2e.framework;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ITUtils {

  private static final ObjectMapper objectMapper = ObjectMapperCreator.create();

  public static List<Path> listFiles(Path path) {
    try (Stream<Path> walk = Files.walk(path)) {
      return walk
          .filter(Files::isRegularFile)
          .collect(Collectors.toList());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static JsonNode readJsonFile(Path path) {
    try (InputStream in = Files.newInputStream(path)) {
      return objectMapper.readTree(in);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String readId(Path target) {
    try {
      Path file = target.resolve("test-id.txt");
      return FileUtils.readFileToString(file.toFile(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException("Unable to read id file", e);
    }
  }

  private ITUtils() {

  }
}
