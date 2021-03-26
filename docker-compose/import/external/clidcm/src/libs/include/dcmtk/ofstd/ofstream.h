/*
 *
 *  Copyright (C) 2002-2005, OFFIS
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
 *  Module:  ofstd
 *
 *  Author:  Andreas Barth
 *
 *  Purpose: C++ header to handle standard and old stream libraries.
 *
 *  Last Update:      $Author: meichel $
 *  Update Date:      $Date: 2005/12/08 16:06:06 $
 *  CVS/RCS Revision: $Revision: 1.7 $
 *  Status:           $State: Exp $
 *
 *  CVS/RCS Log at end of file
 *
 */


#ifndef OFSTREAM_H
#define OFSTREAM_H

#include "dcmtk/config/osconfig.h"

#ifdef USE_STD_CXX_INCLUDES

#include <iostream>
#ifdef HAVE_IOS
#include <ios>
#endif
#include <fstream>
#include <iomanip>
// For standard STREAMS library: preference for standard stringstream
#if defined(HAVE_SSTREAM)
#include <sstream>
#define USE_STRINGSTREAM
#elif defined(HAVE_STRSTREAM)
#include <strstream>
#else
#error DCMTK needs stringstream or strstream type
#endif
#ifdef HAVE_STD_NAMESPACE
namespace std { }
using namespace std;
#endif

#else /* USE_STD_CXX_INCLUDES */

#include <iostream.h>
#include <fstream.h>
// For old STREAMS library: preference for strstream
#if defined(HAVE_STRSTREA_H) || defined(HAVE_STRSTREAM_H)
#ifdef HAVE_STRSTREA_H
#include <strstrea.h>
#else
#include <strstream.h>
#endif
#elif defined(HAVE_SSTREAM_H)
#include <sstream.h>
#define USE_STRINGSTREAM
#else
#error DCMTK needs stringstream or strstream type
#endif
#include <iomanip.h>

#endif

#ifdef USE_STRINGSTREAM

typedef stringstream OFStringStream;
typedef ostringstream OFOStringStream;
typedef istringstream OFIStringStream;

#define OFStringStream_ends ""
#define OFSTRINGSTREAM_GETOFSTRING(oss, string) \
    OFString string((oss).str().c_str());
// The following two macros define a block structure. Please note that variables
// declared between xxx_GETSTR and xxx_FREESTR are only valid within this scope.
#define OFSTRINGSTREAM_GETSTR(oss, chptr) \
{ \
    string chptr##__ = (oss).str(); \
    const char *chptr = chptr##__.c_str();
#define OFSTRINGSTREAM_FREESTR(chptr) \
}

#else /* USE_STRINGSTREAM */

typedef strstream OFStringStream;
typedef ostrstream OFOStringStream;
typedef istrstream OFIStringStream;

#define OFStringStream_ends ends
#define OFSTRINGSTREAM_GETOFSTRING(oss, string) \
    char * string##__ = (oss).str(); \
    OFString string(string##__); \
    delete[] string##__;
// The following two macros define a block structure. Please note that variables
// declared between xxx_GETSTR and xxx_FREESTR are only valid within this scope.
#define OFSTRINGSTREAM_GETSTR(oss, chptr) \
{ \
    const char *chptr = (oss).str();
#define OFSTRINGSTREAM_FREESTR(chptr) \
    delete[] (char *)chptr; \
}

#endif /* USE_STRINGSTREAM */

#endif /* USE_STD_CXX_INCLUDES */


/*
 * CVS/RCS Log:
 * $Log: ofstream.h,v $
 * Revision 1.7  2005/12/08 16:06:06  meichel
 * Changed include path schema for all DCMTK header files
 *
 * Revision 1.6  2004/05/07 10:46:32  meichel
 * Removed unneeded semicolon, reported by gcc 3.4
 *
 * Revision 1.5  2004/01/16 10:30:12  joergr
 * Removed acknowledgements with e-mail addresses from CVS log.
 *
 * Revision 1.4  2003/12/05 10:37:41  joergr
 * Removed leading underscore characters from preprocessor symbols (reserved
 * symbols). Updated copyright date where appropriate.
 *
 * Revision 1.3  2002/12/11 15:54:48  meichel
 * Added empty namespace std declaration, needed on MSVC.
 *
 * Revision 1.2  2002/05/02 14:05:50  joergr
 * Added support for standard and non-standard string streams (which one is
 * supported is detected automatically via the configure mechanism).
 *
 * Revision 1.1  2002/04/16 13:36:03  joergr
 * Added configurable support for C++ ANSI standard includes (e.g. streams).
 *
 */
