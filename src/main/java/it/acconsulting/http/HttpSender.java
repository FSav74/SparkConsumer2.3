/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.acconsulting.http;

import it.acconsulting.bean.EventRecord;
import it.acconsulting.bean.ZBox;
import it.acconsulting.conf.ConfigurationException;
import it.acconsulting.conf.ConfigurationProperty;
import it.acconsulting.util.Utils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

/**
 *
 * @author Admin
 */
public class HttpSender {
    
    private static Logger logger =  Logger.getLogger("GATEWAY");    
    
    public int SendEventDMS(EventRecord Ev, ZBox Zb, CloseableHttpClient conn)
//    public int SendEventDMS(long event_id, String event_date, String tipology, String event_size, 
//            String car_plate, String device_sn, String device_tel, String name_surname, String insurance_number, 
//            String isa_event_lat, String isa_event_long)
    {
        int RetValue = 1; 
        
        try {
            SimpleDateFormat FullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 
            FullDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));        // dati forniti in UTC???
            
            double BLat=Ev.Lat;
            double BLong=Ev.Long;
            String Lat=String.format(Locale.ENGLISH, "%.4f", BLat);
            String Long=String.format(Locale.ENGLISH, "%.4f", BLong);
            
            int Acc = (Utils.uBToI(Ev.EventInfo[3]) << 8) + Utils.uBToI(Ev.EventInfo[2]);
            if (Acc > 0xF000) {
                Acc = -(0xFFFF - Acc);
            }
            float AccTot = ((float) Acc) / 1000;

            String input = GenStringDMS(Ev.IDZBEvents, FullDateFormat.format(Ev.Tempo), ""+Ev.type, ""+AccTot, 
                    Zb.Targa, Utils.toHexString(Zb.SerialN),Zb.NumTel.replace("+","00"), Zb.D.Nome+" "+Zb.D.Cognome, Zb.V.VoucherID, 
                    Lat, Long);
            
            
           /* String FullUrl;
            FullUrl = url+"/"+input;
            FullUrl = FullUrl.replaceAll(" ", "%20");


            URL url1 = new URL(FullUrl);

            System.out.println(url+"/"+input);*/

            //HttpURLConnection conn = (HttpURLConnection) url1.openConnection();

            //conn.setRequestMethod("POST");
  /*          conn.setRequestProperty("Content-Type", "application/json");
//            conn.setRequestProperty("X-API-KEY", "4741EAB8B98658B9F253199BE9BBA");
            
            conn.setRequestProperty("X-API-KEY", API_KEY);

            if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                    throw new RuntimeException("Failed : HTTP error code : "
                            + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                            (conn.getInputStream())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
                if (output.contains("\"message\":\"OK\"")) {
                    RetValue=0;
                    break;
                }
            }

            conn.disconnect();*/
            
            //----------------------------------------------------
            
            
            HttpClientContext context = HttpClientContext.create();
            //HttpGet httpget = new HttpGet("http://www.google.com/search?hl=en&q=httpclient&btnG=Google+Search&aq=f&oq=");
            //HttpGet httpget = new HttpGet("127.0.0.1");
            String host = null;
            String port = null;
            String link = null;
            String apiKey = null;
            
            try{
                host = ConfigurationProperty.ISTANCE.getProperty(ConfigurationProperty.HTTP_DESTINATION_HOST);
                port = ConfigurationProperty.ISTANCE.getProperty(ConfigurationProperty.HTTP_DESTINATION_PORT);
                link = ConfigurationProperty.ISTANCE.getProperty(ConfigurationProperty.HTTP_DESTINATION_LINK);
                apiKey = ConfigurationProperty.ISTANCE.getProperty(ConfigurationProperty.API_KEY);
                
                
            }catch(ConfigurationException c){
                logger.error("Error retrieving http configuration!", c);
            }
            
            HttpPost httpPost = new HttpPost("http://"+host+":"+port+"/"+link);
            
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("X-API-KEY", apiKey));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
            

            //StringEntity entity = new StringEntity(input, ContentType.create("text/plain", "UTF-8"));
            StringEntity entity = new StringEntity(input, ContentType.create("application/json", "UTF-8"));
            
            
            httpPost.setEntity(entity);
     
            CloseableHttpResponse response = conn.execute( httpPost , context);
            HttpEntity myEntity = response.getEntity();
            logger.debug("Content type:"+myEntity.getContentType());
            logger.debug("Content lenght:"+myEntity.getContentLength());
            logger.debug("BODY:"+getStringFromInputStream(myEntity.getContent()));

            //System.out.println(EntityUtils.toByteArray(myEntity).length);

            response.close();
            
            
            
            
            
            
            
            
        } catch (MalformedURLException e) {
              e.printStackTrace();
              RetValue=2;
        } catch (IOException e) {
              e.printStackTrace();
              RetValue=3;
        } catch (Exception e ) {
              e.printStackTrace();
              RetValue=4;
        }
        return RetValue;
    
    }

    public static String GenStringDMS(long event_id, String event_date, String tipology, String event_size, 
            String car_plate, String device_sn, String device_tel, String name_surname, String insurance_number, 
            String isa_event_lat, String isa_event_long) {
        
        String input = "posteventsdevice/"+event_id+"/"+event_date+"/"+tipology+"/"+event_size
                +"/"+car_plate+"/"+device_sn+"/"+device_tel+"/"+name_surname+"/"+insurance_number
                +"/"+isa_event_lat+"/"+isa_event_long;
        
        return input;

    }
    private static boolean getStringFromInputStream(InputStream is) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
                boolean result = false;
		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
                                if (line.contains("\"message\":\"OK\"")) 
                                    result = true;
			}
                        logger.debug("BODY:"+sb.toString());

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
                
                return result;
    }
}
