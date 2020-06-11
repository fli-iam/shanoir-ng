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
 *  Purpose: DicomColorScaleTemplate (Header)
 *
 *  Last Update:      $Author: meichel $
 *  Update Date:      $Date: 2005/12/08 16:01:37 $
 *  CVS/RCS Revision: $Revision: 1.16 $
 *  Status:           $State: Exp $
 *
 *  CVS/RCS Log at end of file
 *
 */


#ifndef DICOSCT_H
#define DICOSCT_H

#include "dcmtk/config/osconfig.h"
#include "dcmtk/dcmdata/dctypes.h"

#include "dcmtk/dcmimage/dicopx.h"
#include "dcmtk/dcmimgle/discalet.h"


/*---------------------*
 *  class declaration  *
 *---------------------*/

/** Template class to scale color images (on pixel data level).
 */
template<class T>
class DiColorScaleTemplate
  : public DiColorPixelTemplate<T>,
    protected DiScaleTemplate<T>
{

 public:

    /** constructor
     *
     ** @param  pixel        pointer to intermediate pixel representation
     *  @param  columns      width of source image
     *  @param  rows         height of source image
     *  @param  left_pos     left coordinate of clipping area
     *  @param  top_pos      top coordinate of clipping area
     *  @param  src_cols     width of clipping area
     *  @param  src_rows     height of clipping area
     *  @param  dest_cols    width of destination image (scaled image)
     *  @param  dest_rows    height of destination image
     *  @param  frames       number of frames
     *  @param  bits         number of bits per plane/pixel
     *  @param  interpolate  use of interpolation when scaling
     */
    DiColorScaleTemplate(const DiColorPixel *pixel,
                         const Uint16 columns,
                         const Uint16 rows,
                         const signed long left_pos,
                         const signed long top_pos,
                         const Uint16 src_cols,
                         const Uint16 src_rows,
                         const Uint16 dest_cols,
                         const Uint16 dest_rows,
                         const Uint32 frames,
                         const int bits,
                         const int interpolate)
      : DiColorPixelTemplate<T>(pixel, OFstatic_cast(unsigned long, dest_cols) * OFstatic_cast(unsigned long, dest_rows) * frames),
        DiScaleTemplate<T>(3, columns, rows, left_pos, top_pos, src_cols, src_rows, dest_cols, dest_rows, frames, bits)
   {
        if ((pixel != NULL) && (pixel->getCount() > 0))
        {
            if (pixel->getCount() == OFstatic_cast(unsigned long, columns) * OFstatic_cast(unsigned long, rows) * frames)
                scale(OFstatic_cast(const T **, OFconst_cast(void *, pixel->getData())), interpolate);
            else {
                if (DicomImageClass::checkDebugLevel(DicomImageClass::DL_Warnings))
                {
                   ofConsole.lockCerr() << "WARNING: could not scale image ... corrupted data." << endl;
                   ofConsole.unlockCerr();
                }
            }
        }
    }

    /** destructor
     */
    virtual ~DiColorScaleTemplate()
    {
    }


 private:

    /** scale pixel data
     *
     ** @param  pixel        pointer to pixel data (3 components9 to be scaled
     *  @param  interpolate  use of interpolation when scaling
     */
    inline void scale(const T *pixel[3],
                      const int interpolate)
    {
        if (Init(pixel))
            scaleData(pixel, this->Data, interpolate);
    }
};


#endif


/*
 *
 * CVS/RCS Log:
 * $Log: dicosct.h,v $
 * Revision 1.16  2005/12/08 16:01:37  meichel
 * Changed include path schema for all DCMTK header files
 *
 * Revision 1.15  2004/04/21 10:00:31  meichel
 * Minor modifications for compilation with gcc 3.4.0
 *
 * Revision 1.14  2004/02/06 11:18:18  joergr
 * Distinguish more clearly between const and non-const access to pixel data.
 *
 * Revision 1.13  2003/12/23 11:45:54  joergr
 * Adapted type casts to new-style typecast operators defined in ofcast.h.
 * Removed leading underscore characters from preprocessor symbols (reserved
 * symbols). Updated copyright header.
 *
 * Revision 1.12  2002/12/09 13:37:52  joergr
 * Renamed parameter/local variable to avoid name clashes with global
 * declaration left and/or right (used for as iostream manipulators).
 *
 * Revision 1.11  2001/11/09 16:45:23  joergr
 * Updated/Enhanced comments.
 *
 * Revision 1.10  2001/06/01 15:49:30  meichel
 * Updated copyright header
 *
 * Revision 1.9  2000/12/08 14:06:02  joergr
 * Added new checking routines to avoid crashes when processing corrupted image
 * data.
 *
 * Revision 1.8  2000/03/08 16:21:52  meichel
 * Updated copyright header.
 *
 * Revision 1.7  1999/08/25 16:58:06  joergr
 * Added new feature: Allow clipping region to be outside the image
 * (overlapping).
 *
 * Revision 1.6  1999/04/28 12:52:00  joergr
 * Corrected some typos, comments and formatting.
 *
 * Revision 1.5  1998/11/27 13:51:07  joergr
 * Added copyright message.
 *
 * Revision 1.4  1998/05/11 14:53:14  joergr
 * Added CVS/RCS header to each file.
 *
 *
 */
