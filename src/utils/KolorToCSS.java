package utils;

import javafx.scene.paint.Color;
/*
    klasa od metod statycznych zwiaazych z kolorami
 */
public class KolorToCSS {

    public static String toWebColor(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}
