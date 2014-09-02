/*
 * WorldMapGrabber.java
 *
 * Created on July 29, 2004, 12:13 PM
 * Last modified: 9/1/04 2:28 PM - changing pxDim array to just pxWidth and pxHeight; can get
 *      actual values just from dimensions of returned image
 * 05-Oct-04:  Trying to deal with stupid timeout issues
 * 06-Oct-04:  Commenting out error messages . . . just return null
 */

/**
 *
 * @author  gcb
 * @version 1.00 06-Oct-04
 */

//import java.awt.*;
//import javax.swing.*;
//import java.awt.event.*;
import java.net.*;
import java.io.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.imageio.*;
import java.awt.image.*;

public class WorldMapGrabber {
    private String sImage;
    private double minLat;
    private double maxLat;
    private double minLong;
    private double maxLong;
    private MapGrabber mg;
    
    /** Creates a new instance of WorldMapGrabber */
    public WorldMapGrabber() {
    }
    
    /** Returns world image with something like requested lat-long and pixel limits
     *    @param llBox integer array (length 4): min lat, max lat, min long, max long; modified on output
     *    @param pxWidth desired width of map in pixels
     *    @param pxHeight desired height of map in pixels
     */
    public BufferedImage getImage(double[] llBox, int pxWidth, int pxHeight) {
        BufferedImage leMonde = null;
        
        java.text.DecimalFormat df9 = new java.text.DecimalFormat("0.000000000");
        
        
        //System.out.println(WorldImage);
        
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(true);
        
        mg = new MapGrabber();
        sImage = " ";
        
        String WorldImage = null;
        try {    // try to get it from drysdale
            WorldImage = "http://drysdale.kgs.ku.edu/kgs/web_services/global_image/chronos.cfm?" +
                         "long_min=" + df9.format(llBox[0]) + "&" +
                         "long_max=" + df9.format(llBox[1]) + "&" +
                         "lat_min=" + df9.format(llBox[2]) + "&" +
                         "lat_max=" + df9.format(llBox[3]) + "&" +
                         "width=" + pxWidth + "&" +
                         "height=" + pxHeight;
            SAXParser saxParser = factory.newSAXParser();
            
            // Connect to the server
            
            URL u = new URL(WorldImage);
            URLConnection uc = u.openConnection();
            HttpURLConnection connection = (HttpURLConnection) uc;
            //connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            
            // Read the response XML Document
            
            InputStream in = connection.getInputStream();
            saxParser.parse(in, mg);
            in.close();
            connection.disconnect();
            //System.out.println("drysdale");
        }
        catch (Exception ex) {
        	try {   // then try neutrino
        		WorldImage = "http://neutrino.kgs.ku.edu/kgs/web_services/global_image/chronos.cfm?" +
                             "long_min=" + df9.format(llBox[0]) + "&" +
                             "long_max=" + df9.format(llBox[1]) + "&" +
                             "lat_min=" + df9.format(llBox[2]) + "&" +
                             "lat_max=" + df9.format(llBox[3]) + "&" +
                             "width=" + pxWidth + "&" +
                             "height=" + pxHeight;
				 SAXParser saxParser = factory.newSAXParser();
				   
				 // Connect to the server
				   
				 URL u = new URL(WorldImage);
				 URLConnection uc = u.openConnection();
				 HttpURLConnection connection = (HttpURLConnection) uc;
				 //connection.setDoOutput(true);
				 connection.setDoInput(true);
				 connection.setRequestMethod("POST");
				   
				 // Read the response XML Document
				   
				 InputStream in = connection.getInputStream();
				 saxParser.parse(in, mg);
				 in.close();
				 connection.disconnect();
				 //System.out.println("neutrino");
        	} catch (Exception ex1){
        		//JOptionPane.showMessageDialog(null,"Unable to obtain world relief image");
        		return null;
        	}
        }
        
        //System.out.println("sImage: " + sImage);
                
        URL url = null;
        try {
            url = new URL(sImage);
        }
        catch (Exception ex) {
            //JOptionPane.showMessageDialog(null,"Unable to obtain world relief image");
            return null;
        }
        
        try {
            leMonde = ImageIO.read(url);
        }
        catch (Exception ex) {
            //JOptionPane.showMessageDialog(null,"Unable to obtain world relief image");
            return null;
        }
        
        // modify latitude-longitude limits to reflect what was actually returned...
        // (pixel dimensions can be obtained from getWidth(), getHeight() for BufferedImage...
        llBox[0] = minLong;
        llBox[1] = maxLong;
        llBox[2] = minLat;
        llBox[3] = maxLat;
        
        return leMonde;
        
    }
    
    private class MapGrabber extends org.xml.sax.helpers.DefaultHandler {
        public void startElement(String nmURI, String locName, String qName, Attributes att)
                    throws SAXException {
            if ( qName.trim().equals("ENVELOPE") ) {
                minLong = Double.parseDouble(att.getValue("minx"));
                maxLong = Double.parseDouble(att.getValue("maxx"));
                minLat  = Double.parseDouble(att.getValue("miny"));
                maxLat  = Double.parseDouble(att.getValue("maxy"));
            } else if ( qName.trim().equals("OUTPUT") ) {
                sImage = att.getValue("url");
            }
        }
        
    }
    
    
}
