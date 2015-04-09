package org.jai.search.client.impl;

import javax.annotation.PostConstruct;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.jai.search.client.SearchClientService;
import org.jai.search.model.ElasticSearchReservedWords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

//@Service(value="searchClientService")
public class SearchClientServiceImpl //implements SearchClientService 
{
    private static final Logger logger = LoggerFactory.getLogger(SearchClientServiceImpl.class);
    
//    private String searchServerClusterNodes = "localhost:9300";
    private String searchServerClusterName = "jaidev";
    
    private Client client;

    public Client getClient()
    {
        if(client == null)
        {
            client = createClient();
        }
        
        return client;
    }
    
//    @PostConstruct
    protected Client createClient()
    {
        if(client == null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Creating client for Search!");
            }            
            //Try starting search client at context loading
            try
            {
                Settings settings = ImmutableSettings.settingsBuilder().put(ElasticSearchReservedWords.CLUSTER_NAME.getText(), searchServerClusterName).build();
                
                TransportClient transportClient = new TransportClient(settings);

                transportClient = transportClient.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
                
                if(transportClient.connectedNodes().size() == 0)
                {
                    logger.error("There are no active nodes available for the transport, it will be automatically added once nodes are live!");
                }
                client = transportClient;
            }
            catch(Exception ex)
            {
                //ignore any exception, dont want to stop context loading
                logger.error("Error occured while creating search client!", ex);
            }
        }
        return client;
    }
    
    public void addNewNode(String name)
    {
        TransportClient transportClient = (TransportClient) client;
        transportClient.addTransportAddress(new InetSocketTransportAddress(name, 9300));
    }
    
    public void removeNode(String name)
    {
        TransportClient transportClient = (TransportClient) client;
        transportClient.removeTransportAddress(new InetSocketTransportAddress(name, 9300));
    }
}
