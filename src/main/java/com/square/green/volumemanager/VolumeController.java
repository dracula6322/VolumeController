package com.square.green.volumemanager;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

public class VolumeController {

  public static class SingletonHolder {
    private SingletonHolder(){
    }

    public static final VolumeController HOLDER_INSTANCE = new VolumeController();
  }

  public static VolumeController getInstance() {
    return SingletonHolder.HOLDER_INSTANCE;
  }
  private static final long MAX_VOLUME_VALUE = 65535;
  private final ProgramExecutor programExecutor = ProgramExecutor.getInstance();

  public void turnOffScreen(String pathToNircmd, Logger logger) {
    String[] commandArray = new String[]{pathToNircmd, "monitor", "off"};
    programExecutor.executeCommand(commandArray, "", logger);
  }

  public void standByMode(String pathToNircmd, Logger logger) {
    String[] commandArray = new String[]{pathToNircmd, "standby"};
    programExecutor.executeCommand(commandArray, "", logger);
  }

  public Pair<Integer, List<List<String>>> changeSoundLevel(String pathToNircmd, long deltaValue, Logger logger) {
    String[] commandArray = new String[]{pathToNircmd, "changesysvolume", String.valueOf(deltaValue)};
    return programExecutor.executeCommand(commandArray, "", logger);
  }

  public void startChangeSound(String pathToNircmd, int intervalInSeconds, int countTimer, Logger logger,
      boolean isSleepInTheEnd, boolean isScreenOff) {
    long deltaValue = -MAX_VOLUME_VALUE / countTimer;
    startChangeSound(pathToNircmd, intervalInSeconds, countTimer, logger, isSleepInTheEnd, isScreenOff, deltaValue);
  }

  public void startChangeSound(String pathToNircmd, int intervalInSeconds, int countTimer, Logger logger,
      boolean isSleepInTheEnd, boolean isScreenOff, long deltaValue) {

    logger.info(String.format("startChangeSound is running in %s", Thread.currentThread()));
    logger.info(String.format("pathToNircmd = %s", pathToNircmd));
    logger.info(String.format("intervalInSecond = %d", intervalInSeconds));
    logger.info(String.format("countTimer = %d", countTimer));
    logger.info(String.format("isSleepInTheEnd = %s", isSleepInTheEnd));
    logger.info(String.format("isScreenOffInTheEnd = %s", isScreenOff));

    File pathToNircmdExe = new File(pathToNircmd);
    if (!pathToNircmdExe.exists()) {
      logger.error("pathToNircmdExe is empty");
      throw new IllegalArgumentException();
    }

    boolean isInterrupted = false;
    for (int i = 0; i < countTimer; i++) {

      Pair<Integer, List<List<String>>> executionResult = changeSoundLevel(pathToNircmd, deltaValue, logger);
      logger.info(executionResult.toString());
      try {
        Thread.sleep(TimeUnit.SECONDS.toMillis(intervalInSeconds));
      } catch (InterruptedException exception) {
        isInterrupted = true;
        Thread.currentThread().interrupt();
        logger.error(exception.getMessage(), exception);
        break;
      }
      logger.info(String.format("%d/%d", i, countTimer));
    }

    if (isInterrupted) {
      return;
    }

    if (isSleepInTheEnd) {
      standByMode(pathToNircmd, logger);
    }

    if (isScreenOff) {
      turnOffScreen(pathToNircmd, logger);
    }
  }
}
