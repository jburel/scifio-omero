/*
 * #%L
 * OME database I/O package for communicating with OME and OMERO servers.
 * %%
 * Copyright (C) 2005 - 2014 Open Microscopy Environment:
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import io.scif.Checker;
import io.scif.FormatException;
import io.scif.MetadataService;
import io.scif.SCIFIO;

import org.junit.Test;

/**
 * Tests {@link OMEROFormat}.
 * 
 * @author Curtis Rueden
 */
public class OMEROFormatTest {

	/** Tests {@link OMEROFormat#getFormatName()}. */
	@Test
	public void testGetFormatName() {
		final OMEROFormat omeroFormat = getFormat();
		assertEquals("OMERO", omeroFormat.getFormatName());
	}

	/** Tests the {@link OMEROFormat.Checker}. */
	@Test
	public void testChecker() throws FormatException {
		final OMEROFormat omeroFormat = getFormat();
		final Checker checker = omeroFormat.createChecker();
		assertFalse(checker.isFormat("asdf"));
		assertTrue(checker.isFormat("asdf.omero"));
		assertTrue(checker.isFormat("omero:asdf"));
		assertEquals("omero", omeroFormat.getSuffixes()[0]);
	}

	/** Tests {@link OMEROFormat#parseArguments}. */
	@Test
	public void testParseArguments() throws FormatException {
		final OMEROFormat omeroFormat = getFormat();
		final MetadataService metadataService =
			omeroFormat.context().service(MetadataService.class);

		final String name = "data";
		final String server = "my.host.name";
		final int port = 4321;
		final String user = "foo";
		final String password = "bar";
		final String sessionID = "nuts";
		final boolean encrypted = true;
		final int imageID = 12;
		final int pixelsID = 357;
		final String omeroString = "name=" + name + //
			"&server=" + server + //
			"&port=" + port + //
			"&user=" + user + //
			"&password=" + password + //
			"&sessionID=" + sessionID + //
			"&encrypted=" + encrypted + //
			"&imageID=" + imageID + //
			"&pixelsID=" + pixelsID;

		final OMEROFormat.Metadata meta =
			(OMEROFormat.Metadata) omeroFormat.createMetadata();
		OMEROFormat.parseArguments(metadataService, omeroString, meta);

		assertEquals(name, meta.getName());
		assertEquals(server, meta.getCredentials().getServer());
		assertEquals(port, meta.getCredentials().getPort());
		assertEquals(user, meta.getCredentials().getUser());
		assertEquals(password, meta.getCredentials().getPassword());
		assertEquals(encrypted, meta.getCredentials().isEncrypted());
		assertEquals(imageID, meta.getImageID());
		assertEquals(pixelsID, meta.getPixelsID());
	}

	/**
	 * Tests {@link OMEROFormat#parseArguments} and creates a new client
	 * and session. 
	 */
    @Test
    public void testOMEROSession() throws FormatException {
        final OMEROFormat omeroFormat = getFormat();
        final MetadataService metadataService =
            omeroFormat.context().service(MetadataService.class);
        final String name = "data";
        final String server = "localhost";
        final int port = 4064;
        final String user = "root";
        final String password = "omero";
        
        final String omeroString = "name=" + name + //
                "&server=" + server + //
                "&port=" + port + //
                "&user=" + user + //
                "&password=" + password + ".omero";

        final OMEROFormat.Metadata meta =
            (OMEROFormat.Metadata) omeroFormat.createMetadata();
        OMEROFormat.parseArguments(metadataService, omeroString, meta);

        assertEquals(name, meta.getName());
        assertEquals(server, meta.getCredentials().getServer());
        assertEquals(port, meta.getCredentials().getPort());
        omero.client c = null;
        try {
            OMEROSession session = new OMEROSession(meta.getCredentials());
            c = session.getClient();
            String sessionID = c.getSessionId();
            assertTrue(sessionID.length() > 0);
            session.close();
        } catch (Exception e) {
            throw new FormatException(e);
        } finally {
            if (c != null) c.__del__();
        }
    }
    
    /**
     * Tests {@link OMEROFormat#parseArguments} and create a session
     * passing an existing session ID.
     * */
    @Test
    public void testOMEROSessionFromSessionID() throws FormatException {
        final OMEROFormat omeroFormat = getFormat();
        final MetadataService metadataService =
            omeroFormat.context().service(MetadataService.class);
        final String name = null;
        omero.client client = null, c = null;
        try {
            client = new omero.client();
            client.createSession();
            final String omeroString = "name=" + name + //
                    "&server=" + client.getProperty("omero.host") + //
                    "&port=" + client.getProperty("omero.port") + //
                    "&sessionID=" + client.getSessionId() +".omero";

            final OMEROFormat.Metadata meta =
                    (OMEROFormat.Metadata) omeroFormat.createMetadata();
                OMEROFormat.parseArguments(metadataService, omeroString, meta);
                
            assertTrue(client.getProperty("omero.host").trim().length() > 0);
            OMEROSession session = new OMEROSession(meta.getCredentials());
            c = session.getClient();
            String sessionID = c.getSessionId();
            assertTrue(sessionID.length() > 0);
            session.close();
            client.closeSession();
        } catch (Exception e) {
            throw new FormatException(e);
        } finally {
            if (client != null) client.__del__();
            if (c != null) c.__del__();
        }
    }

    /**
     * Tests {@link OMEROFormat#parseArguments} and create a session
     * passing an existing session ID.
     * */
    @Test
    public void testOMEROSessionFromSessionIDSpecifyClient() throws FormatException {
        final OMEROFormat omeroFormat = getFormat();
        final MetadataService metadataService =
            omeroFormat.context().service(MetadataService.class);
        final String name = null;
        omero.client client = null;
        try {
            client = new omero.client();
            client.createSession();
            final String omeroString = "name=" + name + //
                    "&server=" + client.getProperty("omero.host") + //
                    "&port=" + client.getProperty("omero.port") + //
                    "&sessionID=" + client.getSessionId() +".omero";

            final OMEROFormat.Metadata meta =
                    (OMEROFormat.Metadata) omeroFormat.createMetadata();
                OMEROFormat.parseArguments(metadataService, omeroString, meta);
                
            assertTrue(client.getProperty("omero.host").trim().length() > 0);
            OMEROSession session = new OMEROSession(meta.getCredentials(), client);
            String sessionID = session.getClient().getSessionId();
            assertTrue(sessionID.length() > 0);
            session.close();
            client.closeSession();
        } catch (Exception e) {
            throw new FormatException(e);
        } finally {
            if (client != null) client.__del__();
        }
    }

	// -- Helper methods --

	private OMEROFormat getFormat() {
		return new SCIFIO().format().getFormatFromClass(OMEROFormat.class);
	}
}
