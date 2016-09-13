
// valmostrato server - last edit on 08/09/2016

package valmostratoServer;

////////////////////////////////////////////////////////////////////////////////

public class ValmostratoServer {
    
////////////////////////////////////////////////////////////////////////////////    

    public static void main(String[] args) throws Exception {
        int port;
        String filesPath;
        if (args.length < 2) {
            port = 6789;
            filesPath = "/Users/";
        } else {
            port = Integer.parseInt(args[0]);
            filesPath = args[1];
        }
        TCPServer valmostratoServer = new TCPServer(filesPath, port);
        valmostratoServer.start();
    }
    
////////////////////////////////////////////////////////////////////////////////    
    
}
