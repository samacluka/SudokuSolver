package jeans;

import game.Group;
import game.Sudoku;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Genie {

    private Sudoku original;

    private Integer populationSize;
    private Double selectionRate;
    private Double randomSelectionRate;
    private Integer nbChildren;
    private Integer maxNbGenerations;
    private Double mutationRate;
    private HashMap<Method[], Integer> crossoverMethods;

    private Integer maxNbGenerationsWithoutImprovement;

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();

        new Genie();

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        long hours = executionTime / (60 * 60 * 1000);
        long minutes = (executionTime % (60 * 60 * 1000)) / (60 * 1000);
        long seconds = (executionTime % (60 * 1000)) / 1000;
        long milliseconds = executionTime % 1000;

        System.out.println("Execution time: " + hours + " hours, " +
                minutes + " minutes, " +
                seconds + " seconds, " +
                milliseconds + " milliseconds");
    }

    public Genie() throws Exception {
        Integer[] grid = {
            8, 0, 2,   0, 0, 3,   5, 1, 0,
            0, 6, 0,   0, 9, 1,   0, 0, 3,
            7, 0, 1,   0, 0, 0,   8, 9, 4,

            6, 0, 8,   0, 0, 4,   0, 2, 1,
            0, 0, 0,   2, 5, 8,   0, 6, 0,
            9, 2, 0,   3, 1, 0,   4, 0, 0,

            0, 0, 0,   4, 0, 2,   7, 8, 0,
            0, 0, 5,   0, 8, 9,   0, 0, 0,
            2, 0, 0,   0, 0, 7,   1, 0, 0
        };

        populationSize = 5000;
        selectionRate = 0.2;
        randomSelectionRate = 0.3;
        nbChildren = 4;
        mutationRate = 0.6;
        maxNbGenerations = 500;
        maxNbGenerationsWithoutImprovement = 50;
        crossoverMethods = new HashMap<>();

        crossoverMethods.put(new Method[]{Sudoku.class.getMethod("getRow", Integer.class), Sudoku.class.getMethod("setRow", Integer.class, Group.class)}, Group.GROUP_SIZE);
        crossoverMethods.put(new Method[]{Sudoku.class.getMethod("getColumn", Integer.class) , Sudoku.class.getMethod("setColumn", Integer.class, Group.class)}, Group.GROUP_SIZE);
        crossoverMethods.put(new Method[]{Sudoku.class.getMethod("getBox", Integer.class) , Sudoku.class.getMethod("setBox", Integer.class, Group.class)}, Group.GROUP_SIZE);

        original = new Sudoku(grid);

        if(original.validate().equals(Boolean.TRUE)){
            System.out.println("Solved Via Marking");
            original.display();
        }
        else {
            System.out.println("Starting GA Solution");

            Sudoku solution = solveGA(original);

            if(solution == null){
                System.err.println("No solution was found.");
            }
            else {
                solution.display();
            }
        }
    }

    private Sudoku solveGA(Sudoku origin) {
        Sudoku[] population;

        int overallNbGenerations = 0;
        while(maxNbGenerations > overallNbGenerations){
            population = createGeneration(origin);

            int nbGenerationsWithoutImprovement = 0;
            int bestScore = 10000;
            while(maxNbGenerationsWithoutImprovement > nbGenerationsWithoutImprovement){
                population = sortByScore(population);

                if(population[0].validate().equals(Boolean.TRUE)) {
                    return population[0];
                }

                if(bestScore <= population[0].getScore()){
                    nbGenerationsWithoutImprovement++;
                }
                else {
                    bestScore = population[0].getScore();
                    nbGenerationsWithoutImprovement = 0;
                }

                population = matePopulation(population);
                population = mutatePopulation(population);

                overallNbGenerations++;
                System.out.println(String.format("Generation %s has a best score of %s", overallNbGenerations, bestScore));
                System.out.println(" ");
                population[0].display();
            }
            System.out.println(String.format("Restarting: %s number of generations", overallNbGenerations));
        }

        return null;
    }

    private Sudoku[] createGeneration(Sudoku origin) {
        Integer[] grid = origin.getOriginalGrid();
        populationSize = populationSize % 2 == 0 ? populationSize : populationSize+1; // must be even for matched mating

        Sudoku[] cousins = new Sudoku[populationSize];

        for(int i = 0; i < populationSize; i++){
            cousins[i] = (new Sudoku(grid)).fillBoxes();
        }

        return cousins;
    }

    private Sudoku[] sortByScore(Sudoku[] population){
        return Arrays.stream(population)
            .sorted((sudoku1, sudoku2) -> {
                // Compare by the scores returned by getScore() method
                int score1 = 0;
                int score2 = 0;
                try {
                    score1 = sudoku1.getScore();
                    score2 = sudoku2.getScore();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return Integer.compare(score1, score2);
            }).toArray(Sudoku[]::new);

    }

    private Sudoku[] matePopulation(Sudoku[] population){
        Sudoku[] newPopulation = new Sudoku[population.length];
        Sudoku[] children;
        for(int i = 0; i < population.length; i = i + nbChildren){
            children = mateCrossover( getParents(population) );
            System.arraycopy(children, 0, newPopulation, i, children.length);
        }

        return newPopulation;
    }

    private Sudoku[] getParents(Sudoku[] population){
        List<Sudoku> parents = new ArrayList<>();

        int nbBestToSelect = (int) (population.length * selectionRate);
        int nbRandomToSelect = (int) (population.length * randomSelectionRate);

        for(int i = 0; i < nbBestToSelect; i++){
            parents.add(population[i]);
        }

        for(int j = 0; j < nbRandomToSelect; j++){
            parents.add(population[ThreadLocalRandom.current().nextInt(0, population.length)]);
        }

        Collections.shuffle(parents);
        return parents.toArray(new Sudoku[0]);
    }

    private Sudoku[] mutatePopulation(Sudoku[] population) {
        Sudoku[] mutatedPopulation = new Sudoku[population.length];
        for(int i = 0; i < population.length; i++){
            mutatedPopulation[i] = ThreadLocalRandom.current().nextInt(0, 101) <= mutationRate * 100 ? population[i].mutate() : population[i];
        }
        return mutatedPopulation;
    }

    private Sudoku[] mateCrossover(Sudoku[] parents){
        return mate(parents, "crossover");
    }

    private Sudoku[] mateBestGenes(Sudoku[] parents){
        return mate(parents, "bestGenes");
    }

    private Sudoku[] mate(Sudoku[] parents, String type){
        Sudoku[] children = new Sudoku[nbChildren];

        List<Method[]> methods = new ArrayList<>(crossoverMethods.keySet());
        List<Integer> maxCrossoverPoints = new ArrayList<>(crossoverMethods.values());

        int methodIndex = 2; // Always boxes //ThreadLocalRandom.current().nextInt(0, methods.size());
        Method[] method = methods.get(methodIndex);
        Integer maxCrossoverPoint = maxCrossoverPoints.get(methodIndex);

        int p1;
        int p2;

        Sudoku parent1;
        Sudoku parent2;

        try {
            // create a number of children
            for(int j = 0; j < nbChildren; j++){

                // Get to different parents
                do {
                    p1 = ThreadLocalRandom.current().nextInt(0, parents.length);
                    p2 = ThreadLocalRandom.current().nextInt(0, parents.length);
                } while(p1 == p2);

                parent1 = parents[p1];
                parent2 = parents[p2];

                children[j] = parent1.copyLocked();

                // for all the crossOverPoints
                for(int i = 0; i < maxCrossoverPoint; i++){
                    int crossoverPoint = ThreadLocalRandom.current().nextInt(0, maxCrossoverPoint);
                    method[1].invoke(
                        children[j],
                        i,
                        (
                            Objects.equals(type, "crossover") ? getCrossover(method[0], i, crossoverPoint, parent1, parent2) :
                            Objects.equals(type, "bestGenes") ? getBestGene(method[0], i, parent1, parent2) :
                            null
                        )
                    );
                }
            }
        }
        catch(IllegalAccessException | InvocationTargetException e){
            throw new RuntimeException(e);
        }

        return children;
    }

    private Group getCrossover(Method method, int groupIndex, int crossoverPoint, Sudoku parent1, Sudoku parent2) throws IllegalAccessException, InvocationTargetException {
        return (Group) method.invoke(
            groupIndex > crossoverPoint ? parent1 : parent2, // take from one or the other parent based on the crossover point
            groupIndex
        );
    }

    private Group getBestGene(Method method, int groupIndex, Sudoku parent1, Sudoku parent2) throws IllegalAccessException, InvocationTargetException {
        Group g1 = (Group) method.invoke(parent1, groupIndex);
        Group g2 = (Group) method.invoke(parent2, groupIndex);

        return g1.getIsValid() && !g2.getIsValid() ? g1 :
        !g1.getIsValid() && g2.getIsValid() ? g2 :
        ThreadLocalRandom.current().nextInt(0, 1) == 1 ? g1 : g2;
    }

}

/*******************************************/
/** PENCIL MARK SOLVES IN ~10 MILLISECONDS */
/*******************************************/
//    Integer[] grid = {
//            8, 0, 2,   0, 0, 3,   5, 1, 0,
//            0, 6, 0,   0, 9, 1,   0, 0, 3,
//            7, 0, 1,   0, 0, 0,   8, 9, 4,
//
//            6, 0, 8,   0, 0, 4,   0, 2, 1,
//            0, 0, 0,   2, 5, 8,   0, 6, 0,
//            9, 2, 0,   3, 1, 0,   4, 0, 0,
//
//            0, 0, 0,   4, 0, 2,   7, 8, 0,
//            0, 0, 5,   0, 8, 9,   0, 0, 0,
//            2, 0, 0,   0, 0, 7,   1, 0, 0
//    };

//Integer[] grid = {
//        8, 7, 3,   4, 1, 0,   9, 0, 0,
//        0, 6, 5,   0, 2, 8,   0, 7, 0,
//        0, 2, 0,   7, 0, 3,   0, 0, 0,
//
//        5, 4, 0,   0, 0, 0,   2, 1, 0,
//        2, 0, 8,   0, 0, 7,   4, 9, 0,
//        6, 9, 0,   0, 8, 0,   0, 0, 0,
//
//        4, 8, 0,   0, 0, 0,   5, 0, 0,
//        7, 0, 0,   0, 3, 1,   6, 0, 9,
//        0, 1, 0,   0, 0, 9,   8, 0, 7
//};

/*******************************************/
/** ********** BEYOND MY PATIENCE ******** */
/*******************************************/
//Integer[] grid = {
//        0, 8, 0,   0, 0, 0,   0, 0, 3,
//        0, 6, 0,   0, 7, 0,   4, 0, 5,
//        3, 0, 4,   0, 6, 1,   0, 0, 0,
//
//        5, 0, 0,   9, 0, 0,   6, 0, 2,
//        0, 0, 0,   0, 0, 0,   0, 3, 1,
//        0, 0, 7,   0, 0, 0,   0, 0, 0,
//
//        0, 0, 0,   1, 0, 6,   2, 0, 0,
//        2, 0, 8,   0, 4, 5,   0, 0, 6,
//        0, 0, 6,   0, 0, 0,   0, 5, 7
//};

//Integer[] grid = {
//        4, 0, 0,   0, 0, 0,   0, 0, 1,
//        0, 1, 6,   9, 0, 7,   0, 0, 8,
//        9, 2, 0,   0, 1, 0,   0, 5, 0,
//
//        0, 0, 0,   0, 0, 1,   0, 0, 0,
//        1, 8, 5,   3, 2, 9,   0, 0, 0,
//        0, 0, 0,   0, 0, 5,   8, 1, 0,
//
//        0, 0, 0,   7, 5, 0,   0, 0, 9,
//        7, 4, 0,   0, 9, 3,   0, 0, 0,
//        3, 0, 9,   0, 0, 0,   0, 2, 7
//};