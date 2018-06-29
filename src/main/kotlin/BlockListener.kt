import io.grpc.ManagedChannelBuilder
import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import iroha.protocol.BlockOuterClass
import iroha.protocol.QueryServiceGrpc
import mu.KLogging
import java.io.File
import java.util.concurrent.Executors
import iroha.protocol.BlockOuterClass.Block;
import iroha.protocol.Queries;
import iroha.protocol.Responses
import jp.co.soramitsu.iroha.BlocksQuery
import jp.co.soramitsu.iroha.ModelBlocksQueryBuilder
import jp.co.soramitsu.iroha.ModelProtoBlocksQuery
import jp.co.soramitsu.iroha.ModelQueryBuilder
import java.math.BigInteger

class BlockListener {
    val stub: QueryServiceGrpc.QueryServiceBlockingStub by lazy {
        val channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext(true).build()
        QueryServiceGrpc.newBlockingStub(channel)
    }

    /**
     * Returns an observable that emits a new block every time it gets it from Iroha
     */
    fun getBlockObservable(): Observable<Responses.BlockQueryResponse> {

        logger.info { "On subscribe to Iroha chain" }

        val client = IrohaClient("localhost")
        val query = ModelBlocksQueryBuilder().creatorAccountId("admin@main")
            .queryCounter(BigInteger.valueOf(1))
            .createdTime(getCurrentTime())
            .build()

        val blob = ModelProtoBlocksQuery(query)
            .signAndAddSignature(getKeys("admin@main"))
            .finish()
            .blob()
        val bs = toByteArray(blob)

        val proto = iroha.protocol.Queries.BlocksQuery.parseFrom(bs)


        return stub.fetchCommits(proto).toObservable()

    }

    /**
     * Logger
     */
    companion object : KLogging()
}
