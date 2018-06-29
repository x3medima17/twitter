
import iroha.protocol.BlockOuterClass
import iroha.protocol.Queries.Query

import iroha.protocol.QueryServiceGrpc
import iroha.protocol.CommandServiceGrpc
import iroha.protocol.Endpoint.TxStatusRequest
import iroha.protocol.Responses.QueryResponse
import iroha.protocol.Primitive.uint256


import com.google.protobuf.InvalidProtocolBufferException
import io.grpc.ManagedChannelBuilder
import com.google.protobuf.ByteString
import jp.co.soramitsu.iroha.*
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import mu.KLogging

import java.io.IOException
import java.math.BigInteger
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit


class IrohaClient(host: String) {
    companion object : KLogging()


    private val crypto by lazy { ModelCrypto() }
    private val txBuilder by lazy { ModelTransactionBuilder() }
    private val queryBuilder by lazy { ModelQueryBuilder() }

    private var queryCounter: Long = 1


    private val channel by lazy {
        ManagedChannelBuilder.forAddress(host, 50051).usePlaintext(true).build()
    }

    private val commandStub by lazy {
        CommandServiceGrpc.newBlockingStub(channel)
    }



    fun prepareQuery(uquery: UnsignedQuery, user: String): Query? {
        val queryBlob = ModelProtoQuery(uquery)
            .signAndAddSignature(getKeys(user))
            .finish()
            .blob()

        val bquery = toByteArray(queryBlob)

        var protoQuery: Query? = null
        try {
            protoQuery = Query.parseFrom(bquery)
        } catch (e: InvalidProtocolBufferException) {
            logger.error { "Exception while converting byte array to protobuf:" + e.message }
        }
        return protoQuery
    }

    fun isStatelessValid(resp: QueryResponse) =
        !(resp.hasErrorResponse() &&
                resp.errorResponse.reason.toString() == "STATELESS_INVALID")



    fun requestStatus(hash: Hash): String {
        // create status request
        logger.info { "Hash of the transaction: " + hash.hex() }

        val txhash = hash.blob()
        val bshash = toByteArray(txhash)

        val request = TxStatusRequest.newBuilder().setTxHash(ByteString.copyFrom(bshash)).build()

        val response = commandStub.status(request)
        return response.getTxStatus().name
    }

    fun sendTransaction(utx: UnsignedTx, creator: String): String? {
        // sign transaction and get its binary representation (Blob)
        val txblob = ModelProtoTransaction(utx)
            .signAndAddSignature(getKeys(creator))
            .finish()
            .blob()

        // Convert ByteVector to byte array
        val bs = toByteArray(txblob)

        // create proto object
        val protoTx: BlockOuterClass.Transaction?
        try {
            protoTx = BlockOuterClass.Transaction.parseFrom(bs)
        } catch (e: InvalidProtocolBufferException) {
            logger.error { "Exception while converting byte array to protobuf:" + e.message }
            return null
        }

        // Send transaction to iroha
        commandStub.torii(protoTx)

        // wait to ensure transaction was processed
        runBlocking {
            delay(5000, TimeUnit.MILLISECONDS)
        }

        return requestStatus(utx.hash())
    }

    fun setAccountDetail(creator_id: String, account_id: String, key: String, value: String) : String? {
        val utx = txBuilder.creatorAccountId(creator_id)
            .createdTime(getCurrentTime())
            .setAccountDetail(account_id, key, value)
            .build()
        return sendTransaction(utx, creator_id)
    }

}
