package partie2.main;

import partie2.exceptions.EmptyProjectException;
import partie2.exceptions.NotFoundPathProjectException;
import partie2.processor.VisitDataCollector;

import java.io.IOException;
import java.util.Scanner;

public class Application {
    private Scanner sc;
    private StringBuilder listFeatures;

    public Application() {
        sc = new Scanner(System.in);
        buildMenu();
    }

    private void buildMenu() {
        listFeatures = new StringBuilder();
        listFeatures.append("==============================   MENU   ==============================\n");
        listFeatures.append("0.        Analyser un nouveau projet\n");
        listFeatures.append("1.        Nombre de classes de l’application.\n");
        listFeatures.append("2.        Nombre de lignes de code de l’application.\n");
        listFeatures.append("3.        Nombre total de méthodes de l’application.\n");
        listFeatures.append("4.        Nombre total de packages de l’application.\n");
        listFeatures.append("5.        Nombre moyen de méthodes par classe.\n");
        listFeatures.append("6.        Nombre moyen de lignes de code par méthode.\n");
        listFeatures.append("7.        Nombre moyen d’attributs par classe.\n");
        listFeatures.append("8.        Les 10% des classes qui possèdent le plus grand nombre de méthodes.\n");
        listFeatures.append("9.        Les 10% des classes qui possèdent le plus grand nombre d’attributs.\n");
        listFeatures.append("10.       Les classes qui font partie en même temps des deux catégories précédentes.\n");
        listFeatures.append("11.       Les classes qui possèdent plus de X méthodes (la valeur de X est à saisir).\n");
        listFeatures.append("12.       Les 10% des méthodes qui possèdent le plus grand nombre de lignes de code (par classe).\n");
        listFeatures.append("13.       Le nombre maximal de paramètres par rapport à toutes les méthodes de l’application.\n");
        listFeatures.append("14.       Générer un graphe d'appel.\n");
        listFeatures.append("menu.     Afficher le menu de nouveau.\n");
        listFeatures.append("quitter.  Quitter l’application.\n");
    }

    private void displayFeatures() {
        System.out.print(listFeatures);
    }

    private void chooseAFeatures(VisitDataCollector visitDataCollector) throws IOException, InterruptedException, EmptyProjectException, NotFoundPathProjectException {
        displayFeatures();
        String choice = "";
        int tmp = 0;
        while (!choice.equals("quitter")) {
            System.out.print("\nCHOISIR UNE OPTION : ");
            choice = sc.nextLine();
            switch (choice.trim()) {
                case "0":
                    System.out.print("Veuillez indiquer le repertoire vers le nouveau projet à analyser : ");
                    String projectPath = sc.nextLine();
                    VisitDataCollector newVisitDataCollector = new VisitDataCollector();
                    newVisitDataCollector.makeAnalysis(projectPath);
                    chooseAFeatures(newVisitDataCollector);
                    break;
                case "1":
                    System.out.println("Le nombre de classes de l'application : " + visitDataCollector.getNumberOfApplicationClasses());
                    break;
                case "2":
                    System.out.println("Le nombre de lignes de code de l’application : " + visitDataCollector.getNumberOfLineOfApplicationCode());
                    break;
                case "3":
                    System.out.println("Le nombre total de méthodes de l’application : " + visitDataCollector.getNumberOfApplicationMethods());
                    break;
                case "4":
                    System.out.println("Le nombre total de packages de l’application : " + visitDataCollector.getTotalNumberOfAppPackages());
                    break;
                case "5":
                    tmp = visitDataCollector.getAverageNumberOfMethodsDependingOnClasses();
                    System.out.println("Le nombre moyen de méthodes par classe : " + (tmp == -1 ? "incalculable car le nombre de classes est égal à 0." : tmp));
                    break;
                case "6":
                    tmp = visitDataCollector.getAverageNumberOfCodeLinesDependingOnMethod();
                    System.out.println("Le nombre moyen de lignes de code par méthode : " + (tmp == -1 ? "incalculable car le nombre de méthodes est égal à 0." : tmp));
                    break;
                case "7":
                    tmp = visitDataCollector.getAverageNumberOfAttributesDependingOnClasses();
                    System.out.println("Le nombre moyen d’attributs par classe : " + (tmp == -1 ? "incalculable car le nombre de classes est égal à 0." : tmp));
                    break;
                case "8":
                    System.out.println("Les 10% des classes qui possèdent le plus grand nombre de méthodes : ");
                    VisitDataCollector.displayMapClassInteger(visitDataCollector.get10PercentsClassesWithMostMethods());
                    break;
                case "9":
                    System.out.println("Les 10% des classes qui possèdent le plus grand nombre d’attributs : ");
                    VisitDataCollector.displayMapClassInteger(visitDataCollector.get10PercentsClassesWithMostAttributes());
                    break;
                case "10":
                    System.out.println("Les classes qui possèdent à la fois le plus grand nombre de méthodes et d'attributs : ");
                    VisitDataCollector.displayMapClassInteger(visitDataCollector.get10PercentsClassesWithMostAttributesAndMethods(visitDataCollector.get10PercentsClassesWithMostAttributes(), visitDataCollector.get10PercentsClassesWithMostMethods()));
                    break;
                case "11":
                    System.out.print("Les classes qui possèdent plus de X méthodes : X = ");
                    boolean badInput = true;
                    while (badInput) {
                        try {
                            int x = sc.nextInt();
                            VisitDataCollector.displayMapClassInteger(visitDataCollector.getClassesWithNbMethodsMoreThanX(x));
                            badInput = true;
                        } catch (Exception e) {
                            System.err.print("X doit être un nombre entier : ");
                        }
                    }

                    break;
                case "12":
                    System.out.println("Les 10% des méthodes qui possèdent le plus grand nombre de lignes de code (par classe) : ");
                    VisitDataCollector.displayMapMethodInteger(visitDataCollector.get10PercentsMethodsWithMostLinesOfCode());
                    break;
                case "13":
                    System.out.println("Le nombre maximal de paramètres par rapport à toutes les méthodes de l’application : ");
                    VisitDataCollector.displayMapMethodInteger(visitDataCollector.getMethodWithMostParams());
                    break;
                case "14":
                    System.err.println("Génération du graphe d'appel ...");
                    Thread.sleep(1000);
                    VisitDataCollector.displayTheCAllGraph(visitDataCollector.getTheCallGraph());
                    visitDataCollector.buildGraphWithGraphViz();
                    visitDataCollector.buildGraphWithJGraphT();
                    Thread.sleep(1000);
                    System.out.println("Graphe d'appel généré !");
                    break;
                case "menu":
                    displayFeatures();
                    break;
                case "quitter":
                    break;
                default:
                    System.err.println("Choix incorrect ... Veuillez recommencer !");
                    break;
            }
        }
    }

    private void launch() throws IOException, InterruptedException, EmptyProjectException, NotFoundPathProjectException {
        System.out.print("Veuillez indiquer le repertoire vers le projet à analyser : ");
        String projectPath = sc.nextLine();

        VisitDataCollector visitDataCollector = new VisitDataCollector();
        visitDataCollector.makeAnalysis(projectPath);

        chooseAFeatures(visitDataCollector);
    }

    public static void main(String[] args) throws IOException, InterruptedException, EmptyProjectException, NotFoundPathProjectException {
        System.out.println
                (
                        " -----------------------------------------------\n" +
                        "| CETTE INTERFACE MET A VOTRE DISPOSITION UN    |\n" +
                        "| ENSEMBLE DE FONCTIONNALITES VOUS PERMETTANT   |\n" +
                        "| DE REALISER UNE ANALYSE STATIQUE D'UN PROJET. |\n" +
                        " -----------------------------------------------\n"
                );

        Application application = new Application();
        application.launch();
    }
}
