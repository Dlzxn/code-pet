package dev.codepet;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.*;
import java.awt.*;
import java.util.function.Consumer;

/** The SDK AFTER_BACKGROUND stage paints before glyphs. No text attributes or document changes. */
final class PetEditorBackground implements AutoCloseable {
    private final Editor editor;private final RangeHighlighter highlighter;
    private Consumer<Graphics2D> painter=g->{};
    private Rectangle previous=new Rectangle();
    PetEditorBackground(Editor editor){
        this.editor=editor;
        highlighter=editor.getMarkupModel().addRangeHighlighter(0,editor.getDocument().getTextLength(),HighlighterLayer.FIRST,(TextAttributes)null,HighlighterTargetArea.EXACT_RANGE);
        highlighter.setGreedyToLeft(true);highlighter.setGreedyToRight(true);
        highlighter.setCustomRenderer(new CustomHighlighterRenderer(){
            @Override public CustomHighlighterOrder getOrder(){return CustomHighlighterOrder.AFTER_BACKGROUND;}
            @Override public void paint(Editor e,RangeHighlighter h,Graphics graphics){Graphics2D g=(Graphics2D)graphics.create();try{painter.accept(g);}finally{g.dispose();}}
        });
    }
    void update(Consumer<Graphics2D> painter,Rectangle dirty){this.painter=painter;Rectangle union=previous.isEmpty()?new Rectangle(dirty):previous.union(dirty);previous=new Rectangle(dirty);union.grow(3,3);if(!editor.isDisposed()){Rectangle visible=union.intersection(editor.getScrollingModel().getVisibleArea());if(!visible.isEmpty())editor.getContentComponent().repaint(visible);}}
    @Override public void close(){painter=g->{};if(highlighter.isValid())highlighter.dispose();if(!editor.isDisposed())editor.getContentComponent().repaint();}
}
