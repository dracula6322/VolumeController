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
  public static final String helpParameters = "help";

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

      if (cmd.hasOption(sleepInTheEnd)) {
        String value = cmd.getOptionValue(sleepInTheEnd);
        System.out.println("value = " + value);
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
