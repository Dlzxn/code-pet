package dev.codepet;

import java.util.UUID;

/** Saved character. Autonomous actions and UI interactions never award experience. */
public final class PetModel implements Cloneable {
    public String id = UUID.randomUUID().toString();
    public String type = "cat";
    public String name = "Байт";
    public int xp;
    public long charactersWritten;
    public long linesWritten;
    public int characterRemainder;
    public int filesCreated;
    public int size = 88;
    public int roamingSize = 30;
    public String color = "Мятный";
    public String secondaryColor = "Снежный";
    public String eyeColor = "Графитовый";
    public String accessoryColor = "Лавандовый";
    public String pattern = PetCatalog.PATTERNS[0];
    public String head = PetCatalog.HEADS[0];
    public String face = PetCatalog.FACES[0];
    public String neck = PetCatalog.NECKS[0];
    public String back = PetCatalog.BACKS[0];
    public String eyes = PetCatalog.EYES[0];
    public String tail = PetCatalog.TAILS[0];
    public String temperament = PetCatalog.TEMPERAMENTS[0];
    public boolean paused;
    public boolean animated = true;
    public boolean roaming = true;
    public boolean speech = true;

    public PetModel copy(){try{return (PetModel)super.clone();}catch(CloneNotSupportedException e){throw new AssertionError(e);}}
    public int level() { return Math.max(1, (int) ((1 + Math.sqrt(1 + .08 * xp)) / 2)); }
    public long levelStartXp() { long l=level(); return 50*l*(l-1); }
    public int nextLevelCost() { return 100*level(); }
    public int levelProgress() { return (int)(xp-levelStartXp()); }
    public int earn(int newCharacters, int newLines) {
        if(paused) return 0;
        newCharacters=Math.max(0,newCharacters); newLines=Math.max(0,newLines);
        charactersWritten=Math.min(Long.MAX_VALUE-newCharacters,charactersWritten)+newCharacters;
        linesWritten=Math.min(Long.MAX_VALUE-newLines,linesWritten)+newLines;
        long chars=(long)characterRemainder+newCharacters;
        long reward=chars/100*5+(long)newLines*2;
        characterRemainder=(int)(chars%100);
        int old=xp; xp=(int)Math.min(Integer.MAX_VALUE,(long)xp+reward);
        return xp-old;
    }
    public void normalize() {
        if(id==null||id.isBlank()) id=UUID.randomUUID().toString();
        type=PetCatalog.species(type).id();
        name=name==null||name.isBlank()?"Байт":name.strip();
        if(name.length()>40) name=name.substring(0,40);
        xp=Math.max(0,xp); charactersWritten=Math.max(0,charactersWritten);linesWritten=Math.max(0,linesWritten);
        characterRemainder=Math.max(0,Math.min(99,characterRemainder));filesCreated=Math.max(0,filesCreated);
        size=Math.max(56,Math.min(112,size)); roamingSize=Math.max(22,Math.min(40,roamingSize));
        color=PetCatalog.valid(color,PetCatalog.COLORS);secondaryColor=PetCatalog.valid(secondaryColor,PetCatalog.COLORS);
        eyeColor=PetCatalog.valid(eyeColor,PetCatalog.COLORS);accessoryColor=PetCatalog.valid(accessoryColor,PetCatalog.COLORS);
        pattern=PetCatalog.valid(pattern,PetCatalog.PATTERNS);head=PetCatalog.valid(head,PetCatalog.HEADS);
        face=PetCatalog.valid(face,PetCatalog.FACES);neck=PetCatalog.valid(neck,PetCatalog.NECKS);back=PetCatalog.valid(back,PetCatalog.BACKS);
        eyes=PetCatalog.valid(eyes,PetCatalog.EYES);tail=PetCatalog.valid(tail,PetCatalog.TAILS);temperament=PetCatalog.valid(temperament,PetCatalog.TEMPERAMENTS);
    }
}
