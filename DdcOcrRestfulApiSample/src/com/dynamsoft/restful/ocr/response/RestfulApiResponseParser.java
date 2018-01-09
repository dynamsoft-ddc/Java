package com.dynamsoft.restful.ocr.response;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.dynamsoft.restful.ocr.https.HttpWebResponse;
import com.dynamsoft.restful.ocr.util.EnumOcrFileMethod;
import com.google.gson.Gson;

public class RestfulApiResponseParser {
	public static RestfulApiBasicResponse parse(HttpWebResponse httpWebResponse, EnumOcrFileMethod enumOcrFileMethod) throws Exception {
		if (httpWebResponse == null) throw new Exception("HttpWebResponse is null.");
		
		if (httpWebResponse.getStatusCode() != 200) 
        	throw new Exception(String.format("Request failed, status code is: " + httpWebResponse.getStatusCode()));
		
		String strResponse = "";
		
		if (httpWebResponse.getHeader("Content-Type").toLowerCase().trim().indexOf("application/json") >= 0)
        {
            try (InputStream is = httpWebResponse.getBodyStream()){
            	strResponse = convertInputStreamToString(is);
            }
        }
		
		switch (enumOcrFileMethod) {
			case Upload:
				return new Gson().fromJson(strResponse, RestfulApiUploadResponse.class);
			
			case Recognize:
				return new Gson().fromJson(strResponse, RestfulApiRecognizationResponse.class);
				
			case Download:
				if(strResponse == null || strResponse.length() == 0) {
					try(InputStream is = httpWebResponse.getBodyStream()){
						RestfulApiDownloadResponse restfulApiDownloadResponse = new RestfulApiDownloadResponse();
						restfulApiDownloadResponse.buffer = convertInputStreamToBytes(is);
						return restfulApiDownloadResponse;
					}
				}
				
				return new Gson().fromJson(strResponse, RestfulApiDownloadResponse.class);
				
			default:
				throw new Exception("Unsupported ocr method.");					
		}
	}
	
	private static String convertInputStreamToString(InputStream is) throws Exception {
		final int bufferSize = 1024;
		final char[] buffer = new char[bufferSize];
		final StringBuilder out = new StringBuilder();
		try(Reader in = new InputStreamReader(is, "UTF-8")){			
			for (; ; ) {
			    int rsz = in.read(buffer, 0, buffer.length);
			    if (rsz < 0)
			        break;
			    out.append(buffer, 0, rsz);
			}
			return out.toString();
		}
	}
	
	private static byte[] convertInputStreamToBytes(InputStream is) throws Exception {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		byte[] data = new byte[32768];

		while ((nRead = is.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}

		buffer.flush();

		return buffer.toByteArray();
	}
}
