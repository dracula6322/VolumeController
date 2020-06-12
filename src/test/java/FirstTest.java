import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirstTest {

  final Logger logger = LoggerFactory.getLogger(this.getClass());
  final VolumeController volumeController = VolumeController.getInstance();

  @Test
  public void name() {
    ArgumentsParsingController argumentsParsingController = ArgumentsParsingController.getInstance();
    String[] args = new String[0];
    ProgramArguments programArguments = argumentsParsingController.getProgramArgumentsFromArgs(args, logger);

    programArguments.setCountTimer(10);
    programArguments.setIntervalInSecond(10);

    logger.info("pathToNircmd = " + programArguments.pathToNircmd);
    logger.info("intervalInSecond = " + programArguments.intervalInSecond);
    logger.info("countTimer = " + programArguments.countTimer);
    logger.info("isSleepInTheEnd = " + programArguments.isSleepInTheEnd);
    logger.info("isScreenOffInTheEnd = " + programArguments.isScreeOffInTheEnd);

    long deltaVolume = -7000;

    volumeController.startChangeSound(programArguments.pathToNircmd, programArguments.intervalInSecond,
        programArguments.countTimer, logger, programArguments.isSleepInTheEnd, programArguments.isScreeOffInTheEnd,
        deltaVolume);

  }
}
