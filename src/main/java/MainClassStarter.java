import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainClassStarter {

  final VolumeController volumeController = VolumeController.getInstance();
  final ArgumentsParsingController argumentsParsingController = ArgumentsParsingController.getInstance();


  public static void main(String[] args) {

    Logger logger = LoggerFactory.getLogger("mainLogger");
    ExecutorService threadPool = Executors.newCachedThreadPool(runnable -> {
      Thread thread = new Thread(runnable);
      logger.info("thread created= " + thread);
      return thread;
    });

    new MainClassStarter().runChangeSound(args, logger);
  }


  public void runChangeSound(String[] args, Logger logger) {

    ProgramArguments programArguments = argumentsParsingController.getProgramArgumentsFromArgs(args, logger);

    logger.info("pathToNircmd = " + programArguments.pathToNircmd);
    logger.info("intervalInSecond = " + programArguments.intervalInSecond);
    logger.info("countTimer = " + programArguments.countTimer);
    logger.info("isSleepInTheEnd = " + programArguments.isSleepInTheEnd);
    logger.info("isScreenOffInTheEnd = " + programArguments.isScreeOffInTheEnd);

    startChangeSound(programArguments, logger);
  }

  private void startChangeSound(ProgramArguments programArguments, Logger logger) {
    volumeController.startChangeSound(programArguments.pathToNircmd, programArguments.intervalInSecond,
        programArguments.countTimer, logger, programArguments.isSleepInTheEnd, programArguments.isScreeOffInTheEnd);
  }


}
