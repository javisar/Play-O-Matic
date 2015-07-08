package com.keyes.youtube;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class GDriveUtil {

	public static String[][] getSpreadSheet(String docId, String tab) {
		try {
		    // Create a trust manager that does not validate certificate chains
		    final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
		        @Override
		        public void checkClientTrusted( final X509Certificate[] chain, final String authType ) {
		        }
		        @Override
		        public void checkServerTrusted( final X509Certificate[] chain, final String authType ) {
		        }
		        @Override
		        public X509Certificate[] getAcceptedIssuers() {
		            return null;
		        }
		    } };
		    
		    // Install the all-trusting trust manager
		    final SSLContext sslContext = SSLContext.getInstance( "SSL" );
		    sslContext.init( null, trustAllCerts, new java.security.SecureRandom() );
		    // Create an ssl socket factory with our all-trusting manager
		    final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
		    //sslSocketFactory.
		    
		    // All set up, we can get a resource through https now:
		   final URLConnection urlCon = new URL( "https://docs.google.com/spreadsheets/d/"+docId+"/export?format=csv&gid="+tab).openConnection();
		    // Tell the url connection object to use our socket factory which bypasses security checks
		    ( (HttpsURLConnection) urlCon ).setSSLSocketFactory( sslSocketFactory );
		    
		    final InputStream input = urlCon.getInputStream();
		    //int c;
		    //while ( ( c = input.read() ) != -1 ) {
		    //	System.out.print(c);
		    //}
		    //input.close();
		    BufferedReader r = new BufferedReader(new InputStreamReader(input));
		    StringBuilder total = new StringBuilder();
		    String line;
		    while ((line = r.readLine()) != null) {
		        total.append(line);
		        total.append("\n");
		    }
		    
		    //StringWriter writer = new StringWriter();
		    //IOUtils.copy(input, writer, "UTF-8");
		    String theString = total.toString();
		    
		    String[][] out = null;
		    
		    String rows[] = theString.split("\n");
		    out = new String[rows.length][];
		    for (int i=0; i<out.length; i++) {
		    	String columns[] = rows[i].split(",");
		    	out[i] = new String[columns.length];
		    	int corrected = 0;
		    	for (int j=0; j<columns.length; j++) {
		    		if (columns[j].length() > 0 && columns[j].charAt(0) == '"') {
		    			out[i][j-corrected] = (columns[j]+", "+columns[j+1]).replace("\"", "");
		    			j++;
		    			corrected += 1;
		    		}
		    		else {
		    			out[i][j-corrected] = columns[j];
		    		}
		    	}
		    }
		    return out;
		    //System.out.println(theString);
		    /*
		    Document doc = Jsoup.parse(theString);
		    Elements rows = doc.select(".grid-table-container table tr");
		    for (Element row : rows) {
		    	Elements columns = row.select("td");
		    	for (Element column : columns) {
		    		if (column.text().length() > 0) {
		    			System.out.println(column.text());
		    		}
		    	}
		    	System.out.println();
		    }
		    */
		    
		} catch ( final Exception e ) {
		    e.printStackTrace();
		}
		return null;
	}
}
