
// valmostrato server - last edit on 08/09/2016

package valmostratoServer;

////////////////////////////////////////////////////////////////////////////////

public class HabString {
    
    private final String ubloxTime;
    private final String ubloxLatitude;
    private final String ubloxLongitude;
    private final String ubloxFixQuality;
    private final String ubloxSat;
    private final String ubloxAltitude;
    private final String ubloxAltitudeM;
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
    
////////////////////////////////////////////////////////////////////////////////    
    
    public HabString(String data) {

        data = data.substring(6, data.length()-9);
        String[] splitData = data.split(",");
        
        ubloxTime = getDateAndTime(splitData[0]);
        ubloxLatitude = cordinatesConversion(splitData[1]);
        ubloxLongitude = cordinatesConversion(splitData[2]);
        ubloxFixQuality = splitData[3];
        ubloxSat = splitData[4];
        ubloxAltitude = splitData[5];
        ubloxAltitudeM = splitData[6];
        ubloxVerticalSpeed = String.valueOf((Double.parseDouble(splitData[7])) * 3.6); // m/s --> km/h
        
        dfrobotLongitude = cordinatesConversion(splitData[8]);
        dfrobotLatitude = cordinatesConversion(splitData[9]);
        dfrobotAltitude = splitData[10];
        dfrobotTime = getDateAndTime(splitData[11]);
        dfrobotTTF = splitData[12];
        dfrobotSat = splitData[13];
        dfrobotSpeed = String.valueOf((Double.parseDouble(splitData[14])) * 3.6); // m/s --> km/h
        
        temperature1 = splitData[15];
        temperature2 = splitData[16];
        pressure1 = splitData[17];
        pressure2 = splitData[18];
        voltage = splitData[19];
        altitudeByPressure = splitData[20];
    }
    
////////////////////////////////////////////////////////////////////////////////
    
    private String cordinatesConversion(String cordinate) {
        if ((cordinate.equals("0.000000")) || (cordinate.equals(""))) {
            return "0.000000";
        } else {
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
    
    public String getLastPositionString(String lastValidData) {
        if (dfrobotLatitude.equals("0.000000")) { //if this is true also longitude, altitude and time are invalid (no fix)
            String[] data = lastValidData.split(",");
            data[data.length - 1] = voltage;
            lastValidData = data[0];
            for (int i = 1; i < data.length; i++) {
                lastValidData += "," + data[i];
            }
            return lastValidData;
        } else {
            return dfrobotLatitude + "," + dfrobotLongitude + "," + 
                   ubloxAltitude + "," +ubloxAltitudeM + "," + 
                   dfrobotSpeed + "," + ubloxVerticalSpeed + "," + 
                   dfrobotTime + "," + voltage;
        }

    }

////////////////////////////////////////////////////////////////////////////////    
    
    public String getCleanString() {
        return ubloxTime + "," + ubloxLatitude + "," + ubloxLongitude + "," +
               ubloxFixQuality + "," + ubloxSat + "," + 
               ubloxAltitude + "," + ubloxAltitudeM + "," + 
               ubloxVerticalSpeed + "," + dfrobotLatitude + "," + 
               dfrobotLongitude + "," + dfrobotAltitude + "," + 
               dfrobotTime + "," + dfrobotTTF + "," + dfrobotSat + "," + 
               dfrobotSpeed + "," + temperature1 + "," + 
               temperature2 + "," + pressure1 + "," + pressure2 + "," + 
               voltage + "," + altitudeByPressure;
    }
    
////////////////////////////////////////////////////////////////////////////////
    
    private String getDateAndTime(String DateAndTime)
    {
        if (DateAndTime.length() == 18) {
            if (DateAndTime.equals("00000000000000.000")) {
                return "00000000000000";
            } else {
                String fixedString = DateAndTime.substring(0, 7);
                int hour = Integer.valueOf(DateAndTime.substring(7, 10)) + 2;
                if (hour < 10) {
                    fixedString += "0";
                }
                fixedString += String.valueOf(hour);
                fixedString += DateAndTime.substring(10, DateAndTime.length() - 4);
                return fixedString;
            }
        } else {
            if (DateAndTime.equals("")) {
                return "000000";
            } else {
                String fixedString = "";
                int hour = Integer.valueOf(DateAndTime.substring(0, 2)) + 2;
                if (hour < 10) {
                    fixedString += "0";
                }
                fixedString += String.valueOf(hour);
                fixedString += DateAndTime.substring(2, 6);
                return fixedString;
            }
        }
    }
    
////////////////////////////////////////////////////////////////////////////////    
    
}
