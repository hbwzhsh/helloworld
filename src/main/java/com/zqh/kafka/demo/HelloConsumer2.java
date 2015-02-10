package com.zqh.kafka.demo;

import kafka.api.FetchRequest;
import kafka.api.FetchRequestBuilder;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.common.TopicAndPartition;
import kafka.javaapi.*;
import kafka.cluster.Broker;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.message.MessageAndOffset;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * Created by hadoop on 14-11-26.
 */
public class HelloConsumer2 {

    public static void main(String[] args) throws Exception{
        String topic = "test2";
        int partition = 1;
        //我们无需要把所有的brokers列表加进去，目的只是为了获得metedata信息，故只要有broker可连接即可
        String brokers = "localhost:9092";
        int maxReads = 100; // 读多少条数据
        // 1.找leader
        PartitionMetadata metadata = null;
        for (String ipPort : brokers.split(",")) {
            SimpleConsumer consumer = null;
            try {
                String[] ipPortArray = ipPort.split(":");
                consumer = new SimpleConsumer(ipPortArray[0], Integer.parseInt(ipPortArray[1]), 100000, 64 * 1024, "leaderLookup");
                List<String> topics = new ArrayList<String>();
                topics.add(topic);
                TopicMetadataRequest req = new TopicMetadataRequest(topics);
                // 取meta信息
                TopicMetadataResponse resp = consumer.send(req);

                //获取topic的所有metedate信息(目测只有一个metedata信息，何来多个？)
                List<TopicMetadata> metaData = resp.topicsMetadata();
                for (TopicMetadata item : metaData) {
                    for (PartitionMetadata part : item.partitionsMetadata()) {
                        //获取每个meta信息的分区信息,这里我们只取我们关心的partition的metedata
                        System.out.println("----"+part.partitionId());
                        if (part.partitionId() == partition) {
                            metadata = part;
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error communicating with Broker [" + ipPort
                        + "] to find Leader for [" + topic + ", " + partition
                        + "] Reason: " + e);
            } finally {
                if (consumer != null)
                    consumer.close();
            }
        }
        if (metadata == null || metadata.leader() == null) {
            System.out.println("meta data or leader not found, exit.");
            return;
        }
        // 拿到leader
        Broker leadBroker = metadata.leader();
        // 获取所有副本
        System.out.println(metadata.replicas());

        // 2.获取lastOffset(这里提供了两种方式：从头取或从最后拿到的开始取，下面这个是从头取)
        long whichTime = kafka.api.OffsetRequest.EarliestTime();
        //这个是从最后拿到的开始取
        //long whichTime = kafka.api.OffsetRequest.LatestTime();
        System.out.println("lastTime:"+whichTime);
        String clientName = "Client_" + topic + "_" + partition;
        SimpleConsumer consumer = new SimpleConsumer(leadBroker.host(), leadBroker.port(), 100000, 64 * 1024, clientName);
        TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);
        Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap<TopicAndPartition, PartitionOffsetRequestInfo>();
        requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(whichTime, 1));
        OffsetRequest request = new OffsetRequest(requestInfo,kafka.api.OffsetRequest.CurrentVersion(), clientName);
        // 获取指定时间前有效的offset列表
        OffsetResponse response = consumer.getOffsetsBefore(request);
        if (response.hasError()) {
            System.out.println("Error fetching data Offset Data the Broker. Reason: "
                            + response.errorCode(topic, partition));
            return;
        }
        // 千万不要认为offset一定是从0开始的
        long[] offsets = response.offsets(topic, partition);
        System.out.println("offset list:" + Arrays.toString(offsets));
        long offset = offsets[0];

        // 读数据
        while (maxReads > 0) {
            // 注意不要调用里面的replicaId()方法，这是内部使用的。
            FetchRequest req = new FetchRequestBuilder().clientId(clientName)
                    .addFetch(topic, partition, offset, 100000).build();
            FetchResponse fetchResponse = consumer.fetch(req);
            if (fetchResponse.hasError()) {
                // 出错处理。这里只直接返回了。实际上可以根据出错的类型进行判断，如code == ErrorMapping.OffsetOutOfRangeCode()表示拿到的offset错误
                // 一般出错处理可以再次拿offset,或重新找leader，重新建立consumer。可以将上面的操作都封装成方法。再在该循环来进行消费
                // 当然，在取所有leader的同时可以用metadata.replicas()更新最新的节点信息。另外zookeeper可能不会立即检测到有节点挂掉，故如果发现老的leader和新的leader一样，可能是leader根本没挂，也可能是zookeeper还没检测到，总之需要等等。
                short code = fetchResponse.errorCode(topic, partition);
                System.out.println("Error fetching data from the Broker:" + leadBroker + " Reason: " + code);
                return;
            }
            //取一批消息
            boolean empty = true;
            for (MessageAndOffset messageAndOffset : fetchResponse.messageSet(topic, partition)) {
                empty = false;
                long curOffset = messageAndOffset.offset();
                //下面这个检测有必要，因为当消息是压缩的时候，通过fetch获取到的是一个整块数据。块中解压后不一定第一个消息就是offset所指定的。就是说存在再次取到已读过的消息。
                if (curOffset < offset) {
                    System.out.println("Found an old offset: " + curOffset + " Expecting: " + offset);
                    continue;
                }
                // 可以通过当前消息知道下一条消息的offset是多少
                offset = messageAndOffset.nextOffset();
                ByteBuffer payload = messageAndOffset.message().payload();
                byte[] bytes = new byte[payload.limit()];
                payload.get(bytes);
                System.out.println(String.valueOf(messageAndOffset.offset()) + ": " + new String(bytes, "UTF-8"));
                maxReads++;
            }
            //进入循环中，等待一会后获取下一批数据
            if(empty){
                Thread.sleep(1000);
            }
        }
        // 退出（这里象征性的写一下）
        if (consumer != null)
            consumer.close();
    }
}
