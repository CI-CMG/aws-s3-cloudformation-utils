package edu.colorado.cires.cmg.s3cfutils.framework;

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

/**
 * Integration test utilities
 */
public final class ITUtils {

  private static final ObjectMapper objectMapper = ObjectMapperCreator.create();

  /**
   * Lists files contained in a given path
   * @param path the path to list files from
   * @return list of {@link Path} contained in the specified path
   */
  public static List<Path> listFiles(Path path) {
    try (Stream<Path> walk = Files.walk(path)) {
      return walk
          .filter(Files::isRegularFile)
          .collect(Collectors.toList());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Parses a json file to an object
   * @param path the json file path
   * @return {@link JsonNode} containing json file contents
   */
  public static JsonNode readJsonFile(Path path) {
    try (InputStream in = Files.newInputStream(path)) {
      return objectMapper.readTree(in);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Gets stack id/prefix from test id file
   * @param target maven target directory path
   * @return the unique stack id/prefix
   */
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
