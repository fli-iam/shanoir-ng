# Install script for directory: C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata

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
  FILE(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/include/dcmtk/dcmdata" TYPE FILE FILES
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/cmdlnarg.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcbytstr.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcchrstr.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dccodec.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcdatset.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcddirif.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcdebug.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcdefine.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcdeftag.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcdicdir.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcdicent.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcdict.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcdirrec.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcelem.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcerror.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcfilefo.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dchashdi.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcistrma.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcistrmb.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcistrmf.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcistrmz.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcitem.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dclist.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcmetinf.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcobject.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcofsetl.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcostrma.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcostrmb.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcostrmf.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcostrmz.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcovlay.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcpcache.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcpixel.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcpixseq.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcpxitem.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcrleccd.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcrlecce.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcrlecp.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcrledec.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcrledrg.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcrleenc.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcrleerg.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcrlerp.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcsequen.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcstack.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcswap.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dctag.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dctagkey.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dctk.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dctypes.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcuid.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcvm.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcvr.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcvrae.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcvras.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcvrat.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcvrcs.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcvrda.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcvrds.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcvrdt.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcvrfd.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcvrfl.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcvris.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcvrlo.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcvrlt.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcvrobow.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcvrof.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcvrpn.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcvrpobw.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcvrsh.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcvrsl.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcvrss.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcvrst.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcvrtm.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcvrui.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcvrul.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcvrulup.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcvrus.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcvrut.h"
    "C:/soft/dcmtk-3.5.4-src/dcmtk-3.5.4/dcmdata/include/dcmtk/dcmdata/dcxfer.h"
    )
ENDIF(NOT CMAKE_INSTALL_COMPONENT OR "${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified")

