
// valmostrato server - last edit on 08/09/2016

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
        
        String lastValidData = "0.000000,0.000000,,M,0.000000,0.000,00.00";
        
        System.out.println("Valmostrato Server on port: " + port + " is ON\n");
        
        while (true) {
            connectionSocket = welcomeSocket.accept(); 
            inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream())); 
            outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            try {
                String clientSentence = inFromClient.readLine();
                try {
                    
                    // Writing on log.txt file
                    writeOnLogFile(clientSentence);
                    
                    if (checkString(clientSentence)) {
                        clientSentence = "invalid data - " + clientSentence;
                    } else {
                        HabString hs = new HabString(clientSentence);
                        // Writing on data.txt file 
                        writeOnDataFile(hs.getCleanString());
                        // Updating lastpos.txt file and save in ram as the last
                        // valid data received
                        lastValidData = hs.getLastPositionString(lastValidData);
                        writeOnLastposFile(lastValidData);
                    }
                   
                   System.out.println("FROM CLIENT = " + clientSentence + "\n");
                   
                   outToClient.writeBytes("OK");
                   connectionSocket.close();
                } catch (IOException e) {
                   System.out.println("ERROR\n"); 
                }
            } catch (IOException e){ 
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
    
    private boolean checkString(String clientSentence) {
        int count = 0;
        for (int i = 0; i < clientSentence.length(); i++) {
            if (clientSentence.charAt(i) == ',') {
                count++;
            }
        }
        if (count != 22) {
            return true;
        }
        return false;
    }
    
////////////////////////////////////////////////////////////////////////////////    
    
}
