package dev.codepet;

import java.util.*;

/** One persistent collection per IDE, one active character, and shared per-file high-water marks. */
public final class PetCollection {
    public ArrayList<PetModel> pets = new ArrayList<>();
    public String activeId = "";
    public ArrayList<FileProgress> files = new ArrayList<>();
    // Legacy 0.1 / 0.2 fields are read once on migration. No old progress is discarded.
    public int xp;
    public String name="Байт", color="Мятный";
    public int size=88;
    public boolean animated=true,roaming=true,paused;

    public static final class FileProgress {
        public String key="";
        public int characters;
        public int lines;
        public boolean creationSeen;
        public FileProgress() {}
    }
    public PetModel active() { normalizeIfEmpty(); return find(activeId); }
    public PetModel find(String id) { return pets.stream().filter(p->p.id.equals(id)).findFirst().orElse(pets.get(0)); }
    public PetModel create(String type, String name) {
        PetModel pet=new PetModel();pet.type=type;pet.name=name;pet.normalize();pets.add(pet);activeId=pet.id;return pet;
    }
    public void select(int offset) {
        PetModel current=active();int index=pets.indexOf(current);
        activeId=pets.get(Math.floorMod(index+offset,pets.size())).id;
    }
    public FileProgress file(String key,int baselineCharacters,int baselineLines) {
        for(FileProgress f:files) if(f.key.equals(key)) return f;
        FileProgress f=new FileProgress(); f.key=key;f.characters=Math.max(0,baselineCharacters);f.lines=Math.max(0,baselineLines);
        if(files.size()>=5000) files.remove(0);
        files.add(f);return f;
    }
    public int record(String key,int characters,int lines,String petId) {
        FileProgress f=file(key,characters,lines);
        int dc=Math.max(0,characters-f.characters),dl=Math.max(0,lines-f.lines);
        f.characters=Math.max(f.characters,characters);f.lines=Math.max(f.lines,lines);
        return find(petId).earn(dc,dl);
    }
    private void normalizeIfEmpty() {
        if(pets.isEmpty()) {
            PetModel pet=new PetModel();pet.xp=Math.max(0,xp);pet.name=name;pet.color=color;pet.size=size;
            pet.animated=animated;pet.roaming=roaming;pet.paused=paused;pet.normalize();pets.add(pet);activeId=pet.id;
            xp=0; name="Байт";color="Мятный";size=88;animated=true;roaming=true;paused=false;
        }
    }
    public PetCollection snapshot() {
        PetCollection copy=new PetCollection();copy.activeId=activeId;
        for(PetModel pet:pets.toArray(PetModel[]::new))if(pet!=null)copy.pets.add(pet.copy());
        for(FileProgress f:files.toArray(FileProgress[]::new))if(f!=null){FileProgress saved=new FileProgress();saved.key=f.key;saved.characters=f.characters;saved.lines=f.lines;saved.creationSeen=f.creationSeen;copy.files.add(saved);}
        return copy;
    }
    public void normalize() {
        if(pets==null)pets=new ArrayList<>();pets.removeIf(Objects::isNull);normalizeIfEmpty();
        Set<String> ids=new HashSet<>();
        for(PetModel pet:pets) {pet.normalize();if(!ids.add(pet.id)){pet.id=UUID.randomUUID().toString();ids.add(pet.id);}}
        activeId=find(activeId).id;
        if(files==null)files=new ArrayList<>();
        files.removeIf(f->f==null||f.key==null||f.key.isBlank());
        if(files.size()>5000) files=new ArrayList<>(files.subList(files.size()-5000,files.size()));
        for(FileProgress f:files){f.characters=Math.max(0,f.characters);f.lines=Math.max(0,f.lines);}
    }
}
