import java.rmi.registry.*;

public class DNSClient {
    public static void main(String args[]) throws Exception {
        Registry r = LocateRegistry.getRegistry("localhost", 8145);
        DNSServerIntf d = (DNSServerIntf) r.lookup("mvsrserver");
        String str = args[0];
        System.out.println("The website name is:" + str);
        System.out.println("The IP Address is:" + d.DNS(str));
    }
}