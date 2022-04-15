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

package com.glavsoft.rfb.encoding.decoder;

import com.glavsoft.drawing.Renderer;
import com.glavsoft.exceptions.TransportException;
import com.glavsoft.transport.Reader;

public class RREDecoder extends Decoder {

	@Override
	public void decode(Reader reader, Renderer renderer,
			FramebufferUpdateRectangle rect) throws TransportException {
		int numOfSubrectangles = reader.readInt32();
		int color = renderer.readPixelColor(reader);
		renderer.fillRect(color, rect);
		for (int i = 0; i < numOfSubrectangles; ++i) {
			color = renderer.readPixelColor(reader);
			int x = reader.readUInt16();
			int y = reader.readUInt16();
			int width = reader.readUInt16();
			int height = reader.readUInt16();
			renderer.fillRect(color, rect.x + x, rect.y + y, width, height);
		}

	}

}
