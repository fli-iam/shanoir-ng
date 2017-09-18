# Install script for directory: C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg

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
  FILE(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/include/dcmtk/dcmjpeg" TYPE FILE FILES
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/ddpiimpl.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/dipijpeg.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/djcodecd.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/djcodece.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/djcparam.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/djdecabs.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/djdecbas.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/djdecext.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/djdeclol.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/djdecode.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/djdecpro.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/djdecsps.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/djdecsv1.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/djdijg12.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/djdijg16.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/djdijg8.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/djeijg12.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/djeijg16.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/djeijg8.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/djencabs.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/djencbas.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/djencext.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/djenclol.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/djencode.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/djencpro.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/djencsps.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/djencsv1.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/djrplol.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/djrploss.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmjpeg/include/dcmtk/dcmjpeg/djutils.h"
    )
ENDIF(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")

