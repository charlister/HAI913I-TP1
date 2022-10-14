package partie2.processor;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.util.mxCellRenderer;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.Renderer;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.*;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;
import partie2.exceptions.EmptyProjectException;
import partie2.exceptions.NotFoundPathProjectException;
import partie2.parser.MyParser;
import partie2.visitors.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
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
    private Map<TypeDeclaration, Map<MethodDeclaration, Set<MethodInvocation>>> mapTheCallGraph;
    private Set<String> setLink;
    private DefaultDirectedGraph<String, DefaultEdge> graph;

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
        this.mapTheCallGraph      = new HashMap<>();
        this.setLink = new HashSet<>();
        this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);
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
        if (methodCounter == 0) {
            return -1;
        }
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
        if (classCounter == 0) {
            return -1;
        }
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

    // 14.
    public Map<TypeDeclaration, Map<MethodDeclaration, Set<MethodInvocation>>> getTheCallGraph() {
        return mapTheCallGraph;
    }

    public static void displayMapClassInteger(Map<TypeDeclaration, Integer> map) {
        map
                .entrySet()
                .stream()
                .forEach(x -> System.out.println(
                        "( CLASS ) : " + x.getKey().getName() + "\t\t"  +
                                "( " + x.getValue() + " )")
                );
    }

    public static void displayMapMethodInteger(Map<MethodDeclaration, Integer> map) {
        map
                .entrySet()
                .stream()
                .forEach(x -> System.out.println(
                        "( METHOD ) : " + x.getKey().getName() + "\t\t"  +
                                "( " + x.getValue() + " )")
                );
    }

//    BUILD GRAPH
    private void collectGraphData(CompilationUnit cu) {
        boolean isMethodNodeAdded;
        TypeDeclarationVisitor visitorClass = new TypeDeclarationVisitor();
        cu.accept(visitorClass);

        for (TypeDeclaration nodeClass : visitorClass.getTypeDeclarationList()) {
            MethodDeclarationVisitor visitorMethod = new MethodDeclarationVisitor();
            nodeClass.accept(visitorMethod);

            Map<MethodDeclaration, Set<MethodInvocation>> mapMethodDeclarationInvocation = new HashMap<>();
            String caller;

            for (MethodDeclaration nodeMethod : visitorMethod.getMethodDeclarationList()) {
                nodeMethod.resolveBinding();
                MethodInvocationVisitor visitorMethodInvocation = new MethodInvocationVisitor();
                nodeMethod.accept(visitorMethodInvocation);
                mapMethodDeclarationInvocation.put(nodeMethod, visitorMethodInvocation.getMethodInvocations());

                caller = nodeClass.getName().toString()+"::"+nodeMethod.getName();

                isMethodNodeAdded = false;

                for (MethodInvocation methodInvocation : visitorMethodInvocation.getMethodInvocations()) {

                    String callee;

                    if (methodInvocation.getExpression() != null) {
                        if (methodInvocation.getExpression().resolveTypeBinding() != null) {
                            if (!isMethodNodeAdded) {
                                graph.addVertex(caller);
                                isMethodNodeAdded = true;
                            }
                            callee = methodInvocation.getExpression().resolveTypeBinding().getName()+"::"+methodInvocation.getName();
                            graph.addVertex(callee);
                            graph.addEdge(caller, callee);

                            setLink.add("\t\""+caller+"\"->\""+callee+"\"\n");
                        }
                    }
                    else if (methodInvocation.resolveMethodBinding() != null) {
                        if (!isMethodNodeAdded) {
                            graph.addVertex(caller);
                            isMethodNodeAdded = true;
                        }
                        callee = methodInvocation.resolveMethodBinding().getDeclaringClass().getName()+"::"+methodInvocation.getName();
                        graph.addVertex(callee);
                        graph.addEdge(caller, callee);

                        setLink.add("\t\""+caller+"\"->\""+callee+"\"\n");
                    }
                    else {
                        if (!isMethodNodeAdded) {
                            graph.addVertex(caller);
                            isMethodNodeAdded = true;
                        }
                        callee = nodeClass.getName()+"::"+methodInvocation.getName();
                        graph.addVertex(callee);
                        graph.addEdge(caller, callee);

                        setLink.add("\t\""+caller+"\"->\""+callee+"\"\n");
                    }
                }
            }
            mapTheCallGraph.put(nodeClass, mapMethodDeclarationInvocation);
        }
    }

    public void buildGraphWithJGraphT() throws IOException {
        JGraphXAdapter<String, DefaultEdge> graphAdapter = new JGraphXAdapter<String, DefaultEdge>(graph);
        mxIGraphLayout layout = new mxCircleLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());

        BufferedImage image = mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, true, null);
        File imgFile = new File("graph_jgrapht.png");
        ImageIO.write(image, "PNG", imgFile);

        if (!imgFile.exists()) {
            System.err.println("Le fichier "+imgFile.getName()+" n'a pas pu être créé !");
        }
    }

    private void writeGraphInDotFile(String fileGraphPath) throws IOException {
        FileWriter fW = new FileWriter(fileGraphPath);
        fW.write("digraph G {\n");
        for (String link : setLink) {
            fW.write(link);
        }
        fW.write("}");
        fW.close();
    }

    private void convertDotToSVG(String fileGraphPath) throws IOException {
        Parser p = new Parser();
        MutableGraph g = p.read(new File(fileGraphPath));
        Renderer render = Graphviz.fromGraph(g).render(Format.SVG);
        render.toFile(new File("graph_graphviz.svg"));
    }

    public void buildGraphWithGraphViz() throws IOException {
        writeGraphInDotFile("graph.dot");
        convertDotToSVG("graph.dot");
    }

    public static void displayTheCAllGraph(Map<TypeDeclaration, Map<MethodDeclaration, Set<MethodInvocation>>> aCallGraph) {
        Set<Map.Entry<TypeDeclaration, Map<MethodDeclaration, Set<MethodInvocation>>>> set = aCallGraph.entrySet();
        String callee;
        for (Map.Entry<TypeDeclaration, Map<MethodDeclaration, Set<MethodInvocation>>> mapEntry1 : set) {
            System.out.println("(CLASS) : " + mapEntry1.getKey().getName());
            for (Map.Entry<MethodDeclaration, Set<MethodInvocation>> mapEntry2: mapEntry1.getValue().entrySet()) {
                System.out.println("\t(METHOD) : " + mapEntry2.getKey().getName());
                for (MethodInvocation methodInvocation : mapEntry2.getValue()) {
                    if (methodInvocation.getExpression() != null) {
                        if (methodInvocation.getExpression().resolveTypeBinding() != null) {
                            callee = methodInvocation.getExpression().resolveTypeBinding().getName()+"::"+methodInvocation.getName();
                            System.out.println("\t\t(CALL) : " + callee);
                        }
                    }
                    else if (methodInvocation.resolveMethodBinding() != null) {
                        callee = methodInvocation.resolveMethodBinding().getDeclaringClass().getName()+"::"+methodInvocation.getName();
                        System.out.println("\t\t(CALL) : " + callee);
                    }
                }
            }
        }
    }

//    COLLECT DATA PROJECT
    public void makeAnalysis(String pathProject) throws IOException, EmptyProjectException, NotFoundPathProjectException {
        MyParser parser = new MyParser(pathProject);
        ArrayList<File> javaFiles = parser.listJavaFilesForFolder();
        if (javaFiles.isEmpty()) {
            throw new EmptyProjectException("Le project "+pathProject+" ne contient aucun fichier source.");
        }
        for(File javaFile : javaFiles) {
            String content = FileUtils.readFileToString(javaFile);
            CompilationUnit cu = parser.parseSource(content.toCharArray());

            collectApplicationMethodsData(cu); 
            collectAppPackages(cu);
            collectAttributeData(cu);
            collectMethodParamsData(cu);
            collectNumberOfApplicationClasses(cu);
            collectNumberOfLineOfApplicationCode(cu);
            collectNumberOfRowByMethod(cu);

            collectGraphData(cu);
        }
    }
}
