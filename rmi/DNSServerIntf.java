import java.rmi.*;

public interface DNSServerIntf extends Remote {
    public String DNS(String s1) throws RemoteException;
}