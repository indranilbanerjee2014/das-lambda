# Java Aurora DAS Event Handler

This project illustrates how to build and deploy a lambda function in Java that will listen to Aurora Postgres Database Activity Stream events.
Aurora Postgres Database Activity Streams (also referred to as DAS), when enabled, creates events for all database activity and writes those events
to a Kinesis Stream that is automatically created when the DAS is enabled on an Aurora Postgres database.

Note: As per the documentation, DAS is also supposed to work for Aurora MySQL, but it does not seem to be working

Steps to get this project to work

1) Create an Aurora Postgres Database using the AWS console or using Cloudformation. For a sample Cloudformation template, refer to https://github.com/aws-samples/aws-aurora-cloudformation-samples/blob/master/cftemplates/Aurora-Postgres-DB-Cluster.yml

2) Once the Aurora Postgres database is created, enable Database Activity Streams using the AWS console or using the AWS CLI, as described in https://docs.aws.amazon.com/AmazonRDS/latest/AuroraUserGuide/DBActivityStreams.Enabling.html

3) You will need to get the ARN of the Kinesis Stream that is created when DAS is enabled. You can get this from the AWS console or using the AWS CLI, as described in https://docs.aws.amazon.com/AmazonRDS/latest/(AuroraUserGuide/DBActivityStreams.Status.html

4) Store the Access Key ID and Secret Access Key of the user who created the database/DAS and store them in the Secrets Manager as variables aws-access-key-id and aws-secret-access-key. These are needed by the Lambda function to decrypt the incoming events from the DAS Kinesis Data Stream, as those events are decrypted. Note the ARNs of the two secrets as they will be needed in the next step

5) Depending on whether you are a Maven or a Gradle user, you need to make some modifications to the template-aurorastreams.yml (Gradle) or template-mvn-aurorastreams.yml (Maven)
    a) Replace the last part of the ARNs of the two secrets in the following lines shown in bold
    
    - AWSSecretsManagerGetSecretValuePolicy:
            SecretArn: !Sub "arn:aws:secretsmanager:${AWS::Region}:${AWS::AccountId}:secret:aws-access-key-id-**WGgS10**"
    
    - AWSSecretsManagerGetSecretValuePolicy:
            SecretArn: !Sub "arn:aws:secretsmanager:${AWS::Region}:${AWS::AccountId}:secret:aws-secret-access-key-**guzlGY**"
    
    b) Replace the last part of the ARN of the Kinesis Data Stream shown in bold, in the following section
    
        Events:
                Stream:
                        Type: Kinesis
                        Properties:
                                Stream: !Sub "arn:aws:kinesis:${AWS::Region}:${AWS::AccountId}:stream/aws-rds-das-cluster-**HKEXEXSQRGYFVBIW5PAAWG3APA**"
                                BatchSize: 10
                                StartingPosition: LATEST
    
    c) The DAS events (with the exception of the heartbeat events that are generated every second) are also written to a DynamoDB table. If you want  to use a different name for the DynamoDB table than the default names provided in the .yml files, please modify the TableName value in the following section of the yml file
    
        AuroraStreamDynamoDBTable:
                Type: AWS::Serverless::SimpleTable
                Properties:
                        TableName: AuroraStreamsGradle
                        PrimaryKey:
                                Name: CorrelationId
                                Type: String 

6) Make sure the AWS CLI is configured with a valid AWS user having permissions to deploy lambda functions in the AWS account

7) Run ./1-create-bucket.sh - This will create an S3 bucket that will be used to store deployment files in the subsequent step

8) Run ./2-deploy-aurorastreams.sh (Gradle) or ./2-deploy-aurorastreams.sh mvn (Maven). This step will build and deploy the lambda function

9) Once the deployment of the lambda function is complete, you should be able to see logs from the lambda function in Cloudwatch logs

10) To see some genuine database activity logs apart from the heartbeat logs, you can log in to your postgres database using a tool such as psql and then create some tables, add rows to tables, run some select queries etc. You should see the logs for these activities in the Cloudwatch logs as well as see entries in DynamoDB
