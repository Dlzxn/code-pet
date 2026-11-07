package dev.codepet;

import java.awt.*;
import java.awt.geom.*;

/** Species props and special anatomy effects for the six individual stories of each species. */
final class PetSpeciesScenes {
    static int variant(PetBehavior.Frame f){try{return f.prop().startsWith("species-")?Integer.parseInt(f.prop().substring(8)):0;}catch(NumberFormatException e){return 0;}}
    static boolean active(PetBehavior.Frame f){return f.action()==PetBehavior.Action.SPECIES_TRICK;}
    static void transform(Graphics2D g,PetModel pet,PetBehavior.Frame f){
        if(!active(f))return;double t=f.phase(),p=f.progress();int v=variant(f);
        switch(pet.type){
            case "ghost" -> {g.translate(0,Math.sin(t*.6)*7);if(v==1){g.translate(Math.sin(p*Math.PI*2)*45,0);g.scale(.65+.35*Math.abs(Math.cos(t)),1);}}
            case "bat" -> {if(v==0||v==2)g.rotate(Math.PI,0,-75);else g.translate(Math.sin(t)*8,-Math.abs(Math.sin(t))*14);}
            case "slime" -> {double squash=Math.sin(t)*.2;g.scale(1+squash,1-squash);}
            case "turtle" -> {if(v==0||v==3){double reveal=.12+.88*Math.abs(Math.cos(p*Math.PI));g.scale(reveal,reveal);}}
            case "rabbit","frog","squirrel" -> g.translate(0,-Math.abs(Math.sin(t))*18);
            case "owl","dragon","chick" -> {g.translate(0,-Math.abs(Math.sin(t*.7))*10);g.rotate(Math.sin(t)*.06,0,-40);}
            case "penguin" -> g.rotate(Math.sin(t)*.2,0,0);
            case "hamster","hedgehog","panda" -> {if(v==1||v==3)g.rotate(Math.sin(p*Math.PI*2)*.6,0,-45);}
        }
    }
    static float opacity(PetModel pet,PetBehavior.Frame f){
        if(!active(f)||!pet.type.equals("ghost"))return 1;
        return switch(variant(f)){case 0->(float)Math.max(0,1-f.progress()*4);case 2->(float)(.15+.85*f.progress());case 1->.35f;default->.6f;};
    }
    static void paint(Graphics2D g,PetModel pet,PetBehavior.Frame f){
        if(!active(f))return;int v=variant(f);double t=f.phase(),p=f.progress();
        Color c=PetCatalog.color(pet.accessoryColor),fur=PetCatalog.color(pet.color);g.setStroke(new BasicStroke(3,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        if(pet.type.equals("ghost")){
            if(v==0||v==2){g.setColor(new Color(223,239,255));for(int side:new int[]{-1,1}){oval(g,side*19-5,-80+Math.sin(t*.6)*7,10,15);g.setColor(new Color(61,70,104));oval(g,side*19-1,-75+Math.sin(t*.6)*7,4,7);g.setColor(new Color(223,239,255));}}
            if(v==1){g.setColor(new Color(144,151,188,140));rect(g,25,-130,16,135);g.drawLine(24,-65,42,-65);}
            if(v==2){g.setColor(new Color(182,199,232,50));for(int i=0;i<7;i++)oval(g,-64+i*17,-30+Math.sin(t+i)*15,30,14);}
            if(v==3){g.setColor(c);rect(g,46,-108,27,34);g.drawArc(49,-122,20,26,0,180);g.setColor(new Color(247,226,143));oval(g,53,-99,13,18);}
            if(v==4){g.setColor(c);rect(g,43,-60+Math.sin(t)*20,25,24);g.drawArc(61,(int)(-58+Math.sin(t)*20),15,18,-90,180);}
            if(v==5){g.setColor(c);rect(g,32,-113,58,74);g.setColor(new Color(226,224,242));rect(g,38,-107,46,62);g.setColor(fur);oval(g,50,-96,23,33);}
            return;
        }
        double x=v==0?43:v==1?55+Math.sin(t)*13:v==2?Math.cos(t*.6)*65:v==3?-45:40;
        double y=v==0?-123:v==1?-27-Math.abs(Math.sin(t))*22:v==2?-85+Math.sin(t*.6)*37:v==3?-12:-48;
        g.setColor(c);
        switch(pet.type){
            case "cat" -> {oval(g,x-15,y-15,30,30);g.setColor(c.brighter());g.drawArc((int)x-11,(int)y-12,18,24,20,270);g.draw(new CubicCurve2D.Double(x,y+15,x+35,y+20,x+14,y+36,x+42,y+24));if(v==1){g.setColor(new Color(240,129,143));oval(g,75,-12,6,6);}}
            case "dog" -> {g.setColor(new Color(234,220,185));rect(g,x-18,y-5,36,10);for(int side:new int[]{-1,1}){oval(g,x+side*18-7,y-10,14,12);oval(g,x+side*18-7,y,14,12);}if(v==4){g.setColor(c);g.draw(new Ellipse2D.Double(45,-99,40,12));}}
            case "fox" -> {g.setColor(new Color(229,169,98));leaf(g,x,y,22);g.setColor(c);for(int i=0;i<3;i++)oval(g,-55+i*17,-3,7,4);}
            case "rabbit" -> {g.setColor(new Color(233,160,87));triangle(g,x-9,y-14,x+10,y-10,x-2,y+24);g.setColor(new Color(126,180,123));g.drawLine((int)x,(int)y-12,(int)x-8,(int)y-26);g.drawLine((int)x,(int)y-12,(int)x+7,(int)y-27);}
            case "panda" -> {g.setColor(new Color(135,183,116));g.drawLine((int)x,(int)y-37,(int)x,(int)y+21);g.drawLine((int)x-3,(int)y-8,(int)x+9,(int)y-8);leaf(g,x+11,y-20,14);}
            case "bear" -> {g.setColor(new Color(224,177,98));rect(g,x-17,y-13,34,31);g.setColor(new Color(245,211,117));oval(g,x-18,y-17,36,12);g.setColor(new Color(145,101,68));g.drawString("мёд",(int)x-12,(int)y+8);}
            case "koala" -> {g.setColor(new Color(124,172,137));g.drawLine((int)x-7,(int)y+23,(int)x+9,(int)y-27);leaf(g,x-7,y-7,17);leaf(g,x+12,y-22,15);}
            case "raccoon" -> {g.setColor(new Color(237,210,116));oval(g,x-14,y-14,28,28);g.setColor(Color.WHITE);g.drawLine((int)x+2,(int)y-12,(int)x+2,(int)y-2);}
            case "hamster" -> {g.setColor(new Color(225,195,136));for(int i=0;i<5;i++)oval(g,x-12+i*6,y+(i%2)*5,8,12);if(v==1){g.setColor(c);g.draw(new Ellipse2D.Double(-65,-128,130,130));}}
            case "mouse" -> {g.setColor(new Color(241,210,116));triangle(g,x-20,y+15,x+19,y+15,x+12,y-18);g.setColor(new Color(217,177,93));oval(g,x-5,y-3,7,7);oval(g,x+8,y+6,5,5);}
            case "squirrel" -> {g.setColor(new Color(178,136,93));oval(g,x-13,y-15,26,30);g.setColor(new Color(142,105,76));oval(g,x-17,y-19,34,12);}
            case "hedgehog" -> {g.setColor(new Color(224,126,123));oval(g,x-14,y-12,28,28);g.setColor(new Color(132,177,117));leaf(g,x+7,y-18,12);}
            case "owl" -> {g.setColor(new Color(239,230,205));rect(g,x-20,y-13,40,28);g.setColor(c);g.drawLine((int)x-19,(int)y-12,(int)x,(int)y+2);g.drawLine((int)x+19,(int)y-12,(int)x,(int)y+2);}
            case "penguin" -> {g.setColor(new Color(166,216,233,175));triangle(g,-70,5,83,5,52,-14);g.setColor(new Color(142,192,215));oval(g,x-20,y-7,34,14);triangle(g,x+10,y,x+26,y-10,x+26,y+10);}
            case "chick" -> {g.setColor(new Color(239,229,211));g.fill(new Arc2D.Double(x-24,y-10,48,35,180,180,Arc2D.CHORD));g.setColor(c);g.drawLine((int)x-20,(int)y+9,(int)x-7,(int)y+1);}
            case "dragon" -> {g.setColor(new Color(238,185,107));for(int i=0;i<4;i++)oval(g,x+i*8,y-7+Math.sin(t+i)*8,10-i,10-i);g.setColor(new Color(243,213,135));oval(g,x-12,y+11,22,10);}
            case "dino" -> {g.setColor(new Color(195,189,166));oval(g,x-24,y-8,48,17);g.setColor(c);g.drawString("{ }",(int)x-17,(int)y+3);}
            case "turtle" -> {g.setColor(fur.darker());oval(g,-43,-54,86,58);g.setColor(PetCatalog.color(pet.secondaryColor));g.draw(new Ellipse2D.Double(-32,-46,64,42));g.drawLine(0,-46,0,-4);g.drawLine(-32,-25,32,-25);}
            case "frog" -> {g.setColor(new Color(128,180,144));oval(g,x-24,y-5,48,13);g.setColor(new Color(239,176,195));g.setStroke(new BasicStroke(3));g.drawLine(6,-70,(int)x,(int)y);}
            case "axolotl" -> {g.setColor(new Color(171,219,231,160));for(int i=0;i<6;i++)g.draw(new Ellipse2D.Double(x+(i%2)*12,y-i*14+(t*10)%14,9+i%3*3,9+i%3*3));}
            case "bat" -> {g.setColor(c);for(int i=0;i<3;i++)g.draw(new Arc2D.Double(-60-i*12,-110-i*5,120+i*24,80+i*10,10,160,Arc2D.OPEN));}
            case "robot" -> {g.setColor(c);for(int i=0;i<8;i++){double a=i*Math.PI/4+t*.2;rect(g,x+Math.cos(a)*18-4,y+Math.sin(a)*18-4,8,8);}oval(g,x-17,y-17,34,34);g.setColor(new Color(70,83,107));oval(g,x-7,y-7,14,14);}
            case "slime" -> {g.setColor(new Color(226,246,245,130));for(int i=0;i<4;i++)oval(g,-25+i*15,-29-Math.sin(t+i)*17,8+i%2*4,8+i%2*4);}
        }
        if(v==3){g.setColor(new Color(180,143,102));rect(g,-66,-8,48,17);}
        if(v==5){g.setColor(c.darker());rect(g,40,-39,43,43);triangle(g,32,-39,62,-66,92,-39);g.setColor(c.brighter());rect(g,54,-20,14,24);}
    }
    private static void leaf(Graphics2D g,double x,double y,double r){g.fill(new Ellipse2D.Double(x-r*.3,y-r,r*.6,r*1.5));}
    private static void oval(Graphics2D g,double x,double y,double w,double h){g.fill(new Ellipse2D.Double(x,y,w,h));}
    private static void rect(Graphics2D g,double x,double y,double w,double h){g.fill(new RoundRectangle2D.Double(x,y,w,h,5,5));}
    private static void triangle(Graphics2D g,double...xy){Path2D p=new Path2D.Double();p.moveTo(xy[0],xy[1]);p.lineTo(xy[2],xy[3]);p.lineTo(xy[4],xy[5]);p.closePath();g.fill(p);}
}
