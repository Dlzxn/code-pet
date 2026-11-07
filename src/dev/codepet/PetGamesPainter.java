package dev.codepet;

import java.awt.*;
import java.awt.geom.*;

final class PetGamesPainter {
    static void before(Graphics2D g,PetModel pet,PetBehavior.Frame f){if(f.action()!=PetBehavior.Action.CINEMA)return;
        Color c=PetCatalog.color(pet.accessoryColor);g.setColor(c.darker());g.fillRoundRect(-67,-105,134,113,24,24);g.setColor(c);g.fillRoundRect(-52,-89,104,66,19,19);g.setColor(c.brighter());g.fillRoundRect(-58,-38,116,25,12,12);g.setColor(c.darker());g.fillRoundRect(-78,-61,24,60,12,12);g.fillRoundRect(54,-61,24,60,12,12);g.setColor(new Color(82,75,88));g.fillRect(-56,4,12,13);g.fillRect(44,4,12,13);
    }
    static void after(Graphics2D g,PetModel pet,PetBehavior.Frame f){
        if(f.action()==PetBehavior.Action.BALL_THINK){g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,22));g.setColor(PetCatalog.color(pet.accessoryColor));g.drawString("…?",28,-138);}
        if(f.action()!=PetBehavior.Action.CINEMA&&f.action()!=PetBehavior.Action.LOUNGE)return;
        g.setColor(new Color(225,133,139));Path2D box=new Path2D.Double();box.moveTo(15,-69);box.lineTo(54,-69);box.lineTo(48,-24);box.lineTo(21,-24);box.closePath();g.fill(box);g.setColor(new Color(248,235,205));for(int i=0;i<4;i++)g.fillRect(22+i*8,-65,4,35);
        g.setColor(new Color(249,226,170));for(int i=0;i<9;i++)g.fill(new Ellipse2D.Double(16+i%4*10,-76-i/4*6,12,12));double bite=Math.sin(f.phase()*1.5);g.fill(new Ellipse2D.Double(2+bite*8,-90-bite*8,10,10));
    }
}
