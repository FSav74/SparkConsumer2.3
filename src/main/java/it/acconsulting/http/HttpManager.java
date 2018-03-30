/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.acconsulting.http;

import it.acconsulting.conf.ConfigurationException;
import it.acconsulting.conf.ConfigurationProperty;
import java.io.Serializable;
import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.log4j.Logger;

/**
 *
 * @author Admin
 */
public enum HttpManager implements Serializable{
    
    INSTANCE;
    
    private PoolingHttpClientConnectionManager cm = null;
    
    private Logger logger =  Logger.getLogger("GATEWAY");    
    
    private HttpManager(){
        
        logger.debug("HttpManager constructor............");
        int maxTot = 0;
        int defaultMaxPerRoute = 0;
        int maxPerRoute = 0;
        
        String httpDestinationHost = null;
        int httpDestinationPort = 0;
        int httpDestinationMaxConn = 0;
        String httpDestinationLink = null;


         try {
            
            String maxTotS = ConfigurationProperty.ISTANCE.getProperty(ConfigurationProperty.HTTP_MAX_CONN_TOT);
            String defaultMaxPerRouteS = ConfigurationProperty.ISTANCE.getProperty(ConfigurationProperty.HTTP_DEFAULT_MAX_CONN_ROUTE);
            String maxPerRouteS = ConfigurationProperty.ISTANCE.getProperty(ConfigurationProperty.HTTP_MAX_CONN_ROUTE);
            
            httpDestinationHost = ConfigurationProperty.ISTANCE.getProperty(ConfigurationProperty.HTTP_DESTINATION_HOST);
            String httpDestinationPortS = ConfigurationProperty.ISTANCE.getProperty(ConfigurationProperty.HTTP_DESTINATION_PORT);
            String httpDestinationMaxConnS = ConfigurationProperty.ISTANCE.getProperty(ConfigurationProperty.HTTP_DESTINATION_MAX_CONN);
            httpDestinationLink = ConfigurationProperty.ISTANCE.getProperty(ConfigurationProperty.HTTP_DESTINATION_LINK);
            
            maxTot = Integer.parseInt(maxTotS);
            defaultMaxPerRoute = Integer.parseInt(defaultMaxPerRouteS);
            maxPerRoute = Integer.parseInt(maxPerRouteS);
            
            httpDestinationPort = Integer.parseInt(httpDestinationPortS);
            httpDestinationMaxConn = Integer.parseInt(httpDestinationMaxConnS);
            
        }catch(ConfigurationException c){
            logger.error("Error retrieving properties for HTTP Manager!",c);
            throw new RuntimeException("Error retrieving properties for HTTP Pool!",c); 
        }catch(NumberFormatException n){
            logger.error("Error retrieving numeric properties for HTTP Pool!",n);
             throw new RuntimeException("Error retrieving properties for Http Pool!",n); 
        }
        
        cm = new PoolingHttpClientConnectionManager();
        // Increase max total connection to 200
        cm.setMaxTotal(maxTot);
        // Increase default max connection per route to 20
        cm.setDefaultMaxPerRoute(defaultMaxPerRoute);
        // Increase max connections for localhost:80 to 50
        HttpHost localhost = new HttpHost(httpDestinationHost, httpDestinationPort);
        cm.setMaxPerRoute(new HttpRoute(localhost), httpDestinationMaxConn);
        
        
    }
    
    public CloseableHttpClient getConnection(){
        
       
        
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();
         logger.debug("STATS HTTP POOL:" + cm.getTotalStats() );
        return httpClient;
    }
    
}
