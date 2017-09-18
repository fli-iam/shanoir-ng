# Install script for directory: C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/ofstd/include/dcmtk/ofstd

# Set the install prefix
IF(NOT DEFINED CMAKE_INSTALL_PREFIX)
  SET(CMAKE_INSTALL_PREFIX "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/../dcmtk-3.5.4-win32-i386")
ENDIF(NOT DEFINED CMAKE_INSTALL_PREFIX)
STRING(REGEX REPLACE "/$" "" CMAKE_INSTALL_PREFIX "${CMAKE_INSTALL_PREFIX}")

# Set the install configuration name.
IF(NOT DEFINED CMAKE_INSTALL_CONFIG_NAME)
  IF(BUILD_TYPE)
    STRING(REGEX REPLACE "^[^A-Za-z0-9_]+" ""
           CMAKE_INSTALL_CONFIG_NAME "${BUILD_TYPE}")
  ELSE(BUILD_TYPE)
    SET(CMAKE_INSTALL_CONFIG_NAME "RELEASE")
  ENDIF(BUILD_TYPE)
  MESSAGE(STATUS "Install configuration: \"${CMAKE_INSTALL_CONFIG_NAME}\"")
ENDIF(NOT DEFINED CMAKE_INSTALL_CONFIG_NAME)

# Set the component getting installed.
IF(NOT CMAKE_INSTALL_COMPONENT)
  IF(COMPONENT)
    MESSAGE(STATUS "Install component: \"${COMPONENT}\"")
    SET(CMAKE_INSTALL_COMPONENT "${COMPONENT}")
  ELSE(COMPONENT)
    SET(CMAKE_INSTALL_COMPONENT)
  ENDIF(COMPONENT)
ENDIF(NOT CMAKE_INSTALL_COMPONENT)

IF(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")
  FILE(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/include/dcmtk/ofstd" TYPE FILE FILES
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/ofstd/include/dcmtk/ofstd/ofalgo.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/ofstd/include/dcmtk/ofstd/ofbmanip.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/ofstd/include/dcmtk/ofstd/ofcast.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/ofstd/include/dcmtk/ofstd/ofcmdln.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/ofstd/include/dcmtk/ofstd/ofconapp.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/ofstd/include/dcmtk/ofstd/ofcond.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/ofstd/include/dcmtk/ofstd/ofconfig.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/ofstd/include/dcmtk/ofstd/ofconsol.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/ofstd/include/dcmtk/ofstd/ofcrc32.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/ofstd/include/dcmtk/ofstd/ofdate.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/ofstd/include/dcmtk/ofstd/ofdatime.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/ofstd/include/dcmtk/ofstd/offname.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/ofstd/include/dcmtk/ofstd/ofglobal.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/ofstd/include/dcmtk/ofstd/oflist.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/ofstd/include/dcmtk/ofstd/oflogfil.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/ofstd/include/dcmtk/ofstd/ofoset.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/ofstd/include/dcmtk/ofstd/ofset.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/ofstd/include/dcmtk/ofstd/ofsetit.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/ofstd/include/dcmtk/ofstd/ofstack.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/ofstd/include/dcmtk/ofstd/ofstd.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/ofstd/include/dcmtk/ofstd/ofstdinc.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/ofstd/include/dcmtk/ofstd/ofstream.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/ofstd/include/dcmtk/ofstd/ofstring.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/ofstd/include/dcmtk/ofstd/ofthread.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/ofstd/include/dcmtk/ofstd/oftime.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/ofstd/include/dcmtk/ofstd/oftimer.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/ofstd/include/dcmtk/ofstd/oftypes.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/ofstd/include/dcmtk/ofstd/ofuoset.h"
    )
ENDIF(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")

