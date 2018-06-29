
fun main(args: Array<String>) {
    try {
        System.loadLibrary("irohajava")
    } catch (e: UnsatisfiedLinkError) {
        System.err.println("Native code library failed to load. \n$e")
        System.exit(1)
    }


    val listener = BlockListener()
    listener.getBlockObservable().subscribe({
        val block = it
        val resp = block.blockResponse
        resp.block.payload.transactionsList
            .flatMap { it.payload.commandsList }
            .filter { it.hasSetAccountDetail() }
            .filter { it.setAccountDetail.key == "tweet" }
            .forEach {
                val author = it.setAccountDetail.accountId
                val value = it.setAccountDetail.value
                println("$author -- $value")
            }

    })
}



