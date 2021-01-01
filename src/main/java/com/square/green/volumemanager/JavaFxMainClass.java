package com.square.green.volumemanager;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaFxMainClass extends Application {

  public static final String COURIER_NEW_FONT = "Times New Roman";
  public static final Logger logger = LoggerFactory.getLogger(JavaFxMainClass.class);
  private final ThreadFactory namedThreadFactory =
      new ThreadFactoryBuilder().setDaemon(true).setNameFormat("JavaFxMainClass %s").build();
  private final ExecutorService gutExecutorService = new ThreadPoolExecutor(1, 1,
      0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), namedThreadFactory);
  private JFXTextField countNumberTextInput;
  private JFXTextField intervalTextInput;
  private CheckBox isSleepCheckBox;
  private JFXCheckBox isTurnOffTheScreenCheckBox;
  private Future<?> future;
  private InvalidationListener invalidationListener;
  private JFXButton startButton;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage stage) {
    initUi(stage);
  }

  private void initUi(Stage stage) {

    invalidationListener = observable -> {
      String buttonText;
      try {
        String countNumber = countNumberTextInput.getText();
        int countNumberInt = Integer.parseInt(countNumber);
        String intervalNumber = intervalTextInput.getText();
        int intervalNumberInt = Integer.parseInt(intervalNumber);
        buttonText = "Начать " + (countNumberInt * intervalNumberInt) + " сек";
      } catch (Exception exception) {
        logger.error(exception.getMessage(), exception);
        buttonText = "Начать";
      }
      startButton.setText(buttonText);
    };

    VBox root = new VBox();
    root.setSpacing(10f);
    root.setAlignment(Pos.CENTER);
    root.setPadding(new Insets(25));
    countNumberTextInput = getCountNumberTextInput();
    root.getChildren().add(countNumberTextInput);
    intervalTextInput = getIntervalTextInput();
    root.getChildren().add(intervalTextInput);
    isSleepCheckBox = getIsSleepCheckBox();
    root.getChildren().add(isSleepCheckBox);
    isTurnOffTheScreenCheckBox = getIsTurnOffTheScreenCheckBox();
    root.getChildren().add(isTurnOffTheScreenCheckBox);
    startButton = getStartButton();
    root.getChildren().add(startButton);
    root.getChildren().add(getStopButton());

    Scene scene = new Scene(root, 400.0f, 400.0f);
    scene.getStylesheets().add("animated_gradient_background.css");

    stage.setTitle("Автоматический будильник");
    stage.setScene(scene);
    stage.show();

    initGradient(root);
  }

  private void initGradient(VBox root) {
    root.getStyleClass().add("animated-gradient");

    ObjectProperty<Color> baseColor = new SimpleObjectProperty<>();
    KeyValue keyValue1 = new KeyValue(baseColor, Color.web("#ee7752"));
    KeyValue keyValue2 = new KeyValue(baseColor, Color.web("#e73c7e"));

    KeyFrame keyFrame1 = new KeyFrame(Duration.ZERO, keyValue1);
    KeyFrame keyFrame2 = new KeyFrame(Duration.millis(15000), keyValue2);
    Timeline timeline = new Timeline(keyFrame1, keyFrame2);

    baseColor.addListener((obs, oldColor, newColor) -> {
      if (newColor == null) {
        return;
      }
      root.setStyle(String.format("-gradient-base: #%02x%02x%02x; ",
          (int) (newColor.getRed() * 255),
          (int) (newColor.getGreen() * 255),
          (int) (newColor.getBlue() * 255)));
    });

    timeline.setAutoReverse(true);
    timeline.setCycleCount(Animation.INDEFINITE);
    timeline.play();
  }

  private JFXTextField getCountNumberTextInput() {
    JFXTextField textField = new JFXTextField();
    textField.setPromptText("Введите количество отсчетов");
    textField.setFont(Font.font(COURIER_NEW_FONT, FontWeight.NORMAL, 20));
    textField.setAlignment(Pos.CENTER);
    textField.textProperty().addListener(invalidationListener);
    textField.setMaxWidth(Double.MAX_VALUE);
    return textField;
  }

  private JFXTextField getIntervalTextInput() {
    JFXTextField textField = new JFXTextField();
    textField.setPromptText("Введите время между отсчетами, сек");
    textField.setFont(Font.font(COURIER_NEW_FONT, FontWeight.NORMAL, 20));
    textField.setAlignment(Pos.CENTER);
    textField.textProperty().addListener(invalidationListener);
    textField.setMaxWidth(Double.MAX_VALUE);
    return textField;
  }

  private JFXButton getStopButton() {
    JFXButton button = new JFXButton();
    button.setText("Остановить");
    button.setFont(Font.font(COURIER_NEW_FONT, FontWeight.NORMAL, 20));
    button.setMaxWidth(Double.MAX_VALUE);
    button.setBackground(
        new Background(new BackgroundFill(Color.PINK, new CornerRadii(8), Insets.EMPTY)));
    return button;
  }

  private JFXButton getStartButton() {
    JFXButton button = new JFXButton();
    button.setText("Начать");
    button.setFont(Font.font(COURIER_NEW_FONT, FontWeight.NORMAL, 20));
    button.setMaxWidth(Double.MAX_VALUE);
    button.setBackground(
        new Background(new BackgroundFill(Color.GREEN, new CornerRadii(8), Insets.EMPTY)));
    button.setOnAction(actionEvent -> startChangingTheVolume());
    return button;
  }

  private void startChangingTheVolume() {
    int countNumber;
    try {
      countNumber = Integer.parseInt(countNumberTextInput.getText());
    } catch (Exception e) {
      logger.error(String.format("Error parsing count number. Current value %s",
          countNumberTextInput.getText()));
      return;
    }

    int intervalInSeconds;
    try {
      intervalInSeconds = Integer.parseInt(intervalTextInput.getText());
    } catch (Exception e) {
      logger.error(String.format("Error parsing interval size. Current value is %s",
          intervalTextInput.getText()));
      return;
    }

    String[] args = new String[]{"--" + ArgumentsParsingManager.COUNT_TIMER,
        String.valueOf(countNumber),
        "--" + ArgumentsParsingManager.INTERVAL_IN_SECOND,
        String.valueOf(intervalInSeconds),
        "--" + ArgumentsParsingManager.SLEEP_IN_THE_END,
        isSleepCheckBox.isSelected() ? "on" : "off",
        "--" + ArgumentsParsingManager.SCREEN_OFF_IN_THE_END,
        isTurnOffTheScreenCheckBox.isSelected() ? "on" : "off",
    };

    if (future != null && !future.isDone() && !future.isCancelled()) {
      stopThread(future);
    }

    future = gutExecutorService.submit(() -> {
      logger.info("runChangeSound");
      runChangeSound(args, logger);
      logger.info("job done");
    });
  }

  public void runChangeSound(String[] args, Logger logger) {
    ProgramArguments programArguments = ArgumentsParsingManager.getInstance()
        .getProgramArgumentsFromArgs(args, logger);
    VolumeController.getInstance()
        .startChangeSound(programArguments.pathToNircmd, programArguments.getIntervalInSecond(),
            programArguments.getCountTimer(), logger, programArguments.isSleepInTheEnd(),
            programArguments.isScreeOffInTheEnd());
  }

  private CheckBox getIsSleepCheckBox() {
    CheckBox checkBox = new JFXCheckBox();
    checkBox.setText("Отправить в сон?");
    checkBox.setFont(Font.font(COURIER_NEW_FONT, FontWeight.NORMAL, 20));
    return checkBox;
  }

  private JFXCheckBox getIsTurnOffTheScreenCheckBox() {
    JFXCheckBox checkBox = new JFXCheckBox();
    checkBox.setText("Выключить монитор?");
    checkBox.setFont(Font.font(COURIER_NEW_FONT, FontWeight.NORMAL, 20));
    return checkBox;
  }

  private void stopThread(Future<?> future) {
    String logMessage;
    if (future != null && !future.isDone() && !future.isCancelled()) {
      boolean result = future.cancel(true);
      logMessage = String.format("We stop thread result = %s", result);
    } else {
      logMessage = "Thread not starting";
    }
    JavaFxMainClass.logger.info(logMessage);
  }
}