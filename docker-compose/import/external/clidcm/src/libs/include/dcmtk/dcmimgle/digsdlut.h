/*
 *
 *  Copyright (C) 1996-2005, OFFIS
 *
 *  This software and supporting documentation were developed by
 *
 *    Kuratorium OFFIS e.V.
 *    Healthcare Information and Communication Systems
 *    Escherweg 2
 *    D-26121 Oldenburg, Germany
 *
 *  THIS SOFTWARE IS MADE AVAILABLE,  AS IS,  AND OFFIS MAKES NO  WARRANTY
 *  REGARDING  THE  SOFTWARE,  ITS  PERFORMANCE,  ITS  MERCHANTABILITY  OR
 *  FITNESS FOR ANY PARTICULAR USE, FREEDOM FROM ANY COMPUTER DISEASES  OR
 *  ITS CONFORMITY TO ANY SPECIFICATION. THE ENTIRE RISK AS TO QUALITY AND
 *  PERFORMANCE OF THE SOFTWARE IS WITH THE USER.
 *
 *  Module:  dcmimgle
 *
 *  Author:  Joerg Riesmeier
 *
 *  Purpose: DicomGSDFLUT (Header)
 *
 *  Last Update:      $Author: meichel $
 *  Update Date:      $Date: 2005/12/08 16:47:41 $
 *  CVS/RCS Revision: $Revision: 1.11 $
 *  Status:           $State: Exp $
 *
 *  CVS/RCS Log at end of file
 *
 */


#ifndef DIGSDLUT_H
#define DIGSDLUT_H

#include "dcmtk/config/osconfig.h"

#include "dcmtk/dcmimgle/didislut.h"


/*---------------------*
 *  class declaration  *
 *---------------------*/

/** Class to compute and store the GSDF lookup table
 */
class DiGSDFLUT
  : public DiDisplayLUT
{

 public:

    /** constructor
     *
     ** @param  count      number of values to be stored in the LUT
     *  @param  max        maximum value to be stored in the LUT
     *  @param  ddl_tab    array of DDL values
     *  @param  val_tab    array of values
     *  @param  ddl_cnt    number of DDL values
     *  @param  gsdf_tab   array with Grayscale Standard Display Function
     *  @param  gsdf_spl   array with helper function used for interpolation
     *  @param  gsdf_cnt   number of values in GSDF array
     *  @param  jnd_min    minimum JND index value
     *  @param  jnd_max    maximum JND index value
     *  @param  lum_min    minimum luminance value to be used (lower border)
     *  @param  lum_max    maximum luminance value to be used (upper border)
     *  @param  amb        (reflected) ambient light value
     *  @param  illum      illumination value
     *  @param  inverse    apply inverse transformation if OFTrue
     *  @param  stream     output stream (used to write curve data to a file)
     *  @param  printMode  write CC and PSC to stream if OFTrue
     */
    DiGSDFLUT(const unsigned long count,
              const Uint16 max,
              const Uint16 *ddl_tab,
              const double *val_tab,
              const unsigned long ddl_cnt,
              const double *gsdf_tab,
              const double *gsdf_spl,
              const unsigned int gsdf_cnt,
              const double jnd_min,
              const double jnd_max,
              const double lum_min,
              const double lum_max,
              const double amb,
              const double illum,
              const OFBool inverse = OFFalse,
              ostream *stream = NULL,
              const OFBool printMode = OFTrue);

    /** destructor
     */
    virtual ~DiGSDFLUT();


 protected:

    /** create lookup table
     *
     ** @param  ddl_tab    array of DDL values
     *  @param  val_tab    array of luminance values
     *  @param  ddl_cnt    number of DDL values
     *  @param  gsdf_tab   array with Grayscale Standard Display Function
     *  @param  gsdf_spl   array with helper function used for interpolation
     *  @param  gsdf_cnt   number of values in GSDF array
     *  @param  jnd_min    minimum JND index value
     *  @param  jnd_max    maximum JND index value
     *  @param  lum_min    minimum luminance value to be used (lower border)
     *  @param  lum_max    maximum luminance value to be used (upper border)
     *  @param  inverse    apply inverse transformation if OFTrue
     *  @param  stream     output stream (used to write curve data to a file)
     *  @param  printMode  write CC and PSC to stream if OFTrue
     *
     ** @return status, true if successful, false otherwise
     */
    int createLUT(const Uint16 *ddl_tab,
                  const double *val_tab,
                  const unsigned long ddl_cnt,
                  const double *gsdf_tab,
                  const double *gsdf_spl,
                  const unsigned int gsdf_cnt,
                  const double jnd_min,
                  const double jnd_max,
                  const double lum_min,
                  const double lum_max,
                  const OFBool inverse = OFFalse,
                  ostream *stream = NULL,
                  const OFBool printMode = OFTrue);
};


#endif


/*
 *
 * CVS/RCS Log:
 * $Log: digsdlut.h,v $
 * Revision 1.11  2005/12/08 16:47:41  meichel
 * Changed include path schema for all DCMTK header files
 *
 * Revision 1.10  2003/12/08 18:21:48  joergr
 * Removed leading underscore characters from preprocessor symbols (reserved
 * symbols). Updated CVS header.
 *
 * Revision 1.9  2003/06/12 15:08:34  joergr
 * Fixed inconsistent API documentation reported by Doxygen.
 *
 * Revision 1.8  2003/02/11 10:01:14  joergr
 * Added support for Dmin/max to calibration routines (required for printers).
 *
 * Revision 1.7  2002/07/18 12:30:26  joergr
 * Added support for hardcopy and softcopy input devices (camera and scanner).
 *
 * Revision 1.6  2002/07/02 16:23:42  joergr
 * Added support for hardcopy devices to the calibrated output routines.
 *
 * Revision 1.5  2001/06/01 15:49:41  meichel
 * Updated copyright header
 *
 * Revision 1.4  2000/03/08 16:24:16  meichel
 * Updated copyright header.
 *
 * Revision 1.3  1999/10/18 15:05:52  joergr
 * Enhanced command line tool dcmdspfn (added new options).
 *
 * Revision 1.2  1999/09/17 12:11:32  joergr
 * Added/changed/completed DOC++ style comments in the header files.
 *
 * Revision 1.1  1999/09/10 08:50:24  joergr
 * Added support for CIELAB display function. Restructured class hierarchy
 * for display functions.
 *
 */
