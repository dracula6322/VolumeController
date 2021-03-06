package com.square.green.volumemanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgramExecutor {

  private static final Logger logger = LoggerFactory.getLogger(ProgramExecutor.class);
  private final ExecutorService inputThread;
  private final ExecutorService errorThread;

  private ProgramExecutor() {
    this.inputThread = Executors.newSingleThreadExecutor(r -> {
      Thread thread = new Thread(r);
      logger.info(String.format("ProgramsExecutor inputThread %s", thread));
      return thread;
    });
    this.errorThread = Executors.newSingleThreadExecutor(r -> {
      Thread thread = new Thread(r);
      logger.info(String.format("ProgramsExecutor errorThread %s", thread));
      return thread;
    });
  }

  public static class SingletonHolder {
    public static final ProgramExecutor HOLDER_INSTANCE = new ProgramExecutor();
  }

  public static ProgramExecutor getInstance() {
    return SingletonHolder.HOLDER_INSTANCE;
  }

  private static List<String> getStringsFromInputStream(InputStream inputStream) {

    String line;
    List<String> result = new ArrayList<>();

    try (Reader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader stdInput = new BufferedReader(inputStreamReader)) {
      while ((line = stdInput.readLine()) != null) {
        result.add(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return result;
  }

  public Pair<Integer, List<List<String>>> executeCommand(String[] stringCommandArray,
      String rootDir, Logger logger) {

    logger.info(Arrays.toString(stringCommandArray));

    ArrayList<String> commandArray = new ArrayList<>(Arrays.asList(stringCommandArray));
    int executionCode = -1;
    List<List<String>> result = new CopyOnWriteArrayList<>();
    for (int i = 0; i < 2; i++) {
      result.add(Collections.emptyList());
    }

    try {
      Runtime runtime = Runtime.getRuntime();
      Process command;
      if (StringUtils.isEmpty(rootDir)) {
        command = runtime.exec(commandArray.toArray(new String[]{}));
      } else {
        command = runtime
            .exec(commandArray.toArray(new String[]{}), new String[0], new File(rootDir));
      }

      CompletableFuture<List<String>> inputCompletableFuture = CompletableFuture
          .supplyAsync(() -> {
            List<String> resultInputString = new ArrayList<>();
            try (InputStream inputString = command.getInputStream()) {
              resultInputString = getStringsFromInputStream(inputString);
            } catch (IOException e) {
              e.printStackTrace();
              logger.error(e.getMessage(), e);
            }
            return resultInputString;
          }, inputThread);

      CompletableFuture<List<String>> errorCompletableFuture = CompletableFuture
          .supplyAsync(() -> {
            List<String> resultInputString = new ArrayList<>();
            try (InputStream inputString = command.getErrorStream()) {
              resultInputString = getStringsFromInputStream(inputString);
            } catch (IOException e) {
              e.printStackTrace();
              logger.error(e.getMessage(), e);
            }
            return resultInputString;
          }, errorThread);

      executionCode = command.waitFor();

      CompletableFuture
          .allOf(inputCompletableFuture, errorCompletableFuture)
          .thenAccept(aVoid -> {
            try {
              result.set(0, inputCompletableFuture.get());
              result.set(1, errorCompletableFuture.get());
            } catch (InterruptedException | ExecutionException e) {
              logger.error(e.getMessage(), e);
              e.printStackTrace();
              Thread.currentThread().interrupt();
            }
          }).thenAccept(aVoid -> {
        inputCompletableFuture.cancel(true);
        errorCompletableFuture.cancel(true);
      }).join();

    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
      logger.error(e.getMessage(), e);
      Thread.currentThread().interrupt();
    }

    Objects.requireNonNull(result);
    assert result.size() == 2;

    return new ImmutablePair<>(executionCode, result);
  }
}
