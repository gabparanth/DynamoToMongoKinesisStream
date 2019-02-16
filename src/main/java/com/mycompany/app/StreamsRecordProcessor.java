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

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.streamsadapter.model.RecordAdapter;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.ShutdownReason;
import com.amazonaws.services.kinesis.clientlibrary.types.InitializationInput;
import com.amazonaws.services.kinesis.clientlibrary.types.ProcessRecordsInput;
import com.amazonaws.services.kinesis.clientlibrary.types.ShutdownInput;
import com.amazonaws.services.kinesis.model.Record;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import java.nio.charset.Charset;
// import com.google.gson.*;

public class StreamsRecordProcessor implements IRecordProcessor {
    private Integer checkpointCounter;

    private final AmazonDynamoDB dynamoDBClient;
    private final String tableName;

    MongoClient mongoClient = MongoClients.create("mongodb+srv://gabriel:gabriel@cluster0-po3pv.mongodb.net/test?retryWrites=true");
    MongoDatabase database = mongoClient.getDatabase("saEnablementTest");
    MongoCollection<Document> collection = database.getCollection("fromDynamodb");

    public StreamsRecordProcessor(AmazonDynamoDB dynamoDBClient2, String tableName) {
        this.dynamoDBClient = dynamoDBClient2;
        this.tableName = tableName;
    }

    @Override
    public void initialize(InitializationInput initializationInput) {
        checkpointCounter = 0;
    }

    /**
     * ProcessRecordsInput.records could be our Kinesis producer side 
     */

    


    @Override
    public void processRecords(ProcessRecordsInput processRecordsInput) {
        for (Record record : processRecordsInput.getRecords()) {
            String data = new String(record.getData().array(), Charset.forName("UTF-8"));
            System.out.println("data from Stream to MongoDB: " + data);
            if (record instanceof RecordAdapter) {
                

                String stringToRecord = data.toString(); 
                
                Document doc = new Document("name", "MongoDB")
                                    .append("type", stringToRecord);
                
                try {

                    collection.insertOne(doc);
                }catch (Exception e) {
                    e.printStackTrace();
                }
                                    

                
                // com.amazonaws.services.dynamodbv2.model.Record streamRecord = ((RecordAdapter) record)
                //         .getInternalObject();

                // switch (streamRecord.getEventName()) {
                //     case "INSERT":
                //     case "MODIFY":
                //         StreamsAdapterDemoHelper.putItem(dynamoDBClient, tableName,
                //                                          streamRecord.getDynamodb().getNewImage());
                //         break;
                //     case "REMOVE":
                //         StreamsAdapterDemoHelper.deleteItem(dynamoDBClient, tableName,
                //                                             streamRecord.getDynamodb().getKeys().get("Id").getN());
                // }
                



            }
            checkpointCounter += 1;
            if (checkpointCounter % 10 == 0) {
                try {
                    processRecordsInput.getCheckpointer().checkpoint();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
    }

    @Override
    public void shutdown(ShutdownInput shutdownInput) {
        if (shutdownInput.getShutdownReason() == ShutdownReason.TERMINATE) {
            try {
                shutdownInput.getCheckpointer().checkpoint();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        
    }    
}

