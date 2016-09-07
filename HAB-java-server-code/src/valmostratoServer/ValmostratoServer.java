
// valmostrato server - last edit on 07/09/2016

package valmostratoServer;

////////////////////////////////////////////////////////////////////////////////

public class ValmostratoServer {
    
////////////////////////////////////////////////////////////////////////////////    

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(args[0]);
        String filesPath = args[1];
        TCPServer valmostratoServer = new TCPServer(filesPath, port);
        valmostratoServer.start();
    }
    
////////////////////////////////////////////////////////////////////////////////    
    
}
