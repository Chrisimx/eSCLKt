import java.nio.file.Files
import java.nio.file.StandardOpenOption
import kotlin.io.path.Path

actual fun maybeSaveDebugFile(fileName: String, data: ByteArray) {
    val path = Path(fileName)
    Files.write(path, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
}