package cloud.carvis.api.util.helpers

import cloud.carvis.api.util.mocks.AwsMock
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import org.awaitility.Awaitility
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.*
import kotlin.streams.toList

@Service
class SesHelper(private val localStack: AwsMock.CarvisLocalStack) {

    fun firstMail(): DocumentContext {
        withTimeout { folderExists("ses") }
        val file = firstFile("ses")
            ?: throw RuntimeException("Unable to extract email from folder 'ses'")
        val json = file.readText(Charsets.UTF_8)
        return JsonPath.parse(json)
    }


    private fun hasFolder(path: String): Boolean {
        return listFiles(path)
            .find { it.isDirectory() } != null
    }

    private fun firstFile(path: String): Path? {
        return listFiles(path)
            .find { it.isRegularFile() }
    }

    private fun folderExists(path: String): Boolean =
        localStack.getDataDir().resolve(path).exists()


    private fun listFiles(path: String): List<Path> {
        val childDir = localStack.getDataDir().resolve(path)
        if (!childDir.isReadable()) {
            return emptyList()
        }

        return Files.walk(childDir)
            .filter { it.isRegularFile() }
            .toList()
    }

    private fun withTimeout(fn: () -> Boolean) {
        Awaitility.await()
            .atMost(30, TimeUnit.SECONDS)
            .until { fn.invoke() }
    }
}