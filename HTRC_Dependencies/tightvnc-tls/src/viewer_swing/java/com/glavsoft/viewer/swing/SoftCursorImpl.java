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

package com.glavsoft.viewer.swing;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;

import com.glavsoft.drawing.SoftCursor;

public class SoftCursorImpl extends SoftCursor {
	private Image cursorImage;

	public SoftCursorImpl(int hotX, int hotY, int width, int height) {
		super(hotX, hotY, width, height);
	}

	public Image getImage() {
		return cursorImage;
	}

	@Override
	protected void createNewCursorImage(int[] cursorPixels, int hotX, int hotY, int width,	int height) {
		cursorImage = Toolkit.getDefaultToolkit().createImage(
				new MemoryImageSource(width, height, cursorPixels, 0, width));

	}

}
