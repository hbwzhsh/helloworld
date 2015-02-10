package com.zqh.kafka.quickstart;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.util.Date;
import java.util.Properties;
import java.util.Random;

public class TestProducer2 {
    public static void main(String[] args) {
        long events = 10;
        Random rnd = new Random();

        // First define properties for how the Producer finds the cluster,
        // serializes the messages and if appropriate directs the message to a specific Partition.
        Properties props = new Properties();
        // 单机多服务模拟集群
        props.put("metadata.broker.list", "localhost:9092,localhost:9093,localhost:9094");
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("partitioner.class", "com.zqh.kafka.quickstart.SimplePartitioner");
        props.put("request.required.acks", "1");

        ProducerConfig config = new ProducerConfig(props);

        // Next you define the Producer object itself
        // The first is the type of the Partition key, the second the type of the message
        Producer<String, String> producer = new Producer<String, String>(config);

        for (long nEvents = 0; nEvents < events; nEvents++) {
            // Now build your message
            long runtime = new Date().getTime();
            String ip = "192.168.2." + rnd.nextInt(255);
            String msg = runtime + ",www.example.com," + ip;

            // Finally write the message to the Broker
            // passing the IP as the partition key
            KeyedMessage<String, String> data = new KeyedMessage<String, String>("my-replicated-topic", ip, msg);
            producer.send(data);
        }
        producer.close();
    }
}