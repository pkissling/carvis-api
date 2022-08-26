package cloud.carvis.api.util.helpers

import cloud.carvis.api.util.mocks.AwsMock
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility
import org.springframework.stereotype.Service
import org.testcontainers.containers.Container.ExecResult
import java.io.File
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import kotlin.io.path.exists
import kotlin.io.path.isReadable
import kotlin.io.path.isRegularFile
import kotlin.streams.toList

@Service
class SesHelper(private val localStack: AwsMock.CarvisLocalStack) {

    fun cleanMails() = localStack.aws("rm -rf /data/ses/*")

    fun latestMail(): DocumentContext {
        withTimeout { folderExists("ses") }
        val file = latestFile("ses")
            ?: throw RuntimeException("Unable to extract email from folder 'ses'")
        val json = file.readText(Charsets.UTF_8)
        return JsonPath.parse(json)
    }

    fun deleteIdentity(identity: String) {
        val result = localStack.aws("ses", "delete-identity", "--identity", identity)
        assertThat(result).extracting(ExecResult::getExitCode).isEqualTo(0)
    }

    fun verifyEmailIdentity(fromMail: String) {
        val result = localStack.aws("ses", "verify-email-identity", "--email-address", fromMail)
        assertThat(result).extracting(ExecResult::getExitCode).isEqualTo(0)
    }

    private fun latestFile(path: String): File? = listFiles(path)
        .maxByOrNull { it.lastModified() }

    private fun folderExists(path: String): Boolean =
        localStack.getDataDir().resolve(path).exists()

    private fun listFiles(path: String): List<File> {
        val childDir = localStack.getDataDir().resolve(path)
        if (!childDir.isReadable()) {
            return emptyList()
        }

        return Files.walk(childDir)
            .filter { it.isRegularFile() }
            .map { it.toFile() }
            .toList()
    }

    private fun withTimeout(fn: () -> Boolean) {
        Awaitility.await()
            .atMost(30, TimeUnit.SECONDS)
            .until { fn.invoke() }
    }
}
