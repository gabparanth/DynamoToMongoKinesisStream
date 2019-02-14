HOW TO : 


Step 1: Create a Stream

aws kinesis create-stream --stream-name StockTradeStream --shard-count 1

Step 2: Launch the producer from the mvn root directory 

mvn exec:java -Dexec.mainClass="com.amazonaws.services.kinesis.samples.stocktrades.writer.StockTradesWriter" -Dexec.args="StockTradeStream eu-west-2"

Step 3: Launch the consumer from the mvn root directory

mvn exec:java -Dexec.mainClass="com.amazonaws.services.kinesis.samples.stocktrades.processor.StockTradesProcessor" -Dexec.args="StockTradesProcessor StockTradeStream eu-west-2"


Step 4: Clean dynamodb And Kinesis 

aws kinesis delete-stream --stream-name StockTradeStream
aws dynamodb delete-table --table-name StockTradesProcessor