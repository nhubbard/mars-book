import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.jetbrains.kotlinx.ktfmt.format.FileType;
import org.jetbrains.kotlinx.ktfmt.format.Formatter;
import org.jetbrains.kotlinx.ktfmt.format.KotlinCode;

/** Keeps the pinned formatter warm while authoring tools generate many independent scripts. */
class KotlinFormattingServer {
  public static void main(String[] args) throws Exception {
    var input = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    for (String request; (request = input.readLine()) != null; ) {
      try {
        int separator = request.indexOf(' ');
        FileType type = FileType.valueOf(request.substring(0, separator));
        String source =
            new String(
                Base64.getDecoder().decode(request.substring(separator + 1)),
                StandardCharsets.UTF_8);
        String formatted =
            Formatter.format(Formatter.META_FORMAT, KotlinCode.Companion.from(source, type));
        System.out.println(
            "OK " + Base64.getEncoder().encodeToString(formatted.getBytes(StandardCharsets.UTF_8)));
      } catch (Exception failure) {
        System.out.println(
            "ERROR "
                + Base64.getEncoder()
                    .encodeToString(failure.toString().getBytes(StandardCharsets.UTF_8)));
      }
    }
  }
}
