package dev.codepet;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.function.Supplier;

public final class PetToolWindow implements ToolWindowFactory,DumbAware {
    @Override public void createToolWindowContent(Project project,ToolWindow window){
        PetPanel panel=new PetPanel(PetState::collection,()->window.hide(null));
        Content content=ContentFactory.getInstance().createContent(panel,"",false);content.setDisposer(panel);window.getContentManager().addContent(content);
    }
    static final class PetPanel extends JPanel implements Disposable {
        private final Supplier<PetCollection> collection;
        private final JLabel name=PetUi.label("",23,PetUi.TEXT),species=PetUi.label("",12,PetUi.MUTED),status=PetUi.label("",11,PetUi.MUTED),counter=PetUi.label("",12,PetUi.MUTED),level=PetUi.label("",12,PetUi.ACCENT);
        private final JLabel shortcuts=PetUi.label("",11,PetUi.MUTED);
        private final JButton activation=PetUi.button("Выпустить в редактор",true),quiet=PetUi.button("Скрыть всех на 30 мин",false);
        private final JProgressBar xp=new JProgressBar();private final PetCanvas canvas;private final Timer timer;
        private final JPanel creation=PetUi.card(),collectionStrip=new JPanel();private String stripSignature="";
        private final HashMap<String,JDialog> dialogs=new HashMap<>();private boolean disposed;
        PetPanel(Supplier<PetCollection> collection,Runnable hide){
            super(new BorderLayout());this.collection=collection;setBackground(PetUi.BG);canvas=new PetCanvas(this::model);
            JPanel body=PetUi.vertical();body.setOpaque(true);body.setBackground(PetUi.BG);body.setBorder(new EmptyBorder(16,16,16,16));
            JPanel header=new JPanel(new BorderLayout());header.setOpaque(false);JLabel title=PetUi.label("Мои питомцы",22,PetUi.TEXT);title.setFont(title.getFont().deriveFont(Font.BOLD));header.add(title);header.add(PetUi.label("● Автосохранение",10,new Color(133,202,172)),BorderLayout.EAST);body.add(header);body.add(Box.createVerticalStrut(8));body.add(counter);body.add(Box.createVerticalStrut(12));
            collectionStrip.setOpaque(false);collectionStrip.setLayout(new BoxLayout(collectionStrip,BoxLayout.X_AXIS));
            JScrollPane strip=new JScrollPane(collectionStrip,ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);strip.setBorder(BorderFactory.createEmptyBorder());strip.getViewport().setOpaque(false);strip.setOpaque(false);strip.setPreferredSize(new Dimension(280,48));body.add(strip);body.add(Box.createVerticalStrut(10));
            JPanel hero=PetUi.card();JPanel top=new JPanel(new BorderLayout());top.setOpaque(false);top.add(species);top.add(status,BorderLayout.EAST);hero.add(top,BorderLayout.NORTH);
            JPanel stage=new JPanel(new BorderLayout(4,0));stage.setOpaque(false);JButton previous=PetUi.button("‹",false),next=PetUi.button("›",false);previous.setToolTipText("Предыдущая карточка");next.setToolTipText("Следующая карточка");previous.getAccessibleContext().setAccessibleName("Предыдущий питомец");next.getAccessibleContext().setAccessibleName("Следующий питомец");previous.addActionListener(e->{data().select(-1);refresh();});next.addActionListener(e->{data().select(1);refresh();});stage.add(navigation(previous),BorderLayout.WEST);stage.add(canvas);stage.add(navigation(next),BorderLayout.EAST);hero.add(stage);
            JPanel summary=PetUi.vertical();name.setAlignmentX(CENTER_ALIGNMENT);name.setFont(name.getFont().deriveFont(Font.BOLD));summary.add(name);summary.add(Box.createVerticalStrut(8));level.setAlignmentX(CENTER_ALIGNMENT);summary.add(level);summary.add(Box.createVerticalStrut(7));
            xp.setStringPainted(true);xp.setForeground(PetUi.ACCENT);xp.setBackground(PetUi.LINE);xp.setBorderPainted(false);xp.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,11));summary.add(xp);summary.add(Box.createVerticalStrut(14));
            activation.setAlignmentX(CENTER_ALIGNMENT);activation.setMaximumSize(new Dimension(Integer.MAX_VALUE,42));activation.addActionListener(e->{if(data().enabled(model().id)&&data().quietUntilMillis>System.currentTimeMillis())data().quietUntilMillis=0;else{boolean show=!data().enabled(model().id);data().enable(model().id,show);if(show)data().quietUntilMillis=0;}refresh();});summary.add(activation);summary.add(Box.createVerticalStrut(8));
            JLabel hint=PetUi.label("<html><center>Можно выпустить несколько питомцев.<br>Стрелки только переключают карточку.</center></html>",10,PetUi.MUTED);hint.setAlignmentX(CENTER_ALIGNMENT);summary.add(hint);
            JPanel actions=new JPanel(new FlowLayout(FlowLayout.CENTER,7,10));actions.setOpaque(false);JButton edit=PetUi.button("Настроить питомца",false),stroke=PetUi.button("♡ Погладить",false);edit.addActionListener(e->openPreferences());stroke.addActionListener(e->canvas.react("Мне нравится!"));actions.add(edit);actions.add(stroke);summary.add(actions);hero.add(summary,BorderLayout.SOUTH);body.add(hero);body.add(Box.createVerticalStrut(12));
            JButton add=PetUi.button("＋ Добавить питомца",true);add.addActionListener(e->{creation.setVisible(!creation.isVisible());revalidate();});body.add(add);body.add(Box.createVerticalStrut(10));buildCreation();body.add(creation);creation.setVisible(false);
            JPanel help=PetUi.card();help.add(shortcuts);body.add(help);body.add(Box.createVerticalStrut(12));
            quiet.addActionListener(e->{data().quietUntilMillis=data().quietUntilMillis>System.currentTimeMillis()?0:System.currentTimeMillis()+30*60*1000;refresh();});body.add(quiet);body.add(Box.createVerticalStrut(8));JButton hideButton=PetUi.button("Скрыть панель",false);hideButton.addActionListener(e->hide.run());body.add(hideButton);
            for(Component c:body.getComponents())if(c instanceof JComponent j){j.setAlignmentX(Component.LEFT_ALIGNMENT);if(c!=creation)j.setMaximumSize(new Dimension(Integer.MAX_VALUE,j.getPreferredSize().height));}
            JScrollPane scroll=new JScrollPane(body);scroll.setBorder(BorderFactory.createEmptyBorder());scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);scroll.getViewport().setBackground(PetUi.BG);scroll.getVerticalScrollBar().setUnitIncrement(20);add(scroll);
            timer=new Timer(40,e->pulse());addHierarchyListener(e->{if((e.getChangeFlags()&HierarchyEvent.SHOWING_CHANGED)!=0){if(isShowing()&&!disposed)timer.start();else timer.stop();}});refresh();
        }
        private JPanel navigation(JButton button){JPanel p=new JPanel(new GridBagLayout());p.setOpaque(false);p.add(button);return p;}
        private PetCollection data(){return collection.get();}private PetModel model(){return data().active();}
        private void buildCreation(){
            JPanel fields=PetUi.vertical();fields.add(PetUi.label("НОВЫЙ ДРУГ",12,PetUi.ACCENT));
            JComboBox<PetCatalog.Species> type=new JComboBox<>(PetCatalog.SPECIES);fields.add(PetUi.row(PetUi.label("Вид",12,PetUi.MUTED),type));JTextField newName=new JTextField("Новый друг");fields.add(PetUi.row(PetUi.label("Имя",12,PetUi.MUTED),newName));
            JButton create=PetUi.button("Создать и выпустить",true);create.addActionListener(e->{PetCatalog.Species s=(PetCatalog.Species)type.getSelectedItem();if(s!=null){data().create(s.id(),newName.getText());data().quietUntilMillis=0;creation.setVisible(false);refresh();revalidate();}});fields.add(create);creation.add(fields);
        }
        PetPreferences preferencesContent(){return new PetPreferences(collection,model().id,()->{refresh();canvas.repaint();});}
        private void openPreferences(){
            String id=model().id;JDialog existing=dialogs.get(id);if(existing!=null){existing.toFront();existing.requestFocus();return;}
            PetPreferences preferences=preferencesContent();JDialog dialog=new JDialog(SwingUtilities.getWindowAncestor(this),"Настройки · "+model().name,Dialog.ModalityType.MODELESS);dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            JPanel content=new JPanel(new BorderLayout());content.setBackground(PetUi.BG);content.add(preferences);JButton done=PetUi.button("Готово",true);done.addActionListener(e->dialog.dispose());JPanel footer=new JPanel(new FlowLayout(FlowLayout.RIGHT));footer.setBackground(PetUi.BG);footer.add(done);content.add(footer,BorderLayout.SOUTH);dialog.setContentPane(content);
            Rectangle screen=getGraphicsConfiguration().getBounds();dialog.setSize(Math.min(480,screen.width-40),Math.min(700,screen.height-60));dialog.setLocationRelativeTo(this);dialog.getRootPane().registerKeyboardAction(e->dialog.dispose(),KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),JComponent.WHEN_IN_FOCUSED_WINDOW);
            dialog.addWindowListener(new WindowAdapter(){@Override public void windowClosed(WindowEvent e){preferences.dispose();dialogs.remove(id);}});dialogs.put(id,dialog);dialog.setVisible(true);
        }
        void pulse(){refresh();canvas.tick();}
        private void refresh(){
            PetCollection c=data();PetModel p=model();boolean enabled=c.enabled(p.id),hidden=c.quietUntilMillis>System.currentTimeMillis();
            java.util.Set<String> enabledIds=c.activePets().stream().map(m->m.id).collect(java.util.stream.Collectors.toSet());
            String signature=c.pets.stream().map(m->m.id+":"+m.name+":"+enabledIds.contains(m.id)).collect(java.util.stream.Collectors.joining("|"))+c.activeId+hidden;
            if(!signature.equals(stripSignature)){stripSignature=signature;collectionStrip.removeAll();for(PetModel member:c.pets){JButton chip=PetUi.button(member.name+(enabledIds.contains(member.id)?(hidden?" · включён":" · на экране"):""),member.id.equals(c.activeId));chip.setToolTipText("Открыть карточку: "+PetCatalog.species(member.type).name());chip.addActionListener(e->{c.activeId=member.id;refresh();});collectionStrip.add(chip);collectionStrip.add(Box.createHorizontalStrut(7));}collectionStrip.revalidate();collectionStrip.repaint();}
            name.setText(p.name);species.setText(PetCatalog.species(p.type).name());status.setText(enabled?(hidden?"Временно скрыт":"● На экране"):"В коллекции");status.setForeground(enabled&&!hidden?new Color(133,202,172):PetUi.MUTED);
            activation.setText(enabled?(hidden?"Вернуть на экран":"Убрать с экрана"):"Выпустить в редактор");activation.getAccessibleContext().setAccessibleDescription("Меняет присутствие только "+p.name+"; другие питомцы остаются на экране");
            shortcuts.setText(enabledIds.isEmpty()?"<html><b>На экране пока никого нет</b><br>Нажми «Выпустить в редактор», чтобы начать.</html>":c.keyboardGames?"<html><b>Игры во время кода</b><br>Enter — мяч · Tab — шарик<br>Удержание Delete — кресло с попкорном<br>Run — диван; наведи курсор, чтобы все разбежались</html>":"<html><b>Игры от клавиш выключены</b><br>Включить: Настроить питомца → Поведение.<br>Run — общий диван и пожелание удачи.</html>");
            counter.setText("На экране: "+(hidden?0:enabledIds.size())+" · В коллекции: "+c.pets.size());level.setText("УРОВЕНЬ "+p.level()+" · "+p.xp+" XP");xp.setMaximum(p.nextLevelCost());xp.setValue(p.levelProgress());xp.setString(p.levelProgress()+" / "+p.nextLevelCost()+" до уровня "+(p.level()+1));quiet.setText(hidden?"Вернуть питомцев":"Скрыть всех на 30 мин");
        }
        @Override public void dispose(){disposed=true;timer.stop();for(JDialog dialog:java.util.List.copyOf(dialogs.values()))dialog.dispose();dialogs.clear();}
    }
}
