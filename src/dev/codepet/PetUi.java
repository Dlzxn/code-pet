package dev.codepet;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;

final class PetUi {
    static final Color BG=new Color(24,27,37),CARD=new Color(33,38,51),TEXT=new Color(232,235,247),MUTED=new Color(150,162,184),ACCENT=new Color(176,161,241),LINE=new Color(52,60,78);
    static JLabel label(String text,int size,Color color){JLabel l=new JLabel(text);l.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,size));l.setForeground(color);return l;}
    static JButton button(String text,boolean primary){
        JButton b=new JButton(text){@Override protected void paintComponent(Graphics graphics){Graphics2D g=(Graphics2D)graphics.create();try{g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);Color base=getBackground();g.setColor(getModel().isRollover()?base.brighter():base);g.fillRoundRect(0,0,getWidth(),getHeight(),12,12);if(isFocusOwner()){g.setColor(TEXT);g.drawRoundRect(2,2,getWidth()-5,getHeight()-5,10,10);}}finally{g.dispose();}super.paintComponent(graphics);}};b.setUI(new BasicButtonUI());b.setFont(new Font(Font.SANS_SERIF,Font.BOLD,12));
        b.setBackground(primary?ACCENT:new Color(46,54,72));b.setForeground(primary?BG:TEXT);b.setBorder(new EmptyBorder(9,14,9,14));b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(false);b.setContentAreaFilled(false);b.setFocusPainted(false);return b;
    }
    static JPanel vertical(){JPanel p=new JPanel();p.setOpaque(false);p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));return p;}
    static JPanel row(Component left,Component right){JPanel p=new JPanel(new BorderLayout(12,0));p.setOpaque(false);p.add(left,BorderLayout.WEST);p.add(right);p.setBorder(new EmptyBorder(7,0,7,0));return p;}
    static JPanel card(){return new RoundCard();}
    static void fit(JPanel body){for(Component c:body.getComponents())if(c instanceof JComponent j){j.setAlignmentX(Component.LEFT_ALIGNMENT);j.setMaximumSize(new Dimension(Integer.MAX_VALUE,j.getPreferredSize().height));}}
    private static final class RoundCard extends JPanel {
        RoundCard(){setOpaque(false);setLayout(new BorderLayout(0,12));setBorder(new EmptyBorder(18,18,18,18));}
        @Override public Dimension getMaximumSize(){return new Dimension(Integer.MAX_VALUE,getPreferredSize().height);}
        @Override protected void paintComponent(Graphics graphics){Graphics2D g=(Graphics2D)graphics.create();try{g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setColor(CARD);g.fillRoundRect(0,0,getWidth(),getHeight(),24,24);g.setColor(LINE);g.drawRoundRect(0,0,getWidth()-1,getHeight()-1,24,24);}finally{g.dispose();}super.paintComponent(graphics);}
    }
}
