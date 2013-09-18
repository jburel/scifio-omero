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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
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

	/** Tests {@link OMEROFormat#getSuffixes()}. */
	@Test
	public void testGetSuffixes() {
		final OMEROFormat omeroFormat = getFormat();
		assertArrayEquals(new String[] { "omero" }, omeroFormat.getSuffixes());
	}

	// -- Helper methods --

	private OMEROFormat getFormat() {
		return new SCIFIO().format().getFormatFromClass(OMEROFormat.class);
	}
}