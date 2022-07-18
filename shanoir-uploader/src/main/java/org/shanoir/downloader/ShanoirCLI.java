package org.shanoir.downloader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;

/**
 * @author grenard and Arthur M.
 *
 *         This abstract class gathers all the common features of a ShanoirTk CLI.
 *
 *         ShanoirTkCLI looks after all repetitive process involved by a CLI
 *         such as parsing common options, establishing connection and provides
 *         util functions. Nevertheless, the particular options of a child class
 *         must be parsed in the postparse funtion.
 *
 *         For instance, the ShanoirTkCLI class will take care of loading user
 *         preferences saved in a user property file. This works for host,
 *         username, password, truststore path...
 */
public abstract class ShanoirCLI {

	/** Logger. */
	protected static Logger log = Logger.getLogger(ShanoirCLI.class);

	/**
	 * @author grenard and Arthur M.
	 *
	 *         This is for overwriting a function used in the establishment of the
	 *         ssl connection.
	 *
	 *         During handshaking, if the URL's hostname and the server's
	 *         identification hostname mismatch, the verification mechanism can call
	 *         back to implementers of HostnameVerifier to determine if this
	 *         connection should be allowed.
	 *
	 *         Since this is a dev version, we allow the connection whatever the
	 *         connection.
	 *
	 */

	/**
	 * To store the path to the user property file. Initialized at
	 * USER_HOME/.shanoir/server.properties.
	 */
	private static String configurationPath;
	static {
		if (SystemUtils.IS_OS_WINDOWS) {
			configurationPath = SystemUtils.USER_HOME + "\\.shanoir\\server.properties";
		} else if (SystemUtils.IS_OS_UNIX) {
			configurationPath = SystemUtils.USER_HOME + "/.shanoir/server.properties";
		}
	}

	/** -host to set the host. Can be defined in the user property file. */
	private static Option hostOption;
	static {
		OptionBuilder.withArgName("host");
		OptionBuilder.hasArg();
		OptionBuilder.isRequired(true);
		OptionBuilder.withDescription("host of the shanoir server.");
		hostOption = OptionBuilder.create("host");
	}

	/** -user to set the user. Can be defined in the user property file. */
	private static Option userOption;
	static {
		OptionBuilder.withArgName("user");
		OptionBuilder.hasArg();
		OptionBuilder.isRequired(true);
		OptionBuilder.withDescription("username for user identitification.");
		userOption = OptionBuilder.create("user");
	}

	/** -password to set the password. Can be defined in the user property file. */
	private static Option passwordOption;
	static {
		OptionBuilder.withArgName("password");
		OptionBuilder.hasArg();
		OptionBuilder.isRequired(false);
		OptionBuilder.withDescription("password for user identitification. Warning: Please ignore this option if possible, as it will be safely asked (and masked) from the console if needed. This option is still available for scripts and backward compatibility.");
		passwordOption = OptionBuilder.create("password");
	}

	/** -h used to request help on command line options. */
	private static Option helpOption;
	static {
		OptionBuilder.hasArg(false);
		OptionBuilder.withDescription("Print help for this application");
		helpOption = OptionBuilder.create("h");
	}

	/** -v returns the version of the application. */
	private static Option versionOption;
	static {
		OptionBuilder.hasArg(false);
		OptionBuilder.withDescription("print the version information and exit");
		versionOption = OptionBuilder.create("v");
	}

	/**
	 * Exit with an error code and display a message.
	 * 
	 * @param msg
	 *            the message to be displayed
	 */
	protected static void exit(final String msg) {
		if (msg != null && !msg.equals(""))
			log.error(msg);
		System.exit(1);
	}

	/**
	 * Exit with an error code, display a message and tell users to call upon Help.
	 * 
	 * @param msg
	 *            the message to be displayed
	 */
	protected static void exitAndHelp(final String msg) {
		if (msg != null && !msg.equals(""))
			log.error(msg);
		log.error("Try '-h' for more information.");
		System.exit(1);
	}

	/**
	 * Util : this function parses an Array of Strings into a List of Longs
	 * 
	 * @param longs
	 *            the Array of Strings
	 * @return the List of Longs
	 * @throws NumberFormatException
	 *             if one of the string of longs cannot be parse into a Long
	 */
	protected static List<Long> parseLongs(String[] longs) throws NumberFormatException {
		List<Long> result = new ArrayList<Long>();
		for (String s : longs) {
			result.add(Long.parseLong(s));
		}
		return result;
	}

	/**
	 * Util : this function parses an Array of Strings into a List of Integer
	 * 
	 * @param integers
	 *            the Array of Strings
	 * @return the List of Integer
	 * @throws NumberFormatException
	 *             if one of the string of integers cannot be parse into a Integer
	 */
	protected static List<Integer> parseIntegers(String[] integers) throws NumberFormatException {
		List<Integer> result = new ArrayList<Integer>();
		for (String s : integers) {
			result.add(Integer.parseInt(s));
		}
		return result;
	}

	/**
	 * Util : transforms any Object Array into a List
	 * 
	 * @param <E>
	 *            the type of the object
	 * @param array
	 *            the array of <E>
	 * @return the List of <E>
	 */
	protected static <E> List<E> toList(E[] array) {
		List<E> resultList = new ArrayList<E>();
		for (int i = 0; i < array.length; i++) {
			resultList.add(array[i]);
		}
		return resultList;
	}

	/** The Command Line itself. */
	protected CommandLine cl;

	/** The description: tip displayed in the help manual of the Command. */
	private String description;

	/** The example: tip displayed in the help manual of the Command. */
	private String example;

	/** The host we are connecting to. */
	protected String host;

	/** The collection of Option used by the Command Line */
	protected Options options;

	/** The properties issued from the user property file. */
	private Properties properties;

	/** The usage: tip displayed in the help manual of the Command. */
	private String usage;

	/**
	 * Constructor of the Shanoir Command Line.
	 *
	 * @param opts
	 *            the additonal options you will need in your proper command line
	 * @param description
	 *            the description of your command line
	 * @param example
	 *            an example on how to use your command line
	 * @param usage
	 *            the general usage of your command line
	 */
	public ShanoirCLI(Options opts, final String description, final String example, final String usage) {
		this.description = description;
		this.example = example;
		this.usage = usage;
		try {
			initFromPropertyFile();
		} catch (FileNotFoundException e) {
			log.warn(e.getMessage());
		} catch (IOException e) {
			log.warn(e.getMessage());
		}
		if (opts == null) {
			opts = new Options();
		}
		opts.addOption(helpOption);
		opts.addOption(versionOption);
		opts.addOption(hostOption);
		opts.addOption(userOption);
		opts.addOption(passwordOption);
		options = opts;
	}

	/**
	 * Gets the host. Can be defined in the user property file.
	 *
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Gets the value of an option.
	 *
	 * @param opt
	 *            the name of the option
	 * @return the value of the option of which the name is opt
	 */
	public String getOptionValue(String opt) {
		return cl.getOptionValue(opt);
	}

	/**
	 * Gets the array of all the values of an option.
	 *
	 * @param opt
	 *            the name of the option
	 * @return the array the array of all the values of the option of which the name
	 *         is opt
	 */
	public String[] getOptionValues(String opt) {
		return cl.getOptionValues(opt);
	}

	/**
	 * Checks whether an option is present within the command line.
	 *
	 * @param opt
	 *            the name of the option
	 * @return true if the option of which the name is opt
	 */
	public boolean hasOption(String opt) {
		return cl.hasOption(opt);
	}

	/**
	 *
	 * @throws IOException
	 * @throws FileNotFoundException
	 *
	 */
	private void initFromPropertyFile() throws FileNotFoundException, IOException {
		properties = new Properties();
		properties.load(new FileInputStream(configurationPath));
		log.info("User Property File found :" + configurationPath);
	}

	/**
	 * This method parses the different options given as argument. It checks whether
	 * username, password, host and truststore options are given in argument
	 * and initialise the matching attributes. If an option above is not given, then
	 * it checks if the value is present is the user property file. If no value is
	 * present, it throws a MissingArgumentException.
	 *
	 * @param args
	 *            the options
	 * @throws MissingArgumentException
	 *             if an option is missing
	 * @throws java.text.ParseException
	 *             if a problem occurs during parsing
	 * @throws DatatypeConfigurationException
	 *             if a problem occurs during type conversion
	 */
	protected void parse(String args[])
			throws MissingArgumentException, java.text.ParseException, DatatypeConfigurationException {
		GnuParser parser = new GnuParser();
		// parse args
		try {
			cl = parser.parse(options, args);
		} catch (final MissingOptionException exc) {
			// a required command line arg is missing. print help.
			HelpFormatter f = new HelpFormatter();
			f.printHelp("ShanoirTkCLI", options);
			exitAndHelp("ShanoirTkCLI ERROR: " + exc.getMessage());
		} catch (final ParseException exc) {
			exitAndHelp("ShanoirTkCLI ERROR: " + exc.getMessage());
		}
		if (cl.hasOption('V')) {
			Package p = ShanoirCLI.class.getPackage();
			System.err.println("ShanoirTkCLI v" + p.getImplementationVersion());
			System.exit(0);
		}

		if (cl.hasOption('h')) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(usage, description, options, example);
			System.exit(0);
		}

		if (cl.hasOption("host")) {
			setHost(cl.getOptionValue("host"));
		} else if (properties.containsKey("host")) {
			this.setHost(properties.getProperty("host"));
		} else {
			exitAndHelp("ShanoirTkCLI ERROR: no host provided. The host parameter is required.");
		}

		postParse();

	}

	/**
	 * This is where the code specific to your Command Line will be parsed.
	 *
	 * @throws MissingArgumentException
	 * @throws java.text.ParseException
	 * @throws DatatypeConfigurationException
	 */
	protected abstract void postParse()
			throws MissingArgumentException, java.text.ParseException, DatatypeConfigurationException;

	/**
	 * Sets the host.
	 *
	 * @param host
	 *            the host to set
	 */
	public void setHost(final String host) {
		String finalHost = host.startsWith("http://") ? host.replace("http://", "https://") : host.startsWith("https://") ? host : "https://" + host;
		this.host = finalHost.endsWith("/") ? finalHost.substring(0, finalHost.length()-1) : finalHost;
	}
}
