/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.utils;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility class with useful methods for MRI diffusion files.
 *
 * @author aferial
 */
public final class DiffusionUtil {

	private static final Logger LOG = LoggerFactory.getLogger(DiffusionUtil.class);
	
	/**
	 * Convert the B values to a string representation.
	 *
	 * @param bval
	 *            the bval
	 *
	 * @return the string
	 */
	public static String bvalToString(final double[] bval) {
		String result = "";
		if (bval != null) {
			for (int i = 0; i < bval.length; i++) {
				result += bval[i];
				if (i != bval.length - 1) {
					result += " ";
				}
			}
		}
		return result;
	}

	/**
	 * Convert the diffusion matrix to a string.
	 *
	 * @param matrix
	 *            the matrix
	 *
	 * @return the string
	 */
	public static String bvecToString(final double[][] matrix) {
		String result = "";
		if (matrix != null) {
			for (int i = 0; i < matrix.length; i++) {
				for (int j = 0; j < matrix[i].length; j++) {
					result += matrix[i][j] + " ";
				}
				result += "\n";
			}
		}
		return result;
	}

	/**
	 * Convert a '.prop' file to a '.bval' and a '.bvec' file into the given
	 * output folder.
	 *
	 * @param propFile
	 *            the '.prop' file to convert
	 * @param outputDirectory
	 *            the output directory for the '.bvec' and '.bval' files
	 *
	 * @return the list< file>
	 */
	public static List<File> propToBvecBval(final File propFile, final File outputDirectory) {
		final List<File> resultList = new ArrayList<File>();
		try {
			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			final DocumentBuilder db = dbf.newDocumentBuilder();
			final Document doc = db.parse(propFile);
			doc.getDocumentElement().normalize();

			String b0 = null;
			String[] bvecAsString = null;
			final NodeList bvecNodeLst = doc.getElementsByTagName("DiffusionGradientOrientation");
			if (bvecNodeLst.getLength() != 0) {
				final Node bvecNode = bvecNodeLst.item(0);
				final Node bvecValue = bvecNode.getFirstChild();
				if (bvecValue != null) {
					if (bvecValue.getNodeValue() != null) {
						String diffusionGradientOrientation = bvecValue.getNodeValue().trim();
						bvecAsString = diffusionGradientOrientation.split(" ");
					}
				}
			}
			final NodeList bvalNodeLst = doc.getElementsByTagName("b0");
			if (bvalNodeLst.getLength() != 0) {
				final Node bvalNode = bvalNodeLst.item(0);
				final Node bvalValue = bvalNode.getFirstChild();
				if (bvalValue != null) {
					if (bvalValue.getNodeValue() != null) {
						b0 = bvalValue.getNodeValue().trim();
					}
				}
			}
			if (b0 != null && bvecAsString != null && bvecAsString.length > 1) {
				// extract the bvec & the bval
				double[][] bvec = convertBvec(bvecAsString);
				double[] bval = convertBval(Double.parseDouble(b0), Integer.parseInt(bvecAsString[0]));
				// write the files
				assert outputDirectory.isDirectory();
				final String name = FilenameUtils.getBaseName(propFile.getName());
				final File bvalFile = new File(outputDirectory, name + ".bval");
				final File bvecFile = new File(outputDirectory, name + ".bvec");
				writeBval(bval, bvalFile);
				writeBvec(bvec, bvecFile);
				resultList.add(bvalFile);
				resultList.add(bvecFile);
			}
		} catch (final Exception exc) {
			LOG.error(exc.getMessage());
		}
		return resultList;
	}

	/**
	 * Convert a simple b value into the exepcted content for a '.bval' file.
	 *
	 * @param b0
	 *            the b0
	 * @param length
	 *            the length
	 *
	 * @return the double[]
	 */
	private static double[] convertBval(final double b0, final int length) {
		final double[] bval = new double[length + 1];
		bval[0] = 0;
		for (int i = 1; i < length + 1; i++) {
			bval[i] = b0;
		}
		return bval;
	}

	/**
	 * Convert a bvec matrix to its String representation.
	 *
	 * @param bvecAsString
	 *            the bvec as string
	 *
	 * @return the double[][]
	 */
	private static double[][] convertBvec(final String[] bvecAsString) {
		final double[][] bvec = new double[3][Integer.parseInt(bvecAsString[0]) + 1];
		bvec[0][0] = 0;
		if (bvec[0].length > 1) {
			bvec[0][1] = 0;
		}
		if (bvec[0].length > 2) {
			bvec[0][2] = 0;
		}

		for (int i = 1; i < bvecAsString.length; i++) {
			int indexX = (i - 1) % 3;
			int indexY = ((i - 1) / 3) + 1;
			bvec[indexX][indexY] = Double.parseDouble(bvecAsString[i]);
		}
		return bvec;
	}

	/**
	 * Writes the given bval file into the given file.
	 *
	 * @param bval
	 *            the bval
	 * @param file
	 *            the file
	 */
	private static void writeBval(final double[] bval, final File file) {
		try {
			final FileWriter wri = new FileWriter(file);
			final String bvalString = DiffusionUtil.bvalToString(bval);
			wri.write(bvalString);
			wri.close();
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}

	/**
	 * Writes the given bvec array into the given file.
	 *
	 * @param bvec
	 *            the bvec
	 * @param file
	 *            the file
	 */
	private static void writeBvec(final double[][] bvec, final File file) {
		try {
			final FileWriter wri = new FileWriter(file);
			wri.write(DiffusionUtil.bvecToString(bvec));
			wri.close();
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}

	/**
	 * Instantiates a new diffusion util.
	 */
	private DiffusionUtil() {

	}
}
