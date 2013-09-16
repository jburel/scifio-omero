/*
 * #%L
 * OME database I/O package for communicating with OME and OMERO servers.
 * %%
 * Copyright (C) 2005 - 2013 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

package io.scif.omero;

import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;
import io.scif.Format;

import java.io.Closeable;
import java.util.Arrays;
import java.util.List;

import omero.ServerError;
import omero.api.ServiceFactoryPrx;
import omero.model.Image;
import omero.model.Pixels;

import org.scijava.plugin.Plugin;

/**
 * Helper class for managing OMERO client sessions.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Format.class)
public class OMEROSession implements Closeable {

	// -- Fields --

	private OMEROFormat.Metadata meta;
	private omero.client client;
	private ServiceFactoryPrx session;

	// -- Constructors --

	public OMEROSession(final OMEROFormat.Metadata meta) throws ServerError,
		PermissionDeniedException, CannotCreateSessionException
	{
		this(meta, null);
	}

	public OMEROSession(final OMEROFormat.Metadata meta, final omero.client c)
		throws ServerError, PermissionDeniedException, CannotCreateSessionException
	{
		this.meta = meta;

		// initialize the client
		if (c == null) {
			if (meta.getServer() != null) {
				client = new omero.client(meta.getServer(), meta.getPort());
			}
			else client = new omero.client();
		}
		else client = c;

		// initialize the session (i.e., log in)
		final String sessionID = meta.getSessionID();
		if (sessionID != null) {
			session = client.createSession(sessionID, sessionID);
		}
		else if (meta.getUser() != null && meta.getPassword() != null) {
			session = client.createSession(meta.getUser(), meta.getPassword());
		}
		else {
			session = client.createSession();
		}

		if (!meta.isEncrypted()) {
			client = client.createClient(false);
			session = client.getSession();
		}

		session.detachOnDestroy();
	}

	// -- OMEROSession methods --

	public omero.client getClient() {
		return client;
	}
	
	public ServiceFactoryPrx getSession() {
		return session;
	}

	public long getPixelsID() throws ServerError {
		final long pixelsID = meta.getPixelsID();
		if (pixelsID != 0) return pixelsID;

		// obtain pixels ID from image ID
		final long imageID = meta.getImageID();
		if (imageID == 0) return 0;
		final List<Image> images =
				session.getContainerService().getImages("Image",
					Arrays.asList(imageID), null);
		if (images == null || images.isEmpty()) {
			throw new IllegalArgumentException("Invalid image ID: " + imageID);
		}
		return images.get(0).getPixels(0).getId().getValue();
	}

	public Pixels getPixels() throws ServerError {
		return session.getPixelsService().retrievePixDescription(getPixelsID());
	}

	// -- Closeable methods --

	@Override
	public void close() {
		if (client != null) client.__del__();
		client = null;
		session = null;
	}

}