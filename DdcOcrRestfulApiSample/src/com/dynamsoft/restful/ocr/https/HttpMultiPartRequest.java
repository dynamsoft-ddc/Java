package com.dynamsoft.restful.ocr.https;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.net.ssl.HttpsURLConnection;

public class HttpMultiPartRequest {
	public static String StrBoundary = String.format("DdcOrcRestfulApiSample%s", UUID.randomUUID().toString().replace("-", ""));
	
	// post multi-part form data
    public static HttpWebResponse post(String strUrl, Map<String , String> dicHeader, FormData formData) throws Exception {
    	if(strUrl == null || strUrl.length() == 0) throw new Exception("Url is invalid.");
    	
    	byte[] bodyData = constructRequestBodyData(formData);
    	
    	// config http connection
    	URL url = new URL(strUrl);    	
    	HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
    	conn.setRequestMethod("POST");
    	conn.setConnectTimeout(1000 * 60 * 10);
    	conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + StrBoundary);
    	conn.setRequestProperty("Content-Length", "" + bodyData.length);   	
    	
    	if(dicHeader != null) {
    		for (Map.Entry<String, String> header : dicHeader.entrySet()) {
    			conn.setRequestProperty(header.getKey(), header.getValue());
    		}
    	}
    	
    	conn.setDoInput(true);
    	conn.setDoOutput(true);
    	
    	try(OutputStream os = conn.getOutputStream()){    		
    		os.write(bodyData);
    	}   	
    	
    	return constructResposneData(conn);
    }

	// construct request body data
    private static byte[] constructRequestBodyData(FormData formData) throws Exception {
    	if (formData == null || !formData.isValid()) return new byte[0];
    	
    	String strCharSet = "UTF-8";
    	
    	try(ByteArrayOutputStream os = new ByteArrayOutputStream())
    	{
    	    try(PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, strCharSet), true))
    	    {
    	    	Boolean bHasItemAdded = false;
                String strNewLine = "\r\n";
                String strBoundarySeparator = "--";
                
                for(SimpleEntry<String, SimpleEntry<Object, String>> formDataItem : formData.getAll()) {
                	if (bHasItemAdded) pw.print(strNewLine);
                	
                	String strKey = (formDataItem.getKey() != null) ? formDataItem.getKey() : "";
                    Object value = (formDataItem.getValue().getKey() != null) ? formDataItem.getValue().getKey() : "";
                    String strFileName = (formDataItem.getValue().getValue() != null) ? formDataItem.getValue().getValue() : "";
                    
                    // write key value pair
                    if (strFileName == null || strFileName.length() == 0)
                    {
                        String strFormDataItem = String.format(
                            "%1$s%2$s%3$sContent-Disposition: form-data; name=\"%4$s\"%3$s%3$s%5$s",
                            strBoundarySeparator,
                            StrBoundary,
                            strNewLine,
                            strKey,
                            value);

                        pw.print(strFormDataItem);                        
                    }
                    // write file data
                    else
                    {
                    	// write base64 or binary data
                    	byte[] fileByte = (value instanceof byte[]) ? (byte[])value : null;
                    	
                    	String strHeader =
                                String.format(
                                    "%1$s%2$s%3$sContent-Disposition: form-data; name=\"%4$s\"; filename=\"%5$s\"%3$sContent-Type: %6$s%3$s%3$s",
                                    strBoundarySeparator,
                                    StrBoundary,
                                    strNewLine,
                                    strKey,
                                    strFileName,
                                    fileByte == null ? "text/plain" : "application/octet-stream");
                    	
                    	pw.print(strHeader);                     	
                    	pw.flush();
                    	
                    	if(fileByte == null) pw.print(value.toString());  
                    	else os.write(fileByte);                    		                    	
                    }
                    
                    bHasItemAdded = true;
                }
                
                if (bHasItemAdded)
                {
                    String strFooter = strNewLine + strBoundarySeparator + StrBoundary + strBoundarySeparator + strNewLine;
                    pw.print(strFooter);
                }
                
                pw.flush();
                
                return os.toByteArray();
    	    }
    	}   	    	
    }
    
    // construct response data
    private static HttpWebResponse constructResposneData(HttpsURLConnection conn) throws Exception {
    	HttpWebResponse response = new HttpWebResponse(); 
    	response.setStatusCode(conn.getResponseCode());
    	
    	Map<String, List<String>> dicResponseHeaders = conn.getHeaderFields();
    	for (Map.Entry<String, List<String>> entry : dicResponseHeaders.entrySet())
    	{
    	    if (entry.getKey() == null) continue;   	    
    	    
    	    String strKey = entry.getKey();
    	    String strVal = "";

    	    List<String> headerValues = entry.getValue();
    	    Iterator<String> it = headerValues.iterator();
    	    if (it.hasNext()) {
    	    	strVal += it.next();

    	        while (it.hasNext()) {
    	        	strVal += it.next();
    	        }
    	    }

    	    response.addHeader(strKey, strVal);
    	}
    	
    	response.setBodyStream(conn.getInputStream());
    	return response;
    }
}
