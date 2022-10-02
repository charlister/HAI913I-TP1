package partie2.analysis;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import partie2.parser.Parser;
import partie2.projectexplorer.ProjectExplorer;
import partie2.visitors.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class VisitDataCollector {
    private int classCounter;
    private int lineOfCodeCounter;
    private int methodCounter;
    private int attributeCounter;
    private Map<TypeDeclaration, Integer> mapClassNbAttributes;
    private Map<TypeDeclaration, Integer> mapClassNbMethods;
    private Map<MethodDeclaration, Integer> mapMethodNbLines;
    private Map<MethodDeclaration, Integer> mapMethodNbParams;
    private Set<String> packageNames;

    public VisitDataCollector() {
        this.classCounter      = 0;
        this.lineOfCodeCounter = 0;
        this.methodCounter     = 0;
        this.attributeCounter  = 0;
        this.mapClassNbAttributes = new HashMap<>();
        this.mapClassNbMethods    = new HashMap<>();
        this.mapMethodNbLines     = new HashMap<>();
        this.mapMethodNbParams    = new HashMap<>();
        this.packageNames         = new HashSet<>();
    }

    // 1 bis.
    private void collectNumberOfApplicationClasses(CompilationUnit cu) {
        TypeDeclarationVisitor visitorClass = new TypeDeclarationVisitor();
        cu.accept(visitorClass);
        classCounter += visitorClass.getTypeDeclarationList().size();
    }

    // 1.
    public int getNumberOfApplicationClasses() {
        return classCounter;
    }

    // 2 bis.
    private void collectNumberOfLineOfApplicationCode(CompilationUnit cu) {
        GreaterVisitor visitorFileSource = new GreaterVisitor();
        cu.accept(visitorFileSource);
        lineOfCodeCounter += visitorFileSource.getNbLinesOfCode();
    }

    // 2.
    public int getNumberOfLineOfApplicationCode() {
        return lineOfCodeCounter;
    }

    // 3 bis.
    private void collectApplicationMethodsData(CompilationUnit cu) {
        TypeDeclarationVisitor visitorClass = new TypeDeclarationVisitor();
        cu.accept(visitorClass);

        int nbMethod = 0;
        for (TypeDeclaration nodeClass : visitorClass.getTypeDeclarationList()) {
            MethodDeclarationVisitor visitorMethod = new MethodDeclarationVisitor();
            nodeClass.accept(visitorMethod);
            nbMethod = visitorMethod.getMethodDeclarationList().size();
            methodCounter += nbMethod;
            mapClassNbMethods.put(nodeClass, nbMethod);
        }
    }

    // 3.
    public int getNumberOfApplicationMethods() {
        return methodCounter;
    }

    // 4 bis.
    private void collectAppPackages(CompilationUnit cu) {
        PackageDeclarationVisitor visitorPackage = new PackageDeclarationVisitor();
        cu.accept(visitorPackage);
        packageNames.addAll(visitorPackage.buildSubPackages());
    }

    // 4.
    public int getTotalNumberOfAppPackages() {
        return packageNames.size();
    }

    // 5.
    public int getAverageNumberOfMethodsDependingOnClasses() {
        return methodCounter/classCounter;
    }

    // 6 bis.
    private void collectNumberOfRowByMethod(CompilationUnit cu) {
        MethodDeclarationVisitor visitorMethod = new MethodDeclarationVisitor();
        cu.accept(visitorMethod);
        for (MethodDeclaration nodeMethod : visitorMethod.getMethodDeclarationList()) {
            GreaterVisitor visitorMethodBody = new GreaterVisitor();
            nodeMethod.accept(visitorMethodBody);
            mapMethodNbLines.put(nodeMethod, visitorMethodBody.getNbLinesOfCode());
        }
    }

    // 6.
    public int getAverageNumberOfCodeLinesDependingOnMethod() {
        return mapMethodNbLines
                .values()
                .stream()
                .reduce(0, (x1, x2) -> x1 + x2) / methodCounter;
    }

    // 7 bis.
    private void collectAttributeData(CompilationUnit cu) {
        TypeDeclarationVisitor visitorClass = new TypeDeclarationVisitor();
        cu.accept(visitorClass);

        int nbAttributes = 0;
        for (TypeDeclaration nodeClass : visitorClass.getTypeDeclarationList()) {
            FieldDeclarationVisitor visitorAttributes = new FieldDeclarationVisitor();
            nodeClass.accept(visitorAttributes);
            nbAttributes = visitorAttributes.getFieldDeclarationList().size();
            attributeCounter += nbAttributes;
            mapClassNbAttributes.put(nodeClass, nbAttributes);
        }
    }

    // 7.
    public int getAverageNumberOfAttributesDependingOnClasses() {
        return attributeCounter/classCounter;
    }

    // 8, 9 bis.
    private int get10PercentsOfTotalNumberOfClasses() {
        return (int) (((double) classCounter)*10/100);
    }

    // 8, 9 bis.
    private Map<TypeDeclaration, Integer> sortMapClassIntegerAndLimit(Map<TypeDeclaration, Integer> map, int numberExpected) {
        // desc on map values
        return map
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())) // delete Comparator.reverseOrder() from parenthesis -> asc on map values
                .limit(numberExpected)
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
    }

    // 8.
    public Map<TypeDeclaration, Integer> get10PercentsClassesWithMostMethods() {
        return sortMapClassIntegerAndLimit(mapClassNbMethods, get10PercentsOfTotalNumberOfClasses());
    }

    // 9.
    public Map<TypeDeclaration, Integer> get10PercentsClassesWithMostAttributes() {
        return sortMapClassIntegerAndLimit(mapClassNbAttributes, get10PercentsOfTotalNumberOfClasses());
    }

    // 10.
    public Map<TypeDeclaration, Integer> get10PercentsClassesWithMostAttributesAndMethods(Map<TypeDeclaration, Integer> map1, Map<TypeDeclaration, Integer> map2) {
        return map1
                .entrySet()
                .stream()
                .filter(x -> map2.containsKey(x.getKey()))
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
    }

    // 11.
    public Map<TypeDeclaration, Integer> getClassesWithNbMethodsMoreThanX(int x) {
        return mapClassNbMethods
                .entrySet()
                .stream()
                .filter(e -> e.getValue()>x)
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
    }

    // 12 bis.
    private Map<MethodDeclaration, Integer> sortMapMethodIntegerAndLimit(Map<MethodDeclaration, Integer> map, int numberExpected) {
        // desc on map values
        return map
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())) // delete Comparator.reverseOrder() from parenthesis -> asc on map values
                .limit(numberExpected)
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
    }

    // 12 bis.
    private int get10PercentsOfTotalNumberOfMethods() {
        return (int) (((double) methodCounter)*10/100);
    }

    // 12.
    public Map<MethodDeclaration, Integer> get10PercentsMethodsWithMostLinesOfCode() {
        return sortMapMethodIntegerAndLimit(mapMethodNbLines, get10PercentsOfTotalNumberOfMethods());
    }

    // 13 bis.
    private void collectMethodParamsData(CompilationUnit cu) {
        MethodDeclarationVisitor visitorMethodParameters = new MethodDeclarationVisitor();
        cu.accept(visitorMethodParameters);
        for (MethodDeclaration nodeMethod :
                visitorMethodParameters.getMethodDeclarationList()) {
            mapMethodNbParams.put(nodeMethod, nodeMethod.parameters().size());
        }
    }

    // 13.
    public Map<MethodDeclaration, Integer> getMethodWithMostParams() {
        return sortMapMethodIntegerAndLimit(mapMethodNbParams, 1);
    }

    public static void displayMap(Map<TypeDeclaration, Integer> map, String msg, String strClassOrMethod) {
        System.out.println(msg);
        map
                .entrySet()
                .stream()
                .forEach(x -> System.out.println(
                        "( " + strClassOrMethod + " ) : " + x.getKey().getName() + "\t\t"  +
                                "( " + x.getValue() + " )")
                );
    }

    public void makeAnalysis(String pathProject) throws IOException {
        ProjectExplorer projectExplorer = new ProjectExplorer(pathProject);
        ArrayList<File> javaFiles = projectExplorer.listJavaFilesForFolder();
        for(File javaFile : javaFiles) {
            String content = FileUtils.readFileToString(javaFile);
            CompilationUnit cu = Parser.parseSource(content.toCharArray());

            collectApplicationMethodsData(cu); 
            collectAppPackages(cu);
            collectAttributeData(cu);
            collectMethodParamsData(cu);
            collectNumberOfApplicationClasses(cu);
            collectNumberOfLineOfApplicationCode(cu);
            collectNumberOfRowByMethod(cu);
        }
    }
}
