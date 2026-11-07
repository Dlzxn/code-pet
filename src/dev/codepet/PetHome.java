package dev.codepet;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.function.Supplier;

/** Level-earned furnishings. No maintenance, currency or chores are required from the user. */
final class PetHome extends JPanel {
    private final Supplier<PetModel> model;
    PetHome(Supplier<PetModel> model){this.model=model;setOpaque(false);setPreferredSize(new Dimension(300,195));setMinimumSize(new Dimension(180,160));}
    @Override protected void paintComponent(Graphics graphics){super.paintComponent(graphics);paint((Graphics2D)graphics,getWidth(),getHeight(),model.get());}
    static void paint(Graphics2D original,int width,int height,PetModel pet){
        Graphics2D g=(Graphics2D)original.create();
        try{
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            Color accent=PetCatalog.color(pet.accessoryColor);int finds=pet.availableFinds();
            g.setPaint(new GradientPaint(0,0,new Color(37,44,63),width,height,new Color(55,54,79)));g.fillRoundRect(0,0,width,height,22,22);
            double sx=Math.min(width/340.0,height/210.0);g.translate(width/2.0-170*sx,height-18-175*sx);g.scale(sx,sx);
            g.setColor(new Color(122,131,157,75));g.fill(new Ellipse2D.Double(22,156,296,19));
            g.setColor(accent.darker());g.fillRoundRect(33,80,99,83,12,12);Path2D roof=new Path2D.Double();roof.moveTo(18,82);roof.lineTo(83,34);roof.lineTo(148,82);roof.closePath();g.fill(roof);
            g.setColor(new Color(32,39,56));g.fillRoundRect(63,112,39,51,18,18);g.setColor(accent.brighter());g.fillOval(71,77,23,23);
            g.setColor(new Color(112,130,155));g.fillRoundRect(164,89,142,8,5,5);g.fillRoundRect(171,95,6,51,4,4);g.fillRoundRect(293,95,6,51,4,4);
            g.setColor(new Color(176,145,116));g.fillRoundRect(181,139,88,22,10,10);
            for(int i=0;i<finds;i++){
                int x=174+i%4*31,y=i<4?63:119;g.setColor(i%2==0?accent:new Color(228,204,135));
                switch(i){case 0->g.fillRoundRect(x,y,24,11,5,5);case 1->{g.drawOval(x,y,10,10);g.drawOval(x+13,y,10,10);g.drawLine(x+10,y+4,x+13,y+4);}case 2->{Path2D cape=new Path2D.Double();cape.moveTo(x+5,y-10);cape.lineTo(x-3,y+16);cape.lineTo(x+28,y+16);cape.lineTo(x+20,y-10);cape.closePath();g.fill(cape);}case 3->{g.fillRoundRect(x,y-8,20,24,4,4);g.drawLine(x-3,y-8,x+10,y-19);g.drawLine(x+10,y-19,x+23,y-8);}case 4->{g.fillRoundRect(x,y,25,5,3,3);g.fillOval(x+5,y-11,13,12);}case 5->{g.rotate(-.4,x+12,y);g.fillRoundRect(x-2,y-8,30,12,5,5);g.rotate(.4,x+12,y);g.drawLine(x+13,y+3,x+5,y+24);g.drawLine(x+13,y+3,x+24,y+24);}}
            }
            g.setColor(new Color(233,234,246));g.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,11));g.drawString(finds==0?"Уют растёт с кодом":"На полке: "+finds+" / 6",159,33);
        }finally{g.dispose();}
    }
}
