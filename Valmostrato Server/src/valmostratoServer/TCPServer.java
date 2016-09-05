
// valmostrato server - last edit on 05/09/2016

package valmostratoServer;

////////////////////////////////////////////////////////////////////////////////

import java.io.*; 
import java.net.*;

////////////////////////////////////////////////////////////////////////////////

public class TCPServer {
    
////////////////////////////////////////////////////////////////////////////////    
    
    private static ServerSocket welcomeSocket;
    private final PrintWriter logFile;
    private final PrintWriter dataFile;
    private final File lastPosFile;
    private final int port;
    
////////////////////////////////////////////////////////////////////////////////    

    public TCPServer(String filesPath, int nPort) throws IOException {
        logFile = new PrintWriter(new FileWriter(filesPath + "log.txt", true));   // log.txt
        dataFile = new PrintWriter(new FileWriter(filesPath +"data.txt", true));  // data.txt
        lastPosFile = new File(filesPath + "lastpos.txt"); // lastpos.txt
        port = nPort;
        welcomeSocket = new ServerSocket(port);
    }
    
////////////////////////////////////////////////////////////////////////////////    
    
    public void start() throws IOException {
        Socket connectionSocket;
        DataOutputStream outToClient;
        BufferedReader inFromClient;
        System.out.println("Valmostrato Server on port: " + port + " is ON\n");
        while (true){
            connectionSocket = welcomeSocket.accept(); 
            inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream())); 
            outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            try{
                String clientSentence = inFromClient.readLine();
                try {
                   HabString hs = new HabString(clientSentence);
                   
                   // Writing on log.txt file
                    writeOnLogFile(clientSentence);
                   
                   // Writing on data.txt file 
                    writeOnDataFile(hs.getCleanString());
                   
                   // Updating lastpos.txt file
                    writeOnLastposFile(hs.getLatPositionString());
                   
                   System.out.println("FROM CLIENT = " + clientSentence + "\n");
                   
                   outToClient.writeBytes("OK");
                   connectionSocket.close();
                }
                catch (IOException e) {
                   System.out.println("ERROR\n"); 
                }
            }
            catch (IOException e){ 
                System.out.println("ERROR\n");
            }
        }
    }
    
//////////////////////////////////////////////////////////////////////////////// 
    
    private void writeOnLastposFile(String string) throws IOException {
        FileWriter fwLastPos = new FileWriter(lastPosFile, false);
        PrintWriter pwLastPos = new PrintWriter(fwLastPos);
        pwLastPos.print(string);
        pwLastPos.flush();
    }
    
//////////////////////////////////////////////////////////////////////////////// 
    
    private void writeOnDataFile(String string) {
        dataFile.print(string + "\n\r");
        dataFile.flush();
    }
    
////////////////////////////////////////////////////////////////////////////////
    
    private void writeOnLogFile(String string) {
        logFile.print(string + "\n\r");
        logFile.flush();
    }
    
////////////////////////////////////////////////////////////////////////////////
    
}
