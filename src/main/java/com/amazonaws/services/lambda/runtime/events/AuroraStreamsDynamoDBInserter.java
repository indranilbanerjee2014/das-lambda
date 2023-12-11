package com.amazonaws.services.lambda.runtime.events;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.events.models.aurorastreams.PostgresActivityEvent;

/**
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not
 * use this file except in compliance with the License. A copy of the License is
 * located at
 *
 * http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * This class inserts the individual fields of an Aurora DAS message into a
 * DynamoDB table
 *
 */
public class AuroraStreamsDynamoDBInserter {

	String tableName;
	Table dynamoTable;

	/**
	 * @param tableName
	 */
	public AuroraStreamsDynamoDBInserter(String tableName) {
		super();
		if (null == tableName) {
			tableName = "DAS_DYNAMO_DB_TABLE";
		}
		this.tableName = tableName;
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
		DynamoDB dynamoDB = new DynamoDB(client);
		this.dynamoTable = dynamoDB.getTable(tableName);
	}

	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * @param correlationId - A unique string passed by the Lambda function calling
	 *                      this method to uniquely identify one particular Aurora
	 *                      DAS message
	 * @param recordNumber  - If one Aurora DAS message contains multiple records,
	 *                      each record has a unique recordNumber
	 * @param clusterId     - The clusterId retrieved from the Aurora DAS message
	 *                      from the PostgresActivityRecords class object created
	 *                      from the DAS message
	 * @param instanceId    - The instanceId retrieved from the Aurora DAS message
	 *                      from the PostgresActivityRecords class object created
	 *                      from the DAS message
	 * @param type          - The type retrieved from the Aurora DAS message from
	 *                      the PostgresActivityRecords class object created from
	 *                      the DAS message
	 * @param event         - The object of PostgresActivityEvent class retrieved
	 *                      from the Aurora DAS message from the
	 *                      PostgresActivityRecords class object created from the
	 *                      DAS message
	 * @return
	 */
	public PutItemOutcome insertIntoDynamoDBTable(String correlationId, int recordNumber, String clusterId,
			String instanceId, String type, PostgresActivityEvent event) {
		Item item = new Item();
		if (null == correlationId) {
			item.withPrimaryKey("CorrelationId", java.util.UUID.randomUUID().toString());
		} else {
			item.withPrimaryKey("CorrelationId", correlationId);
		}
		item.withInt("RecordNumber", recordNumber);
		if (null == clusterId) {
			item.withNull("ClusterId");
		} else {
			item.withString("ClusterId", clusterId);
		}
		if (null == instanceId) {
			item.withNull("InstanceId");
		} else {
			item.withString("InstanceId", instanceId);
		}
		if (null == type) {
			item.withNull("Type");
		} else {
			item.withString("Type", type);
		}
		if (null == event.get_class()) {
			item.withNull("Class");
		} else {
			item.withString("Class", event.get_class());
		}
		if (null == event.getClientApplication()) {
			item.withNull("ClientApplication");
		} else {
			item.withString("ClientApplication", event.getClientApplication());
		}
		if (null == event.getCommand()) {
			item.withNull("Command");
		} else {
			item.withString("Command", event.getCommand());
		}
		if (null == event.getCommandText()) {
			item.withNull("CommandText");
		} else {
			item.withString("CommandText", event.getCommandText());
		}
		if (null == event.getDatabaseName()) {
			item.withNull("DatabaseName");
		} else {
			item.withString("DatabaseName", event.getDatabaseName());
		}
		if (null == event.getDbProtocol()) {
			item.withNull("DbProtocol");
		} else {
			item.withString("DbProtocol", event.getDbProtocol());
		}
		if (null == event.getDbUserName()) {
			item.withNull("DbUserName");
		} else {
			item.withString("DbUserName", event.getDbUserName());
		}
		if (null == event.getEndTime()) {
			item.withNull("EndTime");
		} else {
			item.withString("EndTime", event.getEndTime());
		}
		if (null == event.getErrorMessage()) {
			item.withNull("ErrorMessage");
		} else {
			item.withString("ErrorMessage", event.getErrorMessage());
		}
		if (null == event.getExitCode()) {
			item.withNull("ExitCode");
		} else {
			item.withString("ExitCode", event.getExitCode());
		}
		if (null == event.getLogTime()) {
			item.withNull("LogTime");
		} else {
			item.withString("LogTime", event.getLogTime());
		}
		if (null == event.getNetProtocol()) {
			item.withNull("NetProtocol");
		} else {
			item.withString("NetProtocol", event.getNetProtocol());
		}
		if (null == event.getObjectName()) {
			item.withNull("ObjectName");
		} else {
			item.withString("ObjectName", event.getObjectName());
		}
		if (null == event.getObjectType()) {
			item.withNull("ObjectType");
		} else {
			item.withString("ObjectType", event.getObjectType());
		}
		if (null == event.getPid()) {
			item.withNull("Pid");
		} else {
			item.withString("Pid", event.getPid());
		}
		if (null == event.getRemoteHost()) {
			item.withNull("RemoteHost");
		} else {
			item.withString("RemoteHost", event.getRemoteHost());
		}
		if (null == event.getRemotePort()) {
			item.withNull("RemotePort");
		} else {
			item.withString("RemotePort", event.getRemotePort());
		}
		if (null == event.getRowCount()) {
			item.withNull("RowCount");
		} else {
			item.withString("RowCount", event.getRowCount());
		}
		if (null == event.getServerHost()) {
			item.withNull("ServerHost");
		} else {
			item.withString("ServerHost", event.getServerHost());
		}
		if (null == event.getServerType()) {
			item.withNull("ServerType");
		} else {
			item.withString("ServerType", event.getServerType());
		}
		if (null == event.getServerVersion()) {
			item.withNull("ServerVersion");
		} else {
			item.withString("ServerVersion", event.getServerVersion());
		}
		if (null == event.getServiceName()) {
			item.withNull("ServiceName");
		} else {
			item.withString("ServiceName", event.getServiceName());
		}
		if (null == event.getSessionId()) {
			item.withNull("SessionId");
		} else {
			item.withString("SessionId", event.getSessionId());
		}
		if (null == event.getStartTime()) {
			item.withNull("StartTime");
		} else {
			item.withString("StartTime", event.getStartTime());
		}
		if (null == event.getStatementId()) {
			item.withNull("StatementId");
		} else {
			item.withString("StatementId", event.getStatementId());
		}
		if (null == event.getSubstatementId()) {
			item.withNull("SubstatementId");
		} else {
			item.withString("SubstatementId", event.getSubstatementId());
		}
		if (null == event.getTransactionId()) {
			item.withNull("TransactionId");
		} else {
			item.withString("TransactionId", event.getTransactionId());
		}
		if (null == event.getType()) {
			item.withNull("EventType");
		} else {
			item.withString("EventType", event.getType());
		}
		if ((null == event.getParamList()) || (event.getParamList().size() == 0)) {
			item.withNull("ParamList");
		} else {
			item.withList("ParamList", event.getParamList());
		}
		return dynamoTable.putItem(item);
	}

}
