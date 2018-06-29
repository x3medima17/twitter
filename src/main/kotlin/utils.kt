import io.grpc.ManagedChannelBuilder
import iroha.protocol.CommandServiceGrpc
import jp.co.soramitsu.iroha.ByteVector
import jp.co.soramitsu.iroha.ModelCrypto
import jp.co.soramitsu.iroha.ModelTransactionBuilder
import mu.KLogging
import java.io.IOException
import java.math.BigInteger
import java.nio.file.Files
import java.nio.file.Paths

val logger = KLogging().logger

private val crypto by lazy { ModelCrypto() }
private val txBuilder by lazy { ModelTransactionBuilder() }

private val channel by lazy {
    ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext(true).build()
}

private val commandStub by lazy {
    CommandServiceGrpc.newBlockingStub(channel)
}

fun getCurrentTime(): BigInteger = BigInteger.valueOf(System.currentTimeMillis())


fun getKeys(user: String) = crypto.convertFromExisting(
    readKeyFromFile("resources/$user.pub"),
    readKeyFromFile("resources/$user.priv")
)

fun readKeyFromFile(path: String): String? {
    return try {
        String(Files.readAllBytes(Paths.get(path)))
    } catch (e: IOException) {
        logger.error { "Unable to read key files.\n $e" }
        null
    }
}

fun toByteArray(blob: ByteVector): ByteArray {
    val bs = ByteArray(blob.size().toInt())
    for (i in 0 until blob.size().toInt()) {
        bs[i] = blob.get(i).toByte()
    }
    return bs
}
