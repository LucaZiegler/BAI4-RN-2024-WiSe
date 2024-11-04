package Praktikum1;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

public class HttpResponseData {
    public int StatusCode;
    public String StatusMessage;
    public HashMap<String,String> ResponseHeaders = new HashMap<String,String>();
    public byte[] ResponseContent;
}
