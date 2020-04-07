import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;

public class ArgumentsParsingController {

  public static ProgramArguments parsingParams(String[] args, ProgramArguments defaultValue, Logger logger) {

    Options options = new Options();
    String pathToNircmd = "pathToNircmd";
    String intervalInSecond = "intervalInSecond";
    String countTimer = "countTimer";


    Option pathToNircmdOption = new Option(pathToNircmd, pathToNircmd, true, pathToNircmd);
    pathToNircmdOption.setRequired(false);
    options.addOption(pathToNircmdOption);

    Option intervalInSecondOption = new Option(intervalInSecond, intervalInSecond, true, intervalInSecond);
    intervalInSecondOption.setRequired(false);
    options.addOption(intervalInSecondOption);

    Option countTimerOption = new Option(countTimer, countTimer, true, countTimer);
    countTimerOption.setRequired(false);
    options.addOption(countTimerOption);


    try {
      CommandLineParser parser = new DefaultParser();
      CommandLine cmd = parser.parse(options, args);

      if (cmd.hasOption("help")) {
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

    } catch (ParseException e) {
      logger.debug(e.getMessage());
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("utility-name", options);
      System.exit(1);
    }

    return defaultValue;
  }

}
