import lombok.Data;
import lombok.NonNull;

@Data
public class ProgramArguments {

  @NonNull
  public String pathToNircmd;
  @NonNull
  public int intervalInSecond;
  @NonNull
  public int countTimer;

}
