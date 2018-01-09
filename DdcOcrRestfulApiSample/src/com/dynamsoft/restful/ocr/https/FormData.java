package com.dynamsoft.restful.ocr.https;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

public class FormData {
	private List<SimpleEntry<String, SimpleEntry<Object, String>>> _listFormData;
	
	public FormData() {
		_listFormData = new ArrayList<SimpleEntry<String, SimpleEntry<Object, String>>>();
	}
	
    public void append(String strKey, Object value)
    {
        append(strKey, value, null);
    }
	
	public void append(String strKey, Object value, String strFileName)
    {
		SimpleEntry<String, SimpleEntry<Object, String>> dataItem = new SimpleEntry<String, SimpleEntry<Object, String>>(strKey,
            new SimpleEntry<Object, String>(value, strFileName));

        _listFormData.add(dataItem);
    }
	
    public void clear()
    {
        _listFormData.clear();
    }
    
    public Boolean isValid()
    {
        return _listFormData != null;
    }
    
    public List<SimpleEntry<String, SimpleEntry<Object, String>>> getAll()
    {
        return _listFormData;
    }
}
