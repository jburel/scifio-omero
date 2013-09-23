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
import io.scif.AbstractFormat;
import io.scif.AbstractMetadata;
import io.scif.AbstractParser;
import io.scif.AbstractWriter;
import io.scif.ByteArrayPlane;
import io.scif.ByteArrayReader;
import io.scif.Field;
import io.scif.Format;
import io.scif.FormatException;
import io.scif.ImageMetadata;
import io.scif.MetadataService;
import io.scif.Plane;
import io.scif.SCIFIO;
import io.scif.io.RandomAccessInputStream;
import io.scif.util.FormatTools;

import java.io.IOException;
import java.util.Map;

import net.imglib2.meta.Axes;
import net.imglib2.meta.CalibratedAxis;
import net.imglib2.meta.DefaultCalibratedAxis;
import omero.RDouble;
import omero.RInt;
import omero.ServerError;
import omero.api.RawPixelsStorePrx;
import omero.model.Pixels;

import org.scijava.plugin.Plugin;

/**
 * A SCIFIO {@link Format} which provides read/write access to pixels on an
 * OMERO server.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Format.class)
public class OMEROFormat extends AbstractFormat {

	// -- Format methods --

	@Override
	public String getFormatName() {
		return "OMERO";
	}

	@Override
	public String[] getSuffixes() {
		return new String[] { "omero" };
	}

	// -- Nested classes --

	public static class Metadata extends AbstractMetadata {

		@Field
		private String name;

		@Field
		private String server;

		@Field
		private int port = 4064;

		@Field
		private String sessionID;

		@Field
		private String user;

		@Field
		private String password;

		@Field
		private boolean encrypted;

		@Field(label = "Image ID")
		private long imageID;

		@Field(label = "Pixels ID")
		private long pixelsID;

		@Field
		private int sizeX;

		@Field
		private int sizeY;

		@Field
		private int sizeZ;

		@Field
		private int sizeC;

		@Field
		private int sizeT;

		@Field
		private Double physSizeX;

		@Field
		private Double physSizeY;

		@Field
		private Double physSizeZ;

		@Field
		private Integer physSizeC;

		@Field
		private Double physSizeT;

		@Field
		private String pixelType;

		// -- io.scif.omero.OMEROFormat.Metadata methods --

		public String getName() {
			return name;
		}

		public String getServer() {
			return server;
		}

		public int getPort() {
			return port;
		}

		public String getSessionID() {
			return sessionID;
		}

		public String getUser() {
			return user;
		}

		public String getPassword() {
			return password;
		}

		public boolean isEncrypted() {
			return encrypted;
		}

		public long getImageID() {
			return imageID;
		}

		public long getPixelsID() {
			return pixelsID;
		}

		public int getSizeX() {
			return sizeX;
		}

		public int getSizeY() {
			return sizeY;
		}

		public int getSizeZ() {
			return sizeZ;
		}

		public int getSizeC() {
			return sizeC;
		}

		public int getSizeT() {
			return sizeT;
		}

		public Double getPhysicalSizeX() {
			return physSizeX;
		}

		public Double getPhysicalSizeY() {
			return physSizeY;
		}

		public Double getPhysicalSizeZ() {
			return physSizeZ;
		}

		public Integer getPhysicalSizeC() {
			return physSizeC;
		}

		public Double getPhysicalSizeT() {
			return physSizeT;
		}

		public String getPixelType() {
			return pixelType;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public void setServer(final String server) {
			this.server = server;
		}

		public void setPort(final int port) {
			this.port = port;
		}

		public void setSessionID(final String sessionID) {
			this.sessionID = sessionID;
		}

		public void setUser(final String user) {
			this.user = user;
		}

		public void setPassword(final String password) {
			this.password = password;
		}

		public void setEncrypted(final boolean encrypted) {
			this.encrypted = encrypted;
		}

		public void setImageID(final long imageID) {
			this.imageID = imageID;
		}

		public void setPixelsID(final long pixelsID) {
			this.pixelsID = pixelsID;
		}

		public void setSizeX(final int sizeX) {
			this.sizeX = sizeX;
		}

		public void setSizeY(final int sizeY) {
			this.sizeY = sizeY;
		}

		public void setSizeZ(final int sizeZ) {
			this.sizeZ = sizeZ;
		}

		public void setSizeC(final int sizeC) {
			this.sizeC = sizeC;
		}

		public void setSizeT(final int sizeT) {
			this.sizeT = sizeT;
		}

		public void setPhysicalSizeX(final double physSizeX) {
			this.physSizeX = physSizeX;
		}

		public void setPhysicalSizeY(final double physSizeY) {
			this.physSizeY = physSizeY;
		}

		public void setPhysicalSizeZ(final double physSizeZ) {
			this.physSizeZ = physSizeZ;
		}

		public void setPhysicalSizeC(final int physSizeC) {
			this.physSizeC = physSizeC;
		}

		public void setPhysicalSizeT(final double physSizeT) {
			this.physSizeT = physSizeT;
		}

		public void setPixelType(final String pixelType) {
			this.pixelType = pixelType;
		}

		// -- io.scif.Metadata methods --

		@Override
		public void populateImageMetadata() {
			// TODO: Consider whether this check is really the right approach.
			// It is present because otherwise, the uninitialized format-specific
			// metadata fields overwrite the values populated by the ImgSaver.
			if (getImageCount() > 0) return; // already populated

			// construct dimensional axes
			final CalibratedAxis xAxis = new DefaultCalibratedAxis(Axes.X);
			if (physSizeX != null) xAxis.setCalibration(physSizeX);
			final CalibratedAxis yAxis = new DefaultCalibratedAxis(Axes.Y);
			if (physSizeY != null) yAxis.setCalibration(physSizeY);
			final CalibratedAxis zAxis = new DefaultCalibratedAxis(Axes.Z);
			if (physSizeZ != null) zAxis.setCalibration(physSizeZ);
			final CalibratedAxis cAxis = new DefaultCalibratedAxis(Axes.CHANNEL);
			if (physSizeC != null) cAxis.setCalibration(physSizeC);
			final CalibratedAxis tAxis = new DefaultCalibratedAxis(Axes.TIME);
			if (physSizeT != null) tAxis.setCalibration(physSizeT);
			final CalibratedAxis[] axes = { xAxis, yAxis, zAxis, cAxis, tAxis };
			final int[] axisLengths = { sizeX, sizeY, sizeZ, sizeC, sizeT };

			// obtain pixel type
			final int pixType = FormatTools.pixelTypeFromString(pixelType);

			// populate SCIFIO ImageMetadata
			createImageMetadata(1);
			final ImageMetadata imageMeta = get(0);
			imageMeta.setAxes(axes, axisLengths);
			imageMeta.setPixelType(pixType);
			imageMeta.setMetadataComplete(true);
			imageMeta.setOrderCertain(true);

			// TEMP: Until SCIFIO issue #62 is resolved
			// https://github.com/scifio/scifio/issues/62
			imageMeta.setBitsPerPixel(FormatTools.getBitsPerPixel(pixType));

			// TEMP: Until SCIFIO issue #64 is resolved
			// https://github.com/scifio/scifio/issues/64
			imageMeta.setPlaneCount(sizeZ * sizeC * sizeT);
		}

	}

	public static class Parser extends AbstractParser<Metadata> {

		@Override
		public void typedParse(final RandomAccessInputStream stream,
			final Metadata meta) throws IOException, FormatException
		{
			// parse OMERO credentials from source string
			parseCredentials(scifio(), stream.getFileName(), meta);

			// initialize OMERO session
			final OMEROSession session;
			final Pixels pix;
			try {
				session = createSession(meta);
				pix = session.getPixelsInfo();
			}
			catch (final ServerError err) {
				throw communicationException(err);
			}

			// parse pixel sizes
			meta.setSizeX(pix.getSizeX().getValue());
			meta.setSizeY(pix.getSizeY().getValue());
			meta.setSizeZ(pix.getSizeZ().getValue());
			meta.setSizeC(pix.getSizeC().getValue());
			meta.setSizeT(pix.getSizeT().getValue());

			// parse physical pixel sizes
			final RDouble physSizeX = pix.getPhysicalSizeX();
			if (physSizeX != null) meta.setPhysicalSizeX(physSizeX.getValue());
			final RDouble physSizeY = pix.getPhysicalSizeY();
			if (physSizeY != null) meta.setPhysicalSizeY(physSizeY.getValue());
			final RDouble physSizeZ = pix.getPhysicalSizeZ();
			if (physSizeZ != null) meta.setPhysicalSizeZ(physSizeZ.getValue());
			final RInt physSizeC = pix.getWaveIncrement();
			if (physSizeC != null) meta.setPhysicalSizeC(physSizeC.getValue());
			final RDouble physSizeT = pix.getTimeIncrement();
			if (physSizeT != null) meta.setPhysicalSizeT(physSizeT.getValue());

			// parse pixel type
			meta.setPixelType(pix.getPixelsType().getValue().getValue());

			// terminate OMERO session
			session.close();
		}


	}

	public static class Reader extends ByteArrayReader<Metadata> {

		private OMEROSession session;
		private RawPixelsStorePrx store;

		@Override
		public ByteArrayPlane openPlane(final int imageIndex, final int planeIndex,
			final ByteArrayPlane plane, final int x, final int y, final int w,
			final int h) throws FormatException, IOException
		{
			// TODO: Consider whether to reuse OMERO session from the parsing step.
			if (session == null) initSession();

			final int[] zct = FormatTools.getZCTCoords(this, imageIndex, planeIndex);
			try {
				final byte[] tile = store.getTile(zct[0], zct[1], zct[2], x, y, w, h);
				plane.setData(tile);
			}
			catch (final ServerError err) {
				throw communicationException(err);
			}

			return plane;
		}

		@Override
		public void close() {
			if (session != null) session.close();
			session = null;
			store = null;
		}

		private void initSession() throws FormatException {
			try {
				session = createSession(getMetadata());
				store = session.openPixels();
			}
			catch (final ServerError err) {
				throw communicationException(err);
			}
		}

	}

	public static class Writer extends AbstractWriter<Metadata> {

		private OMEROSession session;
		private RawPixelsStorePrx store;

		@Override
		public void savePlane(final int imageIndex, final int planeIndex,
			final Plane plane, final int x, final int y, final int w, final int h)
			throws FormatException, IOException
		{
			// TODO: Consider whether to reuse OMERO session from somewhere else.
			if (session == null) initSession();

			final byte[] bytes = plane.getBytes();
			final int[] zct =
				FormatTools.getZCTCoords(getMetadata(), imageIndex, planeIndex);
			try {
				store.setPlane(bytes, zct[0], zct[1], zct[2]);
			}
			catch (final ServerError err) {
				throw new FormatException("Error writing to OMERO: imageIndex=" +
					imageIndex + ", planeIndex=" + planeIndex, err);
			}

			System.out.println(bytes.length);
		}

		@Override
		public void close() {
			if (store != null) {
				// save the data
				try {
					store.save();
					store.close();
				}
				catch (final ServerError err) {
					log().error("Error communicating with OMERO", err);
				}
			}
			store = null;
			if (session != null) session.close();
			session = null;
		}

		private void initSession() throws FormatException {
			try {
				final Metadata meta = getMetadata();

				// parse OMERO credentials from destination string
				// HACK: Get destination string from the metadata's dataset name.
				// This is set in the method: AbstractWriter#setDest(String, int).
				parseCredentials(scifio(), meta.getDatasetName(), meta);

				session = createSession(meta);
				store = session.createPixels();
			}
			catch (final ServerError err) {
				throw communicationException(err);
			}
		}

	}

	// -- Helper methods --

	private static void parseCredentials(final SCIFIO scifio,
		final String string, final Metadata meta)
	{
		// strip extension
		final String noExt = string.substring(0, string.lastIndexOf("."));

		// TODO: Use scifio.metadata() instead, once it has been released.
		final Map<String, String> map =
			scifio.get(MetadataService.class).parse(noExt, "\n");

		for (final String key : map.keySet()) {
			final String value = map.get(key);
			if (key.equals("name")) {
				meta.setName(value);
			}
			else if (key.equals("server")) {
				meta.setServer(value);
			}
			else if (key.equals("port")) {
				try {
					meta.setPort(Integer.parseInt(value));
				}
				catch (final NumberFormatException exc) {
					scifio.log().warn("Invalid port: " + value, exc);
				}
			}
			else if (key.equals("sessionID")) {
				meta.setSessionID(value);
			}
			else if (key.equals("user")) {
				meta.setUser(value);
			}
			else if (key.equals("password")) {
				meta.setPassword(value);
			}
			else if (key.equals("encrypted")) {
				meta.setEncrypted(Boolean.parseBoolean(value));
			}
			else if (key.equals("imageID")) {
				try {
					meta.setImageID(Long.parseLong(value));
				}
				catch (final NumberFormatException exc) {
					scifio.log().warn("Invalid image ID: " + value, exc);
				}
			}
			else if (key.equals("pixelsID")) {
				try {
					meta.setPixelsID(Integer.parseInt(value));
				}
				catch (final NumberFormatException exc) {
					scifio.log().warn("Invalid pixels ID: " + value, exc);
				}
			}
		}
	}

	private static OMEROSession createSession(final Metadata meta)
		throws FormatException
	{
		try {
			return new OMEROSession(meta);
		}
		catch (final ServerError err) {
			throw communicationException(err);
		}
		catch (final PermissionDeniedException exc) {
			throw connectionException(exc);
		}
		catch (final CannotCreateSessionException exc) {
			throw connectionException(exc);
		}
	}

	private static FormatException communicationException(final Throwable cause) {
		return new FormatException("Error communicating with OMERO", cause);
	}

	private static FormatException connectionException(final Throwable cause) {
		return new FormatException("Error connecting to OMERO", cause);
	}

}
