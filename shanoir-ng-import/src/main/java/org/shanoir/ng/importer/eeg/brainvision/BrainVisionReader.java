package org.shanoir.ng.importer.eeg.brainvision;

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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.shanoir.ng.importer.model.Channel;
import org.shanoir.ng.importer.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class parses and reads brainvision files.
 * The folder in entry must at least contain a .vhdr file
 * It may contains a .pos, a .eeg and a .vmrk file
 * .eeg file is mandatory to call read() method
 * @author Arne
 * @author Nils Finke
 * @author JcomeD
 */
public class BrainVisionReader {

	private static final String ERROR_TEMPLATE_MSG = "Error: {}: {}";

	private static final Logger LOG = LoggerFactory.getLogger(BrainVisionReader.class);

    private static final String NO_FILE_FOUND_ON_CURRENT_LOCATION = "No file found on current location.";
	private String dataFileLocation;
    private RandomAccessFile dataFile;
    private DataFormat dataFormat;
    private DataOrientation dataOrientation;
    private DataType dataType;
    private float samplingIntervall;
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

    private File file;
    private File markerFile;
    private File positionFile;
    private File eegFile;
    private List<Channel> channels;
    private List<Event> events;
	private int samplingFrequency = 0;
	private boolean hasPosition;

    public BrainVisionReader(final File file) {
        this.file = file;
        hasPosition = false;
        if (file != null && file.exists() && file.getName().toLowerCase().endsWith(".vhdr")) {
            readHeaderFromVHDR();
        } else {
        	LOG.info("No .vhdr file");
        	return;
        }

        if (markerFile != null && markerFile.exists()) {
        	// Create events
        	readEventsFile();
        }

        // Read .pos file if existing
		// Get .pos file
        File parentDir = new File(file.getParent());
		String fileNameWithOutExt = FilenameUtils.removeExtension(file.getName());
		File[] matchingFiles = parentDir.listFiles(new FilenameFilter() {
		    @Override
			public boolean accept(final File dir, final String name) {
		        return name.equals(fileNameWithOutExt + ".pos");
		    }
		});

		if (matchingFiles != null && matchingFiles.length == 1) {
			positionFile = matchingFiles[0];
		}
        if (positionFile != null && positionFile.exists()) {
        	readPositionFile();
        }

        /**
         * Has to be set to 0 initially, reflects changes in buffer size
         */
        if (dataFormat.equals(DataFormat.BINARY)) {
            try {
            	eegFile = new File(dataFileLocation);
                dataFile = new RandomAccessFile(dataFileLocation, "r");
            } catch (FileNotFoundException ex) {
            	LOG.error(ex.getMessage());
            }

            if (pnts == 0 && dataFile != null) {
                try {
                    pnts = (int) (dataFile.length() / bytes / nbchan);
                } catch (IOException ex) {
                	LOG.error(ex.getMessage());
                }
            }
        }

        nSamples = 1;
        isAsciiRead = false;
    }

    /**
     * This method reads the .pos file to complete the Channel list with x, y, z position if existing
     */
    private void readPositionFile() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(positionFile), StandardCharsets.UTF_8))) {
            String newLine = null;
            int channelIndex = 1;

            // Iterate over the line to get the position
            while ((newLine = in.readLine()) != null) {
            	if (newLine.startsWith(String.valueOf(channelIndex))) {
            		String[] tmp = newLine.split("\\s+");
            		if (tmp.length != 5) {
            			continue;
            		}
            		// Find the channel with corresponding name
            		Channel chan = channels
            				.stream()
            				.filter(channel -> channel.getName().contentEquals(tmp[1]))
            				.findFirst()
            				.get();

            		chan.setX(Float.parseFloat(tmp[2]));
            		chan.setY(Float.parseFloat(tmp[3]));
            		chan.setZ(Float.parseFloat(tmp[4]));
            		
            		channelIndex++;
            	}
            }
            hasPosition = true;
        } catch (FileNotFoundException e) {
        	LOG.error(ERROR_TEMPLATE_MSG, NO_FILE_FOUND_ON_CURRENT_LOCATION, e.getMessage());
        } catch (IOException e) {
        	LOG.error(e.getMessage());
        }
    }

    /**
     * This method reads the .vmrk markerFile to get all the events associated.
     */
    private void readEventsFile() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(markerFile), StandardCharsets.UTF_8))) {
            String newLine = null;
            events = new ArrayList<>();
            boolean description = true;

            // Construct a list of events
            while ((newLine = in.readLine()) != null) {

            	// Check if description line exists or not
            	// Consider description is always true, but if not precised in comment
            	if (newLine.startsWith(";")) {
            		if (newLine.contains("<Marker number>") && !newLine.contains("<Description>")) {
            			description = false;
            		}
            	}

            	// Parse every new event coming
            	else if (newLine.startsWith("Mk")) {
                    String[] tmp = newLine.split(",");
                	// <type>,
                    // [<description>],
                	// <position>,
                    // <points>,
                    // <channel number>,
                    // [<date>]
                    int stringIndex = tmp[0].indexOf('=');
                    String eventType =  tmp[0].substring(stringIndex + 1);
                    String descriptionValue = description? tmp[1] : null;
                    String position = description? tmp[2] : tmp[1];
                    int points = Integer.parseInt(description? tmp[3] : tmp[2]);
                    int chNumber = Integer.parseInt(description? tmp[4] : tmp[3]);
                    Date date = null;
                    if (description && tmp.length == 6) {
                    	date = new SimpleDateFormat("yyyyMMddhhmmssSSSSSS").parse(tmp[5]);
                    } else if (!description && tmp.length == 5) {
                    	date = new SimpleDateFormat("yyyyMMddhhmmssSSSSSS").parse(tmp[4]);
                    }
                    Event event = new Event(eventType, descriptionValue, position, points, chNumber, date);
                    events.add(event);
                }
            }
        } catch (FileNotFoundException e) {
        	LOG.error(ERROR_TEMPLATE_MSG, NO_FILE_FOUND_ON_CURRENT_LOCATION, e.getMessage());
        } catch (IOException | ParseException e) {
        	LOG.error(e.getMessage());
        }
	}

	private void readHeaderFromVHDR() {
        int countChannels = 0;

        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String zeile = null;
            channels = new ArrayList<>();
            int channelIndex = 1;
            boolean amplifier = false;

            while ((zeile = in.readLine()) != null) {

                // Open DataFile
                if (zeile.startsWith("DataFile=")) {
                    dataFileLocation = file.getParent() + File.separator + zeile.substring(9);
                }

                // Open MarkerFile
                else if (zeile.startsWith("MarkerFile=")) {
                    markerFile = new File(file.getParent() + File.separator + zeile.substring(11));
                }

                // Read DataFormat
                else if (zeile.startsWith("DataFormat=")) {
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
                else if (zeile.startsWith("DataOrientation=")) {
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
                else if (zeile.startsWith("DataType=")) {
                	if ("TIMEDOMAIN".equals(zeile.substring(9))) {
                        dataType = DataType.TIMEDOMAIN;
                	} else {
                        dataType = DataType.UNKNOWN;
                	}
                }

                // Read number of channels
                else if (zeile.startsWith("NumberOfChannels=")) {
                    nbchan = Integer.parseInt(zeile.substring(17));
                    channelNames = new String[nbchan];
                }

                // Read number of data points
                else if (zeile.startsWith("DataPoints=")) {
                    pnts = Integer.parseInt(zeile.substring(11));
                }

                // Read sampling intervall
                else if (zeile.startsWith("SamplingInterval")) {
                    samplingIntervall = Float.parseFloat(zeile.substring(17));
                }
                
                // Read sampling rate
                else if (zeile.startsWith("Sampling Rate [Hz]: ")) {
                    samplingFrequency = Integer.parseInt(zeile.substring(21));
                }

                // Read binary format
                else if (zeile.startsWith("BinaryFormat=")) {
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
                else if (zeile.startsWith("UseBigEndianOrder=")) {
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
                else if (zeile.startsWith("SkipLines=")) {
                    skipLines = Integer.parseInt(zeile.substring(10));
                }

                // Read skip columns
                else if (zeile.startsWith("SkipColumns=")) {
                    skipColumns = Integer.parseInt(zeile.substring(12));
                }

                // Construct a list of channels
                else if (zeile.startsWith("Ch")) {
                    String[] tmp = zeile.split(",");

                    if (tmp.length == 4) {
                        int stringIndex = tmp[0].indexOf('=');
                        channelNames[countChannels] = tmp[0].substring(stringIndex + 1);
                        if (tmp[2].isEmpty()) {
                            channelResolution = 1;
                        } else {
                            channelResolution = (float) Double.parseDouble(tmp[2]);
                        }
                    	Channel chan = new Channel(tmp[0].substring(stringIndex + 1), channelResolution, tmp[3]);
                    	// Index is index channel - 1
                    	channels.add(chan);
                        countChannels++;
                    } else if (hasPosition && tmp.length == 3) {
                        int stringIndex = tmp[0].indexOf('=');
                        Channel chan = channels.get(Integer.parseInt(tmp[0].substring(2, stringIndex)) - 1);
                        chan.setX(Integer.valueOf(tmp[0].substring(stringIndex + 1)));
                        chan.setY(Integer.valueOf(tmp[1]));
                        chan.setZ(Integer.valueOf(tmp[2]));
                    }
                }
                
                else if (zeile.startsWith("A m p l i f i e r  S e t u p")) {
                	amplifier = true;
                }
                
                // Get the filter list
                else if (zeile.startsWith(String.valueOf(channelIndex)) && amplifier) {
                	/*
                	 * #     Name      Phys. Chn.    Resolution / Unit   Low Cutoff [s]   High Cutoff [Hz]   Notch [Hz]    Series Res. [kOhm] Gradient         Offset
					 * 1     Fp1         1                0.5 µV             DC             1000              Off                0
                	 */
                	Channel channelToGet = channels.get(channelIndex - 1);
                	// Split by spaces
                	// Consider that a channel name can contain a space, split with 2+ spaces (and hope that the separator will always be 2+ spaces..
                	String[] tmp = zeile.split("\\s\\s+");
                	channelToGet.setLowCutoff("DC".equals(tmp[4]) ? 0 : Integer.parseInt(tmp[4]));
                	channelToGet.setHighCutoff("DC".equals(tmp[5]) ? 0 : Integer.parseInt(tmp[5]));
                	channelToGet.setNotch("Off".equals(tmp[6]) ? 0 : Integer.parseInt(tmp[6]));
                	channelIndex ++;
                }
                else if (zeile.startsWith("[Coordinates]")) {
                	hasPosition = true;
                }
            }
        } catch (FileNotFoundException e) {
        	LOG.error(ERROR_TEMPLATE_MSG, NO_FILE_FOUND_ON_CURRENT_LOCATION, e.getMessage());
        } catch (IOException e) {
          	LOG.error(e.getMessage());
        }

        //set some standard values, if header is not complete
        srate = 1e6 / samplingIntervall;

        if (dataType == null) {
            dataType = DataType.TIMEDOMAIN;
        }

    }

    public void read(final int channel, final long from, final long to) {
        int nbSamples = (int) (to - from);
        if (this.nSamples != nbSamples) {
            prepareBuffers(nbSamples);
        }

        if (dataFormat.equals(DataFormat.BINARY)) {
            readBinary(channel, from);
        } else if (dataFormat.equals(DataFormat.ASCII)) {
            if (!isAsciiRead) {
                asciiData = readAscii(new File(dataFileLocation));
                isAsciiRead = true;
            }
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
    private float[] readBinary(final int channel, final long from) {
        try {
            FileChannel inChannel = dataFile.getChannel();
            // Set the start position in the file

            buf.clear();
            if (dataOrientation.equals(DataOrientation.MULTIPLEXED)) {
                inChannel.position((from * nbchan + channel) * bytes);
            } else if (dataOrientation.equals(DataOrientation.VECTORIZED)) {
                inChannel.position((channel * pnts + from) * bytes);
            }

            final int increment = nbchan * bytes - bytes;
            final boolean flag = dataOrientation.equals(DataOrientation.MULTIPLEXED);
            final int localBytes = this.bytes;

            int nRead = inChannel.read(buf);
            if (nRead != -1) {
                // Make buffer ready for read
                buf.rewind();
                for (int i = 0; i < data.length; i++) {
                    if (localBytes == 2) {
                        data[i] = buf.getShort() * channelResolution;
                    } else if (localBytes == 4) {
                        data[i] = buf.getFloat();
                    } else if (localBytes == 8) {
                        data[i] = (float) buf.getDouble();
                    }

                    if (flag) {
                        // This is the next sample in this epoch for the given channel
                        buf.position(buf.position() + increment);
                    }
                }
            }
        } catch (IOException e) {
           	LOG.error(e.getMessage());
        }
        return data;
    }

    private float[][] readAscii(final File dataFileLocation) {
        float[][] out = null;
        try (BufferedReader in = new BufferedReader(new FileReader(dataFileLocation))) {
            out = in.lines()
                    .map(e -> e.replaceAll(",", "."))
                    .map(e -> e.replaceAll("\\s+", " "))
                    .map(e -> e.split(" "))
                    .map(e -> doubleToFloat(Arrays.stream(e).skip(skipColumns).mapToDouble(i -> Double.parseDouble(i)).toArray()))
                    .toArray(float[][]::new);

        } catch (FileNotFoundException e) {
        	LOG.error(ERROR_TEMPLATE_MSG, NO_FILE_FOUND_ON_CURRENT_LOCATION, e.getMessage());
        } catch (IOException e) {
        	LOG.error(e.getMessage());
        }
        return out;
    }

    private void prepareBuffers(final int nSamples) {
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
        	// Nothing to be done here
        } else {
        	LOG.error("Cannot recognize specific BrainVision format");
        }

    }

    public void close() throws IOException {
        if (dataFormat.equals(DataFormat.BINARY) && dataFile != null) {
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

    public float getSamplingIntervall() {
        return samplingIntervall;
    }
    
    public int getSamplingFrequency() {
    	return samplingFrequency;
    }

    public List<Channel> getChannels() {
		return channels;
	}

	public List<Event> getEvents() {
		return events;
	}

	public boolean getHasPosition() {
		return hasPosition;
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

    public static float[] doubleToFloat(final double[] x) {
        float[] y = new float[x.length];
        for (int i = 0; i < y.length; i++) {
            y[i] = (float) x[i];
        }
        return y;
    }
    
    public File getMarkerFile() {
		return markerFile;
	}

	public File getPositionFile() {
		return positionFile;
	}
	
	public File getEegFile() {
		return eegFile;
	}
}