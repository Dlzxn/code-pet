package dev.codepet;

import com.intellij.openapi.components.*;

@Service(Service.Level.APP)
@State(name="CodePet",storages=@Storage("code-pet.xml"))
public final class PetState implements PersistentStateComponent<PetCollection> {
    private PetCollection data=new PetCollection();
    public static PetCollection collection(){return com.intellij.openapi.application.ApplicationManager.getApplication().getService(PetState.class).data;}
    public static PetModel model(){return collection().active();}
    @Override public PetCollection getState(){data.active();return data.snapshot();}
    @Override public void loadState(PetCollection state){state.normalize();data=state;}
}
