/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.acconsulting.sparkconsumer;

import com.google.gson.Gson;

import it.acconsulting.bean.ZBRecord;

import it.acconsulting.dao.ZBRecordsDAO;
import it.acconsulting.db.DBManager;
import it.acconsulting.util.Utils;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import kafka.serializer.StringDecoder;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.log4j.Logger;
import org.apache.parquet.io.api.RecordConsumer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
//import org.apache.spark.streaming.kafka.HasOffsetRanges;
//import org.apache.spark.streaming.kafka.HasOffsetRanges;
//import org.apache.spark.streaming.kafka.KafkaUtils;
//import org.apache.spark.streaming.kafka.OffsetRange;
import org.apache.spark.streaming.kafka010.CanCommitOffsets;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.ConsumerStrategy;
import org.apache.spark.streaming.kafka010.HasOffsetRanges;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.apache.spark.streaming.kafka010.LocationStrategy;
import org.apache.spark.streaming.kafka010.OffsetRange;

/*
import org.apache.spark.streaming.kafka010.CanCommitOffsets;
import org.apache.spark.streaming.kafka010.HasOffsetRanges;
import org.apache.spark.streaming.kafka010.OffsetRange;
*/
import scala.Tuple2;


/**
 * 
 * AGGIUNGO il recupero degli offset da kafka e provo a eseguire la commit;
 * 
 * 
 * -- SALVATAGGIO DATART (esclusi RECORD E) --
 * riceve RDD di json e li converte in RDD di oggetti ZBRecord (quelli usati dal udpServer)
 * Salva sul db i messaggi DATART (esclusi RecordType E : eventi che sono gestiti da Kafka2SparkRecordEConsumer)
 * 
 * 
 * spark-submit --class "it.acconsulting.sparkconsumer.Kafka2SparkDBConsumer" --master local[4]   C:\PROGETTI\NetBeans\SparkConsumer2.1\target\SparkConsumer-2.1-jar-with-dependencies.jar  localhost:9092 my-topic
 * 
 * --conf "spark.executor.extraJavaOptions=-Dlog4j.configuration=C:\PROGETTI\NetBeans\spark-1.6.1\conf\log4j.properties -Dsparkconsumer.pathfile=C:\PROGETTI\NetBeans\SparkConsumer2.1\src\main\resources\sparkconsumer.properties"
 */
public class Kafka2SparkDBConsumer2 {
    private static Logger logger =  Logger.getLogger("GATEWAY");        
    
    public static void main(String[] args) {
        
        int versione = 18;
        java.util.Date date= new java.util.Date();
	Timestamp timestamp = new Timestamp(date.getTime());
        
        logger.info("["+timestamp+"]Starting Kafka2SparkDBConsumer....."+versione);
        
        

        if (args.length < 2) {
          System.err.println("Usage: Kafka2SparkDBConsumer <brokers> <topics>\n" +
              "  <brokers> is a list of one or more Kafka brokers\n" +
              "  <topics> is a list of one or more kafka topics to consume from\n\n");
          System.exit(1);
        }

        //StreamingExamples.setStreamingLogLevels();

        String brokers = args[0];
        String topics = args[1];

        // Create context with a 2 seconds batch interval
        SparkConf sparkConf = new SparkConf().setAppName("Kafka2SparkDBConsumer"+versione).set("spark.cores.max", "55");//.set("spark.streaming.kafka.maxRatePerPartition", "1000");//set("spark.executor.cores", "2");
          
        JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, Durations.seconds(1));

        HashSet<String> topicsSet = new HashSet<String>(Arrays.asList(topics.split(",")));
        HashMap<String, Object> kafkaParams = new HashMap<String, Object>();
        kafkaParams.put("metadata.broker.list", brokers);
        kafkaParams.put("bootstrap.servers", brokers);
        
        kafkaParams.put("group.id", "acgroup");
        // this is required because of in order for burrow to get the offsets 
        kafkaParams.put("offsets.storage", "kafka");
        kafkaParams.put("dual.commit.enabled", "false");
        kafkaParams.put("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        kafkaParams.put("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        //kafkaParams.put("partition.assignment.strategy", "range");

        ArrayList<String> topicArray = new ArrayList<String>();
        topicArray.add(topics);
        
        /*
        1) Upgrade your brokers and set dual.commit.enabled=false and offsets.storage=zookeeper (Commit offsets to Zookeeper Only).
2) Set dual.commit.enabled=true and offsets.storage=kafka and restart (Commit offsets to Zookeeper and Kafka).
3) Set dual.commit.enabled=false and offsets.storage=kafka and restart (Commit offsets to Kafka only).
        */

        LocationStrategy ls = LocationStrategies.PreferConsistent(); 
        ConsumerStrategy cs =ConsumerStrategies.Subscribe(topicArray,kafkaParams);
        //--------------------------------------------------------------------------
        // Create direct kafka stream with brokers and topics
        //--------------------------------------------------------------------------    
          JavaInputDStream<ConsumerRecord> messages = KafkaUtils.createDirectStream(jssc, ls, cs);
                  
                  
                  
       /*     jssc,
            String.class,
            String.class,
            StringDecoder.class,
            StringDecoder.class,
            kafkaParams,
            topicsSet
        );
*/          
        //----------------------------------------------------------------------
        // OFFSET
        //----------------------------------------------------------------------
        final AtomicReference<OffsetRange[]> ranges = new AtomicReference<>();
       
        messages.foreachRDD(
        new VoidFunction<JavaRDD<ConsumerRecord>>() {
        @Override
        public void call(JavaRDD< ConsumerRecord> rdd) throws IOException
        {//OffsetRange[] offsetRanges = ((HasOffsetRanges)rdd).offsetRanges(); // offsetRanges.length = # of Kafka partitions being consumed ... 
            
            if (rdd.rdd() instanceof HasOffsetRanges){
                OffsetRange[] offsetRanges = ((HasOffsetRanges) rdd.rdd()).offsetRanges();
                if (offsetRanges != null){
                    int dime = offsetRanges.length;

                    for(int i=0; i<dime; i++){
                        long from = offsetRanges[i].fromOffset();
                        long until = offsetRanges[i].untilOffset();
                        String topic = offsetRanges[i].topic();
                        int partition = offsetRanges[i].partition();
                        logger.info(topic+">>"+partition+">>>"+from+"<<<>>>>"+until+"<<<<");
                    }

                    ranges.set(offsetRanges);
                }else{
                    logger.error("KO - Recupero Offset");
                }
            }
            
  
             }
        });
        

        
        if (messages.dstream() instanceof CanCommitOffsets){
           // logger.info("------------OFFSET--------------------");
            CanCommitOffsets cco = (CanCommitOffsets)  (messages.dstream()); 
            if (  (ranges!=null)&&(ranges.get()!=null)  )
                cco.commitAsync(ranges.get());
        }


        //------------------------------
        // LOG di TEST
        // Informazioni dalla coda Kafka
        //------------------------------
        //displayTestOffsetKafka(messages);
        
        //--------------------------------------------------------------------------
        // Create Dstream di Stringhe
        //--------------------------------------------------------------------------
      /*  JavaDStream<String> lines = messages.map(new Function<Tuple2<String, String>, String>() {
            @Override
            public String call(Tuple2<String, String> tuple2) {
              return tuple2._2();
        }
        });
*/
        //per test : visualizzo il DStream di Stringhe (in formato jSon)
        //lines.print();

        //--------------------------------------------------------------------------
        //Solo per test
        // Stampa le stringhe ricevute: (sono in formato Json)
        //--------------------------------------------------------------------------
    /*    lines.foreachRDD(new VoidFunction<JavaRDD<String>>() {
             @Override
             public void call(JavaRDD<String> item) throws Exception {

                 item.
                 foreachPartition(new VoidFunction<Iterator<String>>() {
                     @Override
                     public void call(Iterator<String> items) throws Exception {                  
                         int i=1;
                          while ( items.hasNext() ) {
                               String record = items.next();
                               logger.info(i+") "+record);
                               i++;
                          }
                     }
                 });
             }//fine metodoCall
         });//fine metodoforeachRDD
    */   


        //--------------------------------------------------------------------------
        // Trasformo il Dstream di stringhe (Json)
        // in un Dstream di Oggetti Java ZBRecords
        //--------------------------------------------------------------------------
        JavaDStream<ZBRecord> lines2 = messages.map(new Function<ConsumerRecord, ZBRecord>() {
          @Override
          public ZBRecord call(ConsumerRecord tuple2) {

              Gson gson = new Gson();
              ZBRecord sd = gson.fromJson((String)tuple2.value(), ZBRecord.class);
              if (sd==null)
                logger.debug(">>>>>>>NULL"+tuple2);
            return sd;
          }
        });

        //--------------------------------------------------------------------------
        //devo filtrare gli eventi e prendere tutti tranne di tipo E (che vengono elaborati da Kafka2SparkrecordEConsumer)
        // oppure considerare anche tipo E con IDType <> 0,1,2 ( in modo da far elaborare solo E di tipo 0,1,2 a Kafka2SparkrecordEConsumer)
        //--------------------------------------------------------------------------
        JavaDStream<ZBRecord> filteredline = lines2.filter(new Function<ZBRecord,Boolean>() {
            @Override
            public Boolean call(ZBRecord zbrecord) throws Exception {
                //if (zbrecord==null) return false;
                //if (zbrecord.getRecordType()==null) return false;
                if (!zbrecord.getRecordType().equals(ZBRecord.RecordTypes.RecordE))
                    return true;
                else return false;
            }

        });
        //lines2.print();



        //logger.info("Starting RDD Foreach Elaboration.........");


        filteredline.foreachRDD(new VoidFunction<JavaRDD<ZBRecord>>() {

             @Override
            public void call(JavaRDD<ZBRecord> item) throws Exception {

                item.foreachPartition(new VoidFunction<Iterator<ZBRecord>>() {

                    //--------------------------------------------------------------
                    //0)Carico Properties 
                    //--------------------------------------------------------------


                    //--------------------------------------------------------------
                    //1)Creo il pool di Connessioni jdbc
                    //--------------------------------------------------------------
                    DBManager pool = DBManager.INSTANCE;

                    @Override
                    public void call(Iterator<ZBRecord> items) throws Exception {

                        ZBRecordsDAO dao = null;
                        Connection conn = null;
                        
                        try{
                            if ((items!=null)&&(items.hasNext())){
                                //------------------------------------------------------
                                //2)Prendo una connessione
                                //------------------------------------------------------
                                conn = pool.getConnection();

                                dao = new ZBRecordsDAO();
                                int counter = 0;
                                Hashtable update = new Hashtable();
                                while ( items.hasNext() ) {

                                    ZBRecord record = items.next();

                                    if (record!=null){
                                        long idLocalization = dao.InsertZBRecord(conn,record,0); 
                                        if (idLocalization!=0)
                                        update.put(record.IDBlackBox,idLocalization);
                                    }

                                    counter++;
                                }

                                dao.commit(conn);

                                if(update!=null){
                                    logger.debug("Size "+update.size() +" da update....");

                                    Enumeration<Long> key = update.keys();
                                    if (key!=null)
                                        while (key.hasMoreElements()){
                                            Long k = key.nextElement();
                                            Long value = (Long)update.get(k);
                                            dao.executeQueryWithRetry(conn,value,k,0);
                                        }
                                    dao.commit(conn);
                                }
                            

                            }
                             
                         }catch(Exception e){
                             logger.error(">A>Error saving ZbRecords :"+e.toString(),e);
                             logger.error("Stack:"+Utils.getStackTrace(e));
                             //dao.rollback(); 

                             //---------------------------------------------
                             //TODO: HANDLE ERROR : salvare sul filesystem??
                             // 1) se il db è giù 
                             //----------------------------------------------
                         }finally{
                             //-------------------------------------------
                             //3)Rilascio la connessione
                             //-------------------------------------------
                             try{
                                if (dao!=null)
                                    dao.close(conn);
                             }catch(SQLException e){
                                 logger.error("Error Closing Connection :"+e.toString(),e);
                             }
                         }
                     }

                 });

             }//fine metodoCall


         });//fine metodoforeachRDD


        jssc.start();
        try {
            jssc.awaitTermination();
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(Kafka2SparkDBConsumer2.class.getName()).log(Level.SEVERE, null, ex);
        }
      
      
    }
    
    
    //-----------------------------------------
    // Recupera informazioni dalla coda Kafka
    //  Topic, Partition, Offset
    //-----------------------------------------
    /*public static void  displayTestOffsetKafka(JavaPairInputDStream<String, String> messages){
        messages.foreachRDD(
        new Function<JavaPairRDD<String,String>, Void>() {
        @Override
        public Void call(JavaPairRDD< String, String> rdd) throws IOException
        {//OffsetRange[] offsetRanges = ((HasOffsetRanges)rdd).offsetRanges(); // offsetRanges.length = # of Kafka partitions being consumed ... 
            OffsetRange[] offsetRanges = ((HasOffsetRanges) rdd.rdd()).offsetRanges();
            if (offsetRanges != null){
                int dime = offsetRanges.length;
                //logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>OK!!!");
                for(int i=0; i<dime; i++){
                    long from = offsetRanges[i].fromOffset();
                    long until = offsetRanges[i].untilOffset();
                    String topic = offsetRanges[i].topic();
                    int partition = offsetRanges[i].partition();
                    logger.debug("KAFKA - Topic:"+topic+" - Partition:"+partition+" - OFFSET from:"+from+" to:"+until+" ");
                }
            }else{
                //logger.info("KO<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            }
          
        return null; }
        });

    }*/
    
    
}
