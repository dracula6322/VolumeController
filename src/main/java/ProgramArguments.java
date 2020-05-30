import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class ProgramArguments {

  @NonNull
  public String pathToNircmd;
  public int intervalInSecond;
  public int countTimer;
  public boolean isSleepInTheEnd;

}
