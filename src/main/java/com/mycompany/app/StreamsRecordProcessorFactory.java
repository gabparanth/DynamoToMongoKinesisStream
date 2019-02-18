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
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessorFactory;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
public class StreamsRecordProcessorFactory implements IRecordProcessorFactory {


    private final MongoClient mongoDBAtlas;
    private final MongoDatabase mongoDBDatabase;
    private final MongoCollection<Document> mongoDBCollection;


    public StreamsRecordProcessorFactory(MongoClient mongoDBAtlas, MongoDatabase mongoDBDatabase, MongoCollection<Document> mongoDBCollection) {
        this.mongoDBAtlas = mongoDBAtlas;
        this.mongoDBDatabase = mongoDBDatabase;
        this.mongoDBCollection = mongoDBCollection;
   }

    @Override
    public IRecordProcessor createProcessor() {
        return new StreamsRecordProcessor(mongoDBAtlas, mongoDBDatabase, mongoDBCollection);
    }
}