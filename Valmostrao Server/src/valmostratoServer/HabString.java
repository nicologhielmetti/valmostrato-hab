
// valmostrato server - last edit on 05/09/2016

package valmostratoServer;

////////////////////////////////////////////////////////////////////////////////

public class HabString {
    
    final private String ubloxTime;
    final private String ubloxLatitude;
    final private String ubloxLongitude;
    final private String ubloxFixQuality;
    final private String ubloxSat;
    final private String ubloxAltitude;
    final private String ubloxVerticalSpeed;
    
    final private String dfrobotLongitude;
    final private String dfrobotLatitude;
    final private String dfrobotAltitude;
    final private String dfrobotTime;
    final private String dfrobotTTF;
    final private String dfrobotSat;
    final private String dfrobotSpeed;
    
    final private String temperature1;
    final private String temperature2;
    final private String pressure1;
    final private String pressure2;
    final private String voltage;
    final private String altitudeByPressure;
    
    
////////////////////////////////////////////////////////////////////////////////    
    
    public HabString(String data) {
        data = data.substring(6, data.length()-9);
        String[] splittedData = data.split(",");
        
        ubloxTime = splittedData[0];
        ubloxLatitude = cordinatesConversion(splittedData[1]);
        ubloxLongitude = cordinatesConversion(splittedData[2]);
        ubloxFixQuality = splittedData[3];
        ubloxSat = splittedData[4];
        ubloxAltitude = splittedData[5];
        ubloxVerticalSpeed = splittedData[6];
        
        dfrobotLongitude = cordinatesConversion(splittedData[7]);
        dfrobotLatitude = cordinatesConversion(splittedData[8]);
        dfrobotAltitude = splittedData[9];
        dfrobotTime = splittedData[10];
        dfrobotTTF = splittedData[11];
        dfrobotSat = splittedData[12];
        dfrobotSpeed = splittedData[13];
        
        temperature1 = splittedData[14];
        temperature2 = splittedData[15];
        pressure1 = splittedData[16];
        pressure2 = splittedData[17];
        voltage = splittedData[18];
        altitudeByPressure = splittedData[19];
    }
    
////////////////////////////////////////////////////////////////////////////////
    
    private String cordinatesConversion(String cordinate) {
        if (cordinate.equals("0.000000"))
            return "0.000000";
        else
        {
            int i = 0;
            while (cordinate.charAt(i) != '.') { i++; }
            float result = (10 * ((float)(cordinate.charAt(i-2) - 48))) 
                         + (float) cordinate.charAt(i-1) - 48;
            String[] decimali = cordinate.split("\\.");
            result = (float) (result + (Float.parseFloat(decimali[1])) 
                   / (Math.pow(10.0, (float)decimali[1].length())));
            result = result / 60;
            float integerPart = Float.valueOf(decimali[0]) / 100;
            String stringIntegerPart = String.valueOf(integerPart);
            String[] d = stringIntegerPart.split("\\.");
            result = result + Float.valueOf(d[0]);
            return String.valueOf(result);
        }
    }
    
////////////////////////////////////////////////////////////////////////////////    
    
    public String getLatPositionString() {
        return dfrobotLatitude + "," + dfrobotLongitude + "," + 
               ubloxAltitude + "," + dfrobotSpeed + "," + 
               ubloxVerticalSpeed + "," + voltage;
    }

////////////////////////////////////////////////////////////////////////////////    
    
    public String getCleanString() {
        return ubloxTime + "," + ubloxLatitude + "," + ubloxLongitude + "," +
               ubloxFixQuality + "," + ubloxSat + "," + ubloxAltitude + "," +
               ubloxVerticalSpeed + "," + dfrobotLatitude + "," + 
               dfrobotLongitude + "," + dfrobotAltitude + "," +
               dfrobotTime + "," + dfrobotTTF + "," + dfrobotSat + "," +
               dfrobotSpeed + "," + temperature1 + "," + temperature2 + "," +
               pressure1 + "," + pressure2 + "," + voltage + "," +
               altitudeByPressure;
    }
    
////////////////////////////////////////////////////////////////////////////////
    
}
