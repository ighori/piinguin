# piinguin

Example run server:

```
$ sbt "servers/run -p 8080"

```

Example client usage (the table must exist on dynamo to successfully write):

```

$ sbt "clients/console"


scala> import java.net.URL
import java.net.URL

scala> import scala.concurrent.{ExecutionContext, Await}
import scala.concurrent.{ExecutionContext, Await}

scala> import scala.concurrent.duration._
import scala.concurrent.duration._

scala> import com.snowplowanalytics.piinguin.client.PiinguinClient
import com.snowplowanalytics.piinguin.client.PiinguinClient

scala> implicit val ec = ExecutionContext.global
ec: scala.concurrent.ExecutionContextExecutor = scala.concurrent.impl.ExecutionContextImpl@7f4b9eca

scala> val u = new URL("http://localhost:8080")
u: java.net.URL = http://localhost:8080

scala> val c = new PiinguinClient(u)
c: com.snowplowanalytics.piinguin.client.PiinguinClient = com.snowplowanalytics.piinguin.client.PiinguinClient@6e5990ce

scala> val createResult = Await.result(c.createPiiRecord("123", "456"), 10 seconds)

createResult: Either[com.snowplowanalytics.piinguin.client.FailureMessage,com.snowplowanalytics.piinguin.client.SuccessMessage] = Left(FailureMessage(UNAVAILABLE: io exception))

or 

createResult: Either[com.snowplowanalytics.piinguin.client.FailureMessage,com.snowplowanalytics.piinguin.client.SuccessMessage] = Left(FailureMessage(Cannot do operations on a non-existent table (Service: AmazonDynamoDBv2; Status Code: 400; Error Code: ResourceNotFoundException; Request ID: 287894d3-ced6-4382-ab9b-bb4f51a0070c)))

or 

createResult: Either[com.snowplowanalytics.piinguin.client.FailureMessage,com.snowplowanalytics.piinguin.client.SuccessMessage] = Right(SuccessMessage(OK))

scala> import com.snowplowanalytics.piinguin.server.generated.protocols.piinguin.ReadPiiRecordRequest.LawfulBasisForProcessing
scala> val readResult = Await.result(c.readPiiRecord("123", LawfulBasisForProcessing.CONSENT), 10 seconds)

                                                                   ^
readResult: Either[com.snowplowanalytics.piinguin.client.FailureMessage,com.snowplowanalytics.piinguin.client.PiinguinClient.PiiRecord] = Right(PiiRecord(123,456))

```



