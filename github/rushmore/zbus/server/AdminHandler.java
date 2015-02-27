package rushmore.zbus.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import rushmore.zbus.common.BrokerInfo;
import rushmore.zbus.common.Helper;
import rushmore.zbus.common.MessageMode;
import rushmore.zbus.common.MqInfo;
import rushmore.zbus.common.Proto;
import rushmore.zbus.common.json.JSON;
import rushmore.zbus.common.json.JSONObject;
import rushmore.zbus.common.logging.Logger;
import rushmore.zbus.common.logging.LoggerFactory;
import rushmore.zbus.remoting.Message;
import rushmore.zbus.remoting.MessageHandler;
import rushmore.zbus.remoting.RemotingClient;
import rushmore.zbus.remoting.callback.ErrorCallback;
import rushmore.zbus.remoting.nio.Session;
import rushmore.zbus.server.mq.MessageQueue;
import rushmore.zbus.server.mq.PubsubQueue;
import rushmore.zbus.server.mq.ReplyHelper;
import rushmore.zbus.server.mq.RequestQueue;
import rushmore.zbus.server.mq.store.MessageStore;
import rushmore.zbus.common.BrokerInfo;
import rushmore.zbus.common.Helper;
import rushmore.zbus.common.MessageMode;
import rushmore.zbus.common.Proto;
import rushmore.zbus.common.json.JSON;
import rushmore.zbus.common.json.JSONObject;
import rushmore.zbus.common.logging.Logger;
import rushmore.zbus.common.logging.LoggerFactory;
import rushmore.zbus.remoting.Message;
import rushmore.zbus.remoting.MessageHandler;
import rushmore.zbus.remoting.RemotingClient;
import rushmore.zbus.remoting.callback.ErrorCallback;
import rushmore.zbus.remoting.nio.Session;
import rushmore.zbus.server.mq.PubsubQueue;
import rushmore.zbus.server.mq.ReplyHelper;
import rushmore.zbus.server.mq.RequestQueue;
import rushmore.zbus.server.mq.store.MessageStore;

public class AdminHandler extends SubCommandHandler {
	private static final Logger log = LoggerFactory.getLogger(AdminHandler.class);
	protected long trackDelay = 1000;
	protected long trackInterval = 3000;
	
	protected final ScheduledExecutorService trackReportService = Executors.newSingleThreadScheduledExecutor();
	protected final List<RemotingClient> trackClients = new ArrayList<RemotingClient>();
	protected ExecutorService reportExecutor = new ThreadPoolExecutor(4,16, 120, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	
	protected final ConcurrentMap<String, MessageQueue> mqTable;
	protected final ExecutorService mqExecutor;
	protected final String serverAddr;
	
	protected MessageStore messageStore = null;
	
	public AdminHandler(ConcurrentMap<String, MessageQueue> mqTable, ExecutorService mqExecutor, String serverAddr){
		this.mqTable = mqTable;
		this.mqExecutor = mqExecutor;
		this.serverAddr = serverAddr;
		this.initCommands();
	}
	
	public void initCommands(){

		this.registerHandler(Proto.AdminCreateMQ, new MessageHandler() {
			@Override
			public void handleMessage(Message msg, Session sess) throws IOException {
				JSONObject params = null;
				try{
					params = JSON.parseObject(msg.getBodyString());
				} catch(Exception e) {
					log.error(e.getMessage(), e);
					msg.setBody("register param json body invalid");
	    			ReplyHelper.reply400(msg, sess);
	        		return; 
				}
				
				
				String msgId= msg.getMsgId();
				String mqName = params.getString("mqName");
	    		String accessToken = params.getString("accessToken");
	    		String type = params.getString("mqMode");
	    		int mode = 0;
	    		try{
	    			mode = Integer.valueOf(type);
	    		} catch (Exception e){
	    			msg.setBody("mqMode invalid");
	    			ReplyHelper.reply400(msg, sess);
	        		return;  
	    		}
	    		
	    		
	    		if(mqName == null){
	    			msg.setBody("Missing mq_name filed");
	    			ReplyHelper.reply400(msg, sess);
	        		return;  
	    		} 
	    		
	    		MessageQueue mq = null;	
	    		synchronized (mqTable) {
	    			mq = mqTable.get(mqName);
	    			if(mq == null){ 
		    			if(MessageMode.isEnabled(mode, MessageMode.PubSub)){
		    				mq = new PubsubQueue(serverAddr, mqName, mqExecutor, mode);
		    			} else {//默认到消息队列
		    				mq = new RequestQueue(serverAddr, mqName, mqExecutor, mode);
		    				if(messageStore != null){
		    					messageStore.onMessageQueueCreated(mq);
		    				}
		    			} 
		    			mq.setMessageStore(messageStore);
		    			mq.setAccessToken(accessToken);
			    		mq.setCreator(sess.getRemoteAddress());
			    		mqTable.putIfAbsent(mqName, mq);
						log.info("MQ Created: %s", mq);
						ReplyHelper.reply200(msgId, sess); 
						
			    		reportToTrackServer();
			    		return;
		    		}
	    		}
	    		
	    		if(MessageMode.isEnabled(mode, MessageMode.MQ) && !(mq instanceof RequestQueue)){
    				msg.setBody("MsgQueue, type not matched");
	    			ReplyHelper.reply400(msg, sess);
	        		return;  
    			}
	    		if(MessageMode.isEnabled(mode, MessageMode.PubSub) && !(mq instanceof PubsubQueue)){
    				msg.setBody("Pubsub, type not matched");
	    			ReplyHelper.reply400(msg, sess);
	        		return;  
    			}
    			ReplyHelper.reply200(msgId, sess);  
			}
		}); 
		
		this.registerHandler("", new MessageHandler() { 
			@Override
			public void handleMessage(Message msg, Session sess) throws IOException {
				msg = new Message();
				msg.setStatus("200");
				msg.setHead("content-type","text/html");
				String body = Helper.loadFileContent("zbus.htm");
				msg.setBody(body); 
				sess.write(msg);  
			}
		});
		
		this.registerHandler("jquery", new MessageHandler() { 
			@Override
			public void handleMessage(Message msg, Session sess) throws IOException {
				msg = new Message();
				msg.setStatus("200");
				msg.setHead("content-type","application/javascript");
				String body = Helper.loadFileContent("jquery.js"); 
				msg.setBody(body); 
				sess.write(msg);  
			}
		});
		
		this.registerHandler("data", new MessageHandler() { 
			@Override
			public void handleMessage(Message msg, Session sess) throws IOException {
				msg = packServerInfo();
				msg.setStatus("200"); 
				msg.setHead("content-type", "application/json");
				sess.write(msg);  
			}
		});
	}
	
	private Message packServerInfo(){
		Map<String, MqInfo> table = new HashMap<String, MqInfo>();
   		for(Map.Entry<String, MessageQueue> e : this.mqTable.entrySet()){
   			table.put(e.getKey(), e.getValue().getMqInfo());
   		} 
		Message msg = new Message(); 
		BrokerInfo info = new BrokerInfo();
		info.setBroker(serverAddr);
		info.setMqTable(table);  
		msg.setBody(JSON.toJSONString(info));
		return msg;
	}
	
	public void setupTrackServer(String trackServerAddr){ 
		if(trackServerAddr == null) return;
		trackClients.clear();
		String[] serverAddrs = trackServerAddr.split("[;]");
		for(String addr : serverAddrs){
			addr = addr.trim();
			if( addr.isEmpty() ) continue;
			RemotingClient client = new RemotingClient(addr);
			client.onError(new ErrorCallback() {
				@Override
				public void onError(IOException e, Session sess) throws IOException {
					//ignore
				}
			});
			trackClients.add(client);
		} 
		
		this.trackReportService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() { 
				reportToTrackServer();
			}
		}, trackDelay, trackInterval, TimeUnit.MILLISECONDS);
	}
	
	private void reportToTrackServer(){
		reportExecutor.submit(new Runnable() { 
			@Override
			public void run() {
				Message msg = packServerInfo();
				msg.setCommand(Proto.TrackReport);
				for(RemotingClient client : trackClients){
					try {
						client.invokeAsync(msg, null);
					} catch (IOException e) {  
						//ignore
					}
				} 
			}
		}); 
	}
	
	
	public void close(){
		this.trackReportService.shutdown();
		for(RemotingClient client : this.trackClients){
			client.close();
		}   
	}

	public long getTrackDelay() {
		return trackDelay;
	}

	public void setTrackDelay(long trackDelay) {
		this.trackDelay = trackDelay;
	}

	public long getTrackInterval() {
		return trackInterval;
	}

	public void setTrackInterval(long trackInterval) {
		this.trackInterval = trackInterval;
	}

	public MessageStore getMessageStore() {
		return messageStore;
	}

	public void setMessageStore(MessageStore messageStore) {
		this.messageStore = messageStore;
	}
	
}