import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Population {

    private ArrayList<Individual> individualsList = new ArrayList<>();
    private int populationMaxSize;

    private char[] possibleAlleles;
    private int genesLength;

    private int mutationCount = 0;

    //We generate a random population
    public Population(char[] possibleAlleles, int genesLength, int populationSize) {

        for ( int i = 0; i < populationSize; i++) {

            //We add a randomly generated individual to the population
            this.individualsList.add(new Individual(possibleAlleles, genesLength));
        }

        this.populationMaxSize = populationSize;
        this.possibleAlleles = possibleAlleles;
        this.genesLength = genesLength;


    }

    //Methods

    //compute all fitness of the population
    public void calculateFitness(String password) {
        for ( Individual individual : individualsList)
            individual.calcFitness(password);
    }

    //NEW GENERATION
    public void newGeneration() {


        ArrayList<Individual> parents;
        ArrayList<Individual> childs;
        Individual indiv;

        //bubblesort(individualsList);

        ArrayList<Individual> newPop = new ArrayList<>();

        //Top 1 elitism
        indiv = bestFitness();
        indiv.aging();
        newPop.add(indiv);

        //5% new blood
        for (int i = 0; i < individualsList.size()/20 + 1; i++) {
            indiv = new Individual(possibleAlleles, genesLength);
            indiv.age = -1;
            newPop.add(indiv);
        }

        //What's left is offsprings
        while ( newPop.size() < populationMaxSize ) {
            //parents = tournamentSelection(4);
            parents = rouletteWheelSelection();
            childs = uniformCrossover(parents);
            if ( tryMutate(childs) );
                mutationCount++;
            newPop.addAll(childs);
        }

        //We replace the old generation by the new one
        individualsList.clear();
        individualsList.addAll(newPop);

    }

    //PARENT SELECTION METHODS
    //methods to choose two parents to reproduce.
    //region parent selection

    public ArrayList<Individual> tournamentSelection(int nbIndividualsToCompare) {

        ArrayList<Individual> parents = new ArrayList<>();
        ArrayList<Individual> pickedIndividuals = new ArrayList<>();

        int ind_rand;
        Individual Indiv = null;
        boolean alreadyPicked;

        for ( int k = 0; k < nbIndividualsToCompare; k++) {

            do {
                //We pick a random individual from the population and add it to the list
                ind_rand = new Random().nextInt(individualsList.size());
                Indiv = individualsList.get(ind_rand);

                //We check we haven't already picked this one
                alreadyPicked = pickedIndividuals.contains(Indiv);

            } while ( alreadyPicked );

            //We add it to the picked potential parents list
            pickedIndividuals.add(Indiv);
        }

        //We choose the two parents with the best fitness from the picked ones.
        parents.add(bestFitness(pickedIndividuals)); //parent 1
        pickedIndividuals.remove(parents.get(0)); //We remove parent 1 from the picked list
        parents.add(bestFitness(pickedIndividuals)); //parent 2

        return parents;
    }

    public ArrayList<Individual> rouletteWheelSelection() {

        ArrayList<Individual> parents = new ArrayList<>();
        Individual parent;

        //We roulette selet the first parent
        parents.add(rouletteWheelSelectionParent());

        //We roulette select another parent until it's a different one.
        do {
            parent = rouletteWheelSelectionParent();
        }
        while ( parents.get(0) == parent );
        parents.add(parent);

        return parents;
    }


    public Individual rouletteWheelSelectionParent() {

        float fitnessSum = 0;
        //We calculate the sum of all fitnesses
        for (Individual individual : individualsList)
            fitnessSum += individual.fitness;

        //We pick a random number between 0 and the sum
        Random r = new Random();
        double value = (fitnessSum) * r.nextDouble();

        //We decrease this value by the fitness until we have a winner.
        for (Individual individual : individualsList) {
            value -= individual.fitness;
            if (value < 0) return individual;
        }

        System.out.println("Erreur aucun parent choisi!");
        return null;
    }



    //endregion

    //CROSSOVER METHODS
    //region crossover

    //Generate two childs given two parents using uniform crossover.
    public ArrayList<Individual> uniformCrossover(ArrayList<Individual> parents) {

        ArrayList<Individual> childs = new ArrayList<>();
        int length = parents.get(0).getLength();
        char temp;

        //We clone the parents as a template for the two childs
        for ( Individual parent : parents ) {
            //System.out.println("parent avant : " +parent.toString());
            childs.add(parent.copy());
        }

        //Then we randomly exchange gene between childs to mix them.
        for ( int i = 0; i < length; i++) {

            if ( new Random().nextBoolean() ) {
                temp = childs.get(0).genes[i];
                childs.get(0).genes[i] = childs.get(1).genes[i];
                childs.get(1).genes[i] = temp;
            }
        }

        /*
        for ( Individual parent : parents ) {
            System.out.println("parent après : " +parent.toString());
        }
        for ( Individual child : childs ) {
            System.out.println("child : " +child.toString());
        }
        */

        return childs;

    }
    //endregion

    //MUTATIONS
    //region mutation

    //Try to mutate givens individuals depending on their mutation chances, return true if a mutation happened, else false.
    public boolean tryMutate(ArrayList<Individual> individuals) {

        boolean mutated = false;
        for ( Individual Indiv : individuals)
            if ( Indiv.tryMutate() )
                mutated = true;

        return mutated;
    }
    //endregion

    //SURVIVOR SELECTION METHODS
    //region survivor selection
    //Remove the extra population with the worst fitness score.
    public void FitnessSelection() {

        //we compute the number of individuals to remove
        int extraInd = individualsList.size() - populationMaxSize;

        if ( extraInd <= 0 ) return;

        for ( int i = 0; i < extraInd; i++ ) {
            individualsList.remove(worstFitness(individualsList));
        }
    }

    //Remove the extra oldest population
    public void AgeSelection() {

        //we compute the number of individuals to remove
        int extraInd = individualsList.size() - populationMaxSize;

        if ( extraInd <= 0 ) return;

        for ( int i = 0; i < extraInd; i++ ) {
            individualsList.remove(worstFitness(individualsList));
        }
    }

    //Remove the population older than ageMax
    public void AgeSelection(int ageMax) {

        for ( Individual individual : individualsList) {
            if ( individual.age > ageMax )
                individualsList.remove(individual);
        }
    }
    //endregion


    //Return the individual with the best fitness in the given population. O(n)
    public Individual bestFitness(ArrayList<Individual> population) {
        Individual bestIndiv = null;
        for ( Individual individual : population ) {
            if ( bestIndiv == null)
                bestIndiv = individual;
            else if ( individual.fitness > bestIndiv.fitness )
                bestIndiv = individual;
        }
        return bestIndiv;
    }

    //Return the individual with the best fitness in the given population. O(n)
    public Individual bestFitness() {
        return bestFitness(individualsList);
    }

    //Return the individual with the best fitness in the population. O(n)
    public float bestFitnessInPopulation() {
        return bestFitness(individualsList).fitness;
    }


    //Return the individual with the worst fitness in the given population. O(n)
    public Individual worstFitness(ArrayList<Individual> population) {
        Individual worstIndiv = null;
        for ( Individual individual : population ) {
            if ( worstIndiv == null)
                worstIndiv = individual;
            else if ( individual.fitness < worstIndiv.fitness )
                worstIndiv = individual;
        }
        return worstIndiv;
    }

    //Return the oldest individual in given population. O(n)
    public Individual highestAge(ArrayList<Individual> population) {
        Individual oldestIndiv = null;
        for ( Individual individual : population ) {
            if ( oldestIndiv == null)
                oldestIndiv = individual;
            else if ( individual.age > oldestIndiv.age )
                oldestIndiv = individual;
        }
        return oldestIndiv;
    }


    public static void bubblesort(ArrayList<Individual> population) {

        for ( int i = population.size() - 1; i > 0; i--) {
            for ( int j = 0; j < i; j++ ) {
                if ( population.get(j).fitness < population.get(j+1).fitness ) {
                    Collections.swap(population, j, j+1);
                }
            }
        }

    }

    public int getMutationCount() {
        return mutationCount;
    }
    @Override
    public String toString() {
        String str = "";
        int nb = 1;
        for ( Individual individual : individualsList) {
            //str += "\n\t #"+nb+" : ";
            str += "\n\t" + individual.toString();
            nb++;
        }
        return str;
    }
}
