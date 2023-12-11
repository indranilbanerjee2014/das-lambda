package example;

import java.util.List;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.AuroraStreamsDynamoDBInserter;
import com.amazonaws.services.lambda.runtime.events.AuroraStreamsKinesisFirehoseInserter;
import com.amazonaws.services.lambda.runtime.events.AuroraStreamsProcessor;
import com.amazonaws.services.lambda.runtime.events.AuroraStreamsS3Inserter;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.KinesisEventRecord;
import com.amazonaws.services.lambda.runtime.events.models.aurorastreams.PostgresActivityEvent;
import com.amazonaws.services.lambda.runtime.events.models.aurorastreams.PostgresActivityRecords;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.firehose.model.FirehoseException;
import software.amazon.awssdk.services.firehose.model.InvalidArgumentException;
import software.amazon.awssdk.services.firehose.model.InvalidKmsResourceException;
import software.amazon.awssdk.services.firehose.model.ResourceNotFoundException;
import software.amazon.awssdk.services.firehose.model.ServiceUnavailableException;

/**
 * @author ibanerj This class provides an event handler for processing Aurora
 *         Database Activity Streams Right now it works for Aurora Postgres
 *         Databases only but will be extended in the future to other databases
 *         such as Aurora MySQL Database Activity Streams provides streams
 *         database audit activity to a Kinesis Data Stream A lambda function
 *         processing Database Activity Streams needs a Kinesis Request Handler
 *         The messages put onto the Kinesis Stream are encrypted This class
 *         invokes a method on the AuroraStreamsProcessor class to decrypt the
 *         messages The processPostgresActivity method in the
 *         AuroraStreamsProcessor class takes in the encrypted message and
 *         returns an object of the PostgresActivityRecords class The three
 *         classes PostgresActivity, PostgresActivityEvent and
 *         PostgresActivityRecords are classes that represent the structure of
 *         the Database Activity Streams messages for the Aurora Postgres
 *         database
 */
public class HandlerAuroraStream implements RequestHandler<KinesisEvent, String> {
	final String POSTGRES = "postgres";
	final String MYSQL = "mysql";
	final String INVALID_DATABASE_TYPE = "Invalid Database Type";
	final String CORRELATION_ID = "Correlation ID = ";
	String region = System.getenv("AWS_REGION");
	String dynamoDBTableName = System.getenv("DYNAMO_DB_TABLE");
	String firehoseStream = System.getenv("KINESIS_FIREHOSE_STREAM");
	String s3Bucket = System.getenv("S3_BUCKET");
	String s3Prefix = System.getenv("S3_PREFIX");
	AuroraStreamsProcessor auroraStreamsProcessor = new AuroraStreamsProcessor(region);
	AuroraStreamsKinesisFirehoseInserter auroraStreamsKinesisFirehoseInserter = new AuroraStreamsKinesisFirehoseInserter(firehoseStream);
	AuroraStreamsS3Inserter auroraStreamsS3Inserter = new AuroraStreamsS3Inserter(s3Bucket, s3Prefix);
	AuroraStreamsDynamoDBInserter auroraStreamsDynamoDBInserter = new AuroraStreamsDynamoDBInserter(dynamoDBTableName);
	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	LambdaLogger logger;

	/**
	 * @return the auroraStreamsProcessor
	 */
	public AuroraStreamsProcessor getAuroraStreamsProcessor() {
		return auroraStreamsProcessor;
	}

	/**
	 * @param auroraStreamsProcessor the auroraStreamsProcessor to set
	 */
	public void setAuroraStreamsProcessor(AuroraStreamsProcessor auroraStreamsProcessor) {
		this.auroraStreamsProcessor = auroraStreamsProcessor;
	}

	/**
	 * @return the auroraStreamsDynamoDBInserter
	 */
	public AuroraStreamsDynamoDBInserter getAuroraStreamsDynamoDBInserter() {
		return auroraStreamsDynamoDBInserter;
	}

	/**
	 * @param auroraStreamsDynamoDBInserter the auroraStreamsDynamoDBInserter to set
	 */
	public void setAuroraStreamsDynamoDBInserter(AuroraStreamsDynamoDBInserter auroraStreamsDynamoDBInserter) {
		this.auroraStreamsDynamoDBInserter = auroraStreamsDynamoDBInserter;
	}

	/**
	 * This handleRequest method is the method in the lambda function that processes
	 * incoming events. For Aurora Database Activity Streams, the incoming events
	 * comes from a Kinesis Activity Stream that is created by the Aurora DAS. This
	 * method makes a call to the processPostgresActivity of the
	 * AuroraStreamsProcessor class to decrypt the Kinesis streams event that is
	 * encrypted. The processPostgresActivity method takes in the data contained in
	 * the Kinesis stream as well as a portion of the Kinesis Data stream name that
	 * contains the cluster name as inputs. It needs this in the context of the
	 * decryptiuon algorithm. The lambda logger is also passed to the method so it
	 * can log errors if any (or for debugging if required). The return value of the
	 * processPostgresActivity method is an object of the PostgresActivityRecords
	 * class. This example uses log statements to log the different fields of the
	 * Java object. It also makes a call to the insertIntoDynamoDBTable method of
	 * the AuroraStreamsDynamoDBInserter class. This method inserts the values of
	 * the individual fields that are part of the Aurora DAS into a DynamoDB table.
	 * The name of the DynamoDB table is passed as an environment variable to the
	 * Lambda Function. The variable is DYNAMO_DB_TABLE The Lambda Function also
	 * takes in an environment variable DATABASE_TYPE which currently supports only
	 * one value "postgres". In the future it will be extended to support other
	 * databases as well
	 */
	@Override
	public String handleRequest(KinesisEvent event, Context context) {
		logger = context.getLogger();
		String requestId = context.getAwsRequestId();
		String response = new String("200 OK");
		String correlationId = CORRELATION_ID + requestId + ", ";
		int recordNumber = 0;
		for (KinesisEventRecord record : event.getRecords()) {
			String kinesisStreamName = record.getEventSourceARN();
			String databaseType = System.getenv("DATABASE_TYPE");
			if (null == databaseType) {
				logger.log(correlationId
						+ "Specify an environment variable DATABASE_TYPE with values of Postgres or MySQL");
				databaseType = POSTGRES;
			} else if (!((databaseType.equalsIgnoreCase(POSTGRES)) || (databaseType.equalsIgnoreCase(MYSQL)))) {
				logger.log(correlationId + "Invalid Database Type. Supported Database Types are Postgres and MySQL");
			} else {
				String auroraStreamsClusterName = "cluster"
						+ kinesisStreamName.substring(kinesisStreamName.lastIndexOf("-"));
				if (databaseType.equalsIgnoreCase(POSTGRES)) {
					// Call method on AuroraStreamsProcessor class to get back decrypted object of
					// PostgresActivityRecords class from the encrypted Aurora DAS message
					PostgresActivityRecords postgresActivityRecord = auroraStreamsProcessor
							.processPostgresActivity(record.getKinesis().getData(), auroraStreamsClusterName, logger);
					// Retrieve list of PostgresActivityEvent objects and iterate over the list and
					// for each object in the list of PostgresActivityEvent objects, log the fields
					List<PostgresActivityEvent> databaseActivityEventList = postgresActivityRecord
							.getDatabaseActivityEventList();
					int eventNumber = 1;
					for (PostgresActivityEvent databaseActivityEvent : databaseActivityEventList) {
						String recordType = databaseActivityEvent.getType();
						if (recordType.equalsIgnoreCase("heartbeat")) {
							logger.log(correlationId + "This is a heartbeat event");
						} else {
							logger.log(correlationId + "Printing Incoming Record");
							logger.log(correlationId + "Type = " + postgresActivityRecord.getType());
							logger.log(correlationId + "ClusterID = " + postgresActivityRecord.getClusterId());
							logger.log(correlationId + "Instance ID = " + postgresActivityRecord.getInstanceId());
							logger.log(correlationId + "logTime = " + databaseActivityEvent.getLogTime());
							logger.log(correlationId + "statementId = " + databaseActivityEvent.getStatementId());
							logger.log(correlationId + "substatementId = " + databaseActivityEvent.getSubstatementId());
							logger.log(correlationId + "objecttype = " + databaseActivityEvent.getObjectType());
							logger.log(correlationId + "command = " + databaseActivityEvent.getCommand());
							logger.log(correlationId + "objectname = " + databaseActivityEvent.getObjectName());
							logger.log(correlationId + "databasename = " + databaseActivityEvent.getDatabaseName());
							logger.log(correlationId + "dbUserName = " + databaseActivityEvent.getDbUserName());
							logger.log(correlationId + "remoteHost = " + databaseActivityEvent.getRemoteHost());
							logger.log(correlationId + "remotePort = " + databaseActivityEvent.getRemotePort());
							logger.log(correlationId + "sessionId = " + databaseActivityEvent.getSessionId());
							logger.log(correlationId + "rowCount = " + databaseActivityEvent.getRowCount());
							logger.log(correlationId + "commandText = " + databaseActivityEvent.getCommandText());
							logger.log(correlationId + "pid = " + databaseActivityEvent.getPid());
							logger.log(correlationId + "clientApplication = "
									+ databaseActivityEvent.getClientApplication());
							logger.log(correlationId + "exitCode = " + databaseActivityEvent.getExitCode());
							logger.log(correlationId + "class = " + databaseActivityEvent.get_class());
							logger.log(correlationId + "serverVersion = " + databaseActivityEvent.getServerVersion());
							logger.log(correlationId + "serverType = " + databaseActivityEvent.getServerType());
							logger.log(correlationId + "serviceName = " + databaseActivityEvent.getServiceName());
							logger.log(correlationId + "serverHost = " + databaseActivityEvent.getServerHost());
							logger.log(correlationId + "netProtocol = " + databaseActivityEvent.getNetProtocol());
							logger.log(correlationId + "dbProtocol = " + databaseActivityEvent.getDbProtocol());
							logger.log(correlationId + "type = " + databaseActivityEvent.getType());
							logger.log(correlationId + "startTime = " + databaseActivityEvent.getStartTime());
							logger.log(correlationId + "endTime = " + databaseActivityEvent.getEndTime());
							logger.log(correlationId + "errorMessage = " + databaseActivityEvent.getErrorMessage());
							List<String> params = databaseActivityEvent.getParamList();
							if (null != params) {
								for (String param : params) {
									logger.log(correlationId + "param = " + param);
								}
							}
							logger.log(correlationId + "Finished Printing Incoming Record");
							// Invoke method in AuroraStreamsDynamoDBInserter class to insert each Aurora
							// DAS event into DynamoDB as a separate row in DynamoDB
							auroraStreamsDynamoDBInserter.insertIntoDynamoDBTable(requestId,
									recordNumber * 100 + eventNumber, postgresActivityRecord.getClusterId(),
									postgresActivityRecord.getInstanceId(), postgresActivityRecord.getType(),
									databaseActivityEvent);
							String firehoseRecord = getFirehoseRecordAsJsonString(requestId, recordNumber * 100 + eventNumber, postgresActivityRecord.getClusterId(), postgresActivityRecord.getInstanceId(), postgresActivityRecord.getType(), databaseActivityEvent);
							String s3Record = getOpenSearchRecordAsJsonString(requestId, recordNumber * 100 + eventNumber, postgresActivityRecord.getClusterId(), postgresActivityRecord.getInstanceId(), postgresActivityRecord.getType(), databaseActivityEvent);
							auroraStreamsS3Inserter.insertIntoS3(s3Record);
							try {
								logger.log("Now sending a message to Kinesis Firehose *************");
								logger.log("Record being sent to Kinesis Firehose = " + firehoseRecord);
								auroraStreamsKinesisFirehoseInserter.writeRecordToFirehose(firehoseRecord);
								logger.log("Now finished sending a message to Kinesis Firehose *************");
							} catch (ResourceNotFoundException e) {
								logger.log(e.getMessage());
							} catch (InvalidArgumentException e) {
								logger.log(e.getMessage());
							} catch (InvalidKmsResourceException e) {
								logger.log(e.getMessage());
							} catch (ServiceUnavailableException e) {
								logger.log(e.getMessage());
							} catch (FirehoseException e) {
								logger.log(e.getMessage());
							} catch (AwsServiceException e) {
								logger.log(e.getMessage());
							} catch (SdkClientException e) {
								logger.log(e.getMessage());
							}
						}
						eventNumber++;
					}
				} else if (databaseType.equalsIgnoreCase(MYSQL)) {
					// TODO - MySQL implementation
				}
			}
			recordNumber++;
		}

		return response;
	}
	
	private String getFirehoseRecordAsJsonString(String correlationId, int recordNumber, String clusterId, String instanceId, String type, PostgresActivityEvent event) {
		String returnString = "";
		JsonElement jsonElement = gson.toJsonTree(event);
		jsonElement.getAsJsonObject().addProperty("correlationId", correlationId);
		jsonElement.getAsJsonObject().addProperty("recordNumber", recordNumber);
		jsonElement.getAsJsonObject().addProperty("clusterId", clusterId);
		jsonElement.getAsJsonObject().addProperty("instanceId", instanceId);
		jsonElement.getAsJsonObject().remove("paramList");
		returnString = gson.toJson(jsonElement);
		return returnString;
	}
	
	private String getOpenSearchRecordAsJsonString(String correlationId, int recordNumber, String clusterId, String instanceId, String type, PostgresActivityEvent event) {
		String returnString = "";
		JsonElement jsonElement = gson.toJsonTree(event);
		jsonElement.getAsJsonObject().addProperty("correlationId", correlationId);
		jsonElement.getAsJsonObject().addProperty("recordNumber", recordNumber);
		jsonElement.getAsJsonObject().addProperty("clusterId", clusterId);
		jsonElement.getAsJsonObject().addProperty("instanceId", instanceId);
		JsonArray jsonArray = new JsonArray();
		jsonArray.add(jsonElement);
		returnString = gson.toJson(jsonArray);
		return returnString;
	}
	
}