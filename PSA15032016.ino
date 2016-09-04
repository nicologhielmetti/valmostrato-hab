#include <SdFat.h>
#include <OneWire.h>
#include <DallasTemperature.h>

String GNGGA         = String();
String PUBX          = String();
String CGPSINF       = String();
String INFOtoSend = String();
byte   gps_set_sucess = 0;
byte GPSIndex=0;
bool check;
SdFat sd;
SdFile Test;

/*
 GPS Level Convertor Board Test Script
 03/05/2012 2E0UPU
 
 This example connects the GPS via Software Serial.
 Initialise the GPS Module in Flight Mode and then echo's out the NMEA Data to the Arduinos onboard Serial port.
 
 This example code is in the public domain.
 Additional Code by J Coxon (http://ukhas.org.uk/guides:falcom_fsa03)
 
 
                     COLLEAGAMENTI
                     arduino ---------- GPS
                     GND,VCC            GND,VCC
                     D10                RX
                     D9                 TX
 !!!!!!!!! PIN ENABLE GPS collegato a VCC (5V) !!!!!!!!!!

 COLLEAGAMENTI
                     arduino mega ---------- GPS
                     GND,VCC                 GND,VCC
                     TX1                     RX
                     RX1                     TX
                     TX2                     TX SerialToUSB
 !!!!!!!!! PIN ENABLE GPS collegato a VCC (5V) !!!!!!!!!!
 
 */

void start_GSM(){
    //Configuracion GPRS Claro Argentina
    Serial.println("AT");
    delay(2000);
    Serial.println("AT+CREG=1");
    delay(2000);
    Serial.println("AT+SAPBR=3,1,\"APN\",\"INTERNET.WIND\"");
    delay(2000);
    Serial.println("AT+SAPBR=3,1,\"USER\",\"\"");
    delay(2000);
    Serial.println("AT+SAPBR=3,1,\"PWD\",\"\"");
    delay(2000);
    Serial.println("AT+SAPBR=3,1,\"Contype\",\"GPRS\"");
    delay(2000);
    /*Serial.println("AT+CIICR");
    delay(2000);*/
    Serial.println("AT+SAPBR=1,1");
    delay(10000);
    Serial.println("AT+HTTPINIT");
    delay(2000);
    Serial.println("AT+HTTPPARA=\"CID\",1");
    delay(2000);
}

void send_GPRS(){
  //Serial2.print(CGPSINFtoSend);
  //Serial2.println(GNGGAtoSend);
  Serial.print("AT+HTTPPARA=\"URL\",\"http://valmostrato.ddns.net:6789/");
  Serial.print(INFOtoSend);
  //Serial.print(PUBX);
  Serial.println("\"");
  delay(2000);
  Serial.println("AT+HTTPACTION=1"); //now GET action
  delay(2000);        
}

// Calculate expected UBX ACK packet and parse UBX response from GPS
boolean getUBX_ACK(uint8_t *MSG) {
  uint8_t b;
  uint8_t ackByteID = 0;
  uint8_t ackPacket[10];
  unsigned long startTime = millis();
  Serial2.print(" * Reading ACK response: ");
 
  // Construct the expected ACK packet    
  ackPacket[0] = 0xB5;  // header
  ackPacket[1] = 0x62;  // header
  ackPacket[2] = 0x05;  // class
  ackPacket[3] = 0x01;  // id
  ackPacket[4] = 0x02;  // length
  ackPacket[5] = 0x00;
  ackPacket[6] = MSG[2];  // ACK class
  ackPacket[7] = MSG[3];  // ACK id
  ackPacket[8] = 0;   // CK_A
  ackPacket[9] = 0;   // CK_B
 
  // Calculate the checksums
  for (uint8_t i=2; i<8; i++) {
    ackPacket[8] = ackPacket[8] + ackPacket[i];
    ackPacket[9] = ackPacket[9] + ackPacket[8];
  }
 
  while (1) {
 
    // Test for success
    if (ackByteID > 9) {
      // All packets in order!
      Serial2.println(" (SUCCESS!)");
      return true;
    }
 
    // Timeout if no valid response in 3 seconds
    if (millis() - startTime > 3000) { 
      Serial2.println(" (FAILED!)");
      return false;
    }
 
    // Make sure data is available to read
    if (Serial1.available()) {
      b = Serial1.read();
 
      // Check that bytes arrive in sequence as per expected ACK packet
      if (b == ackPacket[ackByteID]) { 
        ackByteID++;
        Serial2.print(b, HEX);
      } 
      else {
        ackByteID = 0;  // Reset and look again, invalid order
      }
 
    }
  }
}

// Send a byte array of UBX protocol to the GPS
void sendUBX(uint8_t *MSG, uint8_t len) {
  for(int i=0; i<len; i++) {
    Serial1.write(MSG[i]);
    Serial2.print(MSG[i], HEX);
  }
  Serial1.println();
}

void initUbloxGPS()
{
  Serial1.begin(9600); 
  // START OUR SERIAL DEBUG PORT
  Serial2.begin(9600);
  Serial2.println("Serial1 Level Convertor Board Test Script");
  Serial2.println("03/06/2012 2E0UPU");
  Serial2.println("Initialising....");
  //
  // THE FOLLOWING COMMAND SWITCHES MODULE TO 4800 BAUD
  // THEN SWITCHES THE SOFTWARE SERIAL TO 4,800 BAUD
  //
  Serial1.print("$PUBX,41,1,0007,0003,4800,0*13\r\n"); 
  delay(1000);
  Serial1.begin(4800);
  Serial1.flush();
 
  //  THIS COMMAND SETS FLIGHT MODE AND CONFIRMS IT 
  Serial2.println("Setting uBlox nav mode: ");
  uint8_t setNav[] = {
    0xB5, 0x62, 0x06, 0x24, 0x24, 0x00, 0xFF, 0xFF, 0x06, 0x03, 0x00, 0x00, 0x00, 0x00, 0x10, 0x27, 0x00, 0x00, 
    0x05, 0x00, 0xFA, 0x00, 0xFA, 0x00, 0x64, 0x00, 0x2C, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x16, 0xDC }; //44byte
  while(!gps_set_sucess)
  {
    sendUBX(setNav, sizeof(setNav)/sizeof(uint8_t));
    gps_set_sucess=getUBX_ACK(setNav);
  }
  gps_set_sucess=0;
 
  // THE FOLLOWING COMMANDS DO WHAT THE $PUBX ONES DO BUT WITH CONFIRMATION
  // UNCOMMENT AS NEEDED
  
  Serial2.println("Switching off NMEA GLL: ");
   uint8_t setGLL[] = { 
   0xB5, 0x62, 0x06, 0x01, 0x08, 0x00, 0xF0, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x01, 0x2B}; //16byte
   while(!gps_set_sucess)
   {    
   sendUBX(setGLL, sizeof(setGLL)/sizeof(uint8_t));
   gps_set_sucess=getUBX_ACK(setGLL);
   }
   gps_set_sucess=0;
   Serial2.println("Switching off NMEA GSA: ");
   uint8_t setGSA[] = { 
   0xB5, 0x62, 0x06, 0x01, 0x08, 0x00, 0xF0, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x02, 0x32}; //16byte
   while(!gps_set_sucess)
   {  
   sendUBX(setGSA, sizeof(setGSA)/sizeof(uint8_t));
   gps_set_sucess=getUBX_ACK(setGSA);
   }
   gps_set_sucess=0;
   Serial2.println("Switching off NMEA GSV: ");
   uint8_t setGSV[] = { 
   0xB5, 0x62, 0x06, 0x01, 0x08, 0x00, 0xF0, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x03, 0x39}; //16byte
   while(!gps_set_sucess)
   {
   sendUBX(setGSV, sizeof(setGSV)/sizeof(uint8_t));
   gps_set_sucess=getUBX_ACK(setGSV);
   }
   gps_set_sucess=0;
   Serial2.print("Switching off NMEA RMC: ");
   uint8_t setRMC[] = { 
   0xB5, 0x62, 0x06, 0x01, 0x08, 0x00, 0xF0, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x04, 0x40}; //16byte
   while(!gps_set_sucess)
   {
   sendUBX(setRMC, sizeof(setRMC)/sizeof(uint8_t));
   gps_set_sucess=getUBX_ACK(setRMC);
   }
   gps_set_sucess=0;
   Serial2.print("Switching off NMEA VTG: ");
   uint8_t setVTG[] = { 
   0xB5, 0x62, 0x06, 0x01, 0x08, 0x00, 0xF0, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x04, 0x46}; //16byte
   while(!gps_set_sucess)
   {
   sendUBX(setVTG, sizeof(setRMC)/sizeof(uint8_t));
   gps_set_sucess=getUBX_ACK(setVTG);
   }
}

void readUbloxString() 
{
  do{
    while(Serial1.available()) Serial1.read();
    while(!Serial1.available());
    while(Serial1.available())
    {
      Serial1.readStringUntil('A');
      GNGGA = Serial1.readStringUntil('*');
    }
  } while(GNGGA.length() <= 1);
  do{
    while(Serial1.available()) Serial1.read();
    Serial1.println("$PUBX,00*33");
    delay(100);
    while(Serial1.available())
    {
      Serial1.readStringUntil('X');
      PUBX = Serial1.readStringUntil('*');
    }
  }while(PUBX.length() <= 1);
}

void saveData()
{
    Test.open("CGPSINF.CSV", O_RDWR | O_CREAT | O_AT_END);
    Test.println(CGPSINF);
    Test.close();
    Test.open("GNGGA.CSV", O_RDWR | O_CREAT | O_AT_END);
    Test.println(GNGGA);
    Test.close();
    Test.open("PUBX.CSV", O_RDWR | O_CREAT | O_AT_END);
    Test.println(PUBX);
    Test.close();
}

String findStr(String str, int virgolaIn, int virgolaFin)
{
  int fine = 0;
  int virgole = 0;
  int inizio = 0;
  bool controllo = true;
  while(virgole < virgolaFin)
  {
    if (str[fine] == ',')
      virgole++;
    if (controllo == true)
      if (virgole == virgolaIn)
      {
       inizio = fine + 1;
       controllo = false;
      }
    fine++;
  }  
  return str.substring(inizio,fine);
}

  
void composeStringToSend(){
  INFOtoSend =  findStr(GNGGA,1,3);
  INFOtoSend += findStr(GNGGA,4,5);
  INFOtoSend += findStr(GNGGA,6,8);
  INFOtoSend += findStr(GNGGA,9,10);
  INFOtoSend += findStr(PUBX,13,14);
  INFOtoSend += findStr(CGPSINF,1,8);
  INFOtoSend += findStr(CGPSINF,9,11);
}

void readFromGPS(){
  do{
    while(Serial.available()) Serial.read(); //svuotamento buffer seriale
    while(!Serial1.available());
    Serial.println("AT+CGPSINF=0");  //invio comando per info gps
    delay(100);
    Serial.readStringUntil('0');
    Serial.readStringUntil(',');
    CGPSINF = Serial.readStringUntil('\r');
  } while (CGPSINF.length() == 1);
}
/*
void readFromGPS()
{
  int  i_chr=0;
  int cont=0; //dichiarazione variabili.
  char c_chr;

  memset(stringToSend, '\0', 100); //inizializzazione stringToSend a \0
  delay(100);
  while(Serial.available()) Serial.read(); //svuotamento buffer seriale
  Serial.println("AT+CGPSINF=0");  //invio comando per info gps
  delay(1000);
  do
  {
    c_chr = Serial.read();
    stringToSend[cont] = c_chr;
    cont++;
  }while(c_chr == '\r');
  while(Serial.read() != ','); //lettura caratteri fino alla virgola scartandoli
  
  i_chr = 0;  //inizializzazione variabili contatori
  cont  = 0;
  
  while(i_chr < 3) //condizione: finchè non legge la terza virgola salva i caratteri in stringToSend 
  {
    c_chr = Serial.read(); //assegnazione a c_chr il carattere letto dalla seriale
    if (c_chr == ',')  //se il carattere è una virgola incremento il contatore
      i_chr++;
    stringToSend[cont] = c_chr; //salvo il stringToSend
    cont++;  //incremento il contatore associato a stringToSend
  }
  
  while(i_chr < 7) //condizione: finchè non legge la settima virgola
  {
    c_chr = Serial.read(); //leggo il carattere in c_chr
    if ((c_chr == '.') && (i_chr == 3))  //se il carattere è . e siamo alla terza virgola allora scartiamo tutti i caratteri nella seriale fino alla quinta virgola
    {
      while(i_chr < 5) //finchè non legge la quinta virgola
      {
        c_chr = Serial.read(); //scarta il carattere
        if (c_chr == ',')  //se è una virgola incrementa il contatore associato
          i_chr++;
      }
      stringToSend[cont] = ','; //poichè l'ultimo carattere scartato è la virgola (utile alla condizione che incrementa il contatore) viene assegnato come ultimo carattere la virgola
      cont++;  //incremento contatore associato a stringToSend
    }
    else
    {
      stringToSend[cont] = c_chr; //salvataggio in stringToSend
      cont++;  //incremento contatore associato a stringToSend
      if(c_chr == ',') //se il carattere letto è una virgola allora incremento il contatore associato
        i_chr++;
    }
  }
}*/

float fmap(float x, float in_min, float in_max, float out_min, float out_max)
{
  return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}

float getAvgVbatt(){
  double sum;
  for(int i=0; i<50; i++){
      sum += analogRead(A5);
      delay(10);
  }
  sum /= 50;
  return ((sum*5.03/1023)/(3.2/13.2));
}
void readSensors()
{
  String allSensors = String();
  OneWire temp(2);
  DallasTemperature _Temp(&temp);
  _Temp.begin();
  _Temp.requestTemperatures();
  allSensors = _Temp.getTempCByIndex(0);
  allSensors += ",";
  allSensors += _Temp.getTempCByIndex(1);
  allSensors += ",";
  allSensors += fmap(analogRead(A4),102.3,920.7,0,103421.359);
  allSensors += ",";
  allSensors += ((analogRead(A4) - 102.4)/ 819.2)* 103421.359;
  allSensors += ",";
  //allSensors += fmap(analogRead(A5),402,494, 11.10, 12.62);
  allSensors += getAvgVbatt();
  //allSensors += (analogRead(A5)*5.03/1023)/(3.2/13.2);
  allSensors += ",";
  allSensors += 44330*(1 - pow(( ((analogRead(A4) - 102.4)/ 819.2)* 103421.359 / 101325),(1/5.255)));
  INFOtoSend += allSensors;
}

/*int8_t sendATcommand2(char* ATcommand, char* expected_answer1, char* expected_answer2, unsigned int timeout) {
  uint8_t x=0,  answer=0;
  char response[100]; //100
  unsigned long previous;
  memset(response, '\0', 100);    // Initialize the string
  delay(100);
  while( Serial.available() > 0) Serial.read();    // Clean the input buffer
  Serial.println(ATcommand);    // Send the AT command 
  x = 0;
  previous = millis();
  do   // this loop waits for the answer
  {
    if (Serial.available() != 0) // if there are data in the UART input buffer, reads it and checks for the asnwer
    {    
      response[x] = Serial.read();
      x++;
      if (strstr(response, expected_answer1) != NULL) // check if the desired answer 1  is in the response of the module
        answer = 1;
      else if (strstr(response, expected_answer2) != NULL) // check if the desired answer 2 is in the response of the module
        answer = 2;
    }
  } while((answer == 0) && ((millis() - previous) < timeout)); // Waits for the asnwer with time out
  return answer;
}*/

void setup()
{
  Serial.begin(9600);
  
  pinMode(3,OUTPUT);  //default pins per pilotare il modulo. Il 4 sarà sostituito per conflitto con chipselect della board SD.
  pinMode(6,OUTPUT);
  pinMode(5,OUTPUT);

  pinMode(4, OUTPUT);
  if (!sd.begin(4, SPI_HALF_SPEED)) Serial2.println("Error 1");
  
  digitalWrite(5,HIGH); //inizializzazione del pin 5 serve al modulo.
  delay(1500);
  digitalWrite(5,LOW);
  
  digitalWrite(3,LOW);  //Abilita    GSM mode.
  digitalWrite(6,HIGH); //Disabilita GPS mode.
  delay(1000);
  
  Serial.println("AT");
  delay(2000);
  Serial.println("AT+CGPSIPR=9600"); //Imposta baud-rate di comunicazione con il GPS a 9600.
  delay(2000);
  Serial.println("AT+CGPSPWR=1");    //Attiva l'antenna GPS.
  delay(2000);
  Serial.println("AT+CGPSOUT=0");
  delay(2000);
  Serial.println("AT+CGPSRST=1");    //Effettua il cold-start del GPS.
  delay(2000);
  initUbloxGPS();
  start_GSM();
}

void loop()
{
  readFromGPS(); //leggere i dati dal GPS eliminando l'ultimo campo + le cifre decimali del tempo e della velocità.
  readUbloxString();
  Serial2.print("GNGGA : ");
  Serial2.println(GNGGA);
  Serial2.print("PUBX : ");
  Serial2.println(PUBX);
  Serial2.print("CGPSINF : ");
  Serial2.println(CGPSINF);
  Serial2.println();
  composeStringToSend();
  Serial2.print("TOSEND : ");
  Serial2.println(INFOtoSend);
  Serial2.println();
  readSensors();  //leggere i dati da tutti i sensori: temperatura, pressione, voltaggio.
  saveData();    //salvataggio stringa su SD.
  send_GPRS();
}

