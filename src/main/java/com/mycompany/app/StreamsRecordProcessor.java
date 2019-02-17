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
import com.mycompany.app.model.Email;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import java.nio.charset.Charset;
import org.json.JSONObject;
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
            // System.out.println("data from Stream to MongoDB: " + data);
            if (record instanceof RecordAdapter) {
                

               
                JSONObject jsonObj = new JSONObject(data).getJSONObject("dynamodb");

                // type should exist for all 3 types
                String type = jsonObj.getJSONObject("NewImage").getJSONObject("type").getString("S");
                String customerID = jsonObj.getJSONObject("NewImage").getJSONObject("customerID").getString("S");
                String from = jsonObj.getJSONObject("NewImage").getJSONObject("from").getString("S");
                String received = jsonObj.getJSONObject("NewImage").getJSONObject("received").getString("S");
                String to = jsonObj.getJSONObject("NewImage").getJSONObject("to").getString("S");

                Float timestamps = jsonObj.getFloat("ApproximateCreationDateTime");



                Document doc = new Document("customerID", customerID);
               try{
                if (type.equals("email")){
                System.out.println("trackergpn : " + type );
                Document email = new Document ("timestamps", timestamps)
                                        .append("from", from)
                                        .append("received", received)
                                        .append("to", to)
                                        .append("subject",jsonObj.getJSONObject("NewImage").getJSONObject("subject").getString("S"))
                                        .append("body", jsonObj.getJSONObject("NewImage").getJSONObject("body").getJSONArray("L").getJSONObject(0).getString("S"));
                
                doc.append("email", email);
                
                } 

                if (type.equals("chat")){
                    
                System.out.println("trackergpn : " + type );
                Document chat = new Document("timestamps", timestamps)
                                                .append("from", from)
                                                .append("received", received)
                                                .append("to", to)
                                                .append("ts2", jsonObj.getJSONObject("NewImage").getJSONObject("chat").getJSONObject("M").getJSONObject("ts2").getString("S"))
                                                .append("content2", jsonObj.getJSONObject("NewImage").getJSONObject("chat").getJSONObject("M").getJSONObject("content2").getString("S"))
                                                .append("from", jsonObj.getJSONObject("NewImage").getJSONObject("chat").getJSONObject("M").getJSONObject("from").getString("S"))
                                                .append("reply", jsonObj.getJSONObject("NewImage").getJSONObject("chat").getJSONObject("M").getJSONObject("reply").getString("S"))
                                                .append("content", jsonObj.getJSONObject("NewImage").getJSONObject("chat").getJSONObject("M").getJSONObject("content").getString("S"))
                                                .append("ts", jsonObj.getJSONObject("NewImage").getJSONObject("chat").getJSONObject("M").getJSONObject("ts").getString("S"));

                doc.append("chat", chat);  

                } 
                if (type.equals("phone")){
                    System.out.println("trackergpn : " + type );
                    Document phone = new Document("timestamps", timestamps)
                                                .append("from", from)
                                                .append("received", received)
                                                .append("to", to)
                                                .append("recordingURI",jsonObj.getJSONObject("NewImage").getJSONObject("recordingURI").getString("S"))
                                                .append("lengthSeconds",jsonObj.getJSONObject("NewImage").getJSONObject("lengthSeconds").getString("N"));
                
                doc.append("phone", phone);                                

                }
                System.out.println("data from Stream to MongoDB: " + type);
                try{
                    collection.insertOne(doc);
                }catch (Exception e){
                    e.printStackTrace();
                    System.out.println("error");
                }
            }catch(Exception e){
                e.printStackTrace();
            }
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

