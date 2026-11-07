package dev.codepet;

import com.intellij.openapi.application.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import java.lang.reflect.*;
import java.util.*;
import javax.swing.SwingUtilities;

/** Service boundary checks with real SDK interfaces and controlled application/document adapters.
 * These do not replace testing an actual PyCharm editor/window. */
public final class SdkChecks {
    static int checks;
    static void check(boolean condition,String message){checks++;if(!condition)throw new AssertionError(message);}
    static Object fallback(Object proxy,Method method,Object[] args){
        if(method.getName().equals("hashCode"))return System.identityHashCode(proxy);
        if(method.getName().equals("equals"))return proxy==args[0];
        if(method.getName().equals("toString"))return "CodePetTestAdapter";
        if(method.getReturnType()==boolean.class)return false;
        if(method.getReturnType()==int.class)return 0;
        if(method.getReturnType()==long.class)return 0L;
        return null;
    }
    @SuppressWarnings("unchecked") static <T>T proxy(Class<T> type,InvocationHandler handler){return (T)Proxy.newProxyInstance(type.getClassLoader(),new Class[]{type},handler);}
    public static void main(String[] args)throws Exception{
        PetState state=new PetState();Map<Class<?>,Object> services=new HashMap<>();services.put(PetState.class,state);
        Application app=proxy(Application.class,(p,m,a)->{
            if(m.getName().equals("getService")||m.getName().equals("getServiceIfCreated"))return services.get(a[0]);
            if(m.getName().equals("isDispatchThread"))return SwingUtilities.isEventDispatchThread();
            if(m.getName().equals("isUnitTestMode")||m.getName().equals("isReadAccessAllowed")||m.getName().equals("isWriteAccessAllowed"))return true;
            if(m.getName().equals("invokeLater")){SwingUtilities.invokeLater((Runnable)a[0]);return null;}
            return fallback(p,m,a);
        });
        ApplicationManager.setApplication(app);
        try{
            SwingUtilities.invokeAndWait(()->{
                boolean[] disposed={false};Project project=proxy(Project.class,(p,m,a)->m.getName().equals("isDisposed")?disposed[0]:fallback(p,m,a));
                StringBuilder text=new StringBuilder("existing_source = 12345\n");
                Document doc=proxy(Document.class,(p,m,a)->switch(m.getName()){
                    case "getTextLength"->text.length();case "getImmutableCharSequence","getCharsSequence"->text.toString();case "getText"->text.toString();default->fallback(p,m,a);
                });
                PetCodingTracker tracker=new PetCodingTracker(project);String url="file:///test/code.py";PetModel first=PetState.model();
                tracker.before(doc,url);text.append("new_source = 67890\n");tracker.changed(doc);tracker.flush();
                check(first.xp==2,"Debounced tracker awards only newly qualifying source line");
                int xp=first.xp;tracker.before(doc,url);text.setLength(0);tracker.changed(doc);tracker.flush();
                tracker.before(doc,url);text.append("existing_source = 12345\nnew_source = 67890\n");tracker.changed(doc);tracker.flush();
                check(first.xp==xp,"Deleting/retyping cannot replay tracker rewards");
                tracker.before(doc,url);text.append("third_source = 1\n");tracker.changed(doc);tracker.discard(doc);tracker.flush();check(first.xp==xp,"Programmatic/disallowed changes cancel pending reward batch");
                tracker.before(doc,url);text.append("fourth_source = 2\n");PetModel second=PetState.collection().create("fox","Test fox");tracker.flush();
                check(first.xp==xp+2&&second.xp==0,"Pending edits credited to originating pet after selection changes");
                tracker.before(doc,url);text.append("fifth_source = 3\n");tracker.changed(doc);tracker.dispose();tracker.flush();check(second.xp==0,"Disposal cancels timer and pending reward");
                PetRoaming roaming=new PetRoaming(project);roaming.start();roaming.dispose();
                try{Field field=PetRoaming.class.getDeclaredField("timer");field.setAccessible(true);check(!((javax.swing.Timer)field.get(roaming)).isRunning(),"Roaming disposal stops the live timer");}catch(ReflectiveOperationException e){throw new AssertionError(e);}
                check(!PetEligibility.projectFile(null,null),"Invalid file/project filtered");
            });
            System.out.println("PASS: "+checks+" SDK adapter checks (tracker routing, exclusion, owner and lifecycle)");
        }finally{ApplicationManager.setApplication(null);}
    }
}
