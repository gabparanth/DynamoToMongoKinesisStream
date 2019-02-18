
Demo DynamoDB - Kinesis - Mongo
mycompany

This program aims to stream data from AWS DynamoDB to MongoDB Atlas using Kinesis. 

TBC: where, in DDB, does Kinesis start streaming : 
- Assumption 1: Kinesis scan the whole DDB table 
- Assumption 2: from the time where we activate de stream 
- Assumption 3: from the time where Kinesis starts

Requirements :
 
1. Create a table "CustomerSupport" in dynamoDB and activate the stream 
Copy/past the stream arn value to streamArn object in StreamsAdapterDemo.java

Note : to improve read/write performances, you can increase the number of write/read units on Dynamodb

2. set your aws-cli to be used under the profile [default]
if profile name is different, it might not work
-> files to update (on OSX):
/Users/[username]/.aws/credentials
/Users/[username]/.aws/config

3. create a DB and collection on mongodbAtlas
Copy/past the mongodb Atlas connexion string value to mongoDBAtlasUrl object in StreamsAdapterDemo.java
same with the database name to databaseName
same with the collection name to CollectionName

4. compilation: mvn clean install 

5. add your secret_access_key and access_key_id in the ruby script and launch to start feeding Dynamodb 

6. launch the stream
mvn exec:java -Dexec.mainClass="com.mycompany.app.StreamsAdapterDemo"

there is treshold sert at 60s execution, it can be change in StreamsAdapterDemo.java Thread.sleep(600000);

7. Don't forget to remove your DynamoDB source table and stream table
-----

Part 2: Tutorial from AWS 
Amazonaws

Step 1: Create a Stream

aws kinesis create-stream --stream-name StockTradeStream --shard-count 1

NOTE:
For each Amazon Kinesis Data Streams application, the KCL uses a unique Amazon DynamoDB table to keep track of the application's state. 
Because the KCL uses the name of the Amazon Kinesis Data Streams application to create the name of the table, each application name must be unique.

Step 2: Launch the producer from the mvn root directory 

mvn exec:java -Dexec.mainClass="com.amazonaws.services.kinesis.samples.stocktrades.writer.StockTradesWriter" -Dexec.args="StockTradeStream eu-west-2"

Step 3: Launch the consumer from the mvn root directory

mvn exec:java -Dexec.mainClass="com.amazonaws.services.kinesis.samples.stocktrades.processor.StockTradesProcessor" -Dexec.args="StockTradesProcessor StockTradeStream eu-west-2"


Step 4: Clean dynamodb And Kinesis 

aws kinesis delete-stream --stream-name StockTradeStream
aws dynamodb delete-table --table-name StockTradesProcessor