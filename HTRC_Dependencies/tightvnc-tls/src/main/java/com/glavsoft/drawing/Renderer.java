// Copyright (C) 2010, 2011, 2012, 2013 GlavSoft LLC.
// All rights reserved.
//
//-------------------------------------------------------------------------
// This file is part of the TightVNC software.  Please visit our Web site:
//
//                       http://www.tightvnc.com/
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//-------------------------------------------------------------------------
//

package com.glavsoft.drawing;

import com.glavsoft.exceptions.TransportException;
import com.glavsoft.rfb.encoding.PixelFormat;
import com.glavsoft.rfb.encoding.decoder.FramebufferUpdateRectangle;
import com.glavsoft.transport.Reader;

import java.util.Arrays;

/**
 * Render bitmap data
 *
 * @author dime @ tightvnc.com
 */
public abstract class Renderer {

    protected Reader reader;
    private final Object lock = new Object();

    public abstract void drawJpegImage(byte[] bytes, int offset,
                                       int jpegBufferLength, FramebufferUpdateRectangle rect);

    protected int width;
    protected int height;
    protected int[] pixels;
    protected SoftCursor cursor;
    public PixelFormat pixelFormat;
    protected ColorDecoder colorDecoder;

    protected void init(Reader reader, int width, int height, PixelFormat pixelFormat) {
        this.reader = reader;
        this.width = width;
        this.height = height;
        initPixelFormat(pixelFormat);
        pixels = new int[width * height];
        Arrays.fill(pixels, 0);
    }

    public void initPixelFormat(PixelFormat pixelFormat) {
        synchronized (lock) {
            this.pixelFormat = pixelFormat;
            colorDecoder = new ColorDecoder(pixelFormat);
        }
    }

    /**
     * Draw byte array bitmap data
     *
     * @param bytes  bitmap data
     * @param x      bitmap x position
     * @param y      bitmap y position
     * @param width  bitmap width
     * @param height bitmap height
     */
    public void drawBytes(byte[] bytes, int x, int y, int width, int height) {
        int i = 0;
        for (int ly = y; ly < y + height; ++ly) {
            int end = ly * this.width + x + width;
            for (int pixelsOffset = ly * this.width + x; pixelsOffset < end; ++pixelsOffset) {
                pixels[pixelsOffset] = getPixelColor(bytes, i);
                i += colorDecoder.bytesPerPixel;
            }
        }
    }

    /**
     * Draw byte array bitmap data (for ZRLE)
     */
    public int drawCompactBytes(byte[] bytes, int offset, int x, int y, int width, int height) {
        synchronized (lock) {
            int i = offset;
            for (int ly = y; ly < y + height; ++ly) {
                int end = ly * this.width + x + width;
                for (int pixelsOffset = ly * this.width + x; pixelsOffset < end; ++pixelsOffset) {
                    pixels[pixelsOffset] = getCompactPixelColor(bytes, i);
                    i += colorDecoder.bytesPerCPixel;
                }
            }
            return i - offset;
        }
    }

    /**
     * Draw int (colors) array bitmap data (for ZRLE)
     */
    public void drawColoredBitmap(int[] colors, int x, int y, int width, int height) {
        synchronized (lock) {
            int i = 0;
            for (int ly = y; ly < y + height; ++ly) {
                int end = ly * this.width + x + width;
                for (int pixelsOffset = ly * this.width + x; pixelsOffset < end; ++pixelsOffset) {
                    pixels[pixelsOffset] = colors[i++];
                }
            }
        }
    }

    /**
     * Draw byte array bitmap data (for Tight)
     */
    public int drawTightBytes(byte[] bytes, int offset, int x, int y, int width, int height) {
        synchronized (lock) {
            int i = offset;
            for (int ly = y; ly < y + height; ++ly) {
                int end = ly * this.width + x + width;
                for (int pixelsOffset = ly * this.width + x; pixelsOffset < end; ++pixelsOffset) {
                    pixels[pixelsOffset] = colorDecoder.getTightColor(bytes, i);
                    i += colorDecoder.bytesPerPixelTight;
                }
            }
            return i - offset;
        }
    }

    /**
     * Draw byte array bitmap data (from array with plain RGB color components. Assumed: rrrrrrrr gggggggg bbbbbbbb)
     */
    public void drawUncaliberedRGBLine(byte[] bytes, int x, int y, int width) {
        synchronized (lock) {
            int end = y * this.width + x + width;
            for (int i = 3, pixelsOffset = y * this.width + x; pixelsOffset < end; ++pixelsOffset) {
                pixels[pixelsOffset] =
//					(0xff & bytes[i++]) << 16 |
//					(0xff & bytes[i++]) << 8 |
//					0xff & bytes[i++];
                        (0xff & 255 * (colorDecoder.redMax & bytes[i++]) / colorDecoder.redMax) << 16 |
                                (0xff & 255 * (colorDecoder.greenMax & bytes[i++]) / colorDecoder.greenMax) << 8 |
                                0xff & 255 * (colorDecoder.blueMax & bytes[i++]) / colorDecoder.blueMax;
            }
        }
    }

    /**
     * Draw paletted byte array bitmap data
     *
     * @param buffer  bitmap data
     * @param rect    bitmap location and dimensions
     * @param palette colour palette
     * @param paletteSize number of colors in palette
     */
    public void drawBytesWithPalette(byte[] buffer, FramebufferUpdateRectangle rect,
                                     int[] palette, int paletteSize) {
        synchronized (lock) {
            // 2 colors
            if (2 == paletteSize) {
                int dx, dy, n;
                int i = rect.y * this.width + rect.x;
                int rowBytes = (rect.width + 7) / 8;
                byte b;

                for (dy = 0; dy < rect.height; dy++) {
                    for (dx = 0; dx < rect.width / 8; dx++) {
                        b = buffer[dy * rowBytes + dx];
                        for (n = 7; n >= 0; n--) {
                            pixels[i++] = palette[b >> n & 1];
                        }
                    }
                    for (n = 7; n >= 8 - rect.width % 8; n--) {
                        pixels[i++] = palette[buffer[dy * rowBytes + dx] >> n & 1];
                    }
                    i += this.width - rect.width;
                }
            } else {
                // 3..255 colors (assuming bytesPixel == 4).
                int i = 0;
                for (int ly = rect.y; ly < rect.y + rect.height; ++ly) {
                    for (int lx = rect.x; lx < rect.x + rect.width; ++lx) {
                        int pixelsOffset = ly * this.width + lx;
                        pixels[pixelsOffset] = palette[buffer[i++] & 0xFF];
                    }
                }
            }
        }
    }

    /**
     * Copy rectangle region from one position to another. Regions may be overlapped.
     *
     * @param srcX    source rectangle x position
     * @param srcY    source rectangle y position
     * @param dstRect destination rectangle
     */
    public void copyRect(int srcX, int srcY, FramebufferUpdateRectangle dstRect) {
        int startSrcY, endSrcY, dstY, deltaY;
        if (srcY > dstRect.y) {
            startSrcY = srcY;
            endSrcY = srcY + dstRect.height;
            dstY = dstRect.y;
            deltaY = +1;
        } else {
            startSrcY = srcY + dstRect.height - 1;
            endSrcY = srcY - 1;
            dstY = dstRect.y + dstRect.height - 1;
            deltaY = -1;
        }
        synchronized (lock) {
            for (int y = startSrcY; y != endSrcY; y += deltaY) {
                System.arraycopy(pixels, y * width + srcX,
                        pixels, dstY * width + dstRect.x, dstRect.width);
                dstY += deltaY;
            }
        }
    }

    /**
     * Fill rectangle region with specified colour
     *
     * @param color colour to fill with
     * @param rect  rectangle region positions and dimensions
     */
    public void fillRect(int color, FramebufferUpdateRectangle rect) {
        fillRect(color, rect.x, rect.y, rect.width, rect.height);
    }

    /**
     * Fill rectangle region with specified colour
     *
     * @param color  colour to fill with
     * @param x      rectangle x position
     * @param y      rectangle y position
     * @param width  rectangle width
     * @param height rectangle height
     */
    public void fillRect(int color, int x, int y, int width, int height) {
        synchronized (lock) {
            int sy = y * this.width + x;
            int ey = sy + height * this.width;
            for (int i = sy; i < ey; i += this.width) {
                Arrays.fill(pixels, i, i + width, color);
            }
        }
    }

    /**
     * Reads color bytes (PIXEL) from reader, returns int combined RGB
     * value consisting of the red component in bits 16-23, the green component
     * in bits 8-15, and the blue component in bits 0-7. May be used directly for
     * creation awt.Color object
     */
    public int readPixelColor(Reader reader) throws TransportException {
        return colorDecoder.readColor(reader);
    }

    public int readTightPixelColor(Reader reader) throws TransportException {
        return colorDecoder.readTightColor(reader);
    }

    public ColorDecoder getColorDecoder() {
        return colorDecoder;
    }

    public int getCompactPixelColor(byte[] bytes, int offset) {
        return colorDecoder.getCompactColor(bytes, offset);
    }

    public int getPixelColor(byte[] bytes, int offset) {
        return colorDecoder.getColor(bytes, offset);
    }

    public int getBytesPerPixel() {
        return colorDecoder.bytesPerPixel;
    }

    public int getBytesPerCPixel() {
        return colorDecoder.bytesPerCPixel;
    }

    public int getBytesPerPixelTight() {
        return colorDecoder.bytesPerPixelTight;
    }

    public void fillColorBitmapWithColor(int[] bitmapData, int decodedOffset, int rlength, int color) {
        while (rlength-- > 0) {
            bitmapData[decodedOffset++] = color;
        }
    }

    /**
     * Width of rendered image
     *
     * @return width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Height of rendered image
     *
     * @return height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Read and decode cursor image
     *
     * @param rect new cursor hot point position and cursor dimensions
     * @throws TransportException
     */
    public void createCursor(int[] cursorPixels, FramebufferUpdateRectangle rect)
            throws TransportException {
        synchronized (cursor.getLock()) {
            cursor.createCursor(cursorPixels, rect.x, rect.y, rect.width, rect.height);
        }
    }

    /**
     * Read and decode new cursor position
     *
     * @param rect cursor position
     */
    public void decodeCursorPosition(FramebufferUpdateRectangle rect) {
        synchronized (cursor.getLock()) {
            cursor.updatePosition(rect.x, rect.y);
        }
    }

    public Object getLock() {
        return lock;
    }
}