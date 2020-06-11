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
 *  Module:  dcmimage
 *
 *  Author:  Joerg Riesmeier
 *
 *  Purpose: DicomHSVImage (Header)
 *
 *  Last Update:      $Author: meichel $
 *  Update Date:      $Date: 2005/12/08 16:01:38 $
 *  CVS/RCS Revision: $Revision: 1.9 $
 *  Status:           $State: Exp $
 *
 *  CVS/RCS Log at end of file
 *
 */


#ifndef DIHSVIMG_H
#define DIHSVIMG_H

#include "dcmtk/config/osconfig.h"

#include "dcmtk/dcmimage/dicoimg.h"


/*---------------------*
 *  class declaration  *
 *---------------------*/

/** Class for HSV images
 */
class DiHSVImage
  : public DiColorImage
{

 public:

    /** constructor
     *
     ** @param  docu    pointer to dataset (encapsulated)
     *  @param  status  current image status
     */
    DiHSVImage(const DiDocument *docu,
               const EI_Status status);

    /** destructor
     */
    virtual ~DiHSVImage();
};


#endif


/*
 *
 * CVS/RCS Log:
 * $Log: dihsvimg.h,v $
 * Revision 1.9  2005/12/08 16:01:38  meichel
 * Changed include path schema for all DCMTK header files
 *
 * Revision 1.8  2003/12/17 18:11:15  joergr
 * Removed leading underscore characters from preprocessor symbols (reserved
 * symbols).
 *
 * Revision 1.7  2001/11/09 16:45:59  joergr
 * Updated/Enhanced comments.
 *
 * Revision 1.6  2001/06/01 15:49:30  meichel
 * Updated copyright header
 *
 * Revision 1.5  2000/03/08 16:21:52  meichel
 * Updated copyright header.
 *
 * Revision 1.4  1999/04/28 12:52:00  joergr
 * Corrected some typos, comments and formatting.
 *
 * Revision 1.3  1998/11/27 13:51:30  joergr
 * Added copyright message.
 *
 * Revision 1.2  1998/05/11 14:53:15  joergr
 * Added CVS/RCS header to each file.
 *
 *
 */
