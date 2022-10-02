package partie2.projectexplorer;

import java.io.File;
import java.util.ArrayList;

public class ProjectExplorer {
    private String projectPath;
    private File folder;

    public ProjectExplorer(String projectPath) {
        this.projectPath = projectPath;
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

    public String getProjectPath() {
        return projectPath;
    }
}
