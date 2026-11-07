package dev.codepet;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/** One shared sofa scene, measured in window coordinates; it never receives input or focus. */
final class PetLounge {
    record Layout(Rectangle bounds,List<Rectangle> sofas,List<Rectangle> seats,Rectangle television,double unit){
        boolean hit(Point point){return point!=null&&seats.stream().anyMatch(r->new Rectangle((int)(r.getCenterX()-28*unit),(int)(r.getMaxY()-16-62*unit),Math.max(1,(int)(56*unit)),Math.max(1,(int)(64*unit))).contains(point));}
    }
    static Layout arrange(Rectangle available,int count){
        if(count<1||available.width<140||available.height<100)return null;
        int width=Math.min(500,Math.min(available.width,Math.max(220,count*54+120)));
        int columns=Math.min(count,Math.max(1,(width-120)/54)),rows=(count+columns-1)/columns;
        double u=Math.min(1,Math.min(width/220.0,available.height/(rows*78.0+106)));
        int height=Math.min(available.height,(int)Math.ceil((rows*78+106)*u)),x=available.x+available.width-width,y=available.y;
        Rectangle bounds=new Rectangle(x,y,width,height);ArrayList<Rectangle> sofas=new ArrayList<>(),seats=new ArrayList<>();
        for(int row=0;row<rows;row++){
            Rectangle sofa=new Rectangle(x+(int)(8*u),y+(int)((55+row*78)*u),width-(int)(106*u),(int)Math.ceil(70*u));sofas.add(sofa);
            int members=Math.min(columns,count-row*columns),seatWidth=sofa.width/members;
            for(int col=0;col<members;col++){int foot=sofa.y+(int)(49*u),h=(int)Math.ceil(100*u)+16;seats.add(new Rectangle(sofa.x+col*seatWidth,foot+16-h,seatWidth,h));}
        }
        Rectangle tv=new Rectangle(x+width-(int)(85*u),y+height-(int)(89*u),(int)Math.ceil(78*u),(int)Math.ceil(62*u));
        return new Layout(bounds,List.copyOf(sofas),List.copyOf(seats),tv,u);
    }
    static void back(Graphics2D g,Layout layout){
        if(layout==null)return;double u=layout.unit();
        for(Rectangle r:layout.sofas()){
            g.setColor(new Color(15,19,34,35));g.fill(new Ellipse2D.Double(r.x-4*u,r.y+r.height-9*u,r.width+8*u,17*u));
            g.setColor(new Color(43,48,75));g.fill(new RoundRectangle2D.Double(r.x,r.y,r.width,60*u,17*u,17*u));
            g.setPaint(new GradientPaint(r.x,r.y,new Color(113,105,166),r.x,r.y+(float)(55*u),new Color(66,66,107)));
            g.fill(new RoundRectangle2D.Double(r.x+7*u,r.y+5*u,r.width-14*u,42*u,12*u,12*u));
            g.setColor(new Color(164,151,205,100));g.setStroke(new BasicStroke((float)Math.max(.6,u)));
            for(int i=1;i<4;i++){double x=r.x+r.width*i/4.0;g.draw(new Line2D.Double(x,r.y+12*u,x,r.y+40*u));}
            g.setColor(new Color(137,123,179));g.fill(new RoundRectangle2D.Double(r.x+7*u,r.y+40*u,r.width-14*u,15*u,8*u,8*u));
        }
    }
    static void front(Graphics2D g,Layout layout,String speech,double phase){
        if(layout==null)return;double u=layout.unit();
        for(Rectangle r:layout.sofas()){
            g.setColor(new Color(70,66,106));g.fill(new RoundRectangle2D.Double(r.x,r.y+42*u,12*u,23*u,7*u,7*u));g.fill(new RoundRectangle2D.Double(r.getMaxX()-12*u,r.y+42*u,12*u,23*u,7*u,7*u));
            g.setColor(new Color(37,39,58));g.fill(new RoundRectangle2D.Double(r.x+10*u,r.y+56*u,r.width-20*u,9*u,5*u,5*u));
            g.setColor(new Color(166,138,113));g.fill(new Rectangle2D.Double(r.x+17*u,r.y+64*u,6*u,6*u));g.fill(new Rectangle2D.Double(r.getMaxX()-23*u,r.y+64*u,6*u,6*u));
        }
        Rectangle tv=layout.television();Graphics2D screen=(Graphics2D)g.create();try{
            screen.translate(tv.x,tv.y);screen.scale(u,u);screen.setColor(new Color(25,30,45));screen.fillRoundRect(0,0,78,49,8,8);
            screen.setPaint(new GradientPaint(0,0,new Color(44,62,78),65,46,new Color(28,36,57)));screen.fillRoundRect(5,5,68,37,4,4);
            screen.setStroke(new BasicStroke(2));for(int i=0;i<4;i++){screen.setColor(i==2?new Color(190,158,237):new Color(131,209,180));screen.drawLine(12+i%2*7,13+i*7,34+(int)(Math.sin(phase+i)*6)+i*5,13+i*7);}
            screen.setColor(new Color(151,160,179));screen.fillRect(35,48,7,7);screen.fillRoundRect(22,54,33,4,3,3);screen.setColor(new Color(143,224,180));screen.fillOval(67,44,3,3);
        }finally{screen.dispose();}
        if(speech.isBlank())return;
        Rectangle r=layout.bounds();g.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,11));FontMetrics fm=g.getFontMetrics();List<String> lines=new ArrayList<>();String line="";
        for(String word:speech.split(" ")){String next=line.isEmpty()?word:line+" "+word;if(fm.stringWidth(next)>r.width-30&&!line.isEmpty()){lines.add(line);line=word;}else line=next;}if(!line.isEmpty())lines.add(line);
        int h=Math.min(3,lines.size())*14+10;g.setColor(new Color(246,244,253,246));g.fillRoundRect(r.x+4,r.y,r.width-8,h,14,14);g.setColor(new Color(48,49,69));for(int i=0;i<Math.min(3,lines.size());i++)g.drawString(lines.get(i),r.x+14,r.y+17+i*14);
    }
}
