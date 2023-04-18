package org.shanoir.ng.processing.vip;

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
	
    public PipelineParameter() {
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
