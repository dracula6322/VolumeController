import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;

public class ArgumentsParsingController {

  public static final String pathToNircmd = "pathToNircmd";
  public static final String intervalInSecond = "intervalInSecond";
  public static final String countTimer = "countTimer";
  public static final String sleepInTheEnd = "sleepInTheEnd";
  public static final String screenOffInTheEnd = "screenOffInTheEnd";
  public static final String helpParameters = "help";

  public static class SingletonHolder {

    public static final ArgumentsParsingController HOLDER_INSTANCE = new ArgumentsParsingController();
  }

  public static ArgumentsParsingController getInstance() {
    return ArgumentsParsingController.SingletonHolder.HOLDER_INSTANCE;
  }

  public ProgramArguments getProgramArgumentsFromArgs(String[] args, Logger logger) {

    String pathToNircmd = "E:\\Programs\\Nircmd\\nircmdc.exe";
    int intervalInSecond = 1;
    int countTimer = 1;
    boolean isSleepInTheEnd = false;
    boolean isScreenOffInTheEnd = false;

    ProgramArguments defaultValueProgramArguments = new ProgramArguments(pathToNircmd, intervalInSecond, countTimer,
        isSleepInTheEnd, isScreenOffInTheEnd);
    ProgramArguments programArguments = ArgumentsParsingController
        .parsingParams(args, defaultValueProgramArguments, logger);

    String pathToNircmdFromResources = VolumeController.getInstance().getPathToNircmdFromResources(logger);

    programArguments.setPathToNircmd(pathToNircmdFromResources);

    return programArguments;
  }

  public static ProgramArguments parsingParams(String[] args, ProgramArguments defaultValue, Logger logger) {

    Options options = new Options();

    Option pathToNircmdOption = new Option(pathToNircmd, pathToNircmd, true, pathToNircmd);
    pathToNircmdOption.setRequired(false);
    options.addOption(pathToNircmdOption);

    Option intervalInSecondOption = new Option(intervalInSecond, intervalInSecond, true, intervalInSecond);
    intervalInSecondOption.setRequired(false);
    options.addOption(intervalInSecondOption);

    Option countTimerOption = new Option(countTimer, countTimer, true, countTimer);
    countTimerOption.setRequired(false);
    options.addOption(countTimerOption);

    Option helpOption = new Option(helpParameters, helpParameters, false, helpParameters);
    helpOption.setRequired(false);
    options.addOption(helpOption);

    Option screenOffInTheEndOption = new Option(screenOffInTheEnd, screenOffInTheEnd, true, screenOffInTheEnd);
    screenOffInTheEndOption.setRequired(false);
    options.addOption(screenOffInTheEndOption);

    Option sleepInTheEndOptions = new Option(sleepInTheEnd, sleepInTheEnd, true, sleepInTheEnd);
    sleepInTheEndOptions.setRequired(false);
    options.addOption(sleepInTheEndOptions);


    try {
      CommandLineParser parser = new DefaultParser();
      CommandLine cmd = parser.parse(options, args);

      if (cmd.hasOption(helpParameters)) {
        throw new ParseException("Find help");
      }

      if (cmd.hasOption(pathToNircmd)) {
        defaultValue.pathToNircmd = cmd.getOptionValue(pathToNircmd);
      }

      if (cmd.hasOption(intervalInSecond)) {
        defaultValue.intervalInSecond = Integer.parseInt(cmd.getOptionValue(intervalInSecond));
      }

      if (cmd.hasOption(countTimer)) {
        defaultValue.countTimer = Integer.parseInt(cmd.getOptionValue(countTimer));
      }

      if (cmd.hasOption(screenOffInTheEnd)) {
        String value = cmd.getOptionValue(screenOffInTheEnd);
        logger.info("value = " + value);
        defaultValue.isScreeOffInTheEnd = value.equals("on");
      }

      if (cmd.hasOption(sleepInTheEnd)) {
        String value = cmd.getOptionValue(sleepInTheEnd);
        logger.info("value = " + value);
        defaultValue.isSleepInTheEnd = value.equals("on");
      }

    } catch (ParseException e) {
      logger.debug(e.getMessage());
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("utility-name", options);
      System.exit(1);
    }

    return defaultValue;
  }

}
