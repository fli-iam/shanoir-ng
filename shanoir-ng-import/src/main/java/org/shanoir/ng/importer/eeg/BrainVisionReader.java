package org.shanoir.ng.importer.eeg;

/*
 * Copyright (C) 2016 Arne Weigenand
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;

/**
 *
 * @author Arne
 * @author Nils Finke
 */
public class BrainVisionReader {

    private File file;
    private File markerFile;
    private String dataFileLocation;
    private RandomAccessFile dataFile;
    private DataFormat dataFormat;
    private DataOrientation dataOrientation;
    private DataType dataType;
    private int samplingIntervall;
    private BinaryFormat binaryFormat;
    private boolean useBigEndianOrder;
    private int skipLines;
    private int skipColumns;
    private float channelResolution;

    private int nbchan;
    private long pnts;
    private double srate;
    private float[] data;
    private float[][] asciiData;
    private boolean isAsciiRead;
    private String[] channelNames;

    private ByteBuffer buf;
    private long nSamples;
    private int bytes;

    public BrainVisionReader(File file) {
        this.file = file;
        if (file.getName().toLowerCase().endsWith(".vhdr")) {
            readHeaderFromVHDR();
        }
        /**
         * Has to be set to 0 initially, reflects changes in buffer size
         */
        if (dataFormat.equals(DataFormat.BINARY)) {
            try {
                dataFile = new RandomAccessFile(dataFileLocation, "r");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(BrainVisionReader.class.getName()).log(Level.ALL, null, ex);
            }

            if (pnts == 0) {
                try {
                    pnts = (int) (dataFile.length() / bytes / (long) nbchan);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        printPropertiesVHDR();

        nSamples = 1;
        isAsciiRead = false;
    }

    private void readHeaderFromVHDR() {
        int countChannels = 0;

        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            String zeile = null;

            while ((zeile = in.readLine()) != null) {

                // Open DataFile
                if (zeile.startsWith("DataFile=")) {
                    dataFileLocation = file.getParent() + File.separator + zeile.substring(9);
                }

                // Open MarkerFile
                if (zeile.startsWith("MarkerFile=")) {
                    markerFile = new File(file.getParent() + File.separator + zeile.substring(11));
                }

                // Read DataFormat
                if (zeile.startsWith("DataFormat=")) {
                    switch (zeile.substring(11)) {
                        case "BINARY":
                            dataFormat = DataFormat.BINARY;
                            break;
                        case "ASCII":
                            dataFormat = DataFormat.ASCII;
                            break;
                        default:
                            dataFormat = DataFormat.UNKNOWN;
                            break;
                    }
                }

                // Read DataOrientation
                if (zeile.startsWith("DataOrientation=")) {
                    switch (zeile.substring(16)) {
                        case "MULTIPLEXED":
                            dataOrientation = DataOrientation.MULTIPLEXED;
                            break;
                        case "VECTORIZED":
                            dataOrientation = DataOrientation.VECTORIZED;
                            break;
                        default:
                            dataOrientation = DataOrientation.UNKNOWN;
                            break;
                    }
                }

                // Read DataType
                if (zeile.startsWith("DataType=")) {
                    switch (zeile.substring(9)) {
                        case "TIMEDOMAIN":
                            dataType = DataType.TIMEDOMAIN;
                            break;
                        default:
                            dataType = DataType.UNKNOWN;
                            break;
                    }
                }

                // Read number of channels
                if (zeile.startsWith("NumberOfChannels=")) {
                    nbchan = Integer.parseInt(zeile.substring(17));
                    channelNames = new String[nbchan];
                }

                // Read number of data points
                if (zeile.startsWith("DataPoints=")) {
                    pnts = Integer.parseInt(zeile.substring(11));
                }

                // Read sampling intervall
                if (zeile.startsWith("SamplingInterval")) {
                    samplingIntervall = Integer.parseInt(zeile.substring(17));
                }

                // Read binary format
                if (zeile.startsWith("BinaryFormat=")) {
                    bytes = 2; // default
                    switch (zeile.substring(13)) {
                        case "UINT_16":
                            binaryFormat = BinaryFormat.UINT_16;
                            bytes = 2;
                            break;
                        case "INT_16":
                            binaryFormat = BinaryFormat.INT_16;
                            bytes = 2;
                            break;
                        case "IEEE_FLOAT_32":
                            binaryFormat = BinaryFormat.IEEE_FLOAT_32;
                            bytes = 4;
                            break;
                        case "IEEE_FLOAT_64":
                            binaryFormat = BinaryFormat.IEEE_FLOAT_64;
                            bytes = 8;
                            break;
                        default:
                            binaryFormat = BinaryFormat.UNKNOWN;
                            break;
                    }
                }

                // Read endian order
                if (zeile.startsWith("UseBigEndianOrder=")) {
                    switch (zeile.substring(18)) {
                        case "NO":
                            useBigEndianOrder = false;
                            break;
                        case "YES":
                            useBigEndianOrder = true;
                            break;
                        default:
                            useBigEndianOrder = false;
                            break;
                    }
                }

                // Read skip lines
                if (zeile.startsWith("SkipLines=")) {
                    skipLines = Integer.parseInt(zeile.substring(10));
                }

                // Read skip columns
                if (zeile.startsWith("SkipColumns=")) {
                    skipColumns = Integer.parseInt(zeile.substring(12));
                }

                // Read channel resolution
                // TODO: IMPORTANT: It could be possible, that each channel has a different resolution!
                if (zeile.startsWith("Ch")) {
                    String[] tmp = zeile.split(",");

                    if (tmp.length == 4) {
                        int stringIndex = tmp[0].indexOf("=");
                        channelNames[countChannels] = tmp[0].substring(stringIndex + 1);
                        if (tmp[2].isEmpty()) {
                            channelResolution = 1;
                        } else {
                            channelResolution = (float) Double.parseDouble(tmp[2]);
                        }
                        countChannels++;
                    }
                }

            }

        } catch (FileNotFoundException e) {
            System.err.println("No file found on current location.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //set some standard values, if header is not complete
        srate = 1e6 / samplingIntervall;

        if (dataType == null) {
            dataType = DataType.TIMEDOMAIN;
        }

    }

    /**
     * Testfunction: Proof manually, if properties are correct.
     */
    private void printPropertiesVHDR() {

        System.out.println("DataFormat: " + dataFormat);
        System.out.println("DataOrientation: " + dataOrientation);
        System.out.println("DataType: " + dataType);
        System.out.println("NumberOfChannels: " + nbchan);
        System.out.println("DataPoints: " + pnts);
        System.out.println("SamplingIntervall: " + samplingIntervall);
        System.out.println("BinaryFormat: " + binaryFormat);
        System.out.println("SkipLines: " + skipLines);
        System.out.println("SkipColumns: " + skipColumns);
        System.out.println("UseBigEndianOrdner: " + useBigEndianOrder);
        System.out.println("ChannelResolution: " + channelResolution);
        String[] tmp = channelNames;
        System.out.print("ChannelNames:");
        for (int i = 0; i < tmp.length; i++) {
            System.out.print(" " + tmp[i]);
        }
        System.out.println("SamplingRate in Hertz: " + srate);

    }

    public void read(int channel, long from, long to) {
        //TODO: check bounds!
        int nSamples = (int) (to - from);
        if (this.nSamples != nSamples) {
            prepareBuffers(nSamples);
        }

        if (dataFormat.equals(DataFormat.BINARY)) {
            readBinary(channel, from, to);
        } else if (dataFormat.equals(DataFormat.ASCII)) {
            if (!isAsciiRead) {
                asciiData = readAscii(new File(dataFileLocation));
                isAsciiRead = true;
            }
            int j = 0;
            System.arraycopy(asciiData[channel], (int) from, data, 0, data.length);
        }
    }

    /**
     * Reads the first data value of channel 1
     *
     * @param dataFile file with data content
     * @param channel
     * @param epochToRead the epoch which have to be read.
     * @return
     */
    private float[] readBinary(int channel, long from, long to) {
        try {
            FileChannel inChannel = dataFile.getChannel();
            // Set the start position in the file

            buf.clear();
            if (dataOrientation.equals(DataOrientation.MULTIPLEXED)) {
                inChannel.position((from * nbchan + channel) * bytes);
            } else if (dataOrientation.equals(DataOrientation.VECTORIZED)) {
                inChannel.position((channel * pnts + from) * bytes);
            }

            final int increment = (nbchan * bytes) - bytes;
            final boolean flag = dataOrientation.equals(DataOrientation.MULTIPLEXED);
            final int bytes = this.bytes;

            int nRead = 0;
            if ((nRead = inChannel.read(buf)) != -1) {
                // Make buffer ready for read
                buf.rewind();
                for (int i = 0; i < data.length; i++) {
                    if (bytes == 2) {
                        data[i] = buf.getShort() * channelResolution;
                    } else if (bytes == 4) {
                        data[i] = buf.getFloat();
                    } else if (bytes == 8) {
                        data[i] = (float) buf.getDouble();
                    }

                    if (flag) {
                        // This is the next sample in this epoch for the given channel  
                        buf.position(buf.position() + increment);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    private float[][] readAscii(File dataFileLocation) {
        float[][] out = null;
        try (BufferedReader in = new BufferedReader(new FileReader(dataFileLocation))) {
            out = in.lines()
                    .map(e -> e.replaceAll(",", "."))
                    .map(e -> e.replaceAll("\\s+", " "))
                    .map(e -> e.split(" "))
                    .map(e -> doubleToFloat(Arrays.stream(e).skip(skipColumns).mapToDouble(i -> Double.parseDouble(i)).toArray()))
                    .toArray(float[][]::new);

        } catch (FileNotFoundException e) {
            System.err.println("No file found on current location.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out;
    }

    private void prepareBuffers(int nSamples) {
        this.nSamples = nSamples;
        data = new float[nSamples];

        if (dataFormat.equals(DataFormat.BINARY) && dataType.equals(DataType.TIMEDOMAIN)) {
            if (dataOrientation.equals(DataOrientation.MULTIPLEXED)) {
                buf = ByteBuffer.allocateDirect(bytes * nSamples * nbchan);
            } else if (dataOrientation.equals(DataOrientation.VECTORIZED)) {
                buf = ByteBuffer.allocateDirect(bytes * nSamples);
            }

            if (useBigEndianOrder) {
                buf.order(ByteOrder.BIG_ENDIAN);
            } else {
                buf.order(ByteOrder.LITTLE_ENDIAN);
            }

        } else if (dataFormat.equals(DataFormat.ASCII) && dataType.equals(DataType.TIMEDOMAIN)) {

        } else {
            System.out.println("Cannot recognize specific BrainVision format");
        }

    }

    public void close() throws IOException {
        if (dataFormat.equals(DataFormat.BINARY)) {
            dataFile.close();
        }
    }

    public double getSrate() {
        return srate;
    }

    public int getNbchan() {
        return nbchan;
    }

    public float[] getData() {
        return data;
    }

    public long getPnts() {
        return pnts;
    }

    public String[] getChannelNames() {
        return channelNames;
    }

    public int getSamplingIntervall() {
        return samplingIntervall;
    }

    public enum BinaryFormat {

        UNKNOWN, UINT_16, INT_16, IEEE_FLOAT_32, IEEE_FLOAT_64
    }

    public enum DataFormat {

        UNKNOWN, BINARY, ASCII
    }

    public enum DataOrientation {

        UNKNOWN, MULTIPLEXED, VECTORIZED
    }

    public enum DataType {

        UNKNOWN, TIMEDOMAIN
    }

    public static float[] doubleToFloat(double[] x) {
        float[] y = new float[x.length];
        for (int i = 0; i < y.length; i++) {
            y[i] = (float) x[i];
        }
        return y;
    }
}