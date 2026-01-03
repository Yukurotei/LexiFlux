package it.yuruni;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import it.yuruni.graphics.animation.Easing;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public final class Utils {
    /**
     *
     * @param sourceTexture the original texture to be resized
     * @param newWidth the desired width of the resized texture
     * @param newHeight the desired height of the resized texture
     * @return a new Texture object resized to the specified dimensions
     */
    public static Texture resizeTo(Texture sourceTexture, int newWidth, int newHeight) {
        if (newWidth <= 0 || newHeight <= 0) throw new IllegalArgumentException("Dimensions must be positive");

        com.badlogic.gdx.graphics.TextureData sourceData = sourceTexture.getTextureData();
        Pixmap sourcePixmap;

        boolean isFile = sourceData instanceof com.badlogic.gdx.graphics.glutils.FileTextureData;
        if (isFile) sourcePixmap = new Pixmap(((com.badlogic.gdx.graphics.glutils.FileTextureData) sourceData).getFileHandle()); else {
            if (!sourceData.isPrepared()) sourceData.prepare();

            sourcePixmap = sourceData.consumePixmap();
        }

        Pixmap resizedPixmap = new Pixmap(newWidth, newHeight, sourcePixmap.getFormat());
        resizedPixmap.setFilter(Pixmap.Filter.NearestNeighbour); //Much faster scaling method than BiLinear

        resizedPixmap.drawPixmap(sourcePixmap,
                0, 0, sourcePixmap.getWidth(), sourcePixmap.getHeight(), // Source rectangle
                0, 0, newWidth, newHeight    // Destination rectangle
        );

        Texture resizedTexture = new Texture(resizedPixmap);

        //Dispose pixmaps
        sourcePixmap.dispose();
        resizedPixmap.dispose();

        return resizedTexture;
    }

    /**
     *
     * @param sourceTexture the original texture to be resized
     * @param percentage the percentage to scale the texture by (e.g., 50 for 50%)
     * @return a new Texture object resized by the specified percentage
     */
    public static Texture resizeTo(Texture sourceTexture, double percentage) {
        if (percentage <= 0) throw new IllegalArgumentException("Percentage must be positive.");

        float scale = (float) (percentage / 100.0);
        int newWidth = Math.round(sourceTexture.getWidth() * scale);
        int newHeight = Math.round(sourceTexture.getHeight() * scale);

        if (newWidth <= 0) newWidth = 1;
        if (newHeight <= 0) newHeight = 1;

        return resizeTo(sourceTexture, newWidth, newHeight);
    }

    /**
     * Rotates a texture by a given angle (multiples of 90 degrees).
     * For angles other than 0, 90, 180, 270, the original texture is returned.
     *
     * @param sourceTexture The original texture to rotate.
     * @param degrees The rotation angle in degrees (0, 90, 180, 270).
     * @return A new, rotated Texture.
     */
    public static Texture rotateTextureRightAngles(Texture sourceTexture, float degrees) {
        if (sourceTexture == null) throw new IllegalArgumentException("Source texture cannot be null.");

        int rotationAngle = ((int) degrees % 360 + 360) % 360;
        if (rotationAngle % 90 != 0) return sourceTexture;

        TextureData sourceData = sourceTexture.getTextureData();
        Pixmap sourcePixmap;

        if (!sourceData.isPrepared()) {
            sourceData.prepare();
        }
        sourcePixmap = sourceData.consumePixmap();


        int originalWidth = sourcePixmap.getWidth();
        int originalHeight = sourcePixmap.getHeight();
        int newWidth = originalWidth;
        int newHeight = originalHeight;

        // Determine new dimensions for 90/270 degree rotations
        if (rotationAngle == 90 || rotationAngle == 270) {
            newWidth = originalHeight;
            newHeight = originalWidth;
        }

        Pixmap rotatedPixmap = new Pixmap(newWidth, newHeight, sourcePixmap.getFormat());

        for (int y = 0; y < originalHeight; y++) {
            for (int x = 0; x < originalWidth; x++) {
                int color = sourcePixmap.getPixel(x, y);
                int rotatedX = 0;
                int rotatedY = switch (rotationAngle) {
                    case 0 -> {
                        rotatedX = x;
                        yield y;
                    }
                    case 90 -> {
                        rotatedX = y;
                        yield newHeight - 1 - x;
                    }
                    case 180 -> {
                        rotatedX = newWidth - 1 - x;
                        yield newHeight - 1 - y;
                    }
                    case 270 -> {
                        rotatedX = newWidth - 1 - y;
                        yield x;
                    }
                    default -> 0;
                };

                rotatedPixmap.drawPixel(rotatedX, rotatedY, color);
            }
        }

        Texture rotatedTexture = new Texture(rotatedPixmap);

        sourcePixmap.dispose();
        rotatedPixmap.dispose();

        return rotatedTexture;
    }


    /**
     * Wraps text by inserting newline characters after a specified number of words.
     *
     * @param text The input string to wrap.
     * @param wordsPerLine The approximate number of words before a line break.
     * @return The wrapped string.
     */
    public static String wrapText(String text, int wordsPerLine) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder wrappedText = new StringBuilder();
        String[] words = text.split(" ");
        int wordCount = 0;

        for (String word : words) {
            wrappedText.append(word).append(" ");
            wordCount++;
            if (wordCount >= wordsPerLine) {
                wrappedText.append("\n");
                wordCount = 0;
            }
        }
        return wrappedText.toString().trim();
    }

    public static float applyEasing(float t, Easing easing) {
        switch (easing) {
            case EASE_IN_QUAD:
                return t * t;
            case EASE_OUT_QUAD:
                return t * (2 - t);
            case EASE_IN_OUT_QUAD:
                return t < 0.5f ? 2 * t * t : -1 + (4 - 2 * t) * t;
            case EASE_IN_CUBIC:
                return t * t * t;
            case EASE_OUT_CUBIC:
                return 1 - (float) Math.pow(1 - t, 3);
            case EASE_IN_OUT_CUBIC:
                return t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
            case EASE_IN_SINE:
                return 1 - (float) Math.cos((t * Math.PI) / 2);
            case EASE_OUT_SINE:
                return (float) Math.sin((t * Math.PI) / 2);
            case EASE_IN_OUT_SINE:
                return -((float) Math.cos(Math.PI * t) - 1) / 2;
            case EASE_IN_EXPO:
                return t == 0 ? 0 : (float) Math.pow(2, 10 * t - 10);
            case EASE_OUT_EXPO:
                return t == 1 ? 1 : 1 - (float) Math.pow(2, -10 * t);
            case EASE_IN_OUT_EXPO:
                if (t == 0) return 0;
                if (t == 1) return 1;
                if (t < 0.5f) return (float) Math.pow(2, 20 * t - 10) / 2;
                return (2 - (float) Math.pow(2, -20 * t + 10)) / 2;
            case EASE_IN_QUART:
                return t * t * t * t;
            case EASE_OUT_QUART:
                return 1 - (float) Math.pow(1 - t, 4);
            case EASE_IN_OUT_QUART:
                return t < 0.5f ? 8 * t * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 4) / 2;
            case EASE_IN_QUINT:
                return t * t * t * t * t;
            case EASE_OUT_QUINT:
                return 1 - (float) Math.pow(1 - t, 5);
            case EASE_IN_OUT_QUINT:
                return t < 0.5f ? 16 * t * t * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 5) / 2;
            case EASE_IN_CIRC:
                return 1 - (float) Math.sqrt(1 - t * t);
            case EASE_OUT_CIRC:
                return (float) Math.sqrt(1 - (float) Math.pow(t - 1, 2));
            case EASE_IN_OUT_CIRC:
                return t < 0.5f ? (1 - (float) Math.sqrt(1 - (float) Math.pow(2 * t, 2))) / 2
                        : ((float) Math.sqrt(1 - (float) Math.pow(-2 * t + 2, 2)) + 1) / 2;
            case EASE_IN_BACK: {
                final float c1 = 1.70158f;
                final float c3 = c1 + 1f;
                return c3 * t * t * t - c1 * t * t;
            }
            case EASE_OUT_BACK: {
                final float c1 = 1.70158f;
                final float c3 = c1 + 1f;
                return 1 + c3 * (float) Math.pow(t - 1, 3) + c1 * (float) Math.pow(t - 1, 2);
            }
            case EASE_IN_OUT_BACK: {
                final float c1 = 1.70158f;
                final float c2 = c1 * 1.525f;
                return t < 0.5f
                        ? ((float) Math.pow(2 * t, 2) * ((c2 + 1) * 2 * t - c2)) / 2
                        : ((float) Math.pow(2 * t - 2, 2) * ((c2 + 1) * (2 * t - 2) + c2) + 2) / 2;
            }
            case EASE_IN_ELASTIC: {
                final float c4 = (2 * (float) Math.PI) / 3;
                if (t == 0) return 0;
                if (t == 1) return 1;
                return -(float) Math.pow(2, 10 * t - 10) * (float) Math.sin((t * 10 - 10.75) * c4);
            }
            case EASE_OUT_ELASTIC: {
                final float c4 = (2 * (float) Math.PI) / 3;
                if (t == 0) return 0;
                if (t == 1) return 1;
                return (float) Math.pow(2, -10 * t) * (float) Math.sin((t * 10 - 0.75) * c4) + 1;
            }
            case EASE_IN_OUT_ELASTIC: {
                final float c5 = (2 * (float) Math.PI) / 4.5f;
                if (t == 0) return 0;
                if (t == 1) return 1;
                if (t < 0.5f) {
                    return -((float) Math.pow(2, 20 * t - 10) * (float) Math.sin((20 * t - 11.125) * c5)) / 2;
                }
                return ((float) Math.pow(2, -20 * t + 10) * (float) Math.sin((20 * t - 11.125) * c5)) / 2 + 1;
            }
            case EASE_IN_BOUNCE:
                return 1 - easeOutBounce(1 - t);
            case EASE_OUT_BOUNCE:
                return easeOutBounce(t);
            case EASE_IN_OUT_BOUNCE:
                return t < 0.5f ? (1 - easeOutBounce(1 - 2 * t)) / 2 : (1 + easeOutBounce(2 * t - 1)) / 2;
            case EASE_OSCILLATE_1:
                return (1 - (float) Math.cos(t * 2 * Math.PI)) / 2;
            case EASE_OSCILLATE_3:
                return (1 - (float) Math.cos(t * 3 * 2 * Math.PI)) / 2;
            case EASE_OSCILLATE_5:
                return (1 - (float) Math.cos(t * 5 * 2 * Math.PI)) / 2;
            case EASE_OSCILLATE_INFINITE:
                return (1 - (float) Math.cos(t * 9999 * 2 * Math.PI)) / 2;
            case LINEAR:
            default:
                return t;
        }
    }

    private static float easeOutBounce(float t) {
        final float n1 = 7.5625f;
        final float d1 = 2.75f;

        if (t < 1f / d1) {
            return n1 * t * t;
        } else if (t < 2f / d1) {
            t -= 1.5f / d1;
            return n1 * t * t + 0.75f;
        } else if (t < 2.5f / d1) {
            t -= 2.25f / d1;
            return n1 * t * t + 0.9375f;
        } else {
            t -= 2.625f / d1;
            return n1 * t * t + 0.984375f;
        }
    }
}
