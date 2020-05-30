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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainClassStarter {

  public static final long MAX_VOLUME_VALUE = 65535;
  public static final long MIN_VOLUME_VALUE = 0;


  public static void main(String[] args) {

    Logger logger = LoggerFactory.getLogger("mainLogger");

    ExecutorService threadPool = Executors.newCachedThreadPool(runnable -> {
      Thread thread = new Thread(runnable);
      logger.info("thread = " + thread);
      return thread;
    });

    runChangeSound(args, logger, threadPool);
  }

  public static void runChangeSound(String[] args, Logger logger, ExecutorService threadPool) {

    String pathToNircmd = "E:\\Programs\\Nircmd\\nircmdc.exe";
    int intervalInSecond = 1;
    int countTimer = 1;
    boolean isSleepInTheEnd = false;

    ProgramArguments defaultValue = new ProgramArguments(pathToNircmd, intervalInSecond, countTimer, isSleepInTheEnd);
    ProgramArguments parsingResult = ArgumentsParsingController.parsingParams(args, defaultValue, logger);

    pathToNircmd = parsingResult.getPathToNircmd();
    intervalInSecond = parsingResult.getIntervalInSecond();
    countTimer = parsingResult.getCountTimer();
    isSleepInTheEnd = parsingResult.isSleepInTheEnd();

    System.out.println("pathToNircmd = " + pathToNircmd);
    System.out.println("intervalInSecond = " + intervalInSecond);
    System.out.println("countTimer = " + countTimer);
    System.out.println("isSleepInTheEnd = " + isSleepInTheEnd);

    startChangeSound(pathToNircmd, threadPool, intervalInSecond, countTimer, logger, isSleepInTheEnd);
  }

  private static void startChangeSound(String pathToNircmd, ExecutorService threadPool, int intervalInSeconds,
      int countTimer, Logger logger, boolean isSleepInTheEnd) {

    logger.info("startChangeSound " + Thread.currentThread());

    File pathToNircmdExe = new File(pathToNircmd);
    if (!pathToNircmdExe.exists()) {
      throw new RuntimeException();
    }
    CountDownLatch countDownLatch = new CountDownLatch(1);
    threadPool.submit(() -> {
      for (int i = 0; i < countTimer; i++) {

        changeSoundLevel(pathToNircmd, -MAX_VOLUME_VALUE / countTimer, threadPool, threadPool);
        try {
          Thread.sleep(TimeUnit.SECONDS.toMillis(intervalInSeconds));
        } catch (InterruptedException e) {
          logger.error(e.getMessage());
          e.printStackTrace();
        }
        logger.info(i + "/" + countTimer);
      }
      if (isSleepInTheEnd) {
        standByMode(pathToNircmd, threadPool, threadPool);
      }
      countDownLatch.countDown();
    });

    try {
      countDownLatch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    threadPool.shutdown();
  }

  public static void changeSoundLevel(String pathToNircmd, long deltaValue, ExecutorService outputThread,
      ExecutorService errorThread) {

    ArrayList<String> commandArrayMass = new ArrayList<>();
    commandArrayMass.add(pathToNircmd);
    commandArrayMass.add("changesysvolume");
    commandArrayMass.add(String.valueOf(deltaValue));

    executeFunctionAndGetStringOutputWithResult(commandArrayMass.toArray(new String[0]), "", outputThread,
        errorThread);
  }

  public static void turnOffScreen(String pathToNircmd, ExecutorService outputThread,
      ExecutorService errorThread) {

    ArrayList<String> commandArrayMass = new ArrayList<>();
    commandArrayMass.add(pathToNircmd);
    commandArrayMass.add("monitor");
    commandArrayMass.add("off");

    executeFunctionAndGetStringOutputWithResult(commandArrayMass.toArray(new String[0]), "", outputThread,
        errorThread);
  }

  public static void standByMode(String pathToNircmd, ExecutorService outputThread,
      ExecutorService errorThread) {

    ArrayList<String> commandArrayMass = new ArrayList<>();
    commandArrayMass.add(pathToNircmd);
    commandArrayMass.add("standby");

    executeFunctionAndGetStringOutputWithResult(commandArrayMass.toArray(new String[0]), "", outputThread,
        errorThread);
  }

  public static Pair<Integer, ArrayList<List<String>>> executeFunctionAndGetStringOutputWithResult(
      String[] stringCommandArray,
      String rootDir, ExecutorService inputThread, ExecutorService errorThread) {

    ArrayList<String> commandArray = new ArrayList<>(Arrays.asList(stringCommandArray));
    int executionCode = -1;
    ArrayList<List<String>> result = new ArrayList<>();
    for (int i = 0; i < 2; i++) {
      result.add(Collections.emptyList());
    }
    CountDownLatch countDownLatch = new CountDownLatch(2);

    try {
      Runtime runtime = Runtime.getRuntime();
      Process command;
      if (TextUtils.isEmpty(rootDir)) {
        command = runtime.exec(commandArray.toArray(new String[]{}));
      } else {
        command = runtime.exec(commandArray.toArray(new String[]{}), new String[0], new File(rootDir));
      }
      inputThread.execute(() -> {
        try {
          InputStream inputString = command.getInputStream();
          List<String> resultInputString = getStringsFromInputStream(inputString);
          inputString.close();
          result.set(0, resultInputString);
        } catch (IOException e) {
          e.printStackTrace();
        }
        countDownLatch.countDown();
      });

      errorThread.execute(() -> {
        try {
          InputStream inputString = command.getErrorStream();
          List<String> resultInputString = getStringsFromInputStream(inputString);
          inputString.close();
          result.set(1, resultInputString);
        } catch (IOException e) {
          e.printStackTrace();
        }
        countDownLatch.countDown();
      });
      executionCode = command.waitFor();

    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
      System.err.println(e);
    }

    try {
      countDownLatch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    Objects.requireNonNull(result);
    assert result.size() == 2;

    return new ImmutablePair<>(executionCode, result);
  }

  private static List<String> getStringsFromInputStream(InputStream inputStream) {

    String line;
    List<String> result = new ArrayList<>();
    try {
      Reader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
      BufferedReader stdInput = new BufferedReader(inputStreamReader);
      while ((line = stdInput.readLine()) != null) {
        result.add(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return result;

  }


}
