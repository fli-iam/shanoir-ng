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
 *  Purpose: DicomMonochromeCopyTemplate (Header)
 *
 *  Last Update:      $Author: meichel $
 *  Update Date:      $Date: 2005/12/08 16:47:48 $
 *  CVS/RCS Revision: $Revision: 1.11 $
 *  Status:           $State: Exp $
 *
 *  CVS/RCS Log at end of file
 *
 */


#ifndef DIMOCPT_H
#define DIMOCPT_H

#include "dcmtk/config/osconfig.h"
#include "dcmtk/dcmdata/dctypes.h"
#include "dcmtk/ofstd/ofbmanip.h"
#include "dcmtk/ofstd/ofcast.h"

#include "dcmtk/dcmimgle/dimopxt.h"


/*---------------------*
 *  class declaration  *
 *---------------------*/

/** Template class to copy monochrome pixel data
 */
template<class T>
class DiMonoCopyTemplate
  : public DiMonoPixelTemplate<T>
{

 public:

    /** constructor
     *
     ** @param  pixel   pointer to monochrome intermediate representation of pixel data
     *  @param  fstart  first frame to be copied
     *  @param  fcount  number of frames to be copied
     *  @param  fsize   size of one frame (in bytes)
     */
    DiMonoCopyTemplate(const DiMonoPixel *pixel,
                       const unsigned long fstart,
                       const unsigned long fcount,
                       const unsigned long fsize)
      : DiMonoPixelTemplate<T>(pixel, fcount * fsize)
    {
        if ((pixel != NULL) && (pixel->getCount() > 0))
        {
            if ((pixel->getCount() > fstart * fsize) && (pixel->getCount() >= (fstart + fcount) * fsize))
                copy(OFstatic_cast(const T *, pixel->getData()) + fstart * fsize);
        }
    }

    /** destructor
     */
    ~DiMonoCopyTemplate()
    {
    }


 private:

    /** copy specified amount of pixel data
     *
     ** @param  pixel  array of pixel data to be copied
     */
    inline void copy(const T *pixel)
    {
        if (pixel != NULL)
        {
            this->Data = new T[this->getCount()];
            if (this->Data != NULL)
                OFBitmanipTemplate<T>::copyMem(pixel, this->Data, this->getCount());
        }
    }
};


#endif


/*
 *
 * CVS/RCS Log:
 * $Log: dimocpt.h,v $
 * Revision 1.11  2005/12/08 16:47:48  meichel
 * Changed include path schema for all DCMTK header files
 *
 * Revision 1.10  2004/04/21 10:00:36  meichel
 * Minor modifications for compilation with gcc 3.4.0
 *
 * Revision 1.9  2003/12/08 18:26:50  joergr
 * Adapted type casts to new-style typecast operators defined in ofcast.h.
 * Removed leading underscore characters from preprocessor symbols (reserved
 * symbols). Updated copyright header.
 *
 * Revision 1.8  2001/06/01 15:49:44  meichel
 * Updated copyright header
 *
 * Revision 1.7  2000/09/12 10:04:44  joergr
 * Corrected bug: wrong parameter for attribute search routine led to crashes
 * when multiple pixel data attributes were contained in the dataset (e.g.
 * IconImageSequence). Added new checking routines to avoid crashes when
 * processing corrupted image data.
 *
 * Revision 1.6  2000/03/08 16:24:18  meichel
 * Updated copyright header.
 *
 * Revision 1.5  1999/09/17 12:23:56  joergr
 * Added/changed/completed DOC++ style comments in the header files.
 *
 * Revision 1.4  1999/04/28 14:49:19  joergr
 * Changed comments/formatting.
 *
 * Revision 1.2  1999/02/11 16:01:56  joergr
 * Corrected some typos and formatting.
 *
 * Revision 1.1  1998/11/27 15:15:50  joergr
 * Added copyright message.
 * Introduced new method and corresponding classes to create a copy of a
 * dcmimage object (optional parameters: frame start and count).
 *
 *
 */
