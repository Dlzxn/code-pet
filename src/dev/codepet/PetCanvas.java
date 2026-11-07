package dev.codepet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/** Character selection stage; interactions are cosmetic and never change progress. */
final class PetCanvas extends JPanel {
    private final java.util.function.Supplier<PetModel> model;
    private PetBehavior behavior=new PetBehavior();
    private final PetDialogue dialogue=new PetDialogue();
    private String shownId="";
    private int previousFiles,previousXp;
    PetCanvas(){this(PetState::model);}
    PetCanvas(java.util.function.Supplier<PetModel> model){
        this.model=model;
        setOpaque(false);setPreferredSize(new Dimension(320,300));setMinimumSize(new Dimension(180,260));setFocusable(false);
        getAccessibleContext().setAccessibleName("Активный питомец");
        addMouseMotionListener(new MouseMotionAdapter(){@Override public void mouseMoved(MouseEvent e){behavior.hover(overPet(e.getPoint()));repaint();}});
        addMouseListener(new MouseAdapter(){@Override public void mouseExited(MouseEvent e){behavior.hover(false);repaint();}});
    }
    private boolean overPet(Point p){return new Rectangle(getWidth()/2-65,getHeight()-210,130,200).contains(p);}
    void react(String text){dialogue.say(text);repaint();}
    void play(){behavior.play();react("Поиграем!");}
    String actionLabel(){return behavior.action().label;}
    void tick(){
        PetModel pet=model.get();
        if(!shownId.equals(pet.id)){shownId=pet.id;behavior=new PetBehavior();previousXp=pet.xp;previousFiles=pet.filesCreated;}
        if(previousFiles!=pet.filesCreated){previousFiles=pet.filesCreated;behavior.eat();dialogue.say("Новый файл — перекус!");}
        if(previousXp!=pet.xp){int delta=pet.xp-previousXp;previousXp=pet.xp;if(delta>0)dialogue.say("+"+delta+" XP за код");}
        behavior.temperament(pet.temperament);behavior.update(.04,1,pet.animated);dialogue.update(.04,pet);repaint();
    }
    @Override protected void paintComponent(Graphics graphics){
        super.paintComponent(graphics);Graphics2D g=(Graphics2D)graphics.create();
        try{
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int width=getWidth(),height=getHeight();
            Color accent=PetCatalog.color(model.get().color);
            g.setPaint(new GradientPaint(0,0,new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),20),width,height,new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),65)));
            g.fillRoundRect(0,0,width,height,28,28);
            g.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),45));g.drawOval(width/2-90,height-39,180,20);
            PetModel pet=model.get();double scale=Math.min((height-70)/240.0,Math.min(width/260.0,pet.size/88.0));
            PetPainter.paint(g,width,height-12,pet,behavior.frame(),dialogue.text(pet),scale);
        }finally{g.dispose();}
    }
}
