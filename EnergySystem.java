/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author Joseph
 */

import com.wolfram.jlink.*;
import java.awt.Color;
import java.io.File;
import java.math.RoundingMode;
import java.text.NumberFormat;
import javax.swing.ImageIcon;
import javax.swing.JLayeredPane;

public class EnergySystem {

    public static String mathKernelPath = "";
    public static ESGUI gui = null;
    public static KernelLink ml = null;

    //Static accessor to the last Genetic Algorithm that was run:
    public static GeneticAlgorithm GA = null;
    //Convergence Sensitivity: limit of identical epoch results before termination
    public static final int CONVERGENCE_SENSITIVITY = 70;
    //Generation Limit: total number of generations that can pass before termination
    public static int GENERATION_LIMIT = 1500;


    /**
     * @param args the command line arguments
     */

    //************************************************************************************************
    //*Initialization Functions                                                                   *
    //************************************************************************************************
    public static void main(String[] args) {


        //Check to see if Mathematica is installed automatically. If not found, prompt user to locate it.
        File kernel;
        if (LinkLocator.isWindows()) {
            kernel = new File("C:\\Program Files\\Wolfram Research\\Mathematica\\8.0\\MathKernel.exe");
            if (kernel.exists()) {
                mathKernelPath = "C:\\Program Files\\Wolfram Research\\Mathematica\\8.0\\MathKernel.exe";
            } else {
                kernel = new File("C:\\Program Files\\Wolfram Research\\Mathematica\\7.0\\MathKernel.exe");
                if (kernel.exists()) {
                    mathKernelPath = "C:\\Program Files\\Wolfram Research\\Mathematica\\7.0\\MathKernel.exe";
                } else {
                    LinkLocator locator = new LinkLocator(gui,true);
                    locator.setVisible(true);
                }
            }
        }

        if (LinkLocator.isMac()) {
            kernel = new File("/Applications/Mathematica.app/Contents/MacOS/MathKernel");
            if (kernel.exists()) {
                mathKernelPath = "/Applications/Mathematica.app/Contents/MacOS/MathKernel";
            } else {
                    LinkLocator locator = new LinkLocator(gui,true);
                    locator.setVisible(true);
            }
        }

        //Create new instance of the GUI and make it visible.
        gui = new ESGUI();
        gui.setVisible(true);
        setNetworkOrderImages();


        //Inform the user as to whether the MathKernel path has been set. If not, exit.
        if (mathKernelPath.equals("")) {
            gui.perturbationLog.setText(gui.perturbationLog.getText() + "Error: No path to MathKernel set!\n");
            gui.scroller.validate();
            gui.scroller.getVerticalScrollBar().setValue(gui.scroller.getVerticalScrollBar().getMaximum());
            gui.networkingLog.setText(gui.networkingLog.getText() + "Error: No path to MathKernel set!\n");
            gui.netScroller.validate();
            gui.netScroller.getVerticalScrollBar().setValue(gui.netScroller.getVerticalScrollBar().getMaximum());
            System.exit(0);
        } else {
            gui.perturbationLog.setText(gui.perturbationLog.getText() + "MathKernel path set!\n");
            gui.scroller.validate();
            gui.scroller.getVerticalScrollBar().setValue(gui.scroller.getVerticalScrollBar().getMaximum());
            gui.networkingLog.setText(gui.networkingLog.getText() + "MathKernel path set!\n");
            gui.netScroller.validate();
            gui.netScroller.getVerticalScrollBar().setValue(gui.netScroller.getVerticalScrollBar().getMaximum());
        }

        //Clear the KernelLink.
        ml = null;

        //Open the link to Mathematica.
        try {
            gui.perturbationLog.setText(gui.perturbationLog.getText() + "Opening link to Mathematica...\n");
            gui.scroller.validate();
            gui.scroller.getVerticalScrollBar().setValue(gui.scroller.getVerticalScrollBar().getMaximum());
            gui.networkingLog.setText(gui.networkingLog.getText() + "Opening link to Mathematica...\n");
            gui.netScroller.validate();
            gui.netScroller.getVerticalScrollBar().setValue(gui.netScroller.getVerticalScrollBar().getMaximum());

            ml = MathLinkFactory.createKernelLink("-linkmode launch -linkname '" + mathKernelPath.replaceAll("\\\\", "\\\\\\\\") +"'");
        } catch (MathLinkException e) {
            ml.clearError();
            ml.newPacket();
            gui.perturbationLog.setText(gui.perturbationLog.getText() + "Fatal error opening link: " + e.getMessage() + "\n");
            gui.scroller.validate();
            gui.scroller.getVerticalScrollBar().setValue(gui.scroller.getVerticalScrollBar().getMaximum());
            gui.networkingLog.setText(gui.networkingLog.getText() + "Fatal error opening link: " + e.getMessage() + "\n");
            gui.netScroller.validate();
            gui.netScroller.getVerticalScrollBar().setValue(gui.netScroller.getVerticalScrollBar().getMaximum());
            return;
        }
        try {
            // Get rid of the initial InputNamePacket the kernel will send when it is launched.
            ml.discardAnswer();
        } catch (MathLinkException e) {
            ml.clearError();
            ml.newPacket();
            gui.perturbationLog.setText(gui.perturbationLog.getText() + "MathLink Exception occurred: " + e.getMessage() + "\n");
            gui.scroller.validate();
            gui.scroller.getVerticalScrollBar().setValue(gui.scroller.getVerticalScrollBar().getMaximum());
            gui.networkingLog.setText(gui.networkingLog.getText() + "MathLink Exception occurred: " + e.getMessage() + "\n");
            gui.netScroller.validate();
            gui.netScroller.getVerticalScrollBar().setValue(gui.netScroller.getVerticalScrollBar().getMaximum());
        }

        setMathGraphs();

        //DEBUGGING PURPOSES ONLY!!!
        //PacketListener stdoutPrinter = new PacketPrinter(System.out);
        //ml.addPacketListener(stdoutPrinter);

      }

    //************************************************************************************************
    //*Network Determination Functions                                                                   *
    //************************************************************************************************

    public static void runGA() {

        try {
            GA = new GeneticAlgorithm(Double.parseDouble(gui.crossoverRate.getText()), Double.parseDouble(gui.mutationRate.getText()), Integer.parseInt(gui.startingPopSize.getText()));
            int epochCount = 0;
            GENERATION_LIMIT = Integer.parseInt(gui.epochLimit.getText());
            String pVal = gui.perturbationFunction.getText();
            pVal = pVal.replaceAll("sin\\((.*)\\)|SIN\\((.*)\\)|Sin\\((.*)\\)|sin\\[(.*)\\]|SIN\\[(.*)\\]|Sin\\[(.*)\\]", "Sin\\[$1$2$3$4$5$6\\]");
            pVal = pVal.replaceAll("cos\\((.*)\\)|COS\\((.*)\\)|Cos\\((.*)\\)|cos\\[(.*)\\]|COS\\[(.*)\\]|Cos\\[(.*)\\]", "Cos\\[$1$2$3$4$5$6\\]");
            pVal = pVal.replaceAll("x|X", "t");
            GA.PFUNCTION = pVal;

            gui.networkingLog.setText(gui.networkingLog.getText() + "Running Genetic Algorithm...\n");
            gui.netScroller.validate();
            gui.netScroller.getVerticalScrollBar().setValue(gui.netScroller.getVerticalScrollBar().getMaximum());
            gui.progressBar.setMaximum(GENERATION_LIMIT);
            gui.progressBar.setValue(0);
            gui.statusLbl.setText("Running...");
            while (GA.convergenceCount <= CONVERGENCE_SENSITIVITY && epochCount <= GENERATION_LIMIT)
            {
                GA.runEpoch();
                if (epochCount % 5 == 0) {
                    gui.networkingLog.setText(gui.networkingLog.getText() + "Current Epoch: " + Integer.toString(epochCount) + "\n");
                    gui.netScroller.validate();
                    gui.netScroller.getVerticalScrollBar().setValue(gui.netScroller.getVerticalScrollBar().getMaximum());
                }
                gui.progressBar.setValue(epochCount);
                epochCount++;
            }
            gui.progressBar.setValue(GENERATION_LIMIT);
            gui.statusLbl.setText("Complete!");
            if (epochCount == GENERATION_LIMIT) {
                gui.networkingLog.setText(gui.networkingLog.getText() + "Epoch Limit Reached!\n");
                gui.netScroller.getVerticalScrollBar().setValue(gui.netScroller.getVerticalScrollBar().getMaximum());
            }

            gui.networkingLog.setText(gui.networkingLog.getText() + "Genetic Algorithm Complete\n");
            gui.netScroller.validate();
            gui.netScroller.getVerticalScrollBar().setValue(gui.netScroller.getVerticalScrollBar().getMaximum());
            setNodePositions();
            gui.networkingLog.setText(gui.networkingLog.getText() + "Departure from Equilibrium: " + GA.lowestMaxPerturbation + "\n");
            gui.netScroller.validate();
            gui.netScroller.getVerticalScrollBar().setValue(gui.netScroller.getVerticalScrollBar().getMaximum());

        } catch (Exception e) {
            gui.networkingLog.setText(gui.networkingLog.getText() + "General Exception occurred: " + e.getMessage() + "\n");
            gui.netScroller.validate();
            gui.netScroller.getVerticalScrollBar().setValue(gui.netScroller.getVerticalScrollBar().getMaximum());
        }
    }

    public static void setNodePositions()
    {
        String positions = GA.bestTour;
        String[] pStringArray = positions.split(" ");
        gui.node0Img.setIcon(new ImageIcon(EnergySystem.class.getResource(pStringArray[0].toString()  +".png")));
        gui.node1Img.setIcon(new ImageIcon(EnergySystem.class.getResource(pStringArray[1].toString()  +".png")));
        gui.node2Img.setIcon(new ImageIcon(EnergySystem.class.getResource(pStringArray[2].toString()  +".png")));
        gui.node3Img.setIcon(new ImageIcon(EnergySystem.class.getResource(pStringArray[3].toString()  +".png")));
        gui.node4Img.setIcon(new ImageIcon(EnergySystem.class.getResource(pStringArray[4].toString()  +".png")));
        gui.node5Img.setIcon(new ImageIcon(EnergySystem.class.getResource(pStringArray[5].toString()  +".png")));
        gui.node6Img.setIcon(new ImageIcon(EnergySystem.class.getResource(pStringArray[6].toString()  +".png")));

    }

    public static void setNetworkOrderImages() {
        String positions = gui.networkOrder.getText();
        String[] pStringArray = positions.split(" ");
        if (pStringArray.length >= 1)
            gui.node0Img1.setIcon(new ImageIcon(EnergySystem.class.getResource(pStringArray[0].toString()  +".png")));
        if (pStringArray.length >= 2)
            gui.node1Img1.setIcon(new ImageIcon(EnergySystem.class.getResource(pStringArray[1].toString()  +".png")));
        if (pStringArray.length >= 3)
            gui.node2Img1.setIcon(new ImageIcon(EnergySystem.class.getResource(pStringArray[2].toString()  +".png")));
       if (pStringArray.length >= 4)
            gui.node3Img1.setIcon(new ImageIcon(EnergySystem.class.getResource(pStringArray[3].toString()  +".png")));
       if (pStringArray.length >= 5)
            gui.node4Img1.setIcon(new ImageIcon(EnergySystem.class.getResource(pStringArray[4].toString()  +".png")));
       if (pStringArray.length >= 6)
            gui.node5Img1.setIcon(new ImageIcon(EnergySystem.class.getResource(pStringArray[5].toString()  +".png")));
       if (pStringArray.length >= 7)
            gui.node6Img1.setIcon(new ImageIcon(EnergySystem.class.getResource(pStringArray[6].toString()  +".png")));

    }

    public static void setMathGraphs()
    {

        ESGUI.x0MC = new MathGraphicsJPanel();
        ESGUI.x1MC = new MathGraphicsJPanel();
        ESGUI.x2MC = new MathGraphicsJPanel();
        ESGUI.x3MC = new MathGraphicsJPanel();
        ESGUI.x4MC = new MathGraphicsJPanel();
        ESGUI.x5MC = new MathGraphicsJPanel();
        ESGUI.x6MC = new MathGraphicsJPanel();

        gui.graphDisplayPane.add(ESGUI.x0MC, JLayeredPane.DEFAULT_LAYER);
        gui.node0Btn.setSelected(true);

        ESGUI.x0MC.setBackground(Color.WHITE);
        ESGUI.x1MC.setBackground(Color.WHITE);
        ESGUI.x2MC.setBackground(Color.WHITE);
        ESGUI.x3MC.setBackground(Color.WHITE);
        ESGUI.x4MC.setBackground(Color.WHITE);
        ESGUI.x5MC.setBackground(Color.WHITE);
        ESGUI.x6MC.setBackground(Color.WHITE);

        ESGUI.x0MC.setLink(ml);
        ESGUI.x1MC.setLink(ml);
        ESGUI.x2MC.setLink(ml);
        ESGUI.x3MC.setLink(ml);
        ESGUI.x4MC.setLink(ml);
        ESGUI.x5MC.setLink(ml);
        ESGUI.x6MC.setLink(ml);

        ESGUI.x0MC.setImageType(MathGraphicsJPanel.GRAPHICS);
        ESGUI.x1MC.setImageType(MathGraphicsJPanel.GRAPHICS);
        ESGUI.x2MC.setImageType(MathGraphicsJPanel.GRAPHICS);
        ESGUI.x3MC.setImageType(MathGraphicsJPanel.GRAPHICS);
        ESGUI.x4MC.setImageType(MathGraphicsJPanel.GRAPHICS);
        ESGUI.x5MC.setImageType(MathGraphicsJPanel.GRAPHICS);
        ESGUI.x6MC.setImageType(MathGraphicsJPanel.GRAPHICS);

        ESGUI.x0MC.setBounds(5, 5, gui.graphDisplayPane.getWidth()-10, gui.graphDisplayPane.getHeight()-10);
        ESGUI.x1MC.setBounds(5, 5, gui.graphDisplayPane.getWidth()-10, gui.graphDisplayPane.getHeight()-10);
        ESGUI.x2MC.setBounds(5, 5, gui.graphDisplayPane.getWidth()-10, gui.graphDisplayPane.getHeight()-10);
        ESGUI.x3MC.setBounds(5, 5, gui.graphDisplayPane.getWidth()-10, gui.graphDisplayPane.getHeight()-10);
        ESGUI.x4MC.setBounds(5, 5, gui.graphDisplayPane.getWidth()-10, gui.graphDisplayPane.getHeight()-10);
        ESGUI.x5MC.setBounds(5, 5, gui.graphDisplayPane.getWidth()-10, gui.graphDisplayPane.getHeight()-10);
        ESGUI.x6MC.setBounds(5, 5, gui.graphDisplayPane.getWidth()-10, gui.graphDisplayPane.getHeight()-10);

    }


    //************************************************************************************************
    //*Perturbation Testing Functions                                                                   *
    //************************************************************************************************

      public static void calculatePerturbation() {
        try {

            //Calculate the necessary formulas for the current network setup...
            Genome testSubject = new Genome();
            testSubject.setGenome(gui.networkOrder.getText());
            if (GA == null) {
                GA = new GeneticAlgorithm(0.7,0.001,150);
            }
            GeneticAlgorithm.calculateAlphas(testSubject);
            testSubject.sortAlphas();
            GeneticAlgorithm.createRestingStateSystem(testSubject);

            String tVal = gui.perturbationFunction.getText();

            tVal = tVal.replaceAll("sin\\(([x0-9]*)\\)|SIN\\(([x0-9]*)\\)|Sin\\(([x0-9]*)\\)|sin\\[([x0-9]*)\\]|SIN\\[([x0-9]*)\\]|Sin\\[([x0-9]*)\\]", "Sin\\[$1$2$3$4$5$6\\]");
            tVal = tVal.replaceAll("cos\\(([x0-9]*)\\)|COS\\(([x0-9]*)\\)|Cos\\(([x0-9]*)\\)|cos\\[([x0-9]*)\\]|COS\\[([x0-9]*)\\]|Cos\\[([x0-9]*)\\]", "Cos\\[$1$2$3$4$5$6\\]");
            tVal = tVal.replaceAll("x|X", "holder");
            
            String constTVal = tVal;
            GeneticAlgorithm.PFUNCTION = constTVal;
            GeneticAlgorithm.createPerturbationStateSystem(testSubject);

            String function0;
            String function1;
            String function2;
            String function3;
            String function4;
            String function5;
            String function6;

            if (gui.xVar.isSelected()) {
                function0 = testSubject.dx0P.replaceAll("holder", "t");
                function1 = testSubject.dx1P.replaceAll("holder", "t");
                function2 = testSubject.dx2P.replaceAll("holder", "t");
                function3 = testSubject.dx3P.replaceAll("holder", "t");
                function4 = testSubject.dx4P.replaceAll("holder", "t");
                function5 = testSubject.dx5P.replaceAll("holder", "t");
                function6 = testSubject.dx6P.replaceAll("holder", "t");
            } else {
                function0 = testSubject.dx0P.replaceAll("holder", gui.perturbationFunction.getText());
                function1 = testSubject.dx1P.replaceAll("holder", gui.perturbationFunction.getText());
                function2 = testSubject.dx2P.replaceAll("holder", gui.perturbationFunction.getText());
                function3 = testSubject.dx3P.replaceAll("holder", gui.perturbationFunction.getText());
                function4 = testSubject.dx4P.replaceAll("holder", gui.perturbationFunction.getText());
                function5 = testSubject.dx5P.replaceAll("holder", gui.perturbationFunction.getText());
                function6 = testSubject.dx6P.replaceAll("holder", gui.perturbationFunction.getText());
            }

                double max = 0;
                
                NumberFormat numFormat = NumberFormat.getInstance();
                numFormat.setGroupingUsed(false);
                numFormat.setMinimumFractionDigits(20);
                
                String EqSystem = "sol = NDSolve[{x0'[t] == " + function0 + ", x1'[t] == " + function1 + ", x2'[t] == " + function2 + ", x3'[t] == " + function3 + ", x4'[t] == " + function4 + ", x5'[t] == " + function5 + ", x6'[t] == " + function6 + ", x0[0] == (0), x1[0] == (0), x2[0] == (0), x3[0] == (0), x4[0] == (0), x5[0] == (0), x6[0] == (0)}, {x0, x1, x2, x3, x4, x5, x6}, {t, 0, 500}]";

                String command0 = EqSystem + "\n" + "Plot[x0[t] /. sol, {t,0,500}]";
                ESGUI.x0MC.setMathCommand(command0);
                String command0a = EqSystem + "\n" + "Max[Table[x0[t] /. sol, {t, 0, 500, 1}]]";
                ml.evaluate(command0a);
                ml.waitForAnswer();
                max = ml.getDouble();
                testSubject.totalFluxuationAmount += max;

                gui.perturbationLog.setText(gui.perturbationLog.getText() + "Max fluxation for Node 0 (Chicago): " + numFormat.format(max) + "\n");
                gui.scroller.validate();
                gui.scroller.getVerticalScrollBar().setValue(gui.scroller.getVerticalScrollBar().getMaximum());

                String command1 = EqSystem + "\n" + "Plot[x1[t] /. sol, {t,0,500}]";
                ESGUI.x1MC.setMathCommand(command1);
                String command1a = EqSystem + "\n" + "Max[Table[x1[t] /. sol, {t, 0, 500, 1}]]";
                ml.evaluate(command1a);
                ml.waitForAnswer();
                max = ml.getDouble();
                testSubject.totalFluxuationAmount += max;

                gui.perturbationLog.setText(gui.perturbationLog.getText() + "Max fluxation for Node 1 (St.Louis): " + numFormat.format(max) + "\n");
                gui.scroller.validate();
                gui.scroller.getVerticalScrollBar().setValue(gui.scroller.getVerticalScrollBar().getMaximum());

                String command2 = EqSystem + "\n" + "Plot[x2[t] /. sol, {t,0,500}]";
                ESGUI.x2MC.setMathCommand(command2);
                String command2a = EqSystem + "\n" + "Max[Table[x2[t] /. sol, {t, 0, 500, 1}]]";
                ml.evaluate(command2a);
                ml.waitForAnswer();
                max = ml.getDouble();
                testSubject.totalFluxuationAmount += max;

                gui.perturbationLog.setText(gui.perturbationLog.getText() + "Max fluxation for Node 2 (Nashville): " + numFormat.format(max) + "\n");
                gui.scroller.validate();
                gui.scroller.getVerticalScrollBar().setValue(gui.scroller.getVerticalScrollBar().getMaximum());

                String command3 = EqSystem + "\n" + "Plot[x3[t] /. sol, {t,0,500}]";
                ESGUI.x3MC.setMathCommand(command3);
                String command3a = EqSystem + "\n" + "Max[Table[x3[t] /. sol, {t, 0, 500, 1}]]";
                ml.evaluate(command3a);
                ml.waitForAnswer();
                max = ml.getDouble();
                testSubject.totalFluxuationAmount += max;

                gui.perturbationLog.setText(gui.perturbationLog.getText() + "Max fluxation for Node 3 (Detroit): " + numFormat.format(max)+ "\n");
                gui.scroller.validate();
                gui.scroller.getVerticalScrollBar().setValue(gui.scroller.getVerticalScrollBar().getMaximum());

                String command4 = EqSystem + "\n" + "Plot[x4[t] /. sol, {t,0,500}]";
                ESGUI.x4MC.setMathCommand(command4);
                String command4a = EqSystem + "\n" + "Max[Table[x4[t] /. sol, {t, 0, 500, 1}]]";
                ml.evaluate(command4a);
                ml.waitForAnswer();
                max = ml.getDouble();
                testSubject.totalFluxuationAmount += max;

                gui.perturbationLog.setText(gui.perturbationLog.getText() + "Max fluxation for Node 4 (Cleveland): " + numFormat.format(max) + "\n");
                gui.scroller.validate();
                gui.scroller.getVerticalScrollBar().setValue(gui.scroller.getVerticalScrollBar().getMaximum());

                String command5 = EqSystem + "\n" + "Plot[x5[t] /. sol, {t,0,500}]";
                ESGUI.x5MC.setMathCommand(command5);
                String command5a = EqSystem + "\n" + "Max[Table[x5[t] /. sol, {t, 0, 500, 1}]]";
                ml.evaluate(command5a);
                ml.waitForAnswer();
                max = ml.getDouble();
                testSubject.totalFluxuationAmount += max;

                gui.perturbationLog.setText(gui.perturbationLog.getText() + "Max fluxation for Node 5 (Minneapolis): " + numFormat.format(max) + "\n");
                gui.scroller.validate();
                gui.scroller.getVerticalScrollBar().setValue(gui.scroller.getVerticalScrollBar().getMaximum());

                String command6 = EqSystem + "\n" + "Plot[x6[t] /. sol, {t,0,500}]";
                ESGUI.x6MC.setMathCommand(command6);
                String command6a = EqSystem + "\n" + "Max[Table[x6[t] /. sol, {t, 0, 500, 1}]]";
                ml.evaluate(command6a);
                ml.waitForAnswer();
                max = ml.getDouble();
                testSubject.totalFluxuationAmount += max;

                gui.perturbationLog.setText(gui.perturbationLog.getText() + "Max fluxation for Node 6 (Indianapolis): " + numFormat.format(max) + "\n");
                gui.scroller.validate();
                gui.scroller.getVerticalScrollBar().setValue(gui.scroller.getVerticalScrollBar().getMaximum());

                gui.perturbationLog.setText(gui.perturbationLog.getText() + "Max total system fluxation:" + numFormat.format(testSubject.totalFluxuationAmount)+"\n");
                gui.scroller.validate();
                gui.scroller.getVerticalScrollBar().setValue(gui.scroller.getVerticalScrollBar().getMaximum());
                
         } catch (MathLinkException e) {
             ml.clearError();
             ml.newPacket();
            gui.perturbationLog.setText(gui.perturbationLog.getText() + "MathLink Exception occurred: " + e.getMessage() + "\n");
            gui.scroller.validate();
            gui.scroller.getVerticalScrollBar().setValue(gui.scroller.getVerticalScrollBar().getMaximum());
        } catch (Exception e) {
            gui.perturbationLog.setText(gui.perturbationLog.getText() + "General Exception occurred: " + e.getMessage() + "\n");
            gui.scroller.validate();
            gui.scroller.getVerticalScrollBar().setValue(gui.scroller.getVerticalScrollBar().getMaximum());
        }
      }
   }
