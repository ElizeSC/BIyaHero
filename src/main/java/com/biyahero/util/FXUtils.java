package com.biyahero.util;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.util.Duration;

public class FXUtils {

    // Fade a node in from 0 opacity
    public static void fadeIn(Node node, double durationMs) {
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    // Fade + slide up from below (card entrance effect)
    public static void slideUp(Node node, double durationMs) {
        node.setOpacity(0);
        node.setTranslateY(20);

        FadeTransition     ft = new FadeTransition(Duration.millis(durationMs), node);
        TranslateTransition tt = new TranslateTransition(Duration.millis(durationMs), node);

        ft.setFromValue(0); ft.setToValue(1);
        ft.setInterpolator(Interpolator.EASE_OUT);

        tt.setFromY(20); tt.setToY(0);
        tt.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(ft, tt).play();
    }

    // Stagger a list of nodes so they slide in one after another
    public static void staggerSlideUp(Iterable<Node> nodes, double baseDelayMs) {
        int[] i = {0};
        for (Node node : nodes) {
            double delay = i[0] * baseDelayMs;
            node.setOpacity(0);
            node.setTranslateY(20);

            FadeTransition      ft = new FadeTransition(Duration.millis(250), node);
            TranslateTransition tt = new TranslateTransition(Duration.millis(250), node);

            ft.setFromValue(0); ft.setToValue(1);
            ft.setInterpolator(Interpolator.EASE_OUT);
            tt.setFromY(20); tt.setToY(0);
            tt.setInterpolator(Interpolator.EASE_OUT);

            ParallelTransition pt = new ParallelTransition(ft, tt);
            pt.setDelay(Duration.millis(delay));
            pt.play();
            i[0]++;
        }
    }

    // Button press pop effect
    public static void buttonPop(Node node) {
        ScaleTransition shrink = new ScaleTransition(Duration.millis(80), node);
        shrink.setToX(0.95); shrink.setToY(0.95);
        ScaleTransition grow = new ScaleTransition(Duration.millis(80), node);
        grow.setToX(1.0); grow.setToY(1.0);
        SequentialTransition seq = new SequentialTransition(shrink, grow);
        seq.play();
    }

    // Pulse a label (e.g. when data updates)
    public static void pulse(Node node) {
        ScaleTransition out = new ScaleTransition(Duration.millis(120), node);
        out.setToX(1.06); out.setToY(1.06);
        ScaleTransition in = new ScaleTransition(Duration.millis(120), node);
        in.setToX(1.0); in.setToY(1.0);
        new SequentialTransition(out, in).play();
    }
}