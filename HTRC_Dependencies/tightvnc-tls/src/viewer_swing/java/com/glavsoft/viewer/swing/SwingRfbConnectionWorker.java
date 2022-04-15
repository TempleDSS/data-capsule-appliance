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

import com.glavsoft.exceptions.*;
import com.glavsoft.rfb.IPasswordRetriever;
import com.glavsoft.rfb.IRfbSessionListener;
import com.glavsoft.rfb.protocol.Protocol;
import com.glavsoft.rfb.protocol.ProtocolSettings;
import com.glavsoft.transport.Reader;
import com.glavsoft.transport.Writer;
import com.glavsoft.utils.Strings;
import com.glavsoft.viewer.*;
import com.glavsoft.viewer.swing.gui.PasswordDialog;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

/**
* @author dime at tightvnc.com
*/
public class SwingRfbConnectionWorker extends SwingWorker<Void, String> implements RfbConnectionWorker, IRfbSessionListener {

    private String predefinedPassword;
    private ConnectionPresenter presenter;
    private JFrame parentWindow;
    private SwingViewerWindowFactory viewerWindowFactory;
    private Logger logger;
    private volatile boolean isStoppingProcess;
    private SwingViewerWindow viewerWindow;
    protected String connectionString;
    protected Protocol workingProtocol;
    protected Socket workingSocket;
    protected ProtocolSettings rfbSettings;
    protected UiSettings uiSettings;

    @Override
    public Void doInBackground() throws Exception {
        if (null == workingSocket) throw new ConnectionErrorException("Null socket");
        workingSocket.setTcpNoDelay(true); // disable Nagle algorithm
        Reader reader = new Reader(workingSocket.getInputStream());
        Writer writer = new Writer(workingSocket.getOutputStream());

        workingProtocol = new Protocol(reader, writer,
                new PasswordChooser(connectionString, parentWindow, this),
                rfbSettings);
        String message = "Handshaking with remote host";
        logger.info(message);
        publish(message);

        workingProtocol.handshake();
//      tryAgain = false;
        return null;
    }

    public SwingRfbConnectionWorker(String predefinedPassword, ConnectionPresenter presenter, JFrame parentWindow,
                                    SwingViewerWindowFactory viewerWindowFactory) {
        this.predefinedPassword = predefinedPassword;
        this.presenter = presenter;
        this.parentWindow = parentWindow;
        this.viewerWindowFactory = viewerWindowFactory;
        logger = Logger.getLogger(getClass().getName());
    }


    @Override
    protected void process(List<String> strings) { // EDT
        String message = strings.get(strings.size() - 1); // get last
        presenter.showMessage(message);
    }

    @Override
    protected void done() { // EDT
        try {
            get();
            presenter.showMessage("Handshake established");
            ClipboardControllerImpl clipboardController =
                    new ClipboardControllerImpl(workingProtocol, rfbSettings.getRemoteCharsetName());
            clipboardController.setEnabled(rfbSettings.isAllowClipboardTransfer());
            rfbSettings.addListener(clipboardController);
            viewerWindow = viewerWindowFactory.createViewerWindow(
                    workingProtocol, rfbSettings, uiSettings, connectionString, presenter);

            workingProtocol.startNormalHandling(this, viewerWindow.getSurface(), clipboardController);
            presenter.showMessage("Started");

            presenter.successfulRfbConnection();
        } catch (CancellationException e) {
            logger.info("Cancelled");
            presenter.showMessage("Cancelled");
            presenter.connectionCancelled();
        } catch (InterruptedException e) {
            logger.info("Interrupted");
            presenter.showMessage("Interrupted");
            presenter.connectionFailed();
        } catch (ExecutionException ee) {
            String errorTitle;
            String errorMessage;
            try {
                throw ee.getCause();
            } catch (UnsupportedProtocolVersionException e) {
                errorTitle = "Unsupported Protocol Version";
                errorMessage = e.getMessage();
                logger.severe(errorMessage);
            } catch (UnsupportedSecurityTypeException e) {
                errorTitle = "Unsupported Security Type";
                errorMessage = e.getMessage();
                logger.severe(errorMessage);
            } catch (AuthenticationFailedException e) {
                errorTitle = "Authentication Failed";
                errorMessage = e.getMessage();
                logger.severe(errorMessage);
                presenter.clearPredefinedPassword();
            } catch (TransportException e) {
//            if ( ! isAppletStopped) {
                errorTitle = "Connection Error";
                errorMessage = "Connection Error: " + e.getMessage();
                logger.severe(errorMessage);
//            }
            } catch (IOException e) {
                errorTitle = "Connection Error";
                errorMessage = "Connection Error: " + e.getMessage();
                logger.severe(errorMessage);
            } catch (FatalException e) {
                errorTitle = "Connection Error";
                errorMessage = "Connection Error: " + e.getMessage();
                logger.severe(errorMessage);
            } catch (Throwable e) {
                errorTitle = "Error";
                errorMessage = "Error: " + e.getMessage();
                logger.severe(errorMessage);
            }
            presenter.showReconnectDialog(errorTitle, errorMessage);
            presenter.clearMessage();
            presenter.connectionFailed();
        }
    }

    @Override
	public void rfbSessionStopped(final String reason) {
        if (workingProtocol != null) {
			workingProtocol.cleanUpSession();
		}
		if (isStoppingProcess) return;
		cleanUpUISessionAndConnection();
        logger.info("Rfb session stopped: " + reason);
        if (presenter.needReconnection()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    presenter.showReconnectDialog("Connection error", reason);
                    presenter.reconnect(predefinedPassword);
                }
            });
        }
	}

    @Override
    public boolean cancel() {
        boolean res = super.cancel(true);
        if (res && workingProtocol != null) {
            workingProtocol.cleanUpSession();
        }
        cleanUpUISessionAndConnection();
        return res;
    }

    private synchronized void cleanUpUISessionAndConnection() {
		isStoppingProcess = true;
		if (workingSocket != null && workingSocket.isConnected()) {
			try {
				workingSocket.close();
			} catch (IOException e) { /*nop*/ }
		}
		if (viewerWindow != null) {
            viewerWindow.close();
		}
		isStoppingProcess = false;
	}

    @Override
    public void setWorkingSocket(Socket workingSocket) {
        this.workingSocket = workingSocket;
    }

    @Override
    public void setRfbSettings(ProtocolSettings rfbSettings) {
        this.rfbSettings = rfbSettings;
    }

    @Override
    public void setUiSettings(UiSettings uiSettings) {
        this.uiSettings = uiSettings;
    }

    @Override
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    /**
     * Ask user for password if needed
     */
    private class PasswordChooser implements IPasswordRetriever {
        PasswordDialog passwordDialog;
        private String connectionString;
        private final JFrame owner;
        private final ConnectionWorker onCancel;

        private PasswordChooser(String connectionString, JFrame parentWindow, ConnectionWorker onCancel) {
            this.connectionString = connectionString;
            this.owner = parentWindow;
            this.onCancel = onCancel;
        }

        @Override
        public String getPassword() {
            return Strings.isTrimmedEmpty(predefinedPassword) ?
                    getPasswordFromGUI() :
                    predefinedPassword;
        }

        private String getPasswordFromGUI() {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        if (null == passwordDialog) {
                            passwordDialog = new PasswordDialog(owner, onCancel);
                        }
                        passwordDialog.setServerHostName(connectionString);
                        passwordDialog.toFront();
                        passwordDialog.setVisible(true);
                    }
                });
            } catch (InterruptedException e) {
                //nop
            } catch (InvocationTargetException e) {
                //nop
            }
            return passwordDialog.getPassword();
        }
    }
}
