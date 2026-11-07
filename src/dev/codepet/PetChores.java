package dev.codepet;

import java.awt.*;
import java.awt.geom.*;

/** Story props. Cleaning affects illustrated glyphs only, never the document. */
final class PetChores {
    static void paint(Graphics2D g,PetModel pet,PetBehavior.Frame f){
        var a=f.action();double t=f.phase(),p=f.progress();Color c=PetCatalog.color(pet.accessoryColor);
        g.setStroke(new BasicStroke(3,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        switch(a){
            case FETCH_BROOM,SWEEP,DUST_LETTERS -> {
                Graphics2D broom=(Graphics2D)g.create();broom.rotate(Math.sin(t*1.5)*.24,34,-26);broom.setColor(new Color(178,139,98));broom.drawLine(38,-128,38,-12);broom.setColor(new Color(224,193,126));Path2D head=new Path2D.Double();head.moveTo(30,-19);head.lineTo(48,-19);head.lineTo(61,7);head.lineTo(20,7);head.closePath();broom.fill(head);broom.setColor(new Color(164,132,88));for(int i=0;i<5;i++)broom.drawLine(31+i*4,-12,25+i*7,5);broom.dispose();
                g.setColor(new Color(207,199,173,(int)(120*(1-p))));for(int i=0;i<8;i++)g.fillOval((int)(53+i*5+Math.sin(t+i)*9),(int)(-2-Math.abs(Math.sin(t+i))*16),3,3);
                if(a==PetBehavior.Action.DUST_LETTERS)glyphs(g,f,c,false,p);
            }
            case FETCH_BUCKET,WASH_LETTERS,POLISH_LETTERS -> {
                g.setColor(c);g.fill(new RoundRectangle2D.Double(-66,-32,30,34,5,5));g.drawArc(-65,-47,28,31,0,180);
                g.setColor(new Color(182,222,235));g.fillOval(-63,-35,24,7);
                glyphs(g,f,c,true,p);g.setColor(new Color(241,237,219));g.fillRoundRect(31,(int)(-37+Math.sin(t*2)*13),23,16,6,6);
                g.setColor(new Color(220,240,250,180));for(int i=0;i<6;i++)g.drawOval(40+i*7,(int)(-70+i*6+Math.sin(t+i)*5),5+i%2*3,5+i%2*3);
            }
            case PUT_AWAY,RETURN_HOME -> {
                g.setColor(c.darker());g.fillRoundRect(28,-54,62,58,7,7);g.setColor(c.brighter());g.drawLine(30,-29,88,-29);g.fillRoundRect(48,-19,17,23,4,4);g.setColor(new Color(234,207,133));g.fillOval(43,-44,11,11);}
            case SEARCH_TOY -> {g.setColor(c);g.draw(new Ellipse2D.Double(38,-82,31,31));g.drawLine(62,-54,79,-35);g.setColor(new Color(225,222,240,80));g.fill(new Ellipse2D.Double(41,-79,25,25));}
            case FIND_TOY,SHOW_FIND,SHOW_GIFT -> {g.setColor(c);g.fillRoundRect(24,-47,46,45,6,6);g.setColor(c.brighter());g.fillRect(43,-48,8,46);g.fillRect(23,-31,48,7);g.setColor(new Color(243,215,135));for(int i=0;i<4;i++){double x=42+Math.cos(t+i*1.6)*28,y=-70+Math.sin(t+i*1.6)*20;g.drawLine((int)x-3,(int)y,(int)x+3,(int)y);g.drawLine((int)x,(int)y-3,(int)x,(int)y+3);} }
            case PACK_PICNIC -> {g.setColor(c);g.fillRoundRect(-53,-13,106,(int)(25*(1-p)+6),7,7);g.setColor(new Color(195,157,113));g.fillRoundRect(25,-35,35,35,5,5);g.drawArc(27,-53,30,30,0,180);}
            case REPAIR -> {g.setColor(c);g.fillRoundRect(27,-23,47,24,4,4);Graphics2D hammer=(Graphics2D)g.create();hammer.rotate(Math.sin(t*2)*.5,24,-52);hammer.setColor(new Color(173,140,103));hammer.drawLine(24,-38,24,-93);hammer.setColor(new Color(173,186,203));hammer.fillRoundRect(8,-105,35,16,4,4);hammer.dispose();}
            case CELEBRATE -> {g.setColor(c);for(int i=0;i<12;i++){double x=Math.sin(t+i*2)*78,y=-165+(p*120+i*17)%130;g.fillRect((int)x,(int)y,4,7);}}
            case THINK -> {g.setColor(c);g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,25));g.drawString("…",35,-136);}
            default -> {}
        }
    }
    private static void glyphs(Graphics2D g,PetBehavior.Frame f,Color c,boolean shine,double p){g.setFont(new Font(Font.MONOSPACED,Font.BOLD,25));g.setColor(c);g.drawString("{"+f.glyph()+"}",25,-56);if(shine){g.setColor(new Color(245,241,199));int x=(int)(25+p*42);g.drawLine(x,-89,x,-74);g.drawLine(x-7,-82,x+7,-82);}}
}
