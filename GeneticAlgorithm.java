/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import com.wolfram.jlink.Expr;
import com.wolfram.jlink.LoopbackLink;
import com.wolfram.jlink.MathLinkException;
import com.wolfram.jlink.MathLinkFactory;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Joseph
 */
 public class GeneticAlgorithm
    {

        public double crossoverRate = 0.7;
        public double mutationRate = 0.001;
        public static int populationSize = 100;
        public static String PFUNCTION = "";

        public final int ELITE_SELECTION = 0;

        public static ArrayList<Genome> population = new ArrayList<>();
        public ArrayList<Genome> childPopulation = new ArrayList<>();
        
        public int generation = 0;
        public int convergenceCount = 0;
        
        public double totalPopulationFitness = 0;
        
        public double bestFitness = 0;
        public double worstFitness = 0;
        public double lowestMaxPerturbation = 0;
     
        public Genome bestRoute = null;
        public Genome worstRoute = null;

        public String bestTour = "";
        public String previousBestTour = "";

        //NODE CONNECTION MAPS:
        public ArrayList<Integer[]> node0 = new ArrayList<Integer[]>() {{ add(new Integer[]{ 0, 1 }); add(new Integer[]{ 0, 2 }); add(new Integer[]{ 0, 3 }); };};
        public ArrayList<Integer[]> node1 = new ArrayList<Integer[]>() {{ add(new Integer[]{ 1, 0 }); add(new Integer[]{ 1, 2 }); add(new Integer[]{ 1, 4 }); add(new Integer[]{ 1, 5 }); };};
        public ArrayList<Integer[]> node2 = new ArrayList<Integer[]>() {{ add(new Integer[]{ 2, 0 }); add(new Integer[]{ 2, 1 }); add(new Integer[]{ 2, 3 }); add(new Integer[]{ 2, 5 }); add(new Integer[]{ 2, 4 }); add(new Integer[]{ 2, 6 }); };};
        public ArrayList<Integer[]> node3 = new ArrayList<Integer[]>() {{ add(new Integer[]{ 3, 0 }); add(new Integer[]{ 3, 2 }); add(new Integer[]{ 3, 6 }); add(new Integer[]{ 3, 5 }); };};
        public ArrayList<Integer[]> node4 = new ArrayList<Integer[]>() {{ add(new Integer[]{ 4, 1 }); add(new Integer[]{ 4, 5 }); add(new Integer[]{ 4, 2 }); };};
        public ArrayList<Integer[]> node5 = new ArrayList<Integer[]>() {{ add(new Integer[]{ 5, 2 }); add(new Integer[]{ 5, 4 }); add(new Integer[]{ 5, 6 }); add(new Integer[]{ 5, 1 }); add(new Integer[]{ 5, 3 }); };};
        public ArrayList<Integer[]> node6 = new ArrayList<Integer[]>() {{ add(new Integer[]{ 6, 3 }); add(new Integer[]{ 6, 5 }); add(new Integer[]{ 6, 2 });};};

        public static ArrayList<ArrayList<Integer[]>> nodeMaps = null;
        
        public static ArrayList<Coords> cityCoordinates = new ArrayList<>();

        public GeneticAlgorithm(double crossOverRate, double mutationRate, int populationSize)
        {
            //Sets population size and various rates
            this.crossoverRate = crossOverRate;
            this.mutationRate = mutationRate;
            GeneticAlgorithm.populationSize = populationSize;
            //Creates starting population
            population = new ArrayList<>(this.populationSize);
            createStartingPopulation();
            nodeMaps = new ArrayList<>();
            nodeMaps = new ArrayListImpl();

            //TESTING ONLY!!!! DELETE ME!!!!!
            cityCoordinates.add(new Coords(41, 52, 55,"N", 87, 37, 40, "W", "Chicago")); //Chicago
            cityCoordinates.add(new Coords(38, 37, 48, "N", 90, 12, 0, "W", "St. Louis")); //St.Louis
            cityCoordinates.add(new Coords(36, 10, 0, "N", 86, 47, 0, "W", "Nashville")); //Nashville
            cityCoordinates.add(new Coords(42, 19, 53, "N", 83, 2, 45, "W", "Detroit")); //Detroit
            cityCoordinates.add(new Coords(41, 28, 56, "N", 81, 40, 11, "W", "Cleveland")); //Cleveland
            cityCoordinates.add(new Coords(44, 59, 0, "N", 93, 16, 0, "W", "Minneapolis")); //Minneapolis
            cityCoordinates.add(new Coords(39, 47, 27.39, "N", 86, 8, 51.67, "W", "Indianapolis")); //Indianapolis
        }

        public static void createStartingPopulation()
        {
            for (int i = 0; i < populationSize; i++)
            {
                population.add(new Genome());
            }
        }

        public void reset()
        {
            totalPopulationFitness = 0;
        }


        public void runEpoch()
        {
            if (bestRoute != null)
            {
                if (bestTour.equals(previousBestTour))
                {
                    convergenceCount++;
                }
                else
                {
                    convergenceCount = 0;
                }
            }
            reset();
            calculateFitness(population);
            childPopulation = new ArrayList<>(this.populationSize);
            for (int i = 0; i < ELITE_SELECTION; i++)
            {
                childPopulation.add(bestRoute.Clone());
            }

            while (childPopulation.size() != populationSize)
            {
                Genome Parent1 = RouletteWheelSelection(population);
                Genome Parent2 = RouletteWheelSelection(population);

                Genome Child1 = new Genome();
                Genome Child2 = new Genome();

                CrossoverPMX(Parent1, Parent2, Child1, Child2);
                Mutate(Child1);
                Mutate(Child2);

                childPopulation.add(Child1);
                childPopulation.add(Child2);
            }

            population = childPopulation;
            generation++;
        }
        
        public static float NextFloat(Random random)
        {
            double mantissa = (random.nextDouble() * 2.0) - 1.0;
            double exponent = Math.pow(2.0, random.nextInt(128 - (-126) + 1) + -126);
            return (float)(mantissa * exponent);
        }

        //****************************************************************************************
        //* TESTING FUNCTIONS ONLY!!!! DELETE! DELETE! DELETE!                                   *
        //****************************************************************************************

       

        public void calculateFitness(ArrayList<Genome> population)
        {
            for (Genome chromosome : population)
            {
                calculateAlphas(chromosome);
                chromosome.sortAlphas();
                createRestingStateSystem(chromosome);
                createPerturbationStateSystem(chromosome);
               
                String EqSystem = "sol = NDSolve[{x0'[t] == " + chromosome.dx0 + ", x1'[t] == " +chromosome.dx1 + ", x2'[t] == " + chromosome.dx2 + ", x3'[t] == " + chromosome.dx3 + ", x4'[t] == " + chromosome.dx4 + ", x5'[t] == " + chromosome.dx5 + ", x6'[t] == " + chromosome.dx6 + ", x0[0] == (0), x1[0] == (0), x2[0] == (0), x3[0] == (0), x4[0] == (0), x5[0] == (0), x6[0] == (0)}, {x0, x1, x2, x3, x4, x5, x6}, {t, 0, 500}]";
                String EqSystemP = "solP = NDSolve[{x0'[t] == " + chromosome.dx0P + ", x1'[t] == " +chromosome.dx1P + ", x2'[t] == " + chromosome.dx2P + ", x3'[t] == " + chromosome.dx3P + ", x4'[t] == " + chromosome.dx4P + ", x5'[t] == " + chromosome.dx5P + ", x6'[t] == " + chromosome.dx6P + ", x0[0] == (0), x1[0] == (0), x2[0] == (0), x3[0] == (0), x4[0] == (0), x5[0] == (0), x6[0] == (0)}, {x0, x1, x2, x3, x4, x5, x6}, {t,0,500}]";
                
                try {
                    
                    EnergySystem.ml.newPacket();
                    EnergySystem.ml.evaluate(EqSystem + "\n" + EqSystemP + "\n" + "x0A = Mean[Table[x0[t] /. sol, {t, 0, 500, 1}]] \nx0PA = Mean[Table[x0[t] /. solP, {t, 0, 500, 1}]] \nx0PM = Max[Table[x0[t] /. solP, {t, 0, 500, 1}]] \nx1A = Mean[Table[x1[t] /. sol, {t, 0, 500, 1}]] \nx1PA = Mean[Table[x1[t] /. solP, {t, 0, 500, 1}]] \nx1PM = Max[Table[x1[t] /. solP, {t, 0, 500, 1}]] \nx2A = Mean[Table[x2[t] /. sol, {t, 0, 500, 1}]] \nx2PA = Mean[Table[x2[t] /. solP, {t, 0, 500, 1}]] \nx2PM = Max[Table[x2[t] /. solP, {t, 0, 500, 1}]] \nx3A = Mean[Table[x3[t] /. sol, {t, 0, 500, 1}]] \nx3PA = Mean[Table[x3[t] /. solP, {t, 0, 500, 1}]] \nx3PM = Max[Table[x3[t] /. solP, {t, 0, 500, 1}]] \nx4A = Mean[Table[x4[t] /. sol, {t, 0, 500, 1}]] \nx4PA = Mean[Table[x4[t] /. solP, {t, 0, 500, 1}]] \nx4PM = Max[Table[x4[t] /. solP, {t, 0, 500, 1}]] \nx5A = Mean[Table[x5[t] /. sol, {t, 0, 500, 1}]] \nx5PA = Mean[Table[x5[t] /. solP, {t, 0, 500, 1}]] \nx5PM = Max[Table[x5[t] /. solP, {t, 0, 500, 1}]] \nx6A = Mean[Table[x6[t] /. sol, {t, 0, 500, 1}]] \nx6PA = Mean[Table[x6[t] /. solP, {t, 0, 500, 1}]] \nx6PM = Max[Table[x6[t] /. solP, {t, 0, 500, 1}]] \nList[Extract[x0A, 1], Extract[x0PA, 1], x0PM, Extract[x1A, 1], Extract[x1PA, 1], x1PM, Extract[x2A, 1], Extract[x2PA, 1], x2PM, Extract[x3A, 1], Extract[x3PA, 1], x3PM, Extract[x4A, 1], Extract[x4PA, 1], x4PM, Extract[x5A, 1], Extract[x5PA, 1], x5PM, Extract[x6A, 1], Extract[x6PA, 1], x6PM]");
                    EnergySystem.ml.waitForAnswer();
                   
                    double[] results = EnergySystem.ml.getDoubleArray1();
                    
                    double fitness = 0;
                    double systemEquilibrium = (double)(results[0] + results[3] + results[6] + results[9] + results[12] + results[15] + results[18])/7.0;
                    //Add together all the maxiumum perturbation values in the system)
                    double maxFluxuationAmount = 0;
                    for (int i = 2; i <= 20 ; i += 3) {
                        maxFluxuationAmount += ((double)results[i] - systemEquilibrium);
                    }
                    
                    chromosome.totalFluxuationAmount = maxFluxuationAmount;
                    
                    //Factor in distance values as part of fitness... 13091 being the most efficient distance.
                    double distanceFitness = (chromosome.totalDistance - 13091.1319394821);
                    
                    //Make fitness a fraction of 1... add 1 to avoid dividing by zero.
                    fitness = 1.0/(double)((maxFluxuationAmount + 1) + (1.0/(distanceFitness +1))); 
                    chromosome.fitness = fitness;
                } catch (MathLinkException e) {
                    EnergySystem.ml.clearError();
                    EnergySystem.ml.newPacket();
                    EnergySystem.gui.networkingLog.setText(EnergySystem.gui.networkingLog.getText() + "MathLink Exception occurred: " + e.getMessage() + "\n");
                    EnergySystem.gui.netScroller.getVerticalScrollBar().setValue(EnergySystem.gui.netScroller.getVerticalScrollBar().getMaximum());
                }
                
                if (worstRoute == null)
                {
                    worstRoute = chromosome.Clone();
                    worstFitness = chromosome.fitness;
                }
                else if (chromosome.fitness < worstFitness)
                {
                    worstRoute = chromosome.Clone();
                    worstFitness = chromosome.fitness;
                }
                else
                {
                    totalPopulationFitness += chromosome.fitness;
                }

                if (bestRoute == null)
                {
                    bestRoute = chromosome.Clone();
                    bestFitness = chromosome.fitness;
                    lowestMaxPerturbation = chromosome.totalFluxuationAmount;
                }
                else if (chromosome.fitness > bestFitness)
                {
                    previousBestTour = bestTour;
                    bestRoute = chromosome.Clone();
                    bestFitness = chromosome.fitness;
                    lowestMaxPerturbation = chromosome.totalFluxuationAmount;
                    bestTour = chromosome.ToString();
                }
                else if (chromosome.fitness == bestFitness)
                {
                    previousBestTour = bestTour;
                }
            }
        }

        public static void calculateAlphas(Genome chromosome) 
        {
            double totalDistance = 0;
            for (int i = 0; i < 7; i++)
            {
                ArrayList<Integer[]> nodeMap = nodeMaps.get(i);
                for (Integer[] args : nodeMap)
                {
                    double calc = calculateDistanceFromAToB(cityCoordinates.get(chromosome.tourVector.get(args[0])), cityCoordinates.get(chromosome.tourVector.get(args[1])));
                    totalDistance += calc;
                    chromosome.alphaMap.put(chromosome.tourVector.get(args[0]).toString() + "-" + chromosome.tourVector.get(args[1]), calc);
                    //EnergySystem.gui.networkingLog.setText(EnergySystem.gui.networkingLog.getText() + "Node " + Integer.toString(i) + ": " + cityCoordinates.get(i).city + " with connection to: " + cityCoordinates.get(args[1]).city); 
                    //EnergySystem.gui.scroller.getVerticalScrollBar().setValue(EnergySystem.gui.scroller.getVerticalScrollBar().getMaximum());
                }
            }
            chromosome.totalDistance = totalDistance;
        }

        public static double calculateDistanceFromAToB(Coords A, Coords B)
        {
            double LatARadians = 0;
            double LongARadians = 0;
            double LatBRadians = 0;
            double LongBRadians = 0;
            double radiusOfEarth = 6378; //Kilometers
            double directionMod = 1;
            directionMod = (A.latDirection.equals("S")) ? -1 : 1;
            LatARadians += (DegreeToRadian(A.latitude + minsAndSecsToDegrees(A.latitudeMinutes, A.latitudeSeconds))) * directionMod; //a1
            directionMod = (B.latDirection.equals("S")) ? -1 : 1;
            LatBRadians += (DegreeToRadian(B.latitude + minsAndSecsToDegrees(B.latitudeMinutes, B.latitudeSeconds))) * directionMod;  //a2

            directionMod = (A.longDirection.equals("W")) ? -1 : 1;
            LongARadians += (DegreeToRadian(A.longitude + minsAndSecsToDegrees(A.longitudeMinutes, A.longitudeSeconds))) * directionMod; //b1
            directionMod = (B.longDirection.equals("W")) ? -1 : 1;
            LongBRadians += (DegreeToRadian(B.longitude + minsAndSecsToDegrees(B.longitudeMinutes, B.longitudeSeconds))) * directionMod; //b2

            double distance = Math.acos(Math.cos(LatARadians)*Math.cos(LongARadians)*Math.cos(LatBRadians)*Math.cos(LongBRadians) + Math.cos(LatARadians)*Math.sin(LongARadians)*Math.cos(LatBRadians)*Math.sin(LongBRadians) + Math.sin(LatARadians)*Math.sin(LatBRadians)) * radiusOfEarth;

            return distance;
        }

        private static double minsAndSecsToDegrees(double mins, double secs)
        {
            return (mins + (secs / 60))/60;
        }

        private static double DegreeToRadian(double angle)
        {
            return Math.PI * angle / 180.0;
        }
        
        public static void createPerturbationStateSystem(Genome chromosome) 
        {
            String dx0 = "";
                for (Object key : chromosome.x0Alphas.keySet()) {
                    dx0 += "((1/" + chromosome.x0Alphas.get(key)+ ")(x"+ key.toString().charAt(2) + "[t]-x0[t]))+";
                }
                //Get rid of trailing + sign...
                dx0 = dx0.substring(0, dx0.length()-1);
               
                 String dx1 = "";
                for (Object key : chromosome.x1Alphas.keySet()) {
                    if( key.toString().charAt(2) == '0') {
                        dx1 += "((1/" + chromosome.x1Alphas.get(key)+ ")((x"+ key.toString().charAt(2) + "[t] + " + PFUNCTION + ")-x1[t]))+";
                    } else {
                        dx1 += "((1/" + chromosome.x1Alphas.get(key)+ ")(x"+ key.toString().charAt(2) + "[t]-x1[t]))+";
                    }
                }
                //Get rid of trailing + sign...
                dx1 = dx1.substring(0, dx1.length()-1);
                
                String dx2 = "";
                for (Object key : chromosome.x2Alphas.keySet()) {
                    if( key.toString().charAt(2) == '0') {
                        dx2 += "((1/" + chromosome.x2Alphas.get(key)+ ")((x"+ key.toString().charAt(2) + "[t] + " + PFUNCTION + ")-x2[t]))+";
                    } else {
                        dx2 += "((1/" + chromosome.x2Alphas.get(key)+ ")(x"+ key.toString().charAt(2) + "[t]-x2[t]))+";
                    }
                }
                //Get rid of trailing + sign...
                dx2 = dx2.substring(0, dx2.length()-1);
                
                String dx3 = "";
                for (Object key : chromosome.x3Alphas.keySet()) {
                    if( key.toString().charAt(2) == '0') {
                        dx3 += "((1/" + chromosome.x3Alphas.get(key)+ ")((x"+ key.toString().charAt(2) + "[t] + " + PFUNCTION + ")-x3[t]))+";
                    } else {
                        dx3 += "((1/" + chromosome.x3Alphas.get(key)+ ")(x"+ key.toString().charAt(2) + "[t]-x3[t]))+";
                    }
                }
                //Get rid of trailing + sign...
                dx3 = dx3.substring(0, dx3.length()-1);
                
                String dx4 = "";
                for (Object key : chromosome.x4Alphas.keySet()) {
                    if( key.toString().charAt(2) == '0') {
                        dx4 += "((1/" + chromosome.x4Alphas.get(key)+ ")((x"+ key.toString().charAt(2) + "[t] + " + PFUNCTION + ")-x4[t]))+";
                    } else {
                        dx4 += "((1/" + chromosome.x4Alphas.get(key)+ ")(x"+ key.toString().charAt(2) + "[t]-x4[t]))+";
                    }
                }
                //Get rid of trailing + sign...
                dx4 = dx4.substring(0, dx4.length()-1);
 
                String dx5 = "";
                for (Object key : chromosome.x5Alphas.keySet()) {
                    if( key.toString().charAt(2) == '0') {
                        dx5 += "((1/" + chromosome.x5Alphas.get(key)+ ")((x"+ key.toString().charAt(2) + "[t] + " + PFUNCTION + ")-x5[t]))+";
                    } else {
                        dx5 += "((1/" + chromosome.x5Alphas.get(key)+ ")(x"+ key.toString().charAt(2) + "[t]-x5[t]))+";
                    }
                }
                //Get rid of trailing + sign...
                dx5 = dx5.substring(0, dx5.length()-1);
                
                String dx6 = "";
                for (Object key : chromosome.x6Alphas.keySet()) {
                   if( key.toString().charAt(2) == '0') {
                        dx6 += "((1/" + chromosome.x6Alphas.get(key)+ ")((x"+ key.toString().charAt(2) + "[t] + " + PFUNCTION + ")-x6[t]))+";
                    } else {
                        dx6 += "((1/" + chromosome.x6Alphas.get(key)+ ")(x"+ key.toString().charAt(2) + "[t]-x6[t]))+";
                    }
                }
                //Get rid of trailing + sign...
                dx6 = dx6.substring(0, dx6.length()-1);
                
                chromosome.dx0P = dx0;
                chromosome.dx1P = dx1;
                chromosome.dx2P = dx2;
                chromosome.dx3P = dx3;
                chromosome.dx4P = dx4;
                chromosome.dx5P = dx5;
                chromosome.dx6P = dx6;
                
        }
        
        public static void createRestingStateSystem(Genome chromosome) 
        {
            String dx0 = "";
                for (Object key : chromosome.x0Alphas.keySet()) {
                    dx0 += "((1/" + chromosome.x0Alphas.get(key)+ ")(x"+ key.toString().charAt(2) + "[t]-x0[t]))+";
                }
                //Get rid of trailing + sign...
                dx0 = dx0.substring(0, dx0.length()-1);
               
                 String dx1 = "";
                for (Object key : chromosome.x1Alphas.keySet()) {
                    dx1 += "((1/" + chromosome.x1Alphas.get(key)+ ")(x"+ key.toString().charAt(2) + "[t]-x1[t]))+";
                }
                //Get rid of trailing + sign...
                dx1 = dx1.substring(0, dx1.length()-1);
                
                String dx2 = "";
                for (Object key : chromosome.x2Alphas.keySet()) {
                    dx2 += "((1/" + chromosome.x2Alphas.get(key)+ ")(x"+ key.toString().charAt(2) + "[t]-x2[t]))+";
                }
                //Get rid of trailing + sign...
                dx2 = dx2.substring(0, dx2.length()-1);
                
                String dx3 = "";
                for (Object key : chromosome.x3Alphas.keySet()) {
                    dx3 += "((1/" + chromosome.x3Alphas.get(key)+ ")(x"+ key.toString().charAt(2) + "[t]-x3[t]))+";
                }
                //Get rid of trailing + sign...
                dx3 = dx3.substring(0, dx3.length()-1);
                
                String dx4 = "";
                for (Object key : chromosome.x4Alphas.keySet()) {
                    dx4 += "((1/" + chromosome.x4Alphas.get(key)+ ")(x"+ key.toString().charAt(2) + "[t]-x4[t]))+";
                }
                //Get rid of trailing + sign...
                dx4 = dx4.substring(0, dx4.length()-1);
 
                String dx5 = "";
                for (Object key : chromosome.x5Alphas.keySet()) {
                    dx5 += "((1/" + chromosome.x5Alphas.get(key)+ ")(x"+ key.toString().charAt(2) + "[t]-x5[t]))+";
                }
                //Get rid of trailing + sign...
                dx5 = dx5.substring(0, dx5.length()-1);
                
                String dx6 = "";
                for (Object key : chromosome.x6Alphas.keySet()) {
                    dx6 += "((1/" + chromosome.x6Alphas.get(key)+ ")(x"+ key.toString().charAt(2) + "[t]-x6[t]))+";
                }
                //Get rid of trailing + sign...
                dx6 = dx6.substring(0, dx6.length()-1);
                
                chromosome.dx0 = dx0;
                chromosome.dx1 = dx1;
                chromosome.dx2 = dx2;
                chromosome.dx3 = dx3;
                chromosome.dx4 = dx4;
                chromosome.dx5 = dx5;
                chromosome.dx6 = dx6;
                
        }
        
       
        public void CrossoverPMX(Genome Parent1, Genome Parent2, Genome Child1, Genome Child2)
        {

            if (NextFloat(Genome.randGen) > crossoverRate || Parent1 == Parent2)
            {
                return;
            }

            Child1 = Parent1.Clone();
            Child2 = Parent2.Clone();

            int beg = 0;
           
            while (beg == 0 || beg == 6 || beg == 7)
            {
                beg = Genome.randGen.nextInt(Parent1.tourVector.size() - 2);
            }

            int end = beg;

            while (end <= beg || end == 7)
            {
                end = Genome.randGen.nextInt(Parent1.tourVector.size() - 1);
            }
            for (int i = beg; i <= end; i++)
            {
                int swapMapPos1 = Child1.tourVector.get(i);
                int swapMapPos2 = Child2.tourVector.get(i);
                swapGenes(Child1, swapMapPos1, swapMapPos2);
                swapGenes(Child2, swapMapPos1, swapMapPos2);
            }
        }

        public void swapGenes(Genome chromosome, int swapNumber1, int swapNumber2)
        {
            int index1 = chromosome.tourVector.indexOf(swapNumber1);
            int index2 = chromosome.tourVector.indexOf(swapNumber2);
            chromosome.tourVector.set(index1, swapNumber2);
            chromosome.tourVector.set(index2, swapNumber1);
        }


        public void Mutate(Genome chromosome)
        {
            if (NextFloat(Genome.randGen) > this.mutationRate)
            {
                return;
            }
            else
            {
                int pos1 = 0;

                while (pos1 == 0 || pos1 == 7)
                {
                    pos1 = Genome.randGen.nextInt(Genome.GENOME_LENGTH);
                }

                int pos2 = pos1;

                while (pos2 == pos1 || pos2 == 0 || pos2 == 7)
                {
                    pos2 = Genome.randGen.nextInt(Genome.GENOME_LENGTH);
                }
                swapGenes(chromosome, pos1, pos2);
                return;
            }
        }

        public Genome RouletteWheelSelection(ArrayList<Genome> population)
        {

            double fSlice = NextFloat(Genome.randGen) * totalPopulationFitness;

            double cfTotal = 0;
            int selectedGenomeIndex = 0;

            for (int i = 0; i < population.size(); i++)
            {

                cfTotal += population.get(i).fitness;

                if (cfTotal > fSlice)
                {
                    selectedGenomeIndex = i;
                    break;
                }
            }
            return population.get(selectedGenomeIndex);
        }

    private class ArrayListImpl extends ArrayList<ArrayList<Integer[]>> {

        public ArrayListImpl() {
        }
        { add(node0); add(node1); add(node2); add(node3); add(node4); add(node5); add(node6); }
    }
    }