package net.gegy1000.terrarium.server.world.rasterization;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

public class RasterCanvas {
    private final int width;
    private final int height;
    private final byte[] data;

    private final BufferedImage rasterImage;
    private final Graphics2D graphics;

    private final ValueColor color = new ValueColor();

    private int originX;
    private int originY;

    public RasterCanvas(int width, int height) {
        this.width = width;
        this.height = height;
        this.data = new byte[width * height];

        this.rasterImage = new BufferedImage(new ColorModel(), new Raster(width, height), false, new Hashtable<>());
        this.graphics = this.rasterImage.createGraphics();

        this.graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        this.graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        this.graphics.setColor(this.color);
    }

    public void setOrigin(int originX, int originY) {
        this.originX = originX;
        this.originY = originY;
    }

    public void setColor(int color) {
        this.color.set(color);
    }

    public void fill(Shape shape) {
        this.graphics.translate(-this.originX, -this.originY);
        this.graphics.fill(shape);
        this.graphics.translate(this.originX, this.originY);
    }

    public void draw(Shape shape) {
        this.graphics.translate(-this.originX, -this.originY);
        this.graphics.draw(shape);
        this.graphics.translate(this.originX, this.originY);
    }

    public void setData(int x, int y, int value) {
        this.data[x + y * this.width] = (byte) (value & 0xFF);
    }

    public int getData(int x, int y) {
        return this.data[x + y * this.width] & 0xFF;
    }

    private class Raster extends WritableRaster {
        public Raster(int width, int height) {
            super(new Model(width, height), new Buffer(width, height), new Point(0, 0));
        }
    }

    private class ColorModel extends DirectColorModel {
        ColorModel() {
            super(8, 0xFF, 0, 0, 0);
        }

        @Override
        public SampleModel createCompatibleSampleModel(int w, int h) {
            return new Model(w, h);
        }
    }

    private class Model extends SinglePixelPackedSampleModel {
        Model(int w, int h) {
            super(DataBuffer.TYPE_BYTE, w, h, new int[] { 0xFF, 0, 0 });
        }

        @Override
        public SampleModel createCompatibleSampleModel(int w, int h) {
            return new Model(w, h);
        }

        @Override
        public SampleModel createSubsetSampleModel(int[] bands) {
            return new Model(this.width, this.height);
        }

        @Override
        public DataBuffer createDataBuffer() {
            return new Buffer(this.width, this.height);
        }
    }

    private class Buffer extends DataBuffer {
        Buffer(int width, int height) {
            super(DataBuffer.TYPE_BYTE, width * height);
        }

        @Override
        public int getElem(int bank, int index) {
            return RasterCanvas.this.data[index] & 0xFF;
        }

        @Override
        public void setElem(int bank, int index, int value) {
            RasterCanvas.this.data[index] = (byte) (value & 0xFF);
        }
    }
}
