import org.junit.jupiter.api.Test

class TwitterTest {
    @Test
    fun setDetail(){
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
}
