import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuiEntryPoint {

  public Future<?> future;

  public static void main(String[] args) {

    Logger logger = LoggerFactory.getLogger(GuiEntryPoint.class);

    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    SwingUtilities.invokeLater(() -> new GuiEntryPoint().createGUI(logger));

  }

  public void createGUI(Logger logger) {

    ExecutorService gutExecutorService = Executors.newSingleThreadExecutor(runnable -> {
      Thread thread = new Thread(runnable);
      logger.info("gutExecutorService thread crated = " + thread);
      return thread;
    });

    initView(logger, gutExecutorService);

  }

  private void initView(Logger logger, ExecutorService gutExecutorService) {

    int countRow = 0;

    JFrame jFrame = new JFrame();
    jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    JPanel newPanel = new JPanel(new GridBagLayout());

    JLabel countTextLabel = new JLabel("Enter count number: ");
    JLabel intervalSizeLabel = new JLabel("Enter interval size in second: ");
    JTextField countTextField = new JTextField(20);
    JTextField intervalSizeField = new JTextField(20);
    JCheckBox isSendInSleep = new JCheckBox("Отправить в сон?", false);
    JCheckBox isScreenOffCheckBox = new JCheckBox("Выключить монитор?", false);

    JButton stopThreadButton = getStopWorkButton(logger);

    JButton startWorkButton = getStartWorkButton(logger, countTextField, intervalSizeField, isSendInSleep,
        isScreenOffCheckBox, gutExecutorService);

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.anchor = GridBagConstraints.WEST;
    constraints.insets = new Insets(10, 10, 10, 10);

    constraints.gridx = 0;
    constraints.gridy = countRow++;
    newPanel.add(countTextLabel, constraints);

    constraints.gridx = 1;
    newPanel.add(countTextField, constraints);

    constraints.gridx = 0;
    constraints.gridy = countRow++;
    newPanel.add(intervalSizeLabel, constraints);

    constraints.gridx = 1;
    newPanel.add(intervalSizeField, constraints);

    constraints.gridx = 0;
    constraints.gridy = countRow;
    newPanel.add(startWorkButton, constraints);

    constraints.gridx = 1;
    constraints.gridy = countRow++;
    newPanel.add(stopThreadButton, constraints);

    constraints.gridx = 0;
    constraints.gridy = countRow++;
    newPanel.add(isSendInSleep, constraints);

    constraints.gridx = 0;
    constraints.gridy = countRow++;
    newPanel.add(isScreenOffCheckBox, constraints);

    constraints.gridx = 0;
    constraints.gridy = countRow;
    constraints.gridwidth = 2;
    constraints.anchor = GridBagConstraints.CENTER;

    getDebugTextArea(newPanel, constraints);

    jFrame.add(newPanel);
    jFrame.pack();
    jFrame.setLocationRelativeTo(null);
    jFrame.setVisible(true);

  }

  private JButton getStartWorkButton(Logger logger, JTextField textUsername, JTextField fieldPassword,
      JCheckBox isSendInSleep, JCheckBox isScreenOffCheckBox, ExecutorService gutExecutorService) {
    JButton startCount = new JButton("Start count down");
    startCount.addActionListener(actionEvent -> {

      int countNumber;
      try {
        countNumber = Integer.parseInt(textUsername.getText());
      } catch (Exception e) {
        logger.error("Error parsing count number\n");
        return;
      }

      int intervalInSeconds;
      try {
        intervalInSeconds = Integer.parseInt(fieldPassword.getText());
      } catch (Exception e) {
        logger.error("Error parsing interval size\n");
        return;
      }

      String[] args = new String[]{"--" + ArgumentsParsingController.countTimer, String.valueOf(countNumber),
          "--" + ArgumentsParsingController.intervalInSecond, String.valueOf(intervalInSeconds),
          "--" + ArgumentsParsingController.sleepInTheEnd, isSendInSleep.isSelected() ? "on" : "off",
          "--" + ArgumentsParsingController.screenOffInTheEnd, isScreenOffCheckBox.isSelected() ? "on" : "off",
      };

      if (future != null && !future.isDone() && !future.isCancelled()) {
        stopThread(logger);
      }

      future = gutExecutorService.submit(() -> {
        logger.info("runChangeSound");
        new MainClassStarter().runChangeSound(args, logger);
        logger.info("job done");
      });

    });

    return startCount;
  }

  private JButton getStopWorkButton(Logger logger) {
    JButton stopThread = new JButton("Stop thread");
    stopThread.addActionListener(actionEvent -> {
      stopThread(logger);
    });
    return stopThread;
  }

  private void getDebugTextArea(JPanel newPanel, GridBagConstraints constraints) {

    JTextArea debug = new JTextArea(16, 58);
    debug.setEditable(false);
    JScrollPane scroll = new JScrollPane(debug);
    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    newPanel.add(scroll, constraints);
  }

  private void stopThread(Logger logger) {
    if (future != null && !future.isDone() && !future.isCancelled()) {
      boolean result = future.cancel(true);
      logger.info("We stop thread result = " + result);
    } else {
      logger.info("Thread was not starting");
    }
  }

}
