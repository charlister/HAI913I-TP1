package partie2.parser;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class MyParser {
    private File folder;

    public MyParser(String projectPath) {
        this.folder = new File(projectPath);
    }

    public ArrayList<File> listJavaFilesForFolderBis(File folder) {
        ArrayList<File> javaFiles = new ArrayList<>();
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                javaFiles.addAll(listJavaFilesForFolderBis(fileEntry));
            } else if (fileEntry.getName().contains(".java")) {
                System.out.println(fileEntry.getName());
                javaFiles.add(fileEntry);
            }
        }
        return javaFiles;
    }

    public ArrayList<File> listJavaFilesForFolder() {
        return listJavaFilesForFolderBis(this.folder);
    }

    public CompilationUnit parseSource(char[] classSource) {
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        Map options = JavaCore.getOptions();
        parser.setBindingsRecovery(true);
        parser.setCompilerOptions(options);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        parser.setUnitName("");
        String[] sources = { "" };
        String[] classpath = { "" };
        parser.setEnvironment(classpath, sources, new String[] { "UTF-8"}, true);
        parser.setSource(classSource);
        return (CompilationUnit) parser.createAST(null);
    }
}
