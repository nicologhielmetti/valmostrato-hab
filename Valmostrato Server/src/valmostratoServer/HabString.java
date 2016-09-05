
// valmostrato server - last edit on 05/09/2016

package valmostratoServer;

////////////////////////////////////////////////////////////////////////////////

public class HabString {
    
    private final String ubloxTime;
    private final String ubloxLatitude;
    private final String ubloxLongitude;
    private final String ubloxFixQuality;
    private final String ubloxSat;
    private final String ubloxAltitude;
    private final String ubloxVerticalSpeed;
    
    private final String dfrobotLongitude;
    private final String dfrobotLatitude;
    private final String dfrobotAltitude;
    private final String dfrobotTime;
    private final String dfrobotTTF;
    private final String dfrobotSat;
    private final String dfrobotSpeed;
    
    private final String temperature1;
    private final String temperature2;
    private final String pressure1;
    private final String pressure2;
    private final String voltage;
    private final String altitudeByPressure;
    
    private boolean error;
    
    
////////////////////////////////////////////////////////////////////////////////    
    
    public HabString(String data) {
        error = false;
        if (!(data.substring(0, 3).equals("POST"))) {
            error = true;
        }
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
        if (error == false) {
            return dfrobotLatitude + "," + dfrobotLongitude + "," + 
                   ubloxAltitude + "," + dfrobotSpeed + "," + 
                   ubloxVerticalSpeed + "," + voltage;
        }
        return "";
    }

////////////////////////////////////////////////////////////////////////////////    
    
    public String getCleanString() {
        if (error == false) {
            return ubloxTime + "," + ubloxLatitude + "," + ubloxLongitude + "," +
                   ubloxFixQuality + "," + ubloxSat + "," + ubloxAltitude + "," +
                   ubloxVerticalSpeed + "," + dfrobotLatitude + "," + 
                   dfrobotLongitude + "," + dfrobotAltitude + "," +
                   dfrobotTime + "," + dfrobotTTF + "," + dfrobotSat + "," +
                   dfrobotSpeed + "," + temperature1 + "," + temperature2 + "," +
                   pressure1 + "," + pressure2 + "," + voltage + "," +
                   altitudeByPressure;
        }
        return "";
    }
    
////////////////////////////////////////////////////////////////////////////////
    
}
