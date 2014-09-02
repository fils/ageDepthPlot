/*
 * ADPIO.java
 *
 * Created on January 13, 2004, 12:41 PM
 * Last modified 2/18/04 2:43 PM
 * 18-June-04:  adding writePlotAsSVG()
 * 21-June-04:  working on writePlotAsSVG() . . .
 * 23-June-04:  working on writePlotAsPNG(), figgering out niceties of screen capture business
 *      (initial hints from http://www.geocities.com/marcoschmidt.geo/java-save-screenshot.html)
 * 25-June-04:  continuing on saving of plot . . .
 * 26-June-04:  Working on writePlotAsSVG
 * 28-June-04:  Ditto.
 * 30-June-04:  Double-ditto.  Trying to get it wrapped up
 * 01-July-04:  Adding new SVG element to clip plotting of data to plot region
 * 02-July-04:  Adding styles for elements in SVG file . . .
 * 20-Sept-04:  Switching to using clipPath and if statements to keep things on plot,
 *              since Adobe Illustrator doesn't seem to handle embedded SVG elements
 *              properly . . .
 * 22-Sept-04:  Wrapping up changes to SavePlotAsSVG . . .
 * 14-Jan-05:   Adding getCSVReaderFromQuery and Twentify
 * 09-Mar-05:   Fixing problem in writeLOCData
 */

/**
 * ADP input & output routines
 *
 * @version 1.20 14-Jan-2005
 * @author gcb
 */

import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;

import javax.swing.*;
import java.util.*;
import java.text.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;

public class ADPIO {

    private static File CurDirOpen = null;  // changed from just CurDir 2/16/04 2:21 PM
    private static File CurDirSave = null;  // added 2/16/04 2:21 PM
    private static String urlstring;

    public static boolean readCoreData(Hashtable CoreDepths) {
        //System.out.println("Entering readCoreData . . . ");
        JFileChooser jfc;
        if (CurDirOpen != null) {
            jfc = new JFileChooser(CurDirOpen);
        } else if (CurDirSave != null) {
            jfc = new JFileChooser(CurDirSave);
        } else {
            jfc = new JFileChooser();
        }
        jfc.setDialogTitle("Open Core Data File");
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = jfc.showOpenDialog(null);
        if (result == JFileChooser.CANCEL_OPTION) {
            jfc = null;
            return false;
        }
        File bingo = jfc.getSelectedFile();
        CurDirOpen = jfc.getCurrentDirectory();
        jfc = null;
        BufferedReader buffy = null;
        String inLine = null;
        StringTokenizer ronny = null;
        try {
            buffy = new BufferedReader(new FileReader(bingo));
            inLine = buffy.readLine();
            ADPApp.CoreFileComment = reDelim(inLine.trim(), "\t", "     ");   // ugly as sin . . .
            ronny = new StringTokenizer(inLine, "\t");
            ADPApp.HoleID = ronny.nextToken().trim();
            buffy.readLine();  // skip column label row
            while ((inLine = buffy.readLine()) != null) {
                ronny = new StringTokenizer(inLine, "\t");
                int CoreId = Integer.parseInt(ronny.nextToken());
                double TopDepth = Double.parseDouble(ronny.nextToken());
                double BotDepth = Double.parseDouble(ronny.nextToken());
                CoreDepths.put(new Integer(CoreId), new ADPDepthRange(TopDepth, BotDepth));
            }
            buffy.close();
            return true;
        } catch (Exception x) {
            JOptionPane.showMessageDialog(null, "Problems reading file: " + x.getMessage());
            return false;
        }
    }


    public static String getJanusLSHLatLong(String leg, String site, String hole) throws IOException {

        //TODO remove localhost dependency here
        String str = "http://chronoslab.chronos.org/janusAmp/rest/latlong/" + leg + "/" + site + "/" + hole;
        URL janusAmpURL = null;
        BufferedReader buffy = null;


        try {
            janusAmpURL = new URL(str);
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            buffy = new BufferedReader(new InputStreamReader(janusAmpURL.openStream()));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        //  convert buffer to a string, parse the XML looking for the content of gml:pos and return this
        String responseText = "";
        String temp = "";

        while ((temp = buffy.readLine()) != null) {
            responseText += temp;
        }

        return responseText;
    }


    // DRF
    public static boolean readJanusStratData(Hashtable StratEvents) {

//        String strURI = JOptionPane.showInputDialog(null, "Enter Janus ADP  URL : ", "", 1);
//        urlstring = strURI;

//        String str = strURI + ".datums";   //  return the datums
        String str = urlstring + ".datums";   //  return the datums



        URL janusAmpURL = null;
        BufferedReader buffy = null;
        String inLine = null;
        StringTokenizer ronny = null;

        System.out.println("In the janus reader (readJanusStratData) with " + str);

        try {
//            buffy = new BufferedReader(new FileReader(bingo));
//            BufferedReader buffy = null;
            try {
//               Example URL: http://dev.chronos.org:9090/janusAmp/loc/datumsADP/1153
                janusAmpURL = new URL(str);
            } catch (MalformedURLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            try {
                buffy = new BufferedReader(
                        new InputStreamReader(janusAmpURL.openStream()));
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

//            String inputLine;

            try {
//                while ((inputLine = buffy.readLine()) != null)
//                    System.out.println(inputLine);
                inLine = buffy.readLine();

                //  try to set the title
                ADPApp.CoreFileComment = reDelim(inLine.trim(), "\t", "     ");   // ugly as sin . . .
                ronny = new StringTokenizer(inLine, "\t");
                ADPApp.HoleID = ronny.nextToken().trim();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }


            ADPApp.StratFileComment = reDelim(inLine.trim(), "\t", "     ");  // first line as comment
            buffy.readLine();  // skip column headers
            int evtno = 0;
            while ((inLine = buffy.readLine()) != null) {
                //System.out.println( inLine );
                evtno++;
                ronny = new StringTokenizer(inLine, "\t", true);
                //System.out.println( "ntoken: " + ronny.countTokens() );
                String Grp = ronny.nextToken();
                String tab = null;
                if (Grp.equals("\t")) {
                    Grp = null;
                } else {
                    tab = ronny.nextToken();
                }
                String Event = ronny.nextToken();
                if (Event.equals("\t")) {
                    Event = null;
                } else {
                    tab = ronny.nextToken();
                }
                String PlotCode = ronny.nextToken();
                if (PlotCode.equals("\t")) {
                    PlotCode = null;
                } else {
                    tab = ronny.nextToken();
                }
                String MinAge = ronny.nextToken();
                if (MinAge.equals("\t")) {
                    MinAge = null;
                } else {
                    tab = ronny.nextToken();
                }
                String MaxAge = ronny.nextToken();
                if (MaxAge.equals("\t")) {
                    MaxAge = null;
                } else {
                    tab = ronny.nextToken();
                }
                String MinDepth = ronny.nextToken();
                if (MinDepth.equals("\t")) {
                    MinDepth = null;
                } else {
                    tab = ronny.nextToken();
                }
                String MaxDepth = null;
                if (ronny.hasMoreTokens()) {
                    MaxDepth = ronny.nextToken();
                }
                ADPStratEvent adpSE = new ADPStratEvent(Grp, Event, PlotCode, MinAge, MaxAge, MinDepth, MaxDepth, 0);
                StratEvents.put(new Integer(evtno), adpSE);
            }
//            buffy.close();
            try {
                buffy.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return true;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Problems reading strat data " + ex.getMessage());
            return false;
        }
    }


    public static boolean readJanusLOCData(Vector LOCPoints) {
//        String str = JOptionPane.showInputDialog(null, "Enter Janus ADP  URL : ", "", 1);

        URL janusAmpURL = null;
        BufferedReader buffy = null;
        String inLine = null;
        StringTokenizer ronny = null;

        String strURI = JOptionPane.showInputDialog(null, "Enter Janus ADP  URL : ", "", 1);
        urlstring = strURI;

        //  called second so get thje URI prefix from the class level variable
        String str = urlstring + ".adp";


        System.out.println("In the janus reader (readJanusLOCData) with " + str);

        try {
            try {
                janusAmpURL = new URL(str);
//                janusAmpURL = new URL("http://chronoslab.chronos.org/janusAmp/loc/doc/2474.adp");
            } catch (MalformedURLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            try {
                buffy = new BufferedReader(
                        new InputStreamReader(janusAmpURL.openStream()));
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            try {
                inLine = buffy.readLine();
                //  try to set the title
                ADPApp.CoreFileComment = reDelim(inLine.trim(), "\t", "     ");   // ugly as sin . . .
                ronny = new StringTokenizer(inLine, "\t");
                ADPApp.HoleID = ronny.nextToken().trim();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            //  what are these 3 lines all about?
//            buffy.readLine();
            buffy.readLine();
            buffy.readLine();

            System.out.println( inLine );
            
//            ADPApp.LOCFileComment = reDelim(inLine.trim(), "\t", "     ");
//            inLine = buffy.readLine();
//            System.out.println( inLine );
//            inLine = buffy.readLine();
//            System.out.println( inLine );

            while ((inLine = buffy.readLine()) != null) {
                System.out.println( "entry: " + inLine );
                ronny = new StringTokenizer(inLine, "\t");
                double age = Double.parseDouble(ronny.nextToken().trim());
                double depth = Double.parseDouble(ronny.nextToken().trim());
                LOCPoints.add(new ADPLOCPoint(age, depth));
            }
            buffy.close();
            return true;
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Problems reading file (loc method): " + ex.getMessage());
            return false;  // return true anyway, since we are going to plot whatever we get
        }

    }


    public static boolean readStratData(Hashtable StratEvents) {
        //System.out.println("Entering readStratData . . . ");
        JFileChooser jfc;
        if (CurDirOpen != null) {
            jfc = new JFileChooser(CurDirOpen);
        } else if (CurDirSave != null) {
            jfc = new JFileChooser(CurDirSave);
        } else {
            jfc = new JFileChooser();
        }
        jfc.setDialogTitle("Open Strat Data File");
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = jfc.showOpenDialog(null);
        if (result == JFileChooser.CANCEL_OPTION) {
            jfc = null;
            return false;
        }
        File bingo = jfc.getSelectedFile();
        CurDirOpen = jfc.getCurrentDirectory();
        jfc = null;
        BufferedReader buffy = null;
        String inLine = null;
        StringTokenizer ronny = null;
        try {
            buffy = new BufferedReader(new FileReader(bingo));
            inLine = buffy.readLine();
            ADPApp.StratFileComment = reDelim(inLine.trim(), "\t", "     ");  // first line as comment
            buffy.readLine();  // skip column headers
            int evtno = 0;
            while ((inLine = buffy.readLine()) != null) {
                //System.out.println( inLine );
                evtno++;
                ronny = new StringTokenizer(inLine, "\t", true);
                //System.out.println( "ntoken: " + ronny.countTokens() );
                String Grp = ronny.nextToken();
                String tab = null;
                if (Grp.equals("\t")) {
                    Grp = null;
                } else {
                    tab = ronny.nextToken();
                }
                String Event = ronny.nextToken();
                if (Event.equals("\t")) {
                    Event = null;
                } else {
                    tab = ronny.nextToken();
                }
                String PlotCode = ronny.nextToken();
                if (PlotCode.equals("\t")) {
                    PlotCode = null;
                } else {
                    tab = ronny.nextToken();
                }
                String MinAge = ronny.nextToken();
                if (MinAge.equals("\t")) {
                    MinAge = null;
                } else {
                    tab = ronny.nextToken();
                }
                String MaxAge = ronny.nextToken();
                if (MaxAge.equals("\t")) {
                    MaxAge = null;
                } else {
                    tab = ronny.nextToken();
                }
                String MinDepth = ronny.nextToken();
                if (MinDepth.equals("\t")) {
                    MinDepth = null;
                } else {
                    tab = ronny.nextToken();
                }
                String MaxDepth = null;
                if (ronny.hasMoreTokens()) {
                    MaxDepth = ronny.nextToken();
                }
                ADPStratEvent adpSE = new ADPStratEvent(Grp, Event, PlotCode, MinAge, MaxAge, MinDepth, MaxDepth, 0);
                StratEvents.put(new Integer(evtno), adpSE);
            }
            buffy.close();
            return true;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Problems reading file: " + ex.getMessage());
            return false;
        }
    }

    public static boolean readLOCData(Vector LOCPoints) {
        //System.out.println("Entering readLOCData . . . ");
        JFileChooser jfc;
        if (CurDirOpen != null) {
            jfc = new JFileChooser(CurDirOpen);
        } else if (CurDirSave != null) {
            jfc = new JFileChooser(CurDirSave);
        } else {
            jfc = new JFileChooser();
        }
        jfc.setDialogTitle("Open LOC Data File");
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = jfc.showOpenDialog(null);
        if (result == JFileChooser.CANCEL_OPTION) {
            jfc = null;
            return false;
        }
        File bingo = jfc.getSelectedFile();
        CurDirOpen = jfc.getCurrentDirectory();
        jfc = null;
        BufferedReader buffy = null;
        String inLine = null;
        StringTokenizer ronny = null;
        try {
            buffy = new BufferedReader(new FileReader(bingo));
            inLine = buffy.readLine();
            //System.out.println( inLine );
            ADPApp.LOCFileComment = reDelim(inLine.trim(), "\t", "     ");
            inLine = buffy.readLine();
            inLine = buffy.readLine();
            while ((inLine = buffy.readLine()) != null) {
                ronny = new StringTokenizer(inLine, "\t");
                double age = Double.parseDouble(ronny.nextToken().trim());
                double depth = Double.parseDouble(ronny.nextToken().trim());
                LOCPoints.add(new ADPLOCPoint(age, depth));
            }
            buffy.close();
            return true;
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Problems reading file (loc method): " + ex.getMessage());
            return false;
        }

    }

    public static boolean writePlot(ADPPlotPanel adpPP) {
        // working on this 25-June-2004, trying to make it reasonable & flexible
        String[] TrialFormats = {"png", "jpeg", "tiff", "pict", "gif", "bmp"};
        String strFormat;
        try {
            Vector imgFormats = new Vector();
            imgFormats.add("svg");
            for (int i = 0; i < TrialFormats.length; i++) {
                if (ImageIO.getImageWritersByFormatName(TrialFormats[i]).hasNext()) {
                    imgFormats.add(TrialFormats[i]);
                }
            }
            Object[] ImageFormats = imgFormats.toArray();
            String msg = "Choose output format";
            String ttl = "Save plot";
            Frame nada = null;
            Icon nil = null;
            strFormat = (String) JOptionPane.showInputDialog(nada, msg, ttl, JOptionPane.PLAIN_MESSAGE,
                    nil, ImageFormats, ImageFormats[0]);
            if (strFormat == null) return false;
        } catch (Exception x) {
            return false;
        }
        JFileChooser jfc;
        if (CurDirSave != null) {
            jfc = new JFileChooser(CurDirSave);
        } else if (CurDirOpen != null) {
            jfc = new JFileChooser(CurDirOpen);
        } else {
            jfc = new JFileChooser();
        }
        jfc.setDialogTitle("Output " + strFormat + " file");
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = jfc.showSaveDialog(null);
        jfc.setVisible(false);
        if (result == JFileChooser.CANCEL_OPTION) {
            jfc = null;
            return false;
        }
        File bingo = jfc.getSelectedFile();
        CurDirSave = jfc.getCurrentDirectory();
        jfc = null;
        if (strFormat.equals("svg")) {   // custom code for svg . . .
            return writePlotAsSVG(bingo, adpPP);
        } else {  // do screen capture and save image
            adpPP.update(adpPP.getGraphics());   // force update after dialog box gone . . .
            try {
                Point pp = adpPP.getLocationOnScreen();
                Rectangle rect = new Rectangle(pp.x, pp.y, adpPP.getWidth(), adpPP.getHeight());
                Robot robby = new Robot();
                BufferedImage lePlot = robby.createScreenCapture(rect);
                ImageIO.write(lePlot, strFormat, bingo);
                return true;
            } catch (Exception ex) {
                return false;
            }
        }
    }

    public static boolean writePlotAsSVG(File bingo, ADPPlotPanel adpPP) {
        /** Width of entire plot area (including margins) in pixels */
        int pxWidth = 700;
        /** Height of entire plot area (including margins) in pixels */
        int pxHeight = 500;
        /** x margin width in pixels */
        int xMargin = 100;
        /** y margin height in pixels */
        int yMargin = 70;
        /** Location of right side of plot in pixels */
        int xRight;
        /** Location of bottom of plot in pixels */
        int yBot;
        /** Width of plot in Ma */
        double AgeWidth;
        /** Height of plot in m */
        double DepthHeight;
        /** Current axis parameters for plot */
        ADPAxisParameters adpXP;
        /** Scale along x (age) axis in pixels/Ma */
        double xScale;
        /** Scale along y (depth) axis in pixels/m */
        double yScale;
        /** A decimal format specification */
        DecimalFormat df1;

        int ix;
        int iy;

        df1 = new DecimalFormat("0.0");
        adpXP = adpPP.getAxisParameters();
        AgeWidth = adpXP.MaxAge - adpXP.MinAge;
        DepthHeight = adpXP.MaxDepth - adpXP.MinDepth;
        xScale = (pxWidth - 2.0 * xMargin) / AgeWidth;
        yScale = (pxHeight - 2.0 * yMargin) / DepthHeight;
        xRight = xMargin + (int) (AgeWidth * xScale);   // added 1/15/04 1:17 PM
        yBot = yMargin + (int) (DepthHeight * yScale);  // ditto

        PrintWriter puffy = null;
        String outLine = null;
        try {
            puffy = new PrintWriter(new FileWriter(bingo));
            puffy.println("<?xml version=\"1.0\"?>");
            puffy.println("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.0 EN\" ");
            puffy.println("   \"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\">");
            puffy.println("<svg width=\"" + pxWidth + "px\" height=\"" + pxHeight + "px\" viewBox=\"0 0 " + pxWidth + " " + pxHeight + "\">");
            puffy.println("<title>" + adpPP.PlotTitle + "</title>");
            puffy.println("<desc>SVG Age-Depth Plot Generated by ADP Version 0.98</desc>");
            puffy.println(" ");
            puffy.println("<defs>");
            puffy.println(" ");
            puffy.println("<!-- Styles for various plot elements -->");
            puffy.println("<style type=\"text/css\"><![CDATA[");
            puffy.println("  text.PlotTitle { text-anchor: middle; font-size: 18; font-weight: bold; font-family: serif; }");
            puffy.println("  text.XAxisTitle { text-anchor: middle; font-size: 14; font-weight: bold; font-family: serif; }");
            puffy.println("  text.YAxisTitle { text-anchor: middle; font-size: 14; font-weight: bold; font-family: serif; }");
            puffy.println("  text.XAxisTickLabel { text-anchor: middle; baseline-shift: -100%; font-size: 12; font-weight: bold; font-family: serif; }");
            puffy.println("  text.YAxisTickLabel { text-anchor: end; baseline-shift: -30%; font-size: 12; font-weight: bold; font-family: serif; }");
            puffy.println("  text.DataPointLabel { text-anchor: start; baseline-shift: 0%; font-size: 10; font-weight: bold; font-family: serif; }");
            puffy.println("  text.LegendLabel { text-anchor: start; baseline-shift: -30%; font-size: 12; font-weight: bold; font-family: serif; }");
            puffy.println("  line.MinorTick { stroke: black; stroke-width: 1; }");
            puffy.println("  line.MajorTick { stroke: black; stroke-width: 1.5; }");
            puffy.println("  line.AgeErrorBar { stroke: black; stroke-width: 1; }");
            puffy.println("  line.DepthErrorBar { stroke: black; stroke-width: 1; }");
            puffy.println("  line.LOCLine { stroke: green; stroke-width: 1; }");
            puffy.println("]]></style>");
            puffy.println(" ");
            puffy.println("<clipPath id=\"plotRegion\">");
            puffy.println("  <rect id=\"plotBorder\" x=\"100\" y=\"70\" width=\"500\" height=\"360\" style=\"stroke: black; fill: none; strokewidth: 2;\"/>");
            puffy.println("</clipPath>");
            puffy.println(" ");
            puffy.println("<!-- Line-of-correlation control point marker -->");
            puffy.println("<g id=\"LOCPoint\">");
            puffy.println("  <desc>A green square filled with white</desc>");
            puffy.println("  <rect x=\"-3\" y=\"-3\" width=\"6\" height=\"6\" style=\"stroke: green; stroke-width: 1; fill: white;\"/>");
            puffy.println("</g>");
            puffy.println(" ");
            puffy.println("<!-- Each of the following groups defines one of plotting symbols. -->");
            puffy.println("<!-- They are all in user coordinates (pixels by default) and centerd around (0,0). -->");
            puffy.println("<g id=\"Symbol01\">");
            puffy.println("  <desc>A red square</desc>");
            puffy.println("  <rect x=\"-3\" y=\"-3\" width=\"6\" height=\"6\" style=\"stroke: red; stroke-width: 1; fill: none;\"/>");
            puffy.println("</g>");
            puffy.println("<g id=\"Symbol02\">");
            puffy.println("  <desc>A blue triangle pointing down</desc>");
            puffy.println("  <polygon points=\"-3 -2, 0 4, 3 -2\" style=\"stroke: blue; stroke-width: 1; fill: none;\"/>");
            puffy.println("</g>");
            puffy.println("<g id=\"Symbol03\">");
            puffy.println("  <desc>A black triangle pointing up</desc>");
            puffy.println("  <polygon points=\"-3 2, 0 -4, 3 2\" style=\"stroke: black; stroke-width: 1; fill: none;\"/>");
            puffy.println("</g>");
            puffy.println("<g id=\"Symbol04\">");
            puffy.println("  <desc>A dark cyan diamond</desc>");
            puffy.println("  <polygon points=\"-4 0, 0 4, 4 0, 0 -4\" style=\"stroke: #008B8B; stroke-width: 1; fill: none;\"/>");
            puffy.println("</g>");
            puffy.println("<g id=\"Symbol05\">");
            puffy.println("  <desc>A brown circle</desc>");
            puffy.println("  <circle cx=\"0\" cy=\"0\" r=\"3\" style=\"stroke: #A52A2A; stroke-width: 1; fill: none;\"/>");
            puffy.println("</g>");
            puffy.println("<g id=\"Symbol06\">");
            puffy.println("  <desc>A solid magenta square</desc>");
            puffy.println("  <rect x=\"-3\" y=\"-3\" width=\"6\" height=\"6\" style=\"stroke: none; fill: magenta;\"/>");
            puffy.println("</g>");
            puffy.println("<g id=\"Symbol07\">");
            puffy.println("  <desc>A solid green triangle (polygon takes user coords only)</desc>");
            puffy.println("  <polygon points=\"-3 -2, 0 4, 3 -2\" style=\"stroke: green; fill: green;\"/>");
            puffy.println("</g>");
            puffy.println("<g id=\"Symbol08\">");
            puffy.println("  <desc>A solid black diamond (polygon takes user coords only)</desc>");
            puffy.println("  <polygon points=\"-4 0, 0 4, 4 0, 0 -4\" style=\"stroke: black; fill: black;\"/>");
            puffy.println("</g>");
            puffy.println("<g id=\"Symbol09\">");
            puffy.println("  <desc>A solid blue circle</desc>");
            puffy.println("  <circle cx=\"0p\" cy=\"0\" r=\"3\" style=\"stroke: none; fill: blue;\"/>");
            puffy.println("</g>");
            puffy.println("</defs>");

            puffy.println(" ");
            puffy.println("<!-- Plot border: -->");
            puffy.println("<use xlink:href=\"#plotBorder\"/>");
            //puffy.println("<rect x=\"" + xMargin + "\" y=\"" + yMargin + "\" " +
            //              "width=\"" + ((int)(xScale*AgeWidth)) + "\" height=\"" + ((int)(yScale*DepthHeight)) + "\" " +
            //              "style=\"stroke: black; fill: none; strokewidth: 2;\"/>");

            ix = pxWidth / 2;
            iy = yMargin - 30;
            puffy.println(" ");
            puffy.println("<!-- Plot title: -->");
            puffy.println("<text class=\"PlotTitle\" x=\"" + ix + "\" y=\"" + iy + "\">" +
                    //" style=\"text-anchor: middle; font-size: 18; font-weight: bold; font-family: serif;\">" +
                    adpPP.PlotTitle + "</text>");

            ix = xMargin - 60;
            iy = yMargin + (int) ((DepthHeight / 2.0) * yScale);
            puffy.println(" ");
            puffy.println("<!-- Depth axis title: -->");
            puffy.println("<text class=\"YAxisTitle\" x=\"" + ix + "\" y = \"" + iy + "\" " +
                    //"style=\"text-anchor: middle;font-size: 14; font-weight: bold; font-family: serif;\" " +
                    "transform=\"rotate(-90," + ix + "," + iy + ")\">" +
                    adpPP.YAxisTitle + "</text>");

            ix = pxWidth / 2;
            iy = yBot + 50;
            puffy.println(" ");
            puffy.println("<!-- Age axis title: -->");
            puffy.println("<text class=\"XAxisTitle\" x=\"" + ix + "\" y=\"" + iy + "\">" +
                    //"style=\"text-anchor: middle; font-size: 14; font-weight: bold; font-family: serif;\">" +
                    adpPP.XAxisTitle + "</text>");

            puffy.println(" ");
            puffy.println("<!-- Depth axis minor (unlabeled) tick marks: -->");
            if (adpXP.DepthIncMinor > 0.0) {
                double dmin = adpXP.DepthIncMinor * Math.ceil(adpXP.MinDepth / adpXP.DepthIncMinor);
                for (double depth = dmin; depth < (adpXP.MaxDepth + .1 * adpXP.DepthIncMinor); depth += adpXP.DepthIncMinor) {
                    puffy.println("<line class=\"MinorTick\" " +
                            "x1=\"" + xMargin + "\" " +
                            "y1=\"" + (yMargin + (int) ((depth - adpXP.MinDepth) * yScale)) + "\" " +
                            "x2=\"" + (xMargin - 7) + "\" " +
                            "y2=\"" + (yMargin + (int) ((depth - adpXP.MinDepth) * yScale)) + "\"/>");
                    //"style=\"stroke: black; stroke-width: 1;\"/>");
                }
            }

            puffy.println(" ");
            puffy.println("<!-- Depth axis major tick marks and labels: -->");
            if (adpXP.DepthIncMajor > 0.0) {
                double dmin = adpXP.DepthIncMajor * Math.ceil(adpXP.MinDepth / adpXP.DepthIncMajor);
                for (double depth = dmin; depth < (adpXP.MaxDepth + .1 * adpXP.DepthIncMajor); depth += adpXP.DepthIncMajor) {
                    puffy.println("<line class=\"MajorTick\" " +
                            "x1=\"" + xMargin + "\" " +
                            "y1=\"" + (yMargin + (int) ((depth - adpXP.MinDepth) * yScale)) + "\" " +
                            "x2=\"" + (xMargin - 10) + "\" " +
                            "y2=\"" + (yMargin + (int) ((depth - adpXP.MinDepth) * yScale)) + "\"/>");
                    //"style=\"stroke: black; stroke-width: 1.5;\"/>");
                    puffy.println("<text class=\"YAxisTickLabel\" " +
                            "x=\"" + (xMargin - 13) + "\" " +
                            "y=\"" + (yMargin + (int) ((depth - adpXP.MinDepth) * yScale)) + "\">" +
                            //"style=\"text-anchor: end; baseline-shift: -30%; font-size: 12; font-weight: bold; font-family: serif;\">" +
                            df1.format(depth) + "</text>");
                }
            }

            puffy.println(" ");
            puffy.println("<!-- Age axis minor (unlabeled) tick marks: -->");
            if (adpXP.AgeIncMinor > 0.0) {
                double amin = adpXP.AgeIncMinor * Math.ceil(adpXP.MinAge / adpXP.AgeIncMinor);
                for (double age = amin; age < (adpXP.MaxAge + .1 * adpXP.AgeIncMinor); age += adpXP.AgeIncMinor) {
                    //g.drawLine( xMargin+(int)((age-MinAge)*xScale), yMargin+(int)(DepthHeight*yScale), xMargin+(int)((age-MinAge)*xScale), yMargin+(int)(DepthHeight*yScale)+7 );
                    puffy.println("<line class=\"MinorTick\" " +
                            "x1=\"" + (xMargin + (int) ((age - adpXP.MinAge) * xScale)) + "\" " +
                            "y1=\"" + (yBot) + "\" " +
                            "x2=\"" + (xMargin + (int) ((age - adpXP.MinAge) * xScale)) + "\" " +
                            "y2=\"" + (yBot + 7) + "\"/>");
                    //"style=\"stroke: black; stroke-width: 1;\"/>");
                }
            }

            puffy.println(" ");
            puffy.println("<!-- Age axis major tick marks and labels: -->");
            if (adpXP.AgeIncMajor > 0.0) {
                double amin = adpXP.AgeIncMajor * Math.ceil(adpXP.MinAge / adpXP.AgeIncMajor);
                for (double age = amin; age < (adpXP.MaxAge + .1 * adpXP.AgeIncMajor); age += adpXP.AgeIncMajor) {
                    //g.drawLine( xMargin+(int)((age-MinAge)*xScale), yMargin+(int)(DepthHeight*yScale), xMargin+(int)((age-MinAge)*xScale), yMargin+(int)(DepthHeight*yScale)+10 );
                    puffy.println("<line class=\"MajorTick\" " +
                            "x1=\"" + (xMargin + (int) ((age - adpXP.MinAge) * xScale)) + "\" " +
                            "y1=\"" + (yBot) + "\" " +
                            "x2=\"" + (xMargin + (int) ((age - adpXP.MinAge) * xScale)) + "\" " +
                            "y2=\"" + (yBot + 10) + "\"/>");
                    //"style=\"stroke: black; stroke-width: 1.5;\"/>");
                    //drawJustifiedString( g, df1.format(age), xMargin+(int)((age-MinAge)*xScale), yMargin+(int)(DepthHeight*yScale)+13, 0.5, 1.0 );
                    puffy.println("<text class=\"XAxisTickLabel\" " +
                            "x=\"" + (xMargin + (int) ((age - adpXP.MinAge) * xScale)) + "\" " +
                            "y=\"" + (yBot + 13) + "\">" +
                            //"style=\"text-anchor: middle; baseline-shift: -100%; font-size: 12; font-weight: bold; font-family: serif;\">" +
                            df1.format(age) + "</text>");
                }
            }

            if (ADPSymbolPalette.PlotCodes != null) {
                puffy.println(" ");
                puffy.println("<!-- Legend: -->");
                ix = xRight + 20;
                iy = yMargin + 10;
                for (int j = 0; j < ADPSymbolPalette.PlotCodes.size(); j++) {
                    if (((Boolean) ADPSymbolPalette.PlotGroupOn.get(j)).booleanValue() &
                            ((Boolean) ADPSymbolPalette.PlotGroupPresent.get(j)).booleanValue()) {
                        String grp = (String) ADPSymbolPalette.PlotCodes.get(j);
                        // 14-Apr-05:  Bad, bad, bad...
                        if (grp.equals("F")) {
                            grp = "Forams";
                        } else if (grp.equals("D")) {
                            grp = "Diatoms";
                        } else if (grp.equals("R")) {
                            grp = "Rads";
                        } else if (grp.equals("N")) {
                            grp = "Nannos";
                        }
                        //drawSymbol( g, ix+20, iy+j*20, grp );
                        puffy.println("<use xlink:href=\"#Symbol0" + (j + 1) + "\" x=\"" + ix + "\" y=\"" + iy + "\"/>");
                        //g.drawString( grp, ix+30, iy+j*20+5 );
                        puffy.println("<text class=\"LegendLabel\" " +
                                "x=\"" + (ix + 10) + "\" " +
                                "y=\"" + iy + "\">" +
                                //"style=\"text-anchor: start; baseline-shift: -30%; font-size: 12; font-weight: bold; font-family: serif;\">" +
                                grp + "</text>");
                        iy += 20;
                    }
                }
            }

            // Will now use clipPath instead . . . (20-Sept-04)
            //puffy.println(" ");
            //puffy.println("<!-- Limiting viewport to plot region -->");
            //int pxw = (int)(xScale*AgeWidth);
            //int pxh = (int)(yScale*DepthHeight);
            //puffy.println("<svg x=\"" + xMargin + "\" y=\"" + yMargin + "\" " +
            //              "width=\"" + pxw + "\" " +
            //              "height=\"" + pxh + "\" " +
            //              "viewBox =\"0 0 " + pxw + " " + pxh +"\">");

            if (ADPApp.StratEvents != null) {
                puffy.println(" ");
                puffy.println("<!-- Data points: -->");
                Enumeration se = ADPApp.StratEvents.elements();
                while (se.hasMoreElements()) {
                    ADPStratEvent evt = (ADPStratEvent) se.nextElement();
                    boolean drawIt = true;
                    int igrp = ADPSymbolPalette.PlotCodes.indexOf(evt.EventGroup);
                    if (igrp < 0) drawIt = false;
                    if (drawIt) {
                        try {
                            drawIt = ((Boolean) ADPSymbolPalette.PlotGroupOn.get(igrp)).booleanValue();
                        } catch (Exception x) {
                            drawIt = false;
                        }
                    }
                    if (Double.isNaN(evt.MidAge) || Double.isNaN(evt.MidDepth)) drawIt = false;
                    // 20-Sept-04:  Now only plotting points in plot region since Illustrator
                    // does not heed "clipPath" in "use" elements and Batik does not
                    // draw "use" elements with clipPath included . . .
                    if (evt.MidAge <= adpXP.MinAge || evt.MidAge >= adpXP.MaxAge) drawIt = false;
                    if (evt.MidDepth <= adpXP.MinDepth || evt.MidDepth >= adpXP.MaxDepth) drawIt = false;
                    if (drawIt) {
                        if (!Double.isNaN(evt.MinAge) && !Double.isNaN(evt.MaxAge)) {
                            //g.drawLine( xmg+(int)(xScale*(MinAge-RefAge)), ymg+(int)(yScale*(MidDepth-RefDepth)), xmg+(int)(xScale*(MaxAge-RefAge)), ymg+(int)(yScale*(MidDepth-RefDepth)) );
                            //adding clipPath in 20-Sept-04;  have to add xMargin and yMargin back in,
                            //since not using embedded svg element
                            puffy.println("<line class=\"AgeErrorBar\" " +
                                    "x1=\"" + (xMargin + (int) (xScale * (evt.MinAge - adpXP.MinAge))) + "\" " +
                                    "y1=\"" + (yMargin + (int) (yScale * (evt.MidDepth - adpXP.MinDepth))) + "\" " +
                                    "x2=\"" + (xMargin + (int) (xScale * (evt.MaxAge - adpXP.MinAge))) + "\" " +
                                    "y2=\"" + (yMargin + (int) (yScale * (evt.MidDepth - adpXP.MinDepth))) + "\" " +
                                    "style=\"clip-path: url(#plotRegion);\"/>");
                        }
                        if (!Double.isNaN(evt.MinDepth) && !Double.isNaN(evt.MaxDepth)) {
                            //System.out.println( "MinDepth, MaxDepth, MidAge: " + df2.format(MinDepth) + ", " + df2.format(MaxDepth) + ", " + df2.format(MidAge) );
                            //g.drawLine( xmg+(int)(xScale*(MidAge-RefAge)), ymg+(int)(yScale*(MinDepth-RefDepth)), xmg+(int)(xScale*(MidAge-RefAge)), ymg+(int)(yScale*(MaxDepth-RefDepth)) );
                            puffy.println("<line class=\"DepthErrorBar\" " +
                                    "x1=\"" + (xMargin + (int) (xScale * (evt.MidAge - adpXP.MinAge))) + "\" " +
                                    "y1=\"" + (yMargin + (int) (yScale * (evt.MinDepth - adpXP.MinDepth))) + "\" " +
                                    "x2=\"" + (xMargin + (int) (xScale * (evt.MidAge - adpXP.MinAge))) + "\" " +
                                    "y2=\"" + (yMargin + (int) (yScale * (evt.MaxDepth - adpXP.MinDepth))) + "\" " +
                                    "style=\"clip-path: url(#plotRegion);\"/>");
                        }
                        ix = xMargin + (int) (xScale * (evt.MidAge - adpXP.MinAge));
                        iy = yMargin + (int) (yScale * (evt.MidDepth - adpXP.MinDepth));
                        // can't use clipPath, unfortunately (Illustrator ignores, Batik won't draw) . . .
                        puffy.println("<use xlink:href=\"#Symbol0" + (igrp + 1) + "\" x=\"" + ix + "\" y=\"" + iy + "\"/>");
                        if (adpPP.ShowLabels) {
                            puffy.println("<text class=\"DataPointLabel\" " +
                                    "x=\"" + (ix + 7) + "\" " +
                                    "y=\"" + (iy - 3) + "\">" +
                                    //"style=\"text-anchor: start; baseline-shift: 0%; font-size: 10; font-weight: bold; font-family: serif;\">" +
                                    evt.EventLabel + "</text>");

                        }
                    }  // end if drawIt        
                }  // end while se.hasMoreElements 
            }  // end if StratEvents!=null

            if (ADPApp.LOCPoints != null) {

                puffy.println(" ");
                puffy.println("<!-- Line of correlation: line segments -->");
                Enumeration eloc = ADPApp.LOCPoints.elements();
                ADPLOCPoint adp = (ADPLOCPoint) eloc.nextElement();
                int px0 = xMargin + (int) ((adp.Age - adpXP.MinAge) * xScale);
                int py0 = yMargin + (int) ((adp.Depth - adpXP.MinDepth) * yScale);
                while (eloc.hasMoreElements()) {
                    adp = (ADPLOCPoint) eloc.nextElement();
                    // Adding xMargin, yMargin back in, 20-Sept-04
                    int px1 = xMargin + (int) ((adp.Age - adpXP.MinAge) * xScale);
                    int py1 = yMargin + (int) ((adp.Depth - adpXP.MinDepth) * yScale);
                    // Adding clipPath, 20-Sept-04
                    puffy.println("<line class=\"LOCLine\" " +
                            "x1=\"" + px0 + "\" " +
                            "y1=\"" + py0 + "\" " +
                            "x2=\"" + px1 + "\" " +
                            "y2=\"" + py1 + "\" " +
                            "style=\"clip-path: url(#plotRegion);\"/>");
                    px0 = px1;
                    py0 = py1;
                }

                puffy.println(" ");
                puffy.println("<!-- Line of correlation: control points -->");
                eloc = ADPApp.LOCPoints.elements();   // start over again for control poinnts, which need to be on top of everything
                while (eloc.hasMoreElements()) {
                    adp = (ADPLOCPoint) eloc.nextElement();
                    int px = xMargin + (int) ((adp.Age - adpXP.MinAge) * xScale);
                    int py = yMargin + (int) ((adp.Depth - adpXP.MinDepth) * yScale);
                    if (xMargin < px && px < xRight && yMargin < py && py < yBot) {  // 20-Sept-04
                        puffy.println("<use xlink:href=\"#LOCPoint\" x=\"" + px + "\" y=\"" + py + "\"/>");
                    }
                    //puffy.println("<rect x=\"" + (px-3) + "\" " +
                    //                    "y=\"" + (py-3) + "\" " +
                    //                    "width=\"6\" height=\"6\" " +
                    //                    "style=\"stroke: green; stroke-width: 1; fill: white;\"/>");
                }
            }  // end if LOCPoints != null

            //removing embedded svg element, 20-Sept-04...
            //puffy.println(" ");
            //puffy.println("</svg>");

            puffy.println(" ");
            puffy.println("</svg>");
            puffy.close();
            return true;
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Problems writing file: " + ex.getMessage());
            return false;
        }
    }

    public static boolean writeLOCData(Vector LOCPoints) {
        //System.out.println("Entering writeLOCData . . . ");
        JFileChooser jfc;

        if (CurDirSave != null) {
            jfc = new JFileChooser(CurDirSave);
        } else if (CurDirOpen != null) {
            jfc = new JFileChooser(CurDirOpen);
        } else {
            jfc = new JFileChooser();
        }

        jfc.setSelectedFile(new File(ADPApp.HoleID + ".loc"));
        jfc.setDialogTitle("Save LOC Data File");
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        //
        int result = jfc.showSaveDialog(null);
        if (result == JFileChooser.CANCEL_OPTION) {
            jfc = null;
            return false;
        }
        File bingo = jfc.getSelectedFile();
        CurDirSave = jfc.getCurrentDirectory();
        jfc = null;
        PrintWriter puffy = null;
        String outLine = null;
        try {
            puffy = new PrintWriter(new FileWriter(bingo));
            //System.out.println( inLine );
            Calendar today = Calendar.getInstance();
            int yr = today.get(Calendar.YEAR);
            int mo = today.get(Calendar.MONTH) + 1;  // 0-based months!
            int dy = today.get(Calendar.DAY_OF_MONTH);
            String dt = "" + yr + (mo < 10 ? ("0" + mo) : ("" + mo)) + (dy < 10 ? ("0" + dy) : ("" + dy));
            puffy.println(ADPApp.HoleID + "\t" + dt);
            puffy.println("AGE\tDEPTH");
            int n = ADPApp.LOCPoints.size();
            puffy.println("" + n);
            DecimalFormat df5 = new DecimalFormat("0.0####");
            for (int i = 0; i < n; i++) {
                // 09-Mar-05:  Switching ADPAgeDepthPoint to ADPLOCPoint
                ADPLOCPoint adp = (ADPLOCPoint) ADPApp.LOCPoints.get(i);
                puffy.println(df5.format(adp.Age) + "\t" + df5.format(adp.Depth));
            }
            puffy.close();
            return true;
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Problems writing file: " + ex.getMessage());
            return false;
        }
    }

    public static boolean writeProjData(Hashtable StratEvents, Vector LOCPoints) {
        //System.out.println("Entering writeLOCData . . . ");
        String WhoBe = JOptionPane.showInputDialog("Your name or initials: ", "");
        if (WhoBe == null) return false;
        String Comment = JOptionPane.showInputDialog("Header line comment: ", "");
        if (Comment == null) return false;
        JFileChooser jfc;
        if (CurDirSave != null) {
            jfc = new JFileChooser(CurDirSave);
        } else if (CurDirOpen != null) {
            jfc = new JFileChooser(CurDirOpen);
        } else {
            jfc = new JFileChooser();
        }
        jfc.setDialogTitle("Save Projected Data File");
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = jfc.showSaveDialog(null);
        if (result == JFileChooser.CANCEL_OPTION) {
            jfc = null;
            return false;
        }
        File bingo = jfc.getSelectedFile();
        CurDirSave = jfc.getCurrentDirectory();
        jfc = null;
        PrintWriter puffy = null;
        DecimalFormat df5 = new DecimalFormat("0.0####");
        try {
            puffy = new PrintWriter(new FileWriter(bingo));
            puffy.println(ADPApp.HoleID + "\t" + 0 + "\t" + WhoBe + "\t" + Comment);
            puffy.println("Grp\tEvent\tPlotcode\tYoung Age\tOld Age\tTop depth\tBottom depth");
            Enumeration ese = StratEvents.elements();
            while (ese.hasMoreElements()) {
                ADPStratEvent se = (ADPStratEvent) ese.nextElement();
                Enumeration eloc = LOCPoints.elements();
                ADPAgeDepthPoint adp = (ADPAgeDepthPoint) eloc.nextElement();
                double a0 = adp.Age;
                double d0 = adp.Depth;
                double ProjMinAge = Double.NaN;   // projected min age (gcb, 2/18/04)
                double ProjMaxAge = Double.NaN;   // projected max age
                while (eloc.hasMoreElements()) {
                    adp = (ADPAgeDepthPoint) eloc.nextElement();
                    double a1 = adp.Age;
                    double d1 = adp.Depth;
                    if (d0 <= se.MinDepth && se.MinDepth <= d1) {
                        if (d1 - d0 > 0.0) {
                            ProjMinAge = a0 + (a1 - a0) * (se.MinDepth - d0) / (d1 - d0);
                        } else {
                            ProjMinAge = a0;
                        }
                    }
                    if (d0 <= se.MaxDepth && se.MaxDepth <= d1) {
                        if (d1 - d0 > 0.0) {
                            ProjMaxAge = a0 + (a1 - a0) * (se.MaxDepth - d0) / (d1 - d0);
                        } else {
                            ProjMaxAge = a0;
                        }
                    }
                    a0 = a1;
                    d0 = d1;
                }
                puffy.println(se.EventGroup + "\t" + se.EventName + "\t" + se.EventLabel + "\t" +
                        (Double.isNaN(ProjMinAge) ? "" : df5.format(ProjMinAge)) + "\t" +
                        (Double.isNaN(ProjMaxAge) ? "" : df5.format(ProjMaxAge)) + "\t" +
                        df5.format(se.MinDepth) + "\t" + df5.format(se.MaxDepth));
            }
            puffy.close();
            return true;
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Problems writing file: " + ex.getMessage());
            return false;
        }
    }

    private static String reDelim(String oldString, String oldDelim, String newDelim) {
        StringTokenizer sluggo = new StringTokenizer(oldString, oldDelim);
        String newString = sluggo.nextToken();
        while (sluggo.hasMoreTokens()) {
            newString += newDelim + sluggo.nextToken();
        }
        return newString;
    }

    /**
     * Generates BufferedReader for query results serialized as CSV
     *
     * @param query the neptune query to be processed
     * @return BufferedReader of query results serialized as CSV
     * @throws Exception
     */
    public static BufferedReader getCSVReaderFromQuery(String query) throws Exception {
        try {
            URL url = new URL("http://services.chronos.org/qdf/query/neptune?sql=" + Twentify(query) + "&serializeAs=csv");
            //System.out.println(url);
            return new BufferedReader(new InputStreamReader(url.openStream()));
        } catch (Exception ex) {
            throw new Exception(ex);
        }
    }

    /**
     * Generates BufferedReader for query results serialized as CSV
     *
     * @param query the neptune query to be processed
     * @return BufferedReader of query results serialized as CSV
     * @throws Exception
     */
    public static BufferedReader getCSVReaderFromJanusAmp(String query) throws Exception {
        try {
            URL url = new URL("http://services.chronos.org/qdf/query/neptune?sql=" + Twentify(query) + "&serializeAs=csv");
            //System.out.println(url);
            return new BufferedReader(new InputStreamReader(url.openStream()));
        } catch (Exception ex) {
            throw new Exception(ex);
        }
    }

    /**
     * Replaces blanks with %20 for use in URL query
     *
     * @param in the String to be processed
     * @return the processed String
     */
    public static String Twentify(String in) {
        StringTokenizer ronny = new StringTokenizer(in);
        if (ronny.countTokens() == 0) return null;
        String out = ronny.nextToken();  // first word
        while (ronny.hasMoreTokens()) {
            out += "%20" + ronny.nextToken();
        }
        return out;
    }


}
