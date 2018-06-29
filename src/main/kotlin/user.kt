
fun main(args: Array<String>) {
    try {
        System.loadLibrary("irohajava")
    } catch (e: UnsatisfiedLinkError) {
        System.err.println("Native code library failed to load. \n$e")
        System.exit(1)
    }
    val client = IrohaClient("localhost")
    val account_id = "bob@main"
    println(client.setAccountDetail(account_id, account_id, "tweet", "my second tweet"))
}


