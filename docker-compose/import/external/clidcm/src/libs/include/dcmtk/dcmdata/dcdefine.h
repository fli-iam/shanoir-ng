/*
 *
 *  Copyright (C) 1994-2005, OFFIS
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
 *  Module:  dcmdata
 *
 *  Author:  Andreas Barth
 *
 *  Purpose: common defines for configuration
 *
 *  Last Update:      $Author: meichel $
 *  Update Date:      $Date: 2005/12/08 16:28:05 $
 *  Source File:      $Source: /share/dicom/cvs-depot/dcmtk/dcmdata/include/dcmtk/dcmdata/dcdefine.h,v $
 *  CVS/RCS Revision: $Revision: 1.8 $
 *  Status:           $State: Exp $
 *
 *  CVS/RCS Log at end of file
 *
 */

#ifndef DCDEFINE_H
#define DCDEFINE_H

#include "dcmtk/config/osconfig.h"    /* make sure OS specific configuration is included first */

#define INCLUDE_CSTRING
#include "dcmtk/ofstd/ofstdinc.h"

/* memzero */
#ifdef HAVE_MEMSET
#  undef memzero
#  define memzero(d, n) memset((d), 0, (n))
#else
#  ifdef HAVE_BZERO
#    undef memzero
#    define memzero(d, n) bzero((d), (n))
#  endif
#endif

/* memcpy */
#ifndef HAVE_MEMCPY
#  ifdef HAVE_BCOPY
#    undef memcpy
#    define memcpy(d, s, n) bcopy((s), (d), (n))
#  endif
#endif

/* memmove */
#ifndef HAVE_MEMMOVE
#  ifdef HAVE_BCOPY
#    undef memmove
#    define memmove(d, s, n) bcopy ((s), (d), (n))
#  endif
#endif

/* memcmp */
#ifndef HAVE_MEMCMP
#  ifdef HAVE_BCMP
#    undef memcmp
#    define memcmp(d, s, n) bcmp((s), (d), (n))
#  endif
#endif

/* strchr, strrchr */
#ifndef HAVE_STRCHR
#  ifdef HAVE_INDEX
#    undef strchr
#    define strchr index
#    undef strrchr
#    define strrchr rindex
#  endif
#endif

#endif

/*
 * CVS/RCS Log:
 * $Log: dcdefine.h,v $
 * Revision 1.8  2005/12/08 16:28:05  meichel
 * Changed include path schema for all DCMTK header files
 *
 * Revision 1.7  2002/11/27 12:07:21  meichel
 * Adapted module dcmdata to use of new header file ofstdinc.h
 *
 * Revision 1.6  2001/06/01 15:48:35  meichel
 * Updated copyright header
 *
 * Revision 1.5  2000/03/08 16:26:12  meichel
 * Updated copyright header.
 *
 * Revision 1.4  1999/03/31 09:24:33  meichel
 * Updated copyright header in module dcmdata
 *
 *
 */
