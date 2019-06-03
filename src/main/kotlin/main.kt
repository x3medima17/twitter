import jp.co.soramitsu.iroha.java.*
import java.time.Instant


val adminKeys = Utils.parseHexKeypair(
        "313a07e6384776ed95447710d15e59148473ccfc052a681317a72a69f2a49910",
        "f101537e319568c765b2cc89698325604991dca57b9716b58016b253506cab70"
)

val adminId = "admin@test"
var irohaAPI = IrohaAPI("localhost", 50051)

fun main() {

    val query = BlocksQueryBuilder(adminId, Instant.now(), 1)
            .buildSigned(adminKeys)

    val obs = irohaAPI.blocksQuery(query).map { response ->
        response.blockResponse.block
    }

    obs.blockingSubscribe { block ->
        block.blockV1.payload.transactionsList
                .flatMap { it.payload.reducedPayload.commandsList }
                .filter { it.hasSetAccountDetail() }
                .map{ it.setAccountDetail }
                .filter { it.key == "tweet" }
                .forEach {
                    println("${it.accountId} : ${it.value}")
                }
    }


}