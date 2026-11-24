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
        String mode = request.substring(0, separator);
        boolean preserveImports = mode.equals("SCRIPT_IMPORTS");
        FileType type = preserveImports ? FileType.SCRIPT : FileType.valueOf(mode);
        String source =
            new String(
                Base64.getDecoder().decode(request.substring(separator + 1)),
                StandardCharsets.UTF_8);
        String formatted =
            Formatter.format(
                preserveImports
                    ? Formatter.META_FORMAT.toBuilder().removeUnusedImports(false).build()
                    : Formatter.META_FORMAT,
                KotlinCode.Companion.from(source, type));
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
