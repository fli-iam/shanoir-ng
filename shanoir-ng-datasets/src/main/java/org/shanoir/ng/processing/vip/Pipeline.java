package org.shanoir.ng.processing.vip;

import java.util.List;
import java.util.Map;

public class Pipeline {
    String identifier;
    String name;
    String version;
    String description;
    boolean canExecute;
    List<PipelineParameter> parameters;
    Map<String, String> properties;
    List<ErrorCodeAndMessage> errorCodesAndMessages;

    public Pipeline(String identifier, String name, String version, String description, boolean canExecute,
			List<PipelineParameter> parameters, Map<String, String> properties,
			List<ErrorCodeAndMessage> errorCodesAndMessages) {
		super();
		this.identifier = identifier;
		this.name = name;
		this.version = version;
		this.description = description;
		this.canExecute = canExecute;
		this.parameters = parameters;
		this.properties = properties;
		this.errorCodesAndMessages = errorCodesAndMessages;
	}

	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the canExecute
	 */
	public boolean isCanExecute() {
		return canExecute;
	}

	/**
	 * @param canExecute the canExecute to set
	 */
	public void setCanExecute(boolean canExecute) {
		this.canExecute = canExecute;
	}

	/**
	 * @return the parameters
	 */
	public List<PipelineParameter> getParameters() {
		return parameters;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(List<PipelineParameter> parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return the properties
	 */
	public Map<String, String> getProperties() {
		return properties;
	}

	/**
	 * @param properties the properties to set
	 */
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	/**
	 * @return the errorCodesAndMessages
	 */
	public List<ErrorCodeAndMessage> getErrorCodesAndMessages() {
		return errorCodesAndMessages;
	}

	/**
	 * @param errorCodesAndMessages the errorCodesAndMessages to set
	 */
	public void setErrorCodesAndMessages(List<ErrorCodeAndMessage> errorCodesAndMessages) {
		this.errorCodesAndMessages = errorCodesAndMessages;
	}

	public class PipelineParameter {
        String name;
        String type;
        boolean isOptional;
        boolean isReturnedValue;
        String defaultValue; // @alaeessaki not described in swagger in java client, it's described as an Object, but i think to avoid errors for now i'll make it any. TODO specify the tyoe.
        String description;
        
		public PipelineParameter(String name, String type, boolean isOptional, boolean isReturnedValue,
				String defaultValue, String description) {
			super();
			this.name = name;
			this.type = type;
			this.isOptional = isOptional;
			this.isReturnedValue = isReturnedValue;
			this.defaultValue = defaultValue;
			this.description = description;
		}
		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}
		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}
		/**
		 * @return the type
		 */
		public String getType() {
			return type;
		}
		/**
		 * @param type the type to set
		 */
		public void setType(String type) {
			this.type = type;
		}
		/**
		 * @return the isOptional
		 */
		public boolean isOptional() {
			return isOptional;
		}
		/**
		 * @param isOptional the isOptional to set
		 */
		public void setOptional(boolean isOptional) {
			this.isOptional = isOptional;
		}
		/**
		 * @return the isReturnedValue
		 */
		public boolean isReturnedValue() {
			return isReturnedValue;
		}
		/**
		 * @param isReturnedValue the isReturnedValue to set
		 */
		public void setReturnedValue(boolean isReturnedValue) {
			this.isReturnedValue = isReturnedValue;
		}
		/**
		 * @return the defaultValue
		 */
		public String getDefaultValue() {
			return defaultValue;
		}
		/**
		 * @param defaultValue the defaultValue to set
		 */
		public void setDefaultValue(String defaultValue) {
			this.defaultValue = defaultValue;
		}
		/**
		 * @return the description
		 */
		public String getDescription() {
			return description;
		}
		/**
		 * @param description the description to set
		 */
		public void setDescription(String description) {
			this.description = description;
		}
        
        
        
    }
   
    public class ErrorCodeAndMessage { 
        int errorCode;
        String errorMessage;
        Map<String, String> errorDetails;
        
        public ErrorCodeAndMessage(int errorCode, String errorMessage, Map<String, String> errorDetails) {
			super();
			this.errorCode = errorCode;
			this.errorMessage = errorMessage;
			this.errorDetails = errorDetails;
		}
		/**
		 * @return the errorCode
		 */
		public int getErrorCode() {
			return errorCode;
		}
		/**
		 * @param errorCode the errorCode to set
		 */
		public void setErrorCode(int errorCode) {
			this.errorCode = errorCode;
		}
		/**
		 * @return the errorMessage
		 */
		public String getErrorMessage() {
			return errorMessage;
		}
		/**
		 * @param errorMessage the errorMessage to set
		 */
		public void setErrorMessage(String errorMessage) {
			this.errorMessage = errorMessage;
		}
		/**
		 * @return the errorDetails
		 */
		public Map<String, String> getErrorDetails() {
			return errorDetails;
		}
		/**
		 * @param errorDetails the errorDetails to set
		 */
		public void setErrorDetails(Map<String, String> errorDetails) {
			this.errorDetails = errorDetails;
		}
        
        
        
    }
}
