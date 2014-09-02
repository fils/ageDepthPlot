/*
 * dlgAxisParameters.java
 *
 * Created on January 17, 2004, 2:24 PM
 * Last modified 2/9/04 4:10 PM
 * 07-Apr-04: Starting to add javadoc comments
 */

/**
 * Dialog box for specifying axis parameters
 * 
 * @version 0.90 07-Apr-04
 * @author  gcb
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.text.*;

public class dlgAxisParameters extends JDialog implements ActionListener {
    public ADPAxisParameters adpAP;         // local copy of axis parameters
    private JButton cmdCancel;
    private JButton cmdDefaults;
    private JButton cmdOK;
    private JTextField edtMajAgeInc;
    private JTextField edtMajDepthInc;
    private JTextField edtMaxAge;
    private JTextField edtMaxDepth;
    private JTextField edtMinAge;
    private JTextField edtMinAgeInc;
    private JTextField edtMinDepth;
    private JTextField edtMinDepthInc;
    private JLabel lblAge;
    private JLabel lblDepth;
    private JLabel lblMajInt;
    //private JLabel lblMajTick;
    private JLabel lblMax;
    private JLabel lblMin;
    private JLabel lblMinInt;
    //private JLabel lblMinTick;
    private DecimalFormat df1 = new DecimalFormat("0.0######");
    private boolean blnOK;  // true when OK button clicked . . .
    
    /** Creates a new instance of dlgAxisParameters */
    public dlgAxisParameters(JFrame parent) {
        super(parent, "ADP Axis Parameters", true);
        
        setLocation(200,200);

        //lblMajTick = new JLabel();
        //lblMinTick = new JLabel();
        lblMin = new JLabel();
        lblMax = new JLabel();
        lblMajInt = new JLabel();
        lblMinInt = new JLabel();
        lblDepth = new JLabel();
        edtMinDepth = new JTextField();
        edtMaxDepth = new JTextField();
        edtMajDepthInc = new JTextField();
        edtMinDepthInc = new JTextField();
        lblAge = new JLabel();
        edtMinAge = new JTextField();
        edtMaxAge = new JTextField();
        edtMajAgeInc = new JTextField();
        edtMinAgeInc = new JTextField();
        cmdOK = new JButton();
        cmdDefaults = new JButton();
        cmdCancel = new JButton();
        
        Container c = getContentPane();
        c.setLayout( new BorderLayout(10,10) );
        
        // following panels just provide comfortable margins
        JPanel NorthPanel = new JPanel();
        NorthPanel.setMinimumSize( new Dimension( 1, 10 ) );  // min top margin of 10 pixels
        c.add(NorthPanel, BorderLayout.NORTH);
        JPanel WestPanel = new JPanel();
        WestPanel.setMinimumSize( new Dimension( 10, 1 ) );   // min left margin of 10 pixels
        c.add(WestPanel, BorderLayout.WEST);
        JPanel EastPanel = new JPanel();
        EastPanel.setMinimumSize( new Dimension( 10, 1 ) );  // min right margin of 10 pixels
        c.add(EastPanel, BorderLayout.EAST);
         
        JPanel GridPanel = new JPanel();
        
        GridPanel.setLayout( new GridLayout(3,5,15,5) );
        
        /*
        for (int i=0; i<3; i++) GridPanel.add( new JLabel(" ") );
        lblMajTick.setText("Major Tick");
        lblMajTick.setHorizontalAlignment( JLabel.CENTER );
        GridPanel.add( lblMajTick );
        lblMinTick.setText("Minor Tick");
        lblMinTick.setHorizontalAlignment( JLabel.CENTER );
        GridPanel.add( lblMinTick );
         */
          
        GridPanel.add( new JLabel(" ") );
        
        lblMin.setText("Minimum");
        lblMin.setHorizontalAlignment( JLabel.CENTER );
        GridPanel.add(lblMin);

        lblMax.setText("Maximum");
        lblMax.setHorizontalAlignment( JLabel.CENTER );
        GridPanel.add(lblMax);

        lblMajInt.setText("Major Ticks");
        lblMajInt.setHorizontalAlignment( JLabel.CENTER );
        GridPanel.add(lblMajInt);

        lblMinInt.setText("Minor Ticks");
        lblMinInt.setHorizontalAlignment( JLabel.CENTER );
        GridPanel.add(lblMinInt);

        lblDepth.setText("Depth (m)");
        lblDepth.setHorizontalAlignment( JLabel.LEFT );
        GridPanel.add(lblDepth);

        edtMinDepth.setText("");
        edtMinDepth.setHorizontalAlignment(JTextField.CENTER);
        //edtMinDepth.addActionListener(this);  // 11-Feb-04:  will process changes when OK clicked
        GridPanel.add(edtMinDepth);

        edtMaxDepth.setText("");
        edtMaxDepth.setHorizontalAlignment(JTextField.CENTER);
        //edtMaxDepth.addActionListener(this);
        GridPanel.add(edtMaxDepth);
        
        edtMajDepthInc.setText("");
        edtMajDepthInc.setHorizontalAlignment(JTextField.CENTER);
        //edtMajDepthInc.addActionListener(this);
        GridPanel.add(edtMajDepthInc);

        edtMinDepthInc.setText("");
        edtMinDepthInc.setHorizontalAlignment(JTextField.CENTER);
        //edtMinDepthInc.addActionListener(this);
        GridPanel.add(edtMinDepthInc);

        lblAge.setText("Age (ma)");
        lblAge.setHorizontalAlignment( JLabel.LEFT );
        GridPanel.add(lblAge);

        edtMinAge.setText("");
        edtMinAge.setHorizontalAlignment(JTextField.CENTER);
        //edtMinAge.addActionListener(this);
        GridPanel.add(edtMinAge);

        edtMaxAge.setText("");
        edtMaxAge.setHorizontalAlignment(JTextField.CENTER);
        //edtMaxAge.addActionListener(this);
        GridPanel.add(edtMaxAge);

        edtMajAgeInc.setText("");
        edtMajAgeInc.setHorizontalAlignment(JTextField.CENTER);
        //edtMajAgeInc.addActionListener(this);
        GridPanel.add(edtMajAgeInc);

        edtMinAgeInc.setText("");
        edtMinAgeInc.setHorizontalAlignment(JTextField.CENTER);
        //edtMinAgeInc.addActionListener(this);
        GridPanel.add(edtMinAgeInc);
                        
        c.add( GridPanel, BorderLayout.CENTER );
        
        JPanel ButtonPanel = new JPanel();
        ButtonPanel.setLayout( new FlowLayout(FlowLayout.CENTER, 20, 5) );
              
        cmdOK.setText("OK");
        cmdOK.addActionListener(this);
        ButtonPanel.add(cmdOK);

        cmdDefaults.setText("Defaults");
        cmdDefaults.addActionListener(this);
        ButtonPanel.add(cmdDefaults);

        cmdCancel.setText("Cancel");
        cmdCancel.addActionListener(this);
        ButtonPanel.add(cmdCancel);        
        
        c.add( ButtonPanel, BorderLayout.SOUTH );
        
        pack();
    }
    
    public void actionPerformed( ActionEvent e ) {
        if ( e.getSource()==cmdOK ) {
            // process depth axis inputs
            try {
                adpAP.MinDepth = Double.parseDouble( edtMinDepth.getText() );
            } catch ( NumberFormatException nfx ) {
                JOptionPane.showMessageDialog(null,"Minimum depth must be numeric.");
                return;  // back to dialog box for another try
            }
            try {
                adpAP.MaxDepth = Double.parseDouble( edtMaxDepth.getText() );
                if ( adpAP.MaxDepth < adpAP.MinDepth ) {
                    JOptionPane.showMessageDialog(null,"Maximum depth must be greater than minimum depth.");
                    return;  // back to dialog box
                }
            } catch ( NumberFormatException nfx ) {
                JOptionPane.showMessageDialog(null,"Maximum depth must be numeric.");
                return;  // back to dialog box
            }
            try {
                adpAP.DepthIncMajor = Double.parseDouble( edtMajDepthInc.getText() );
            } catch ( NumberFormatException nfx ) {
                // in this case, just set tick interval to reasonable value and go on
                adpAP.DepthIncMajor = ADPApp.pretty( (adpAP.MaxDepth-adpAP.MinDepth)/5.0 );
            }
            try {
                adpAP.DepthIncMinor = Double.parseDouble( edtMinDepthInc.getText() );
                // make sure minor interval is an even divisor of major interval
                if ( adpAP.DepthIncMinor > 0.0 ) {   // if <=0 just leave it; will not be drawn
                    double n = Math.round( adpAP.DepthIncMajor/adpAP.DepthIncMinor );
                    if ( n < 1.0 ) n = 1.0;
                    adpAP.DepthIncMinor = adpAP.DepthIncMajor/n;
                }
            } catch ( NumberFormatException nfx ) {
                adpAP.DepthIncMinor = adpAP.DepthIncMajor/5.0;  // fix it and go on
            }
            // process age axis parameter input
            try {
                adpAP.MinAge = Double.parseDouble( edtMinAge.getText() );
            } catch ( NumberFormatException nfx ) {
                JOptionPane.showMessageDialog(null,"Minimum Age must be numeric.");
                return;  // back to dialog box
            }
            try {
                adpAP.MaxAge = Double.parseDouble( edtMaxAge.getText() );
                if ( adpAP.MaxAge < adpAP.MinAge ) {
                    JOptionPane.showMessageDialog(null,"Maximum Age must be greater than minimum Age.");
                    return;  // back to dialog box
                }
            } catch ( NumberFormatException nfx ) {
                JOptionPane.showMessageDialog(null,"Maximum Age must be numeric.");
                return;  // back to dialog box
            }
            try {
                adpAP.AgeIncMajor = Double.parseDouble( edtMajAgeInc.getText() );
            } catch ( NumberFormatException nfx ) {
                // set to reasonable value and go on
                adpAP.AgeIncMajor = ADPApp.pretty( (adpAP.MaxAge-adpAP.MinAge)/5.0 );
            }
            try {
                adpAP.AgeIncMinor = Double.parseDouble( edtMinAgeInc.getText() );
                if ( adpAP.AgeIncMinor > 0.0 ) {  // if <=0 leave it; will not be printed
                    double n = Math.round( adpAP.AgeIncMajor/adpAP.AgeIncMinor );
                    if ( n < 1.0 ) n = 1.0;
                    adpAP.AgeIncMinor = adpAP.AgeIncMajor/n;
                }
            } catch ( NumberFormatException nfx ) {
                adpAP.AgeIncMinor = adpAP.AgeIncMajor/5.0;  // fix it and go on
            }
            blnOK = true;
            setVisible(false);
        } else if ( e.getSource()==cmdDefaults ) {
            adpAP = new ADPAxisParameters( ADPApp.getDefaultAxisParameters() );
            edtMinDepth.setText( df1.format(adpAP.MinDepth) );
            edtMaxDepth.setText( df1.format(adpAP.MaxDepth) );
            edtMajDepthInc.setText( df1.format(adpAP.DepthIncMajor) );
            edtMinDepthInc.setText( df1.format(adpAP.DepthIncMinor) );
            edtMinAge.setText( df1.format(adpAP.MinAge) );
            edtMaxAge.setText( df1.format(adpAP.MaxAge) );
            edtMajAgeInc.setText( df1.format(adpAP.AgeIncMajor) );
            edtMinAgeInc.setText( df1.format(adpAP.AgeIncMinor) );
            // then just return to dialog box
        } else if ( e.getSource()==cmdCancel ) {
            blnOK = false;
            setVisible(false);
        }      
    }
    
    public boolean showDialog( ADPAxisParameters[] adpap ) {
        adpAP = new ADPAxisParameters( adpap[0] );  // local copy of axis parameters
        edtMinDepth.setText( df1.format(adpAP.MinDepth) );
        edtMaxDepth.setText( df1.format(adpAP.MaxDepth) );
        edtMajDepthInc.setText( df1.format(adpAP.DepthIncMajor) );
        edtMinDepthInc.setText( df1.format(adpAP.DepthIncMinor) );
        edtMinAge.setText( df1.format(adpAP.MinAge) );
        edtMaxAge.setText( df1.format(adpAP.MaxAge) );
        edtMajAgeInc.setText( df1.format(adpAP.AgeIncMajor) );
        edtMinAgeInc.setText( df1.format(adpAP.AgeIncMinor) );
        blnOK = false;
        show();
        if ( blnOK ) {  // if changes accepted, replace input parameters w/ new ones
            adpap[0] = new ADPAxisParameters( adpAP );
        }
        return blnOK;
    }
    
}
