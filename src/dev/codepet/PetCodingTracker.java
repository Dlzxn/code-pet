package dev.codepet;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import javax.swing.Timer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** Debounced scans, bounded pending documents, and no stored source text. All access on EDT. */
@Service(Service.Level.PROJECT)
public final class PetCodingTracker implements Disposable {
    private record Pending(String key,String owner){}
    private final Project project;
    private final IdentityHashMap<Document,Pending> pending=new IdentityHashMap<>();
    private final Timer timer;
    private boolean disposed;
    public PetCodingTracker(Project project){this.project=project;timer=new Timer(650,e->flush());timer.setRepeats(false);}
    static String key(String url){
        try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(url.getBytes(StandardCharsets.UTF_8)));}
        catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}
    }
    public void before(Document document,VirtualFile file){before(document,file.getUrl());}
    void before(Document document,String url){
        if(disposed||document.getTextLength()>500_000)return;
        String owner=PetState.model().id;
        Pending prior=pending.get(document);
        if(prior!=null&&prior.owner.equals(owner))return;
        if(prior!=null){apply(document,prior);pending.remove(document);}
        if(pending.size()>=24)flush();
        PetMetrics.Counts counts=PetMetrics.count(document.getImmutableCharSequence());
        String key=key(url);
        PetCollection.FileProgress f=PetState.collection().file(key,counts.characters(),counts.lines());
        // Existing/reloaded text is a baseline, never a retroactive reward.
        f.characters=Math.max(f.characters,counts.characters());f.lines=Math.max(f.lines,counts.lines());
        pending.put(document,new Pending(key,owner));
    }
    public void changed(Document document){if(!disposed&&pending.containsKey(document))timer.restart();}
    public void discard(Document doc){
        Pending entry=pending.remove(doc);
        if(entry!=null&&doc.getTextLength()<=500_000){PetMetrics.Counts c=PetMetrics.count(doc.getImmutableCharSequence());PetCollection.FileProgress f=PetState.collection().file(entry.key,c.characters(),c.lines());f.characters=Math.max(f.characters,c.characters());f.lines=Math.max(f.lines,c.lines());}
        if(pending.isEmpty())timer.stop();
    }
    private void apply(Document doc,Pending entry){
        if(doc.getTextLength()>500_000)return;
        PetMetrics.Counts c=PetMetrics.count(doc.getImmutableCharSequence());
        PetState.collection().record(entry.key,c.characters(),c.lines(),entry.owner);
    }
    void flush(){
        timer.stop();
        if(!disposed&&!project.isDisposed())pending.forEach(this::apply);
        pending.clear();
    }
    @Override public void dispose(){disposed=true;timer.stop();pending.clear();}
}
