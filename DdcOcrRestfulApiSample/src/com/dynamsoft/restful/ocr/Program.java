package com.dynamsoft.restful.ocr;

import java.util.HashMap;
import java.util.Map;

import com.dynamsoft.restful.ocr.https.FormData;
import com.dynamsoft.restful.ocr.https.HttpMultiPartRequest;
import com.dynamsoft.restful.ocr.https.HttpWebResponse;
import com.dynamsoft.restful.ocr.response.RestfulApiBasicResponse;
import com.dynamsoft.restful.ocr.response.RestfulApiDownloadResponse;
import com.dynamsoft.restful.ocr.response.RestfulApiRecognizationResponse;
import com.dynamsoft.restful.ocr.response.RestfulApiResponseParser;
import com.dynamsoft.restful.ocr.response.RestfulApiUploadResponse;
import com.dynamsoft.restful.ocr.util.Comm;
import com.dynamsoft.restful.ocr.util.EnumOcrFileMethod;

public class Program {
	// sample entry
	public static void main(String[] args) {
		// setup api key
		Map<String , String> dicHeader = new HashMap<String, String>();
		dicHeader.put("x-api-key", Configuration.strApiKey);
		
		// 1. upload file
		System.out.println("-----------------------------------------------------------------------");
		System.out.println("1. Upload file...");
		FormData formData = new FormData();
        formData.append("method", EnumOcrFileMethod.Upload.toString());
        formData.append("file", Comm.getFileData("example.jpg"), "example.jpg");
        
        HttpWebResponse httpWebResponse;
        RestfulApiBasicResponse restfulApiResponse;
        
        String strFileName;
        try
        {
            httpWebResponse = HttpMultiPartRequest.post(Configuration.strOcrBaseUri, dicHeader, formData);
            restfulApiResponse = RestfulApiResponseParser.parse(httpWebResponse, EnumOcrFileMethod.Upload);
            
            if ((strFileName = handleRestfulApiResponse(restfulApiResponse, EnumOcrFileMethod.Upload)) == null) return;
        }
        catch (Exception ex)
        {
        	System.out.println(ex.getMessage());
            return;
        }
        
        // 2. recognize the uploaded file
        System.out.println(System.getProperty("line.separator") + "-----------------------------------------------------------------------");
        System.out.println("2. Recognize the uploaded file...");
        
        formData.clear();
        formData.append("method", EnumOcrFileMethod.Recognize.toString());
        formData.append("file_name", strFileName);
        formData.append("language", "eng");
        formData.append("output_format", "UFormattedTxt");
        formData.append("page_range", "1-10");
        
        try
        {
            httpWebResponse = HttpMultiPartRequest.post(Configuration.strOcrBaseUri, dicHeader, formData);
            restfulApiResponse = RestfulApiResponseParser.parse(httpWebResponse, EnumOcrFileMethod.Recognize);
            
            if ((strFileName = handleRestfulApiResponse(restfulApiResponse, EnumOcrFileMethod.Recognize)) == null) return;
        }
        catch (Exception ex)
        {
        	System.out.println(ex.getMessage());
            return;
        }

        // 3. download the recognized file
        System.out.println(System.getProperty("line.separator") + "-----------------------------------------------------------------------");
        System.out.println("3. Download the recognized file...");
        
        formData.clear();
        formData.append("method", EnumOcrFileMethod.Download);
        formData.append("file_name", strFileName);
        
        try
        {
            httpWebResponse = HttpMultiPartRequest.post(Configuration.strOcrBaseUri, dicHeader, formData);
            restfulApiResponse = RestfulApiResponseParser.parse(httpWebResponse, EnumOcrFileMethod.Download);
            
            if ((strFileName = handleRestfulApiResponse(restfulApiResponse, EnumOcrFileMethod.Download)) == null) return;
        }
        catch (Exception ex)
        {
        	System.out.println(ex.getMessage());
            return;
        }       
	}
	
	// handle restful api response to control ocr step and print message
	private static String handleRestfulApiResponse(RestfulApiBasicResponse restfulApiResponse, EnumOcrFileMethod enumOcrFileMethod) throws Exception {
		String strFileName = null;
		
		switch (enumOcrFileMethod)
        {
			case Upload:
				RestfulApiUploadResponse uploadResponse = (restfulApiResponse instanceof RestfulApiUploadResponse) ?
						(RestfulApiUploadResponse)restfulApiResponse : null;
						
				if (uploadResponse == null || uploadResponse.error_code != 0) {
					if (uploadResponse == null) System.out.println("Upload Failed.");
					else System.out.println("Upload Failed: " + uploadResponse.error_msg);
	
					return strFileName;
				}
						
				strFileName = uploadResponse.name;
				System.out.println("Upload success: " + strFileName);
				break;
				
			case Recognize:
				RestfulApiRecognizationResponse recognizationResponse = (restfulApiResponse instanceof RestfulApiRecognizationResponse) ?
						(RestfulApiRecognizationResponse)restfulApiResponse : null;
				
				if (!(recognizationResponse != null && recognizationResponse.outputs != null
						&& recognizationResponse.outputs.size() > 0 && recognizationResponse.outputs.get(0).error_code == 0)) {
					
					if (recognizationResponse != null && recognizationResponse.outputs != null
							&& recognizationResponse.outputs.size() > 0
							&& recognizationResponse.outputs.get(0).error_code != 0) {
						System.out.println("Recognization failed: " + recognizationResponse.outputs.get(0).error_msg);
						
					} else if (recognizationResponse != null && recognizationResponse.error_code != 0) {
						System.out.println("Recognization failed: " + recognizationResponse.error_msg);
						
					} else {
						System.out.println("Recognization failed.");
					}
					
					return strFileName;
				}
				
				strFileName = recognizationResponse.outputs.get(0).output;
				System.out.println("Recognization success: " + strFileName);						
				break;
				
			case Download:
				RestfulApiDownloadResponse downloadResponse = (restfulApiResponse instanceof RestfulApiDownloadResponse) ?
						(RestfulApiDownloadResponse)restfulApiResponse : null;
				
				if (downloadResponse == null || downloadResponse.error_code != 0) {
					
					if (downloadResponse == null) System.out.println("Download failed.");
					else System.out.println("Download failed: " + downloadResponse.error_msg);
	
					return strFileName;
				}
				
				strFileName = "";
				System.out.println("Result: " + new String(downloadResponse.buffer, "UTF-16"));
				break;			
			
		default:
				System.out.println("Unsupported ocr method.");
				return strFileName;				
        }
		
		return strFileName;
	}
}
