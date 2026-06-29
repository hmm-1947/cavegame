package com.joshuastar.renderer;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public final class TextureLoader {

    private TextureLoader() {
    }

    public static BufferedImage load(String path) {

        try {

            BufferedImage image = ImageIO.read(
                    TextureLoader.class.getResourceAsStream(path));

            if (image == null) {
                throw new RuntimeException("Couldn't load texture: " + path);
            }

            return image;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedImage buildAtlas(String[] tilePaths, int tileSize, int atlasSize) {

        BufferedImage atlas = new BufferedImage(
                tileSize * atlasSize, tileSize * atlasSize, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = atlas.createGraphics();

        for (int i = 0; i < tilePaths.length; i++) {

            if (tilePaths[i] == null)
                continue;

            BufferedImage tile = load(tilePaths[i]);

            int x = (i % atlasSize) * tileSize;
            int y = (i / atlasSize) * tileSize;

            g.drawImage(tile, x, y, tileSize, tileSize, null);
        }

        g.dispose();

        return atlas;
    }

    public static byte[] loadRGBA(BufferedImage image) {

        int width = image.getWidth();
        int height = image.getHeight();

        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);

        byte[] out = new byte[width * height * 4];

        for (int i = 0; i < pixels.length; i++) {
            int p = pixels[i];

            out[i * 4] = (byte) ((p >> 16) & 0xFF);
            out[i * 4 + 1] = (byte) ((p >> 8) & 0xFF);
            out[i * 4 + 2] = (byte) (p & 0xFF);
            out[i * 4 + 3] = (byte) ((p >> 24) & 0xFF);
        }

        return out;
    }

    public static byte[] loadRGBA(String path) {

        BufferedImage image = load(path);

        int width = image.getWidth();
        int height = image.getHeight();

        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);

        byte[] out = new byte[width * height * 4];

        for (int i = 0; i < pixels.length; i++) {
            int p = pixels[i];

            out[i * 4] = (byte) ((p >> 16) & 0xFF);
            out[i * 4 + 1] = (byte) ((p >> 8) & 0xFF);
            out[i * 4 + 2] = (byte) (p & 0xFF);
            out[i * 4 + 3] = (byte) ((p >> 24) & 0xFF);
        }

        return out;
    }
}