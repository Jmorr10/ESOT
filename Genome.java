/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;



/**
 *
 * @author Joseph
 */
public class Genome
    {

        public ArrayList<Integer> tourVector = new ArrayList<>();
        public static final int GENOME_LENGTH = 7;
        public double fitness = 0.0;
        public HashMap alphaMap = null;
        public HashMap x0Alphas = null;
        public HashMap x1Alphas = null;
        public HashMap x2Alphas = null;
        public HashMap x3Alphas = null;
        public HashMap x4Alphas = null;
        public HashMap x5Alphas = null;
        public HashMap x6Alphas = null;
        public String dx0 = "";
        public String dx1 = "";
        public String dx2 = "";
        public String dx3 = "";
        public String dx4 = "";
        public String dx5 = "";
        public String dx6 = "";
        public String dx0P = "";
        public String dx1P = "";
        public String dx2P = "";
        public String dx3P = "";
        public String dx4P = "";
        public String dx5P = "";
        public String dx6P = "";
        public double totalDistance = 0.0;
        public double totalFluxuationAmount = 0.0;
        public static Random randGen = new Random();

        public Genome()
        {
            tourVector = grabPermutations(GENOME_LENGTH);
            alphaMap = new HashMap();
            x0Alphas = new HashMap();
            x1Alphas = new HashMap();
            x2Alphas = new HashMap();
            x3Alphas = new HashMap();
            x4Alphas = new HashMap();
            x5Alphas = new HashMap();
            x6Alphas = new HashMap();
        }

        public static ArrayList<Integer> grabPermutations(int length)
        {
            ArrayList<Integer> permutations = new ArrayList<>();
            Boolean filled = false;
            while (!filled)
            {
                int rand = randGen.nextInt(length);
                if (permutations.contains(rand))
                {
                    continue;
                }
                else
                {
                    permutations.add(rand);
                    if (permutations.size() == length)
                    {
                        filled = true;
                    }
                }
            }
            return permutations;
        }

        public void setGenome(ArrayList<Integer> chromo)
        {
            this.tourVector = chromo;
        }

        public void setGenome(String chromo)
        {
            ArrayList<Integer> conversion = new ArrayList<>();
            
            if (chromo.equals("") || chromo == null)
            {
                setGenome("0 1 2 3 4 5 6");
                return;
            }
            for (int i = 0; i < GENOME_LENGTH; i++)
            {
                conversion.add(Integer.parseInt(chromo.split(" ")[i].toString()));
            }
            this.tourVector = conversion;
        }
        
        public void sortAlphas(){
            
            for (Object key : alphaMap.keySet()) {
                switch (key.toString().charAt(0)) {
                    
                    case '0':
                        x0Alphas.put(key, alphaMap.get(key));
                        break;
                    case '1':
                        x1Alphas.put(key, alphaMap.get(key));
                        break;
                    case '2':
                        x2Alphas.put(key, alphaMap.get(key));
                        break;
                    case '3':
                        x3Alphas.put(key, alphaMap.get(key));
                        break;
                    case '4':
                        x4Alphas.put(key, alphaMap.get(key));
                        break;
                    case '5':
                        x5Alphas.put(key, alphaMap.get(key));
                        break;    
                    case '6':
                        x6Alphas.put(key, alphaMap.get(key));
                        break;
                }
            }
            
            
            
        }

        public void toConsole()
        {
            for (int i = 0; i < tourVector.size(); i++)
            {
                if (i == tourVector.size() - 1)
                {
                    EnergySystem.gui.networkingLog.setText(EnergySystem.gui.networkingLog.getText() + tourVector.get(i).toString() + "\n");
                }
                else
                {
                    EnergySystem.gui.networkingLog.setText(EnergySystem.gui.networkingLog.getText() + tourVector.get(i).toString() + " ");
                }
            }
        }

        public String ToString()
        {
            String str = "";
            for (int i = 0; i < tourVector.size(); i++)
            {
                if (i == tourVector.size() - 1)
                {
                    str += tourVector.get(i).toString();
                }
                else
                {
                    str += tourVector.get(i).toString() + " ";
                }
            }
            return str;
        }

        public Genome Clone()
        {
            Genome clone = new Genome();
            clone.setGenome(this.tourVector);
            clone.totalDistance = this.totalDistance;
            clone.fitness = this.fitness;
            
            clone.alphaMap = (HashMap)this.alphaMap.clone();
            clone.x0Alphas = (HashMap)this.x0Alphas.clone();
            clone.x1Alphas = (HashMap)this.x1Alphas.clone();
            clone.x2Alphas = (HashMap)this.x2Alphas.clone();
            clone.x3Alphas = (HashMap)this.x3Alphas.clone();
            clone.x4Alphas = (HashMap)this.x4Alphas.clone();
            clone.x5Alphas = (HashMap)this.x5Alphas.clone();
            clone.x6Alphas = (HashMap)this.x6Alphas.clone();
            
            clone.dx0 = this.dx0;
            clone.dx1 = this.dx1;
            clone.dx2 = this.dx2;
            clone.dx3 = this.dx3;
            clone.dx4 = this.dx4;
            clone.dx5 = this.dx5;
            clone.dx6 = this.dx6;
            
            clone.dx0P = this.dx0P;
            clone.dx1P = this.dx1P;
            clone.dx2P = this.dx2P;
            clone.dx3P = this.dx3P;
            clone.dx4P = this.dx4P;
            clone.dx5P = this.dx5P;
            clone.dx6P = this.dx6P;
       
            clone.totalFluxuationAmount = this.totalFluxuationAmount;
            
            return clone;
        }
    }
