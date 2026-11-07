package dev.codepet;

import java.util.Random;

/** Rare, local, non-interactive speech. No source code leaves the IDE. */
final class PetDialogue {
    private final Random random=new Random();
    private double untilNext=60,remaining;
    private String text="";
    void say(String value){text=value;remaining=4;}
    void update(double seconds,PetModel pet){
        remaining=Math.max(0,remaining-seconds);untilNext-=seconds;
        if(!pet.speech){remaining=0;return;}
        if(untilNext<=0){String[] lines={"Я рядом","Хороший ритм","Исследую пиксели","Тут уютно","Ловлю вдохновение","Проверяю гравитацию","Код растёт — и я тоже","Маленький перерыв для лап"};say(lines[random.nextInt(lines.length)]);untilNext=65+random.nextInt(70);}
    }
    String text(PetModel pet){return pet.speech&&remaining>0?text:"";}
}
