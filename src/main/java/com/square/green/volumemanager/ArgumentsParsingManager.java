package com.square.green.volumemanager;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;

public class ArgumentsParsingManager {

  public static final String PATH_TO_NIRCMD = "pathToNircmd";
  public static final String INTERVAL_IN_SECOND = "intervalInSecond";
  public static final String COUNT_TIMER = "countTimer";
  public static final String SLEEP_IN_THE_END = "sleepInTheEnd";
  public static final String SCREEN_OFF_IN_THE_END = "screenOffInTheEnd";
  public static final String HELP_PARAMETERS = "help";

  private static class SingletonHolder {

    private static final ArgumentsParsingManager HOLDER_INSTANCE = new ArgumentsParsingManager();

    private SingletonHolder() {
    }
  }

  public static ArgumentsParsingManager getInstance() {
    return ArgumentsParsingManager.SingletonHolder.HOLDER_INSTANCE;
  }

  public ProgramArguments getProgramArgumentsFromArgs(String[] args, Logger logger) {
    ProgramArguments defaultValue = ProgramArguments.DEFAULT_VALUE;
    ProgramArguments programArguments = parsingParams(args, defaultValue, logger);
    String pathToNircmdFromResources =
        getPathToNircmdFromResources(programArguments.pathToNircmd, logger);
    programArguments.setPathToNircmd(pathToNircmdFromResources);
    return programArguments;
  }

  private String getPathToNircmdFromResources(String pathToNircmd, Logger logger) {
    try {
      URI exeFile = ResourceFileExtractor.getFile("nircmdc.exe");
      File file = new File(exeFile);
      if (file.exists()) {
        return file.getAbsolutePath();
      }
    } catch (IOException | URISyntaxException e) {
      e.printStackTrace();
      logger.error(e.getMessage(), e);
    }

    File fileToNircmd = new File(pathToNircmd);
    boolean exists = fileToNircmd.exists();
    if (!exists) {
      logger.error(String.format("pathToNircmd is missing %s", pathToNircmd));
    }

    return pathToNircmd;
  }

  private ProgramArguments parsingParams(String[] args, ProgramArguments defaultValue,
      Logger logger) {

    Options options = new Options();

    Option pathToNircmdOption = new Option(PATH_TO_NIRCMD, PATH_TO_NIRCMD, true, PATH_TO_NIRCMD);
    pathToNircmdOption.setRequired(false);
    options.addOption(pathToNircmdOption);

    Option intervalInSecondOption = new Option(INTERVAL_IN_SECOND, INTERVAL_IN_SECOND, true,
        INTERVAL_IN_SECOND);
    intervalInSecondOption.setRequired(false);
    options.addOption(intervalInSecondOption);

    Option countTimerOption = new Option(COUNT_TIMER, COUNT_TIMER, true, COUNT_TIMER);
    countTimerOption.setRequired(false);
    options.addOption(countTimerOption);

    Option helpOption = new Option(HELP_PARAMETERS, HELP_PARAMETERS, false, HELP_PARAMETERS);
    helpOption.setRequired(false);
    options.addOption(helpOption);

    Option screenOffInTheEndOption = new Option(SCREEN_OFF_IN_THE_END, SCREEN_OFF_IN_THE_END, true,
        SCREEN_OFF_IN_THE_END);
    screenOffInTheEndOption.setRequired(false);
    options.addOption(screenOffInTheEndOption);

    Option sleepInTheEndOptions = new Option(SLEEP_IN_THE_END, SLEEP_IN_THE_END, true,
        SLEEP_IN_THE_END);
    sleepInTheEndOptions.setRequired(false);
    options.addOption(sleepInTheEndOptions);

    try {
      CommandLineParser parser = new DefaultParser();
      CommandLine cmd = parser.parse(options, args);

      if (cmd.hasOption(HELP_PARAMETERS)) {
        throw new ParseException("Find help");
      }

      if (cmd.hasOption(PATH_TO_NIRCMD)) {
        defaultValue.pathToNircmd = cmd.getOptionValue(PATH_TO_NIRCMD);
      }

      if (cmd.hasOption(INTERVAL_IN_SECOND)) {
        defaultValue.setIntervalInSecond(Integer.parseInt(cmd.getOptionValue(INTERVAL_IN_SECOND)));
      }

      if (cmd.hasOption(COUNT_TIMER)) {
        defaultValue.setCountTimer(Integer.parseInt(cmd.getOptionValue(COUNT_TIMER)));
      }

      if (cmd.hasOption(SCREEN_OFF_IN_THE_END)) {
        String value = cmd.getOptionValue(SCREEN_OFF_IN_THE_END);
        defaultValue.setScreeOffInTheEnd("on".equals(value));
      }

      if (cmd.hasOption(SLEEP_IN_THE_END)) {
        String value = cmd.getOptionValue(SLEEP_IN_THE_END);
        defaultValue.setSleepInTheEnd("on".equals(value));
      }

    } catch (ParseException e) {
      logger.debug(e.getMessage());
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("utility-name", options);
      System.exit(1);
    }

    logger.info(defaultValue.toString());
    return defaultValue;
  }
}
