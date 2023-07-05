/**
 * @file dicomOrientation.cc
 * @brief given two dicom slices, this library will compute the anatomical orientation of the volume, with GIS semantic
 * @author Alban Gaignard (Alban.Gaignard@irisa.fr)
 * @date 25 of october, 2005
 */

#include "dicomOrientation.hh"

//double dirCos [9] = {0,0,0,0,0,0,0,0,0};
double obliquityThresholdCosineValue = 0.8;
int debugDcm = 1;

double extractZCoordFromImagePos(string tag){
  double pos;
  string::size_type deb = 0;
  string::size_type fin;
  for (int i = 0; i<3 ; i++){
    fin = tag.find("\\",deb);
    //std::cout << deb << " -> " << fin <<" : " << tagvalue.substr(deb,fin-deb) << std::endl;
    pos = atof(tag.substr(deb,fin-deb).c_str());
    deb = fin+1;
    //std::cout << pos << std::endl;
  }
  return pos;
}

int isZDirectionPositive(string firstSlice, string secondSlice, bool zPos){
  // on compare la position dans l'espace des deux coupes pour determiner
  // si z croit avec le numero de la coupe -> true
  // Lecture de l'image dicom ----------------------------
  typedef signed short       PixelType;
  const unsigned int         Dimension = 2;
  typedef itk::Image< PixelType, Dimension >      ImageType;
  typedef itk::ImageFileReader< ImageType >     ReaderType;
  ReaderType::Pointer reader = ReaderType::New();
  typedef itk::GDCMImageIO       ImageIOType;
  ImageIOType::Pointer dicomIO = ImageIOType::New();
  typedef itk::MetaDataDictionary   DictionaryType;
  const  DictionaryType & dictionary = dicomIO->GetMetaDataDictionary();
  typedef itk::MetaDataObject< std::string > MetaDataStringType;
  DictionaryType::ConstIterator end = dictionary.End();

  double z1, z2;

  ///
  reader->SetFileName( firstSlice.c_str() );
  reader->SetImageIO( dicomIO );
  try {
    reader->Update();
  } catch (itk::ExceptionObject &ex) {
    std::cout << ex << std::endl;
    return EXIT_FAILURE;
  }

  std::string entryId = "0020|0032";
  DictionaryType::ConstIterator tagItr = dictionary.Find( entryId );
  if( tagItr != end ) {
    MetaDataStringType::ConstPointer entryvalue = dynamic_cast<const MetaDataStringType *>(tagItr->second.GetPointer() );
    if( entryvalue ) {
      std::string tagvalue = entryvalue->GetMetaDataObjectValue();
      if( debugDcm  ){
	std::cout << "Image Position (" << entryId <<  ") ";
	std::cout << " is: " << tagvalue << std::endl;
      }
      z1 = extractZCoordFromImagePos(tagvalue);
    }
  }
  
  reader->SetFileName( secondSlice.c_str() );
  reader->SetImageIO( dicomIO );
  try {
    reader->Update();
  } catch (itk::ExceptionObject &ex) {
    std::cout << ex << std::endl;
    return EXIT_FAILURE;
  }

  tagItr = dictionary.Find( entryId );
  if( tagItr != end ) {
    MetaDataStringType::ConstPointer entryvalue = dynamic_cast<const MetaDataStringType *>(tagItr->second.GetPointer() );
    if( entryvalue ) {
      std::string tagvalue = entryvalue->GetMetaDataObjectValue();
       if( debugDcm  ){
	 std::cout << "Image Position (" << entryId <<  ") ";
	 std::cout << " is: " << tagvalue << std::endl;
       }
      z2 = extractZCoordFromImagePos(tagvalue);
    }
  }

  if (z1<z2){
    zPos = true;
  } else zPos = false;

  return 0;
}

int getDirectionCosines(string slicePath, double dirCos[9]){
  // Lecture de l'image dicom ----------------------------
  typedef signed short       PixelType;
  const unsigned int         Dimension = 2;
  typedef itk::Image< PixelType, Dimension >      ImageType;
  typedef itk::ImageFileReader< ImageType >     ReaderType;
  ReaderType::Pointer reader = ReaderType::New();
  typedef itk::GDCMImageIO       ImageIOType;
  ImageIOType::Pointer dicomIO = ImageIOType::New();

  reader->SetFileName( slicePath.c_str() );
  reader->SetImageIO( dicomIO );
  try {
    reader->Update();
  } catch (itk::ExceptionObject &ex) {
    std::cout << ex << std::endl;
    return EXIT_FAILURE;
  }
  // iteration sur les tags dicom --------------------------
   typedef itk::MetaDataDictionary   DictionaryType;
   const  DictionaryType & dictionary = dicomIO->GetMetaDataDictionary();
   typedef itk::MetaDataObject< std::string > MetaDataStringType;
   DictionaryType::ConstIterator itr = dictionary.Begin();
   DictionaryType::ConstIterator end = dictionary.End();

   if (debugDcm) {
     while( itr != end ) {
       itk::MetaDataObjectBase::Pointer  entry = itr->second;
       MetaDataStringType::Pointer entryvalue = dynamic_cast<MetaDataStringType *>( entry.GetPointer() ) ;
       if( entryvalue ) {
	 std::string tagkey   = itr->first;
	 std::string labelId;
	 bool found =  itk::GDCMImageIO::GetLabelFromTag( tagkey, labelId );
	 std::string tagvalue = entryvalue->GetMetaDataObjectValue();
	 if( found ) {
	   std::cout << "(" << tagkey << ") " << labelId;
	   std::cout << " = " << tagvalue.c_str() << std::endl;
	 } else {
	   std::cout << "(" << tagkey <<  ") " << "Unknown";
	   std::cout << " = " << tagvalue.c_str() << std::endl;
	 }
       }
       ++itr;
     }
   }

  std::string entryId = "0020|0037";
  DictionaryType::ConstIterator tagItr = dictionary.Find( entryId );
  if( tagItr != end ) {
    MetaDataStringType::ConstPointer entryvalue = dynamic_cast<const MetaDataStringType *>(tagItr->second.GetPointer() );
    if( entryvalue ) {
      std::string tagvalue = entryvalue->GetMetaDataObjectValue();
      
      if (debugDcm){
	std::cout << "Image Orientation (" << entryId <<  ") ";
	std::cout << " is: " << tagvalue << std::endl;
      }
	
      string::size_type deb = 0;
      string::size_type fin;
      for (int i = 0; i<6 ; i++){
	fin = tagvalue.find("\\",deb);
	//if (debugDcm) std::cout << deb << " -> " << fin <<" : " << tagvalue.substr(deb,fin-deb) << std::endl; 
	dirCos[i] = atof(tagvalue.substr(deb,fin-deb).c_str());
	deb = fin+1;
	if (debugDcm) std::cout << dirCos[i] << std::endl;
      }

    } else std::cout << "The tag (" << entryId << ") isn't available" << std::endl;
  }

  // calcul du produit vectoriel pour avoir les cosinus
  // directeurs du vecteur normal
  dirCos[6] = dirCos[1]*dirCos[5]-dirCos[2]*dirCos[4];
  dirCos[7] = dirCos[2]*dirCos[3]-dirCos[0]*dirCos[5];
  dirCos[8] = dirCos[0]*dirCos[4]-dirCos[1]*dirCos[3];

  return 0;
}



string getMajorAxisFromPatientRelativeDirectionCosine(double x, double y, double z){
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

string makeImageOrientationLabel( double rowX,double rowY,double rowZ,
				  double colX,double colY,double colZ, bool zDir) {
  
  if (debugDcm) {
    std::cout << "rowX : " << rowX << std::endl;
    std::cout << "rowY : " << rowY << std::endl;
    std::cout << "rowZ : " << rowZ << std::endl;
    std::cout << "colX : " << colX << std::endl;
    std::cout << "colY : " << colY << std::endl;
    std::cout << "colZ : " << colZ << std::endl;
    std::cout << "zDir : " << zDir << std::endl;
  }
  
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

string getOrientationFrom2Slices(string slice1, string slice2){
  bool zDirectionPositive = true;
  isZDirectionPositive(slice1, slice2, zDirectionPositive);
  double dirCos [9] = {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
  getDirectionCosines(slice1, dirCos);
  return makeImageOrientationLabel(dirCos[0],dirCos[1],dirCos[2],dirCos[3],dirCos[4],dirCos[5],zDirectionPositive);
}
