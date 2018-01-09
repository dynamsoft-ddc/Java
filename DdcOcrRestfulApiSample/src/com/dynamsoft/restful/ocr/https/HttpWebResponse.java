package com.dynamsoft.restful.ocr.https;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class HttpWebResponse {
	private Map<String , String> _dicResponseHeader;	
	private InputStream _is;
	private int _iStatusCode;
	
	public HttpWebResponse() {
		_dicResponseHeader = new HashMap<String, String>();
	}
	
	public void addHeader(String strKey, String strVal) {
		_dicResponseHeader.put(strKey, strVal);
	}	
	public String getHeader(String strKey) {
		return _dicResponseHeader.get(strKey);
	}
	
	public void setBodyStream(InputStream is) {
		_is = is;
	}
	public InputStream getBodyStream() {
		return _is;
	}	
	
	public void setStatusCode(int i) {
		_iStatusCode = i;
	}
	public int getStatusCode() {
		return _iStatusCode;
	}
}
