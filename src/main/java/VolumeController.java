import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

public class VolumeController {

  public static class SingletonHolder {

    public static final VolumeController HOLDER_INSTANCE = new VolumeController();
  }

  public static VolumeController getInstance() {
    return SingletonHolder.HOLDER_INSTANCE;
  }

  public static final long MAX_VOLUME_VALUE = 65535;
  final ProgramExecutor programExecutor = ProgramExecutor.getInstance();

  public void turnOffScreen(String pathToNircmd, Logger logger) {
    String[] commandArray = new String[]{pathToNircmd, "monitor", "off"};
    programExecutor.executeCommand(commandArray, "", logger);
  }

  public void standByMode(String pathToNircmd, Logger logger) {
    String[] commandArray = new String[]{pathToNircmd, "standby"};
    programExecutor.executeCommand(commandArray, "", logger);
  }

  public String getPathToNircmdFromResources(Logger logger) {

    String result = "";
    try {
      URI exeFile = ResourceFileExtractor.getFile("nircmdc.exe");
      File file = new File(exeFile);
      result = file.getAbsolutePath();
    } catch (IOException | URISyntaxException e) {
      e.printStackTrace();
      logger.error(e.getMessage(), e);
    }
    return result;
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

    logger.info("startChangeSound is running in " + Thread.currentThread());

    logger.info("pathToNircmd = " + pathToNircmd);
    logger.info("intervalInSecond = " + intervalInSeconds);
    logger.info("countTimer = " + countTimer);
    logger.info("isSleepInTheEnd = " + isSleepInTheEnd);
    logger.info("isScreenOffInTheEnd = " + isScreenOff);

    File pathToNircmdExe = new File(pathToNircmd);
    if (!pathToNircmdExe.exists()) {
      logger.error("pathToNircmdExe is empty");
      throw new RuntimeException();
    }

    boolean isInterrupted = false;
    for (int i = 0; i < countTimer; i++) {

      Pair<Integer, List<List<String>>> executionResult = changeSoundLevel(pathToNircmd, deltaValue, logger);
      logger.info(executionResult.toString());
      try {
        Thread.sleep(TimeUnit.SECONDS.toMillis(intervalInSeconds));
      } catch (InterruptedException e) {
        isInterrupted = true;
        logger.error(e.getMessage(), e);
        e.printStackTrace();
        break;
      }
      logger.info(i + "/" + countTimer);
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
