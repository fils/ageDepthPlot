/*
 * NeptuneHoleInfo.java
 *
 * Created on April 14, 2004, 2:58 PM
 * 19-Apr-04:  Adding sample count
 */

/**
 * Simple structure for identifying information for a Neptune hole
 *
 * @version 19-Apr-04
 * @author  gcb
 */
public class NeptuneHoleInfo {
    
    /** The hole ID */
    public String holeID;
    /** The latitude */
    public String latitude;
    /** The longitude */
    public String longitude;
    /** Number of samples for hole in sample table */
    public String sampleCount;
    /** Depth of water (meters) */
    public String waterDepth;
    /** Meters penetrated */
    public String metersPen;
    /** Meters recovered */
    public String metersRec;
    /** Ocean code */
    public String oceanCode;
    
    /** Creates a new instance of NeptuneHoleInfo */
    public NeptuneHoleInfo() {
    }
    
    /** Creates a new instance of NeptunHoleInfo with arguments */
    public NeptuneHoleInfo(String hid, String lat, String lon, String nos, String dep, String pen, String rec, String sea) {
        holeID = hid;
        latitude = lat;
        longitude = lon;
        sampleCount = nos;
        waterDepth = dep;
        metersPen = pen;
        metersRec = rec;
        oceanCode = sea;      
    }
    
    public String toString() {
        return  "holeID: " + holeID + "; " +
                "latitude: " + latitude + "; " +
                "longitude: " + longitude + "; " +
                "sampleCount: " + sampleCount + "; " +
                "waterDepth: " + waterDepth + "; " +
                "metersPen: " + metersPen + "; " +
                "metersRec: " + metersRec + "; " +
                "oceanCode: " + oceanCode;
    }
    
}
