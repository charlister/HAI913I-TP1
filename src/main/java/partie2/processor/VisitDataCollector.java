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
import java.util.List;
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
    private DefaultDirectedGraph<String, DefaultEdge> graphJGraphT;

    /**
     * Constructeur par défaut pour la classe {@link VisitDataCollector}
     */
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
        this.graphJGraphT = new DefaultDirectedGraph<>(DefaultEdge.class);
    }

    // 1 bis.
    /**
     * Compter le nombre de classes présentes dans le projet, y compris les classes imbriquées.
     * @param cu AST Node racine
     */
    private void collectNumberOfApplicationClasses(CompilationUnit cu) {
        TypeDeclarationVisitor visitorClass = new TypeDeclarationVisitor();
        cu.accept(visitorClass);
        classCounter += visitorClass.getTypeDeclarationList().size();
    }

    // 1.
    /**
     * accéder au nombre de classes du projet.
     * @return le nombre de classes du projet parsé.
     */
    public int getNumberOfApplicationClasses() {
        return classCounter;
    }

    // 2 bis.
    /**
     * Compter le nombre de lignes de code du projet.
     * @param cu AST Node racine
     */
    private void collectNumberOfLineOfApplicationCode(CompilationUnit cu) {
        GreaterVisitor visitorFileSource = new GreaterVisitor();
        cu.accept(visitorFileSource);
        lineOfCodeCounter += visitorFileSource.getNbLinesOfCode();
    }

    // 2.
    /**
     * Obtenir le nombre de lignes de code du projet.
     * @return le nombre de lignes de code du projet parsé.
     */
    public int getNumberOfLineOfApplicationCode() {
        return lineOfCodeCounter;
    }

    // 3 bis.
    /**
     * Compter le nombre de méthodes du projet et collecter le nombre de méthodes par classe.
     * @param cu AST Node racine
     */
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
    /**
     * obtenir le nombre de méthodes du projet.
     * @return le nombre de méthodes présentes dans le projet.
     */
    public int getNumberOfApplicationMethods() {
        return methodCounter;
    }

    // 4 bis.
    /**
     * Collecter les noms de packages extraits à partir du projet.
     * Ex : un fichier .java dans un package x.y.z1 permet de déduire les packages x, x.y et x.y.z1
     *      Un autre fichier .java du même projet dans un package x.y.z2 ne permettra de récupérer que le pacakge x.y.z2 car x et x.y ont déjà été extraits.
     * @param cu AST Node racine
     */
    private void collectAppPackages(CompilationUnit cu) {
        PackageDeclarationVisitor visitorPackage = new PackageDeclarationVisitor();
        cu.accept(visitorPackage);
        packageNames.addAll(visitorPackage.buildSubPackages());
    }

    // 4.
    /**
     * Obtenir le nombre total de packages du projet.
     * @return le nombre de packages du projet.
     */
    public int getTotalNumberOfAppPackages() {
        return packageNames.size();
    }

    // 5.
    /**
     * Obtenir le nombre moyen de méthodes par rapport au nombre de classes.
     * @return (nombre de méthodes)/(nombre de classes)
     */
    public int getAverageNumberOfMethodsDependingOnClasses() {
        return methodCounter/classCounter;
    }

    // 6 bis.
    /**
     * Collecter le nombre de lignes par méthode.
     * @param cu AST Node racine
     */
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
    /**
     * Obtenir le nombre moyen de ligne de code des méthodes.
     * @return (somme du nombre de lignes de code des méthodes)/(nombre de méthodes)
     */
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
    /**
     * Compter le nombre d'attributs du projet et collecter le nombre d'attributs par classe.
     * @param cu AST Node racine
     */
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
    /**
     * Obtenir le nombre moyen d'attributs par classe
     * @return (le nombre total d'attributs)/(le nombre de classes)
     */
    public int getAverageNumberOfAttributesDependingOnClasses() {
        if (classCounter == 0) {
            return -1;
        }
        return attributeCounter/classCounter;
    }

    // 8, 9 bis.
    /**
     * Obtenir les 10% du nombre de classes du projet (on ne prend que la partie entière).
     * @return (nombre de classes)*0.1
     */
    private int get10PercentsOfTotalNumberOfClasses() {
        return (int) (((double) classCounter)*10/100);
    }

    // 8, 9 bis.
    /**
     * Trier une map de type <TypeDeclaration, Integer> dans un ordre décroissant et en extraire une sous-partie.
     * @param map map à trier
     * @param numberExpected nombre constituant le limit de la sous-partie.
     * @return une map contenant au maximum numberExpected couples ({@link TypeDeclaration} ; {@link Integer})
     */
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
    /**
     * obtenir les 10% de classes avec le plus grand nombre de méthodes.
     * @return les 10% de classes avec le plus grand nombre de méthodes.
     */
    public Map<TypeDeclaration, Integer> get10PercentsClassesWithMostMethods() {
        return sortMapClassIntegerAndLimit(mapClassNbMethods, get10PercentsOfTotalNumberOfClasses());
    }

    // 9.
    /**
     * obtenir les 10% de classes avec le plus grand nombre d'attributs.
     * @return les 10% de classes avec le plus grand nombre d'attributs.
     */
    public Map<TypeDeclaration, Integer> get10PercentsClassesWithMostAttributes() {
        return sortMapClassIntegerAndLimit(mapClassNbAttributes, get10PercentsOfTotalNumberOfClasses());
    }

    // 10.
    /**
     * obtenir les 10% de classes avec le plus grand nombre de méthodes et d'attributs.
     * @return les 10% de classes avec le plus grand nombre de méthodes et d'attributs.
     */
    public Map<TypeDeclaration, Integer> get10PercentsClassesWithMostAttributesAndMethods(Map<TypeDeclaration, Integer> map1, Map<TypeDeclaration, Integer> map2) {
        return map1
                .entrySet()
                .stream()
                .filter(x -> map2.containsKey(x.getKey()))
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
    }

    // 11.
    /**
     * Obtenir les classes qui ont plus de x méthodes.
     * @param x (+1), le nombre minimal de méthodes dont doit disposer une classe pour figurer dans la map.
     * @return les classes qui ont plus de x méthodes.
     */
    public Map<TypeDeclaration, Integer> getClassesWithNbMethodsMoreThanX(int x) {
        return mapClassNbMethods
                .entrySet()
                .stream()
                .filter(e -> e.getValue()>x)
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
    }

    // 12 bis.
    /**
     * trier une map de type <MethodDeclaration, Integer> dans un ordre décroissant.
     * @param map map à trier
     * @param numberExpected nombre limite de couple <MethodDeclaration, Integer> à extraire de la map.
     * @return une map trié dans un ordre décroissant avec numberExpected éléments au maximum.
     */
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
    /**
     * Obtenir les 10% du nombre de méthodes du projet (on ne prend que la partie entière).
     * @return (nombre de méthodes)*0.1
     */
    private int get10PercentsOfTotalNumberOfMethods() {
        return (int) (((double) methodCounter)*10/100);
    }

    // 12.

    /**
     * Les 10% de méthodes avec le plus de lignes de code.
     * @return une liste triée dans un ordre décroissant ne contenant que 10% de couples du type <MethodDeclaration, Integer>.
     */
    public Map<MethodDeclaration, Integer> get10PercentsMethodsWithMostLinesOfCode() {
        return sortMapMethodIntegerAndLimit(mapMethodNbLines, get10PercentsOfTotalNumberOfMethods());
    }

    // 13 bis.
    /**
     * collecter le nombre de paramètres par méthode
     * @param cu AST Node racine.
     */
    private void collectMethodParamsData(CompilationUnit cu) {
        MethodDeclarationVisitor visitorMethodParameters = new MethodDeclarationVisitor();
        cu.accept(visitorMethodParameters);
        for (MethodDeclaration nodeMethod :
                visitorMethodParameters.getMethodDeclarationList()) {
            mapMethodNbParams.put(nodeMethod, nodeMethod.parameters().size());
        }
    }

    // 13.
    /**
     * Obtenir la méthode qui possède le plus de paramètres et son nombre de paramètres.
     * @return une map avec un seul élément qui est la méthode qui possède le plus de paramètres.
     */
    public Map<MethodDeclaration, Integer> getMethodWithMostParams() {
        return sortMapMethodIntegerAndLimit(mapMethodNbParams, 1);
    }

    // 14.
    /**
     * Obtenir un objet permettant d'accéder à l'ensemble des informations nécessaires à former un graphe d'appel.
     * @return une map contenant un graphe d'appel.
     */
    public Map<TypeDeclaration, Map<MethodDeclaration, Set<MethodInvocation>>> getTheCallGraph() {
        return mapTheCallGraph;
    }

    /**
     * Afficher une map de type <TypeDeclaration, Integer>
     * @param map map à afficher
     */
    public static void displayMapClassInteger(Map<TypeDeclaration, Integer> map) {
        map
                .entrySet()
                .stream()
                .forEach(x -> System.out.println(
                        "( CLASS ) : " + x.getKey().getName() + "\t\t"  +
                                "( " + x.getValue() + " )")
                );
    }

    /**
     * Afficher une map de type <MethodDeclaration, Integer>
     * @param map map à afficher
     */
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

    /**
     * Collecter l'ensemble des informations nécessaire à constituer un graphe d'appel,
     * récupérer l'ensemble des liens dans un ensemble afin de générer un fichier .dot pour réaliser un affichage graphique avec GraphViz,
     * remplir un objet de type DefaultDirectedGraph<String, DefaultEdge> pour réaliser un affichage graphique avec JGraphT
     * @param cu
     */
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
                                graphJGraphT.addVertex(caller);
                                isMethodNodeAdded = true;
                            }
                            callee = methodInvocation.getExpression().resolveTypeBinding().getName()+"::"+methodInvocation.getName();
                            graphJGraphT.addVertex(callee);
                            graphJGraphT.addEdge(caller, callee);

                            setLink.add("\t\""+caller+"\"->\""+callee+"\"\n");
                        }
                    }
                    else if (methodInvocation.resolveMethodBinding() != null) {
                        if (!isMethodNodeAdded) {
                            graphJGraphT.addVertex(caller);
                            isMethodNodeAdded = true;
                        }
                        callee = methodInvocation.resolveMethodBinding().getDeclaringClass().getName()+"::"+methodInvocation.getName();
                        graphJGraphT.addVertex(callee);
                        graphJGraphT.addEdge(caller, callee);

                        setLink.add("\t\""+caller+"\"->\""+callee+"\"\n");
                    }
                    else {
                        if (!isMethodNodeAdded) {
                            graphJGraphT.addVertex(caller);
                            isMethodNodeAdded = true;
                        }
                        callee = nodeClass.getName()+"::"+methodInvocation.getName();
                        graphJGraphT.addVertex(callee);
                        graphJGraphT.addEdge(caller, callee);

                        setLink.add("\t\""+caller+"\"->\""+callee+"\"\n");
                    }
                }
            }
            mapTheCallGraph.put(nodeClass, mapMethodDeclarationInvocation);
        }
    }

    /**
     * Réaliser un affichage graphique avec JGraphT
     * @throws IOException
     */
    public void buildGraphWithJGraphT() throws IOException {
        JGraphXAdapter<String, DefaultEdge> graphAdapter = new JGraphXAdapter<String, DefaultEdge>(graphJGraphT);
        mxIGraphLayout layout = new mxCircleLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());

        BufferedImage image = mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, true, null);
        File imgFile = new File("graph_jgrapht.png");
        if (imgFile.exists())
            imgFile.delete();

        ImageIO.write(image, "PNG", imgFile);

        if (!imgFile.exists()) {
            System.err.println("Le fichier "+imgFile.getName()+" n'a pas pu être créé !");
        }
        else {
            System.out.println(imgFile.getAbsolutePath());
        }
    }

    /**
     * Générer un fichier .dot à partir des liens récupérés dans la méthode collectGraphData
     * @param fileGraphPath chemin vers un fichier .dot
     * @throws IOException
     */
    private void writeGraphInDotFile(String fileGraphPath) throws IOException {
        FileWriter fW = new FileWriter(fileGraphPath);
        fW.write("digraph G {\n");
        for (String link : setLink) {
            fW.write(link);
        }
        fW.write("}");
        fW.close();
    }

    /**
     * Convertir un fichier .dot au format .svg
     * @param fileGraphPath chemin vers le fichier .dot à convertir en svg.
     * @throws IOException
     */
    private void convertDotToSVG(String fileGraphPath) throws IOException {
        Parser p = new Parser();
        MutableGraph g = p.read(new File(fileGraphPath));
        Renderer render = Graphviz.fromGraph(g).render(Format.SVG);
        File imgFile = new File("graph_graphviz.svg");
        if (imgFile.exists())
            imgFile.delete();
        render.toFile(imgFile);
        if (imgFile.exists())
            System.out.println(imgFile.getAbsolutePath());
    }

    /**
     * Affichage graphique d'un graphe avec GraphViz.
     * @throws IOException
     */
    public void buildGraphWithGraphViz() throws IOException {
        writeGraphInDotFile("graph.dot");
        convertDotToSVG("graph.dot");
    }

    /**
     * Afficher en mode console le graphe d'appel.
     * @param aCallGraph
     */
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

    /**
     * collecter l'ensemble des données souhaitées pour le TP.
     * @param pathProject chemin vers le projet à analyser.
     * @throws IOException
     * @throws EmptyProjectException
     * @throws NotFoundPathProjectException
     */
    public void makeAnalysis(String pathProject) throws IOException, EmptyProjectException, NotFoundPathProjectException {
        MyParser parser = new MyParser(pathProject);
        List<File> javaFiles = parser.listJavaFilesForFolder();
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
