package Praktikum1;

import java.util.Dictionary;
import java.util.Hashtable;

public class HttpRequestData {
    public String Method;
    public String Path;
    public Dictionary<String,String> Headers = new Hashtable<String,String>();
    public String Content;
    public String Version;
}
