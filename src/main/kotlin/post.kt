import jp.co.soramitsu.iroha.java.Transaction
import jp.co.soramitsu.iroha.java.Utils


fun main() {
    val tx = Transaction.builder(adminId)
            .setAccountDetail(adminId, "tweet", "Hello world")
            .sign(adminKeys)
            .build()
    irohaAPI.transactionSync(tx)
    repeat(10) {
        println(irohaAPI.txStatusSync(Utils.hash(tx)))
        Thread.sleep(100)
    }
}