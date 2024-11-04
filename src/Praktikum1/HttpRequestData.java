package Praktikum1;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

public class HttpRequestData {
    public String Method;
    public String Path;
    public HashMap<String,String> Headers = new HashMap<String,String>();
    public String Content;
    public String Version;
}
