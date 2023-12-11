//package example;
//
//import static java.nio.ByteBuffer.wrap;
//import static java.util.Collections.singletonList;
//import static net.andreinc.mockneat.unit.objects.Reflect.reflect;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.Mockito.mock;
//import java.io.ByteArrayOutputStream;
//import java.lang.reflect.Field;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.zip.GZIPOutputStream;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentMatchers;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
//import com.amazonaws.services.dynamodbv2.model.PutItemResult;
//import com.amazonaws.services.lambda.runtime.Context;
//import com.amazonaws.services.lambda.runtime.LambdaLogger;
//import com.amazonaws.services.lambda.runtime.events.AuroraStreamsDynamoDBInserter;
//import com.amazonaws.services.lambda.runtime.events.AuroraStreamsProcessor;
//import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
//import com.amazonaws.services.lambda.runtime.events.models.aurorastreams.PostgresActivity;
//import com.amazonaws.services.lambda.runtime.events.models.aurorastreams.PostgresActivityEvent;
//import com.amazonaws.services.lambda.runtime.events.models.aurorastreams.PostgresActivityRecords;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import net.andreinc.mockneat.MockNeat;
//import net.andreinc.mockneat.unit.objects.Reflect;
//import java.nio.ByteBuffer;
//
//@ExtendWith(MockitoExtension.class)
//public class HandlerAuroraStreamTest {
//	private ObjectMapper objectMapper = new ObjectMapper();
//	private HandlerAuroraStream lambda;
//
//	@Mock
//	private AuroraStreamsProcessor auroraStreamsProcessor;
//
//	@Mock
//	private AuroraStreamsDynamoDBInserter auroraStreamsDynamoDBInserter;
//
//	@Test
//	@ExtendWith(MockitoExtension.class)
//	public void testLambdaHandleRequest() throws Exception {
//		Map<String, String> envMap = new HashMap<String, String>();
//		envMap.put("AWS_REGION", "us-west-2");
//		envMap.put("DYNAMO_DB_TABLE", "DummyTable");
//		envMap.put("DATABASE_TYPE", "postgres");
//		//setEnv(envMap);
//		lambda = new HandlerAuroraStream();
//		lambda.logger = new TestLogger();
//		Context testContext = new TestContext();
//		auroraStreamsProcessor = mock(AuroraStreamsProcessor.class);
//		auroraStreamsDynamoDBInserter = mock(AuroraStreamsDynamoDBInserter.class);
//		lambda.setAuroraStreamsProcessor(auroraStreamsProcessor);
//		lambda.setAuroraStreamsDynamoDBInserter(auroraStreamsDynamoDBInserter);
//		PostgresActivityRecords postgresActivityRecords = getPostgresActivityRecords();
//		Mockito.when(auroraStreamsProcessor.processPostgresActivity(ArgumentMatchers.any(ByteBuffer.class),
//				ArgumentMatchers.anyString(), ArgumentMatchers.any(LambdaLogger.class)))
//				.thenReturn(postgresActivityRecords);
//		Mockito.when(auroraStreamsDynamoDBInserter.insertIntoDynamoDBTable(ArgumentMatchers.anyString(),
//				ArgumentMatchers.anyInt(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
//				ArgumentMatchers.anyString(), ArgumentMatchers.any(PostgresActivityEvent.class)))
//				.thenReturn(new PutItemOutcome(new PutItemResult()));
//		String response = lambda.handleRequest(createKinesisEvent(false), testContext);
//		System.out.println("response = " + response);
//		assertEquals(response, new String("200 OK"));
//	}
//
//	private KinesisEvent createKinesisEvent(boolean gzip) throws Exception {
//
//		MockNeat m = MockNeat.threadLocal();
//		Reflect<PostgresActivity> activity = reflect(PostgresActivity.class)
//				.field("type", "DatabaseActivityMonitoringRecords").field("version", "1.1")
//				.field("databaseActivityEvents", m.strings().size(50).get()).field("key", m.strings().size(50).get());
//
//		List<PostgresActivity> activityList = activity.list(4).get();
//
//		byte[] data = objectMapper.writeValueAsBytes(activityList);
//
//		if (gzip) {
//			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
//			GZIPOutputStream gzipOut = new GZIPOutputStream(byteOut);
//			gzipOut.write(data);
//			gzipOut.close();
//			data = byteOut.toByteArray();
//		}
//		KinesisEvent.Record record = new KinesisEvent.Record();
//		record.setData(wrap(data));
//		KinesisEvent.KinesisEventRecord kinesisEventRecord = new KinesisEvent.KinesisEventRecord();
//		kinesisEventRecord.setEventID("shardId-1234567");
//		kinesisEventRecord.setKinesis(record);
//		kinesisEventRecord.setEventSourceARN("aws-rds-das-cluster-HKEXEXSQRGYFVBIW5PAAWG3APA");
//		KinesisEvent kinesisEvent = new KinesisEvent();
//		kinesisEvent.setRecords(singletonList(kinesisEventRecord));
//		return kinesisEvent;
//	}
//
//	private PostgresActivityRecords getPostgresActivityRecords() {
//		MockNeat m = MockNeat.threadLocal();
//		List<String> params = new ArrayList<String>();
//		params.add("param1");
//		params.add("param2");
//		params.add("param3");
//		Reflect<PostgresActivityEvent> activityEvent = reflect(PostgresActivityEvent.class)
//				.field("_class", m.strings().size(20).get()).field("clientApplication", m.strings().size(25).get())
//				.field("command", m.strings().size(10).get()).field("commandText", m.strings().size(50).get())
//				.field("databaseName", m.strings().size(10).get()).field("dbProtocol", m.strings().size(10).get())
//				.field("dbUserName", m.strings().size(15).get()).field("endTime", m.strings().size(20).get())
//				.field("errorMessage", m.strings().size(25).get()).field("exitCode", m.strings().size(10).get())
//				.field("logTime", m.strings().size(25).get()).field("netProtocol", m.strings().size(25).get())
//				.field("objectName", m.strings().size(25).get()).field("objectType", m.strings().size(25).get())
//				.field("pid", m.strings().size(25).get()).field("remoteHost", m.strings().size(25).get())
//				.field("remotePort", m.strings().size(25).get()).field("rowCount", m.strings().size(3).get())
//				.field("serverHost", m.strings().size(25).get()).field("serverType", m.strings().size(25).get())
//				.field("serverVersion", m.strings().size(25).get()).field("serviceName", m.strings().size(25).get())
//				.field("sessionId", m.strings().size(25).get()).field("startTime", m.strings().size(25).get())
//				.field("statementId", m.strings().size(25).get()).field("substatementId", m.strings().size(25).get())
//				.field("transactionId", m.strings().size(25).get()).field("type", m.strings().size(25).get())
//				.field("paramList", params);
//		List<PostgresActivityEvent> activityEventList = activityEvent.list(4).get();
//		Reflect<PostgresActivityRecords> postgresActivityRecords = reflect(PostgresActivityRecords.class)
//				.field("clusterId", m.strings().size(20).get()).field("instanceId", m.strings().size(20).get())
//				.field("type", m.strings().size(20).get()).field("databaseActivityEventList", activityEventList);
//		return postgresActivityRecords.get();
//	}
//}
