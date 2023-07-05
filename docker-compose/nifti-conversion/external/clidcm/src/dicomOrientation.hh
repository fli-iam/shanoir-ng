#ifndef DICOM_ORIENTATION_HH
#define DICOM_ORIENTATION_HH

#include <cmath>

static inline string getMajorAxisFromPatientRelativeDirectionCosine(double x, double y, double z)
{
  static float obliquityThresholdCosineValue = 0.8;
  string axis = "";
  string orientationX = "";
  string orientationY = "";
  string orientationZ = "";
  // x varie de R->L
  // y          P->A
  // z          F->H
  if (x < 0) {
    orientationX = "R";
  }  else orientationX = "L";
  if (y < 0) {
    orientationY = "A";
  }  else orientationY = "P";
  if (z < 0) {
    orientationZ = "F";
  }  else orientationZ = "H";

  double absX = std::abs(x);
  double absY = std::abs(y);
  double absZ = std::abs(z);

  //std::cout << orientationX << " " << orientationY << " " << orientationZ << std::endl;
  //std::cout << x << " " << y << " " << z << std::endl;
  //std::cout << absX << " " << absY << " " << absZ << std::endl;

  // a l'origine > et non >=
  if (absX > obliquityThresholdCosineValue && absX >= absY && absX >= absZ) {
    axis=orientationX;
  } else if (absY > obliquityThresholdCosineValue && absY >= absX && absY >= absZ) {
    axis=orientationY;
  } else if (absZ > obliquityThresholdCosineValue && absZ >= absX && absZ >= absY) {
    axis=orientationZ;
  }
  return axis;
}

/** @brief Give the GIS orientation string, constructed from the 3 direction cosines of the row and column vector of a dicom slice.
 *  @author Alban Gaignard, alban.gaignard@irisa.fr
 *  @param[in] rowX cos(alpha), cartesian coordinate along the x (from right to left hand side of the patient)
 *  axis in the patient based coordinate system of the ROW vector
 *  @param[in] rowY cos(beta), cartesian coordinate along the y (from the anterior to the posterior side of the patient)
 *  axis in the patient based coordinate system of the ROW vector
 *  @param[in] rowZ cos(gamma), cartesian coordinate along the z (from the feet to the head of the patient)
 *  axis in the patient based coordinate system of the ROW vector
 *  @param[in] colX cos(alpha), cartesian coordinate along the x (from right to left hand side of the patient)
 *  axis in the patient based coordinate system of the COLUMN vector
 *  @param[in] colY cos(beta), cartesian coordinate along the y (from the anterior to the posterior side of the patient)
 *  axis in the patient based coordinate system of the COLUMN vector
 *  @param[in] colZ cos(gamma), cartesian coordinate along the z (from the feet to the head of the patient)
 *  axis in the patient based coordinate system of the COLUMN vector
 *  @param[in] zDir boolean value specifying if the z axis is growing positvely or not.
 *  @result a string determining the orientation of data in GIS format
 *  @see the GIS specifications : http://visages.wiki.irisa.fr/tiki-index.php?page=PageGis
 */
static inline string makeImageOrientationLabel( double rowX,double rowY,double rowZ,
						double colX,double colY,double colZ, bool zDir) {
  
  
  string label = "";
  //direction des lignes de la coupe :
  string rowAxis = getMajorAxisFromPatientRelativeDirectionCosine(rowX,rowY,rowZ);
  //std::cout << "rowAxis : "<<rowAxis<<std::endl;
  //direction des colonnes de la coupe :
  string colAxis = getMajorAxisFromPatientRelativeDirectionCosine(colX,colY,colZ);
  //std::cout << "clAxis : "<<colAxis<<std::endl;
  bool ZPositive = zDir;

  // x varie de R->L
  // y          A->P
  // z          F->H
  if (rowAxis != "" && colAxis != "") {
    //if coupes axiales
    if ( ((rowAxis == "R" || rowAxis == "L") && (colAxis == "A" || colAxis == "P")) ||
	 ((colAxis == "R" || colAxis == "L") && (rowAxis == "A" || rowAxis == "P")) ) {
      //std::cout << "AXIAL\n";
      // 16 coupes axiales
      if (rowAxis == "R") {
	if (colAxis == "A") {
	  label = "LPRA";
	} else if (colAxis == "P") {
	  label = "LARP";
	}
      } else if (rowAxis == "L") {
	if (colAxis == "A") {
	  label = "RPLA";
	} else if (colAxis == "P") {
	  label = "RALP";
	}
      } else if (rowAxis == "P") {
	if (colAxis == "L") {
	  label = "ARPL";
	} else if (colAxis == "R") {
	  label = "ALPR";
	}
      } else if (rowAxis == "A") {
	if (colAxis == "L") {
	  label = "PRAL";
	} else if (colAxis == "R") {
	  label = "PLAR";
	}
      }
      if (ZPositive) {
	label+="FH";
	return label;
      } else {
	label+="HF";
	return label;
      }
    }

    else if ( ((rowAxis == "R" || rowAxis == "L") && (colAxis == "H" || colAxis == "F")) ||
	 ((colAxis == "R" || colAxis == "L") && (rowAxis == "H" || rowAxis == "F")) ) {
      //std::cout << "CORONAL\n";
      // 16 coupes coronales
      if (rowAxis == "L") {
	if (colAxis == "H") {
	  label = "RFLH";
	} else if (colAxis == "F") {
	  label = "RHLF";
	}

      } else if (rowAxis == "R") {
	if (colAxis == "H") {
	  label = "LFRH";
	} else if (colAxis == "F") {
	  label = "LHRF";
	}

      } else if (rowAxis == "H"){
	if (colAxis == "L"){
	  label = "FRHL";
	} else if (colAxis == "R"){
	  label = "FLHR";
	}

      } else if (rowAxis == "F"){
	if (colAxis == "L"){
	  label = "HRFL";
	} else if (colAxis == "R"){
	  label = "HLFR";
	}
      }
      if (ZPositive) {
	label+="PA";
	return label;
      } else {
	label+="AP";
	return label;
      }
    }

    else if ( ((rowAxis == "A" || rowAxis == "P") && (colAxis == "H" || colAxis == "F")) ||
	      ((colAxis == "A" || colAxis == "P") && (rowAxis == "H" || rowAxis == "F")) ) {
      //std::cout << "SAGITTAL\n";
      // 16 coupes sagittales
      if (rowAxis == "A") {
	if (colAxis == "H") {
	  label = "PFAH";
	} else if (colAxis == "F") {
	  label = "PHAF";
	}
      } else if (rowAxis == "P") {
	if (colAxis == "H") {
	  label = "AFPH";
	} else if (colAxis == "F") {
	  label = "AHPF";
	}
      } else if (rowAxis == "F"){
	if (colAxis == "A"){
	  label = "HPFA";
	} else if (colAxis == "P"){
	  label = "HAFP";
	}
      } else if (rowAxis == "H"){
	if (colAxis == "A"){
	  label = "FPHA";
	} else if (colAxis == "P"){
	  label = "FAHP";
	}
      }
      if (ZPositive) {
	label+="LR";
	return label;
      } else {
	label+="RL";
	return label;
      }
    }

    else {
      label="OBLIQUE";
      return label;
    }
  }
  return label;
}


#endif
