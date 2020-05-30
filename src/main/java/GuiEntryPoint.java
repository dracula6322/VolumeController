import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuiEntryPoint {

  public static JTextArea debug;
  public static ExecutorService gutExecutorService = Executors.newSingleThreadExecutor();
  public static Future<?> future;


  public static void main(String[] args) {

    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        new GuiEntryPoint().createGUI(LoggerFactory.getLogger(GuiEntryPoint.class));
      }
    });
  }

  public void createGUI(Logger logger) {

    ExecutorService threadPool = Executors.newCachedThreadPool(runnable -> {
      Thread thread = new Thread(runnable);
      logger.info("thread = " + thread);
      return thread;
    });

    JLabel countTextLabel = new JLabel("Enter count number: ");
    JLabel intervalValue = new JLabel("Enter interval size in second: ");
    JTextField textUsername = new JTextField(20);
    JTextField fieldPassword = new JTextField(20);
    JButton startCount = new JButton("Start count down");
    JButton stopThread = new JButton("Stop thread");

    JCheckBox isSendInSleep = new JCheckBox("Отправить в сон?", false);

    stopThread.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        if (future != null && !future.isDone() && !future.isCancelled()) {
          future.cancel(true);
          debug.append("We stop thread\n");
        } else {
          debug.append("Thread was not starting\n");
        }
      }
    });

    startCount.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {

        int countNumber;
        try {
          countNumber = Integer.parseInt(textUsername.getText());
        } catch (Exception e) {
          debug.append("Error parsing count number\n");
          return;
        }

        int intervalInSeconds;
        try {
          intervalInSeconds = Integer.parseInt(fieldPassword.getText());
        } catch (Exception e) {
          debug.append("Error parsing interval size\n");
          return;
        }

        String[] args = new String[]{"--" + ArgumentsParsingController.countTimer, String.valueOf(countNumber),
            "--" + ArgumentsParsingController.intervalInSecond, String.valueOf(intervalInSeconds),
                "--" + ArgumentsParsingController.sleepInTheEnd, isSendInSleep.isSelected() ? "on" : "off"
        };

        if (future != null && !future.isDone() && !future.isCancelled()) {
          future.cancel(true);
        }

        Logger logger = LoggerFactory.getLogger("mainLogger");
        future = gutExecutorService.submit(() -> {
          MainClassStarter.runChangeSound(args, logger, threadPool);

        });

      }
    });

    JFrame jFrame = new JFrame("JPanel Demo Program");
    jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    JPanel newPanel = new JPanel(new GridBagLayout());

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.anchor = GridBagConstraints.WEST;
    constraints.insets = new Insets(10, 10, 10, 10);

    constraints.gridx = 0;
    constraints.gridy = 0;
    newPanel.add(countTextLabel, constraints);

    constraints.gridx = 1;
    newPanel.add(textUsername, constraints);

    constraints.gridx = 0;
    constraints.gridy = 1;
    newPanel.add(intervalValue, constraints);

    constraints.gridx = 1;
    newPanel.add(fieldPassword, constraints);

    constraints.gridx = 0;
    constraints.gridy = 2;
    newPanel.add(startCount, constraints);

    constraints.gridx = 1;
    constraints.gridy = 2;
    newPanel.add(stopThread, constraints);

    constraints.gridx = 0;
    constraints.gridy = 3;
    newPanel.add(isSendInSleep, constraints);

    constraints.gridx = 0;
    constraints.gridy = 4;
    constraints.gridwidth = 2;
    constraints.anchor = GridBagConstraints.CENTER;
    debug = new JTextArea(16, 58);
    debug.setEditable(false);
    JScrollPane scroll = new JScrollPane(debug);
    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    newPanel.add(scroll, constraints);

    // set border for the panel
    newPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Login Panel"));

    // add the panel to this frame
    jFrame.add(newPanel);

    jFrame.pack();
    jFrame.setLocationRelativeTo(null);
    jFrame.setVisible(true);

  }


}
