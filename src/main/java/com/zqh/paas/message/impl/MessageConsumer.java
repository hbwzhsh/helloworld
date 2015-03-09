package com.zqh.paas.message.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.serializer.StringDecoder;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.zqh.paas.PaasException;
import com.zqh.paas.config.ConfigurationCenter;
import com.zqh.paas.config.ConfigurationWatcher;
import com.zqh.paas.message.IMessageListener;
import com.zqh.paas.message.Message;

public class MessageConsumer implements ConfigurationWatcher {
	public static final Logger log = Logger.getLogger(MessageConsumer.class);

	private String confPath = "/com/zqh/paas/message/messageConsumer";
	private static final String Msg_Processor_Num = "msg.processor.num";

	private ConfigurationCenter confCenter = null;

	private IMessageListener listener = null;

	private ConsumerConnector consumer = null;
	private ExecutorService executor = null;
	private ArrayList<String> monitorTopicList = null;
	private String groupId = null;

	private int nThreads = 1;
	private Properties props = null;

	public MessageConsumer() {
		if (log.isInfoEnabled()) {
			log.info("starting MessageConsumer...");
		}
	}

	public void init() {
		if (log.isInfoEnabled()) {
			log.info("init MessageConsumer...");
		}
		try {
			process(confCenter.getConfAndWatch(confPath, this));
		} catch (PaasException e) {
			log.error("",e);
		}
	}

	@SuppressWarnings("rawtypes")
	public void process(String conf) {
		if (log.isInfoEnabled()) {
			log.info("new MessageConsumer configuration is received: " + conf);
		}

		JSONObject json = JSONObject.fromObject(conf);
		Iterator keys = json.keySet().iterator();
		boolean threadNumChanged = false;
		boolean changed = false;
		if (props == null) {
			props = new Properties();
			changed = true;
		}
		props.put("group.id", groupId);
		if (keys != null) {
			String key = null;
			while (keys.hasNext()) {
				key = (String) keys.next();
				if (Msg_Processor_Num.equals(key)) {
					nThreads = json.getInt(key);
				} else {
					if (props.containsKey(key)) {
						if (props.get(key) == null || !props.get(key).equals(json.getString(key))) {
							props.put(key, json.getString(key));
							changed = true;
						}
					} else {
						props.put(key, json.getString(key));
						changed = true;
					}
				}
			}
		}
		if (changed || threadNumChanged) {
			stopConsumeMessage(executor, consumer);
			ConsumerConfig cfg = new ConsumerConfig(props);
			consumer = Consumer.createJavaConsumerConnector(cfg);
			startConsumeMessage();
		}
	}

	public void startConsumeMessage() {
		if (log.isInfoEnabled()) {
			log.info("start consume message...");
		}
		if (monitorTopicList == null || monitorTopicList.size() == 0) {
			return;
		}
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (String monitorTopic : monitorTopicList) {
			map.put(monitorTopic, nThreads);
		}
		Map<String, List<KafkaStream<String, Message>>> topicMessageStreams = consumer
				.createMessageStreams(map, new StringDecoder(null), new MessageDecoder(null));

		executor = Executors.newFixedThreadPool(nThreads * monitorTopicList.size());
		int i = 0;
		for (String monitorTopic : monitorTopicList) {
			List<KafkaStream<String, Message>> streams = topicMessageStreams.get(monitorTopic);
			for (final KafkaStream<String, Message> stream : streams) {
				executor.execute(new MessageProcessor(i, stream, listener));
				i++;
			}
		}
	}

	public void stopConsumeMessage(ExecutorService oldExecutor, ConsumerConnector oldConsumer) {
		if (log.isInfoEnabled()) {
			log.info("stop consume message...");
		}
		if (oldConsumer != null) {
			if (log.isInfoEnabled()) {
				log.info("old consumer is closed: " + oldConsumer);
			}
			oldConsumer.shutdown();
		}
		if (oldExecutor != null) {
			if (log.isInfoEnabled()) {
				log.info("begin to close old executor: " + oldExecutor);
			}
			oldExecutor.shutdown();
			try {
				while (!oldExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
					if (log.isInfoEnabled()) {
						log.info("old executor is not closed: " + oldExecutor);
					}
				}
			} catch (InterruptedException e) {
				log.error("",e);
			}
			if (log.isInfoEnabled()) {
				log.info("old executor is closed: " + oldExecutor);
			}
		}
	}

	public String getConfPath() {
		return confPath;
	}

	public void setConfPath(String confPath) {
		this.confPath = confPath;
	}

	public ConfigurationCenter getConfCenter() {
		return confCenter;
	}

	public void setConfCenter(ConfigurationCenter confCenter) {
		this.confCenter = confCenter;
	}

	public IMessageListener getListener() {
		return listener;
	}

	public void setListener(IMessageListener listener) {
		this.listener = listener;
	}

	public ArrayList<String> getMonitorTopicList() {
		return monitorTopicList;
	}

	public void setMonitorTopicList(ArrayList<String> monitorTopicList) {
		this.monitorTopicList = monitorTopicList;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
}
