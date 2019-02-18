/**
 * Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * This file is licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License. A copy of
 * the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
*/


package com.mycompany.app;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBStreams;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBStreamsClientBuilder;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.streamsadapter.AmazonDynamoDBStreamsAdapterClient;
import com.amazonaws.services.dynamodbv2.streamsadapter.StreamsWorkerFactory;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessorFactory;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoClients;
import org.bson.Document;

public class StreamsAdapterDemo {
    private static Worker worker;
    private static KinesisClientLibConfiguration workerConfig;
    private static IRecordProcessorFactory recordProcessorFactory;

    private static AmazonDynamoDB dynamoDBClient;
    private static AmazonCloudWatch cloudWatchClient;
    private static AmazonDynamoDBStreams dynamoDBStreamsClient;
    private static AmazonDynamoDBStreamsAdapterClient adapterClient;

    private static MongoClient mongodbAtlasClient;
    private static MongoDatabase mongoDBDatabase;
    private static MongoCollection<Document> mongoDBCollection;

    private static String mongoDBAtlasUrl = "mongodb+srv://gabriel:gabriel@cluster0-po3pv.mongodb.net/test?retryWrites=true";
    private static String databaseName = "saEnablementTest";
    private static String collectionName = "fromDynamodb";
    private static String streamArn = "arn:aws:dynamodb:eu-west-2:900382475277:table/CustomerSupport/stream/2019-02-18T13:17:10.255";

    private static Regions awsRegion = Regions.EU_WEST_2;
    private static AWSCredentialsProvider awsCredentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();

 

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Starting demo...");

        cloudWatchClient = AmazonCloudWatchClientBuilder.standard()
                                                        .withRegion(awsRegion)
                                                        .build();
        dynamoDBStreamsClient = AmazonDynamoDBStreamsClientBuilder.standard()
                                                                  .withRegion(awsRegion)
                                                                  .build();
        adapterClient = new AmazonDynamoDBStreamsAdapterClient(dynamoDBStreamsClient);
            
        mongodbAtlasClient = MongoClients.create(mongoDBAtlasUrl);
        mongoDBDatabase = mongodbAtlasClient.getDatabase(databaseName);
        mongoDBCollection = mongoDBDatabase.getCollection(collectionName);

        recordProcessorFactory = new StreamsRecordProcessorFactory(
                                                        mongodbAtlasClient, 
                                                        mongoDBDatabase,
                                                        mongoDBCollection);


        workerConfig = new KinesisClientLibConfiguration("streams-adapter-demo",
                                                        streamArn,
                                                        awsCredentialsProvider,
                                                        "streams-demo-worker")
                .withMaxRecords(1000)
                .withIdleTimeBetweenReadsInMillis(500)
                .withInitialPositionInStream(InitialPositionInStream.TRIM_HORIZON);

        System.out.println("Creating worker for stream: " + streamArn);
        worker = StreamsWorkerFactory.createDynamoDbStreamsWorker(recordProcessorFactory, workerConfig, adapterClient, dynamoDBClient, cloudWatchClient);
        System.out.println("Starting worker...");
        Thread t = new Thread(worker);
        t.start();

        Thread.sleep(600000);
        worker.shutdown();
        t.join();

        System.out.println("Done.");
    }
}
