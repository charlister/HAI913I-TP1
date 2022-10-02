package partie2.main;

import partie2.analysis.VisitDataCollector;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        String projectPath = "src\\main\\java\\project";

        VisitDataCollector visitDataCollector = new VisitDataCollector();
        visitDataCollector.makeAnalysis(projectPath);

        System.out.println("NUMBER OF CLASSES : " + visitDataCollector.getNumberOfApplicationClasses());
        System.out.println("NUMBER OF METHODS : " + visitDataCollector.getNumberOfApplicationMethods());
        System.out.println("METHODS AVERAGE : " + visitDataCollector.getAverageNumberOfMethodsDependingOnClasses());
    }
}
