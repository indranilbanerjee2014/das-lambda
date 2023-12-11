package com.amazonaws.services.lambda.runtime.events;

import software.amazon.awssdk.services.firehose.FirehoseClient;
import software.amazon.awssdk.services.firehose.model.FirehoseException;
import software.amazon.awssdk.services.firehose.model.InvalidArgumentException;
import software.amazon.awssdk.services.firehose.model.InvalidKmsResourceException;
import software.amazon.awssdk.services.firehose.model.PutRecordRequest;
import software.amazon.awssdk.services.firehose.model.Record;
import software.amazon.awssdk.services.firehose.model.ResourceNotFoundException;
import software.amazon.awssdk.services.firehose.model.ServiceUnavailableException;

import java.nio.ByteBuffer;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.exception.SdkClientException;

public class AuroraStreamsKinesisFirehoseInserter {
	String deliveryStream;
	FirehoseClient firehoseClient;
	/**
	 * 
	 */
	public AuroraStreamsKinesisFirehoseInserter() {
		super();
	}
	
	/**
	 * @param deliveryStream
	 */
	public AuroraStreamsKinesisFirehoseInserter(String deliveryStream) {
		super();
		this.deliveryStream = deliveryStream;
		this.firehoseClient = FirehoseClient.create();
	}

	/**
	 * @param deliveryStream
	 * @param firehoseClient
	 */
	public AuroraStreamsKinesisFirehoseInserter(String deliveryStream, FirehoseClient firehoseClient) {
		super();
		this.deliveryStream = deliveryStream;
		this.firehoseClient = firehoseClient;
	}
	/**
	 * @return the deliveryStream
	 */
	public String getDeliveryStream() {
		return deliveryStream;
	}
	/**
	 * @param deliveryStream the deliveryStream to set
	 */
	public void setDeliveryStream(String deliveryStream) {
		this.deliveryStream = deliveryStream;
	}
	/**
	 * @return the firehoseClient
	 */
	public FirehoseClient getFirehoseClient() {
		return firehoseClient;
	}
	/**
	 * @param firehoseClient the firehoseClient to set
	 */
	public void setFirehoseClient(FirehoseClient firehoseClient) {
		this.firehoseClient = firehoseClient;
	}
	
	public void writeRecordToFirehose(String jsonRecord) throws ResourceNotFoundException, InvalidArgumentException, InvalidKmsResourceException, ServiceUnavailableException, FirehoseException, AwsServiceException, SdkClientException  {
		
		try {
			SdkBytes recordInBytes = SdkBytes.fromByteBuffer(ByteBuffer.wrap((jsonRecord).getBytes()));
			Record firehoseRecord = Record.builder().data(recordInBytes).build();
			PutRecordRequest.Builder putRecordRequest = PutRecordRequest.builder();
			putRecordRequest.deliveryStreamName(deliveryStream);
			putRecordRequest.record(firehoseRecord);
			firehoseClient.putRecord(putRecordRequest.build());
		} catch (ResourceNotFoundException e) {
			throw e;
		} catch (InvalidArgumentException e) {
			throw e;
		} catch (InvalidKmsResourceException e) {
			throw e;
		} catch (ServiceUnavailableException e) {
			throw e;
		} catch (FirehoseException e) {
			throw e;
		} catch (AwsServiceException e) {
			throw e;
		} catch (SdkClientException e) {
			throw e;
		}

	}
	
}
