package dev.codepet;

final class PetReactions {
    static void signal(PetModel pet,String event,long now){if(pet.lastReactionNanos!=0&&now-pet.lastReactionNanos<15_000_000_000L)return;pet.lastReactionNanos=now;pet.ideEvent=event;pet.ideSignal++;}
    static PetBehavior.Action action(String event){return switch(event){case "tests-passed"->PetBehavior.Action.CELEBRATE;case "large-paste"->PetBehavior.Action.WATCH;default->PetBehavior.Action.THINK;};}
    static String text(String event){return switch(event){case "tests-passed"->"Тесты прошли!";case "large-paste"->"Ого, сколько нового";default->"Разберёмся, я рядом";};}
}
