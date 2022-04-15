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

package com.glavsoft.rfb.protocol.state;

import com.glavsoft.exceptions.TransportException;
import com.glavsoft.exceptions.UnsupportedSecurityTypeException;
import com.glavsoft.rfb.protocol.ProtocolContext;
import com.glavsoft.rfb.protocol.auth.AuthHandler;

import java.util.logging.Logger;

public class SecurityType33State extends SecurityType37State {

	public SecurityType33State(ProtocolContext context) {
		super(context);
	}

	@Override
	protected void negotiateAboutSecurityType()
			throws TransportException, UnsupportedSecurityTypeException {
        Logger.getLogger(getClass().getName()).info("Get Security Type");
		int type = reader.readInt32();
		Logger.getLogger(getClass().getName()).info("Type received: " + type);
		if (0 == type)
			// throw exception with reason
			throw new UnsupportedSecurityTypeException(reader.readString());
		AuthHandler typeSelected = selectAuthHandler(new byte[] {(byte) (0xff & type)},
					context.getSettings().authCapabilities);
		if (typeSelected != null) {
			setUseSecurityResult(typeSelected);
			Logger.getLogger(getClass().getName()).info("Type accepted: " + typeSelected.getName());
		} else
			throw new UnsupportedSecurityTypeException(
					"No security types supported. Server sent '" +
					type + "' security type, but we do not support it.");
		changeStateTo(new AuthenticationState(context, typeSelected));
	}

}
