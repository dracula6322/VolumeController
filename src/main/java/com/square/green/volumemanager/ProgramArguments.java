package com.square.green.volumemanager;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class ProgramArguments {

  static final ProgramArguments DEFAULT_VALUE = new ProgramArguments(
      "D:\\Program\\Nircmd\\nircmdc.exe", 0, 0, false, false);

  @NonNull
  public String pathToNircmd;
  private int intervalInSecond;
  private int countTimer;
  private boolean isSleepInTheEnd;
  private boolean isScreeOffInTheEnd;
}
