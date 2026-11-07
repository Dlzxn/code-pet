package dev.codepet;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.*;

final class PetEligibility {
    private static final Set<String> SOURCE=Set.of("py","pyi","java","kt","kts","js","jsx","ts","tsx","c","h","cpp","hpp","cc","cs","go","rs","rb","php","swift","m","mm","scala","sh","bash","zsh","lua","r","sql","html","css","scss","sass","less","vue","svelte","dart","ex","exs","erl","hs","clj","groovy","pl","jl","zig","toml","yaml","yml","json","xml");
    static boolean projectFile(Project project,VirtualFile file) {
        if(project==null||project.isDisposed()||file==null||!file.isValid()||file.isDirectory()||!file.isInLocalFileSystem())return false;
        if(ignored(file.getPath(),project.getBasePath()))return false;
        return ProjectFileIndex.getInstance(project).isInContent(file);
    }
    static boolean ignored(String filePath,String projectPath){
        String path=projectPath!=null&&filePath.startsWith(projectPath+"/")?"/"+filePath.substring(projectPath.length()+1):filePath;
        for(String excluded:new String[]{"/.git/","/.idea/","/node_modules/","/build/","/dist/","/__pycache__/","/.venv/","/venv/"})if(path.contains(excluded))return true;
        return false;
    }
    static boolean source(VirtualFile file){String ext=file.getExtension();return ext!=null&&SOURCE.contains(ext.toLowerCase(Locale.ROOT));}
}
