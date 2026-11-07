package dev.codepet;

import java.awt.*;

/** Small geometry helpers for temporary visits. No text changes, focus requests or clicks. */
final class PetScenePlacement {
    static Rectangle fit(Rectangle area, Dimension sprite, Point desired, Rectangle avoid) {
        if (area.width < sprite.width + 16 || area.height < sprite.height + 16) return null;
        int x = Math.max(area.x + 8, Math.min(area.x + area.width - sprite.width - 8, desired.x));
        int y = Math.max(area.y + 8, Math.min(area.y + area.height - sprite.height - 8, desired.y));
        Rectangle result = new Rectangle(x, y, sprite.width, sprite.height);
        return avoid != null && result.intersects(avoid) ? null : result;
    }
    static Point interpolate(Point from, Point to, double progress) {
        double p = Math.max(0, Math.min(1, progress));
        // Smooth acceleration, then deceleration; no jump between distant points.
        p = p * p * (3 - 2 * p);
        return new Point((int)Math.round(from.x + (to.x - from.x) * p),
                         (int)Math.round(from.y + (to.y - from.y) * p));
    }
}
