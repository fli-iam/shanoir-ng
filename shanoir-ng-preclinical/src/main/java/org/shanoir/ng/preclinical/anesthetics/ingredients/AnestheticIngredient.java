package org.shanoir.ng.preclinical.anesthetics.ingredients;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.preclinical.anesthetics.anesthetic.Anesthetic;
import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;
import org.shanoir.ng.shared.validation.RefValueExists;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;
/**
 * Anesthetic Ingredient
 */
@Entity
@Table(name = "anesthetic_ingredient")
@JsonPropertyOrder({ "_links", "name","concentration","concentration_unit" })
public class AnestheticIngredient extends HalEntity   {
	
  @ManyToOne
  @NotNull
  @JsonIgnore
  @JsonManagedReference
  private Anesthetic anesthetic = null;
	
  @JsonProperty("name")
  @RefValueExists
  @ManyToOne
  private Reference name = null;
  
  @JsonProperty("concentration")
  private Double concentration;
  
  @JsonProperty("concentration_unit")
  @RefValueExists
  @ManyToOne
  private Reference concentrationUnit = null;
  
  /**
	* Init HATEOAS links
	*/
  @PostLoad
  public void initLinks() {
	this.addLink(Links.REL_SELF, "anesthetic/"+ anesthetic.getId() +"/ingredient/" + getId());
  }
			
  @Override
  @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "IdOrGenerate")
  @GenericGenerator(name = "IdOrGenerate", strategy = "increment")
  public Long getId() {
  	return super.getId();
  }
  
  public AnestheticIngredient anesthetic(Anesthetic anesthetic) {
    this.anesthetic = anesthetic;
    return this;
  }

  @ApiModelProperty(value = "none")
  public Anesthetic getAnesthetic() {
    return anesthetic;
  }

  public void setAnesthetic(Anesthetic anesthetic) {
    this.anesthetic = anesthetic;
  }

  public AnestheticIngredient name(Reference name) {
    this.name = name;
    return this;
  }

  @ApiModelProperty(value = "none")
  public Reference getName() {
    return name;
  }

  public void setName(Reference name) {
    this.name = name;
  }
  
    
  public AnestheticIngredient concentration(Double concentration) {
    this.concentration = concentration;
    return this;
  }
  
  @ApiModelProperty(value = "none")
  public Double getConcentration() {
    return concentration;
  }

  public void setConcentration(Double concentration) {
    this.concentration = concentration;
  }
  
  public AnestheticIngredient concentrationUnit(Reference unit) {
    this.concentrationUnit = unit;
    return this;
  }

  @ApiModelProperty(value = "none")
  public Reference getConcentrationUnit() {
    return concentrationUnit;
  }

  public void setConcentrationUnit(Reference unit) {
    this.concentrationUnit = unit;
  }

  
  @Override
  public int hashCode() {
	return Objects.hash(name);
  }
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnestheticIngredient other = (AnestheticIngredient) obj;
		if (concentration == null) {
			if (other.concentration != null)
				return false;
		} else if (!concentration.equals(other.concentration))
			return false;
		if (concentrationUnit == null) {
			if (other.concentrationUnit != null)
				return false;
		} else if (!concentrationUnit.equals(other.concentrationUnit))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (anesthetic == null) {
			if (other.anesthetic != null)
				return false;
		} else if (!anesthetic.equals(other.anesthetic))
			return false;
		return true;
	}

  @Override	
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AnestheticIngredient {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    concentration: ").append(toIndentedString(concentration)).append("\n");
    sb.append("    concentration unit: ").append(toIndentedString(concentrationUnit)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

