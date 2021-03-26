/*
 ** mydcmview.h
 ** Login : <wiest@localhost.localdomain>
 ** Started on  Tue Jul 19 09:43:56 2005 Nicolas Wiest
 ** $Id$
 **
 ** Copyright (C) 2005 Nicolas Wiest
 ** This program is free software; you can redistribute it and/or modify
 ** it under the terms of the GNU General Public License as published by
 ** the Free Software Foundation; either version 2 of the License, or
 ** (at your option) any later version.
 **
 ** This program is distributed in the hope that it will be useful,
 ** but WITHOUT ANY WARRANTY; without even the implied warranty of
 ** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 ** GNU General Public License for more details.
 **
 ** You should have received a copy of the GNU General Public License
 ** along with this program; if not, write to the Free Software
 ** Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

#ifndef   	MYDCMVIEW_H_
# define   	MYDCMVIEW_H_

#include <sstream>
#include <algorithm>
#include <cstdlib>
#include <cassert>

#include <QtDebug>
#include <QDir>
#include <QFileInfo>
//#include <QTreeWidget>
//#include <QTreeWidgetItem>

//#include <QStandardItemModel>
//#include <QStandardItem>

#include "ui_dicomfile.h"
#undef SIZEOF_LONG
#define SIZEOF_LONG 16

// From Dcmtk:
#include <dcmtk/config/osconfig.h>    /* make sure OS specific configuration is included first */
#include <dcmtk/ofstd/ofstream.h>
#include <dcmtk/dcmdata/dctk.h>
#include <dcmtk/dcmdata/dcdebug.h>
#include <dcmtk/dcmdata/cmdlnarg.h>
#include <dcmtk/ofstd/ofconapp.h>
#include <dcmtk/dcmdata/dcuid.h>       /* for dcmtk version name */
#include <dcmtk/dcmdata/dcistrmz.h>    /* for dcmZlibExpectRFC1950Encoding */
// For dcm images
#include <dcmtk/dcmimgle/dcmimage.h>
// For color images
#include <dcmtk/dcmimage/diregist.h>

//#define INCLUDE_CSTDLIB
//#define INCLUDE_CSTRING
#include "dcmtk/ofstd/ofstdinc.h"

#ifdef WITH_ZLIB
#include <zlib.h>        /* for zlibVersion() */
#endif

#include <map>
#include <cstdio>

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#include "newmat.h"
#include "Image3D.hh"

#include "DcmCSAHeader.hh"

#include "dicomOrientation.hh"
#include "Serializers.hh"

// NiftiSerializers.hh

extern std::string destdir;
extern std::string keepAcq;

using namespace vistal;

class Dicomfile
{
public:
    // Specific Constructs for file selection
    /*   QGraphicsScene      *scene; */
    /*   QGraphicsPixmapItem *gitm; */
    typedef std::list<DcmItem*> ImList;
    typedef std::map<DcmItem*, ImList> SequenceMap;
    typedef std::map<DcmItem*, SequenceMap> SeriesMap;
    //typedef std::map<DcmItem*, ImList> SeriesMap;
    typedef std::map<DcmItem*, SeriesMap> StudyMap;
    typedef std::map<DcmItem*, StudyMap> PatientStudiesMap;

    /*
//Philips - must set 2001,105F length to 8 - allows reading of stack sequence
//  2005,1071. Philips AP angulation
//  2005,1072. Philips RL angulation
//  2005,1073. Philips FH angulation
//2001,100B Philips slice orientation (TRANSVERSAL, AXIAL, SAGITTAL)
*/


private:
    QString                lastdir;
    QStringListIterator*   dicomImagePath;
    QStringList		 ImagesList;
    std::list<QStringList> Images4DList;
    QString                basePath;
    //  bool rightclick;


    QString toText(DcmObject* obj)
    {
        // Convert a DcmObject to Text
        QString result;
        if (!obj) return result;

        OFString str;
        DcmElement *elt = dynamic_cast<DcmElement*>(obj);
        if (elt)
        {
            elt->getOFStringArray(str);
            result = str.c_str();
        }

        DcmTag t = obj->getTag(); OFString name(t.getTagName());
        if (name == "CSAImageHeaderInfo")
        {
            CSAHeader header(*elt);
            header.dump();

        }
        return result;
    }



public:
    Dicomfile(): dicomImagePath(NULL)//, image(NULL)//, rightclick(false)
    {
    }

    ~Dicomfile()
    {
        if (dicomImagePath) delete dicomImagePath;
        //    delete scene;
    }

    // Performs a recursive directory discovery, allows for splitted dicom directories
    // like the one found from siemens console written files...
    void recursiveListing(QFileInfoList& list, QDir current)
    {
        current.setFilter(QDir::Files | QDir::NoSymLinks);
        list += current.entryInfoList();


        QDir up = current;
        up.cdUp();
        current.setFilter(QDir::Dirs | QDir::NoSymLinks);
        QFileInfoList t = current.entryInfoList();
        for (int i = 0; i < t.size();++i)
        {
            QString tmp = t.at(i).path();
       //     std::cout << tmp <<  t.at(i).path() <<  t.at(i).absoluteFilePath();

            if (tmp != current.path() && tmp != up.path()) // Avoid staying at the same level or leveling up...
                recursiveListing(list, tmp);
        }
    }

    // load a directory full of dicom files
    void loaddir(QString dirname)
    {
        std::cout << "Reading dicom files from directory " << dirname.toAscii().data() << std::endl;
        DicomImageClass::setDebugLevel(DicomImageClass::DL_Warnings | DicomImageClass::DL_Errors);

        // Renders UID in a human readable form
        std::map<QString, QString> human_readable;
        // If multiple series of the same name exists we need to perform some "renaming"
        std::map<QString, int> series_exists;
        // Associate files to the corresponding serie
        std::map<QString, QStringList> SeriesFiles;
        // Associate Series with corresponding patient and Study
        std::map<QString, QStringList> StudySeriesPatients;
        std::map<QString, std::map<double, QStringList> > SerieMultiVolume;
        //std::map<QString, std::map<float, QString>
        // We need to somehow handle the 4D images

        // For every files in the directory

        QFileInfoList list;


        QDir dir(dirname);
        recursiveListing(list, dir.absolutePath());
        std::cout << list.size() << std::endl;

        //     std::cout << "     Bytes Filename" << std::endl;
        //QProgressDialog progress("Reading Dicom data...", "Cancel", 0, list.size(), this);
        for (int i = 0; i < list.size(); ++i)
        {
            //  progress.setValue(i); app->processEvents();
            //  if (progress.wasCanceled()) break;
            std::cout << "."; std::flush(std::cout);

            QFileInfo fileInfo = list.at(i);


            QStringList PatientStudy;
            DcmFileFormat dfile;

            //            qDebug() << fileInfo.absoluteFilePath();
            if (!dfile.loadFile(fileInfo.absoluteFilePath().toLatin1()).good()) continue;
            DcmDataset* obj = dfile.getDataset();
            OFString uid;
            DcmElement* elt;

            if (!obj->findAndGetElement(DCM_PhotometricInterpretation, elt).good())
                continue; // Skip non Image data...

            if (obj->findAndGetOFString(DCM_StudyInstanceUID, uid).good())
            {
                QString t(uid.c_str()); t.replace("^"," ").replace("  ", " ").replace("/","_");
                if (obj->findAndGetOFString(DCM_StudyDescription, uid).good())
                {
                    QString t2(uid.c_str()); t2.replace("^"," ").replace("  ", " ").replace("/","_");
                    if (human_readable.find(t) == human_readable.end() )
                        human_readable[t] = t2;
                }
                PatientStudy << t;
            }
            else if (obj->findAndGetOFString(DCM_StudyDescription, uid).good())
            {
                QString t(uid.c_str()); t.replace("^"," ").replace("  ", " ").replace("/","_");
                PatientStudy << t;
            }

            if (obj->findAndGetOFString(DCM_PatientID, uid).good())
            {
                QString t(uid.c_str());t.replace("^"," ").replace("  ", " ").replace("/","_");
                if (obj->findAndGetOFString(DCM_PatientsName, uid).good())
                {
                    QString t2(uid.c_str()); t2.replace("^"," ").replace("  ", " ").replace("/","_");
                    if (human_readable.find(t) == human_readable.end() )
                        human_readable[t] = t2;
                }
                PatientStudy << t;
            }
            else
                if (obj->findAndGetOFString(DCM_PatientsName, uid).good())
                {
                QString t(uid.c_str());t.replace("^"," ").replace("  ", " ").replace("/","_");
                PatientStudy << t;
            }
            QString curSerie;
            if (obj->findAndGetOFString(DCM_SeriesInstanceUID, uid).good())
            {
                QString t(uid.c_str()); t.replace("^"," ").replace("  ", " ").replace("/","_");

                if (obj->findAndGetOFString(DCM_SeriesDescription, uid).good())
                {
                    QString t2(uid.c_str()); t2.replace("^"," ").replace("  ", " ").replace("/","_");
                    if (human_readable.find(t) == human_readable.end())
                    { // Rename multiple data
                        //                        qDebug() << t2 << t;
                        if (series_exists[t2] == 0)
                        {
                            human_readable[t] = t2;
                            series_exists[t2]++;
                        }
                        else
                        {
                            series_exists[t2]++;
                            t2 = QString("%1_%2").arg(t2).arg((char)('@'+series_exists[t2]));
                            human_readable[t] = t2;
                            //qDebug() << t2 << t;
                        }
                    }
                }

                StudySeriesPatients[t] = PatientStudy;
                SeriesFiles[t] << fileInfo.absoluteFilePath();
                curSerie = t;
            }
            else if (obj->findAndGetOFString(DCM_SeriesDescription, uid).good())
            {
                QString t(uid.c_str()); t.replace("^"," ").replace("  ", " ").replace("/","_");
                StudySeriesPatients[t] = PatientStudy;
                SeriesFiles[t] << fileInfo.absoluteFilePath();
                curSerie = t;
            }

            // Maybe the series can be split into two parts and/or ordered
            double value;
            if (obj->findAndGetFloat64(DCM_SliceLocation, value, 0).good())
            {
                SerieMultiVolume[curSerie][value] << fileInfo.absoluteFilePath();
            }
            else if (obj->findAndGetFloat64(DCM_ImagePositionPatient, value, 2).good())
            {
                SerieMultiVolume[curSerie][value] << fileInfo.absoluteFilePath();
            }
            else //
            {
                std::cout << "Need to properly sort the image files" << std::endl;
                SerieMultiVolume[curSerie][i] << fileInfo.absoluteFilePath();
            }
        } // Building of the "Study/Patient/Series/image" struct finished

        std::cout << std::endl;


        for (std::map<QString, QStringList>::iterator it = StudySeriesPatients.begin();
        it != StudySeriesPatients.end(); ++it)
        {
            QString t = it->second.at(0);
            // First the study name

            if (human_readable.find(it->second.at(0)) != human_readable.end())
                t = human_readable[it->second.at(0)];

            std::cout << t.toAscii().data() << std::endl;

            t = it->second.at(1);

            if (human_readable.find(it->second.at(1)) != human_readable.end())
                t = human_readable[it->second.at(1)];

            std::cout << "\t"<< t.toAscii().data() << std::endl;


            t = it->first;


            if (human_readable.find(it->first) != human_readable.end())
                t = human_readable[it->first];

            if (t.contains("PhoenixZIPReport")) continue;
            if (t.contains("CSI")) continue;
            if (t.contains("svs_")) continue;

            if (t.contains("PosDisp")) continue;
            if (t.contains("TENSOR")) {  std::cout << "skipping " << t.toAscii().data() << std::endl;  continue; }
            std::cout << "\t\t" << t.toAscii().data() <<std::endl;

            if (keepAcq == "")
                ExportData(SerieMultiVolume[it->first]);
            else
                if (t.contains(keepAcq.c_str()))
                    ExportData(SerieMultiVolume[it->first]);
        }

    }


    void ExportData(std::map<double, QStringList>& files)
    {

        std::vector<float> FlipAngles, EchoTimes, RepetitionTimes, InversionTimes;
        float SpacingBetweenSlice = -1;
        QString PatientInfo; // Id, Name, Sex, BirthDate,...
        QString SequenceDetails; // Modality, ScanningSequence, SequenceVariant, ScanOption # separated string
        QString ContrastBolus; // contains the contrast info from the dicom  0018,{0010,1041,1044,1048,1049}

        QString sessionUID;
        std::string acquisitionDate;

        int depth = 32;
        // some parameters check in the "column" need to be performed, so that each image is properly constructed
        // Like check each slice "TE" in a "column" (i.e. per position order is important)
        //
        //for(std::map<double, QStringList>::iterator ite = files.begin(); ite != files.end(); ++ite)
        //	qDebug() << "\t\t\t" << ite->second.size();

        // Force Nifti Output
        // Compatibility enforced by the "property" file, in case of conversion in other format
#ifndef WIN32
        setenv("VISTAL_FMT", "NIFTI", 1);
#else
        putenv( "VISTAL_FMT=NIFTI" );
#endif
        std::cout << "\t\t\t" <<  files.size() << std::endl /* << files */ ;
        QString  AcqName,AcqNumber, SeriesNumber;
        QString scanningseq;

        // Using the Image Patient Position and Image Patient Orientation,
        // one should be able to compute the proper projection of first slice to last slice
        // this can be used to properly find the orientation of the data.

        // First assume the order of file to be coherent
        // Retrieve DCM_SeriesNumber and DCM_AcquisitionNumber

        // We will also retrieve the ImageOrientationPatient and ImagePatientPosition
        // in order to build both the transform matrix and quaternion as defined in the "nifti header"

        // the qform_code and sform_code being then directly the NIFTI_XFORM_SCANNER_ANAT
        // and need to be modified (if registration to another of it's possible state)
        // cf Nii.hh of nifti.nimh.nih.gov


        // Thus we will need to build a few transform matrix at this stage
        // But first let's go through all the dicom file and read/import them
        Image3D<float>* img = 0;

        float b0 = -1;
        std::vector<std::vector<float> > gradients;

        QStringList dirs;
        DcmElement* elt;


        // We need to check for the ordering of each slice in the volumes
        // like order by echo_time or other param

        // if no echo_time available, we'd like to sort on the 'bvalue' / gradient
        if ((*files.begin()).second.size() > 1) // 4D Set
        {

            bool sortTE = false;
            bool sortTR = false;
            bool sortFA = false;
            bool sortdiff = false;

            {

                double tefirst, tesecond; // Echo Time
                double trfirst, trsecond; // Relaxation Time
                double fafirst, fasecond; // Flip Angle

                double bvalfirst = 0, bvalsecond = 0; // Bvalue indictive of dwi images

                // get the te of the first and second image if != sort accordingly
                QString f1 =  (*files.begin()).second.front();
                QString f2 =  (*files.begin()).second.back();

                DcmFileFormat *data = new DcmFileFormat();
                OFCondition status = data->loadFile(f1.toLatin1().data());
                DcmDataset* obj = data->getDataset();

                obj->findAndGetFloat64(DCM_EchoTime, tefirst, 0);
                obj->findAndGetFloat64(DCM_RepetitionTime, trfirst, 0);
                obj->findAndGetFloat64(DCM_FlipAngle, fafirst, 0);


                if (obj->findAndGetElement(DCM_DiffusionBValue, elt).good())
                    bvalfirst =	toText(elt).toFloat();
                if (obj->findAndGetElement(DcmTagKey(0x2001, 0x1003), elt).good())
                    bvalfirst =	toText(elt).toFloat();

                if ((data->getDataset()->findAndGetElement(DcmTagKey(0x0029,0x1010), elt)).good())
                {
                    //			  std::cout << "Found CSA Header (1)" << std::endl;
                    CSAHeader header(*elt);
                    if (header.hasGradients())
                        bvalfirst = header.BValue();
                }


                status = data->loadFile(f2.toLatin1().data());
                obj = data->getDataset();

                obj->findAndGetFloat64(DCM_EchoTime, tesecond, 0);
                obj->findAndGetFloat64(DCM_RepetitionTime, trsecond, 0);
                obj->findAndGetFloat64(DCM_FlipAngle, fasecond, 0);


                if (obj->findAndGetElement(DCM_DiffusionBValue, elt).good())
                    bvalsecond =	toText(elt).toFloat();
                if (obj->findAndGetElement(DcmTagKey(0x2001, 0x1003), elt).good())
                    bvalsecond =	toText(elt).toFloat();

                if ((data->getDataset()->findAndGetElement(DcmTagKey(0x0029,0x1010), elt)).good())
                {
                    CSAHeader header(*elt);
                    if (header.hasGradients())
                        bvalsecond = header.BValue();
                }
                //                else


                if (tefirst != tesecond) sortTE = true;
                if (trfirst != trsecond) sortTR = true;
                if (fafirst != fasecond) sortFA = true;
                if (!sortTE && !sortTR && !sortFA && ((bvalfirst != 0) || (bvalsecond != 0))  /* && (*files.begin()).second.size() > 6 */) sortdiff = true;
                //qDebug() << bvalfirst << bvalsecond << sortdiff << sortTE << sortTR << sortFA;

            }
            if (sortTE || sortTR || sortFA)
                for (std::map<double, QStringList>::iterator it = files.begin(); it != files.end(); ++it)
                {
                QMap<double, QString> final;
                double val = 0;

                for (QStringList::iterator f = it->second.begin(); f != it->second.end(); ++f)
                {
                    if (sortTE)
                    {
                        // load each file and set the proper order
                        DcmFileFormat *data = new DcmFileFormat();
                        OFCondition status = data->loadFile(f->toLatin1().data());
                        DcmDataset* obj = data->getDataset();

                        obj->findAndGetFloat64(DCM_EchoTime, val, 0);
                        final[val] = *f;
                    }
                    if (sortTR)
                    {
                        // load each file and set the proper order
                        DcmFileFormat *data = new DcmFileFormat();
                        OFCondition status = data->loadFile(f->toLatin1().data());
                        DcmDataset* obj = data->getDataset();

                        obj->findAndGetFloat64(DCM_RepetitionTime, val, 0);
                        final[val] = *f;
                    }
                    if (sortFA)
                    {
                        // load each file and set the proper order
                        DcmFileFormat *data = new DcmFileFormat();
                        OFCondition status = data->loadFile(f->toLatin1().data());
                        DcmDataset* obj = data->getDataset();

                        obj->findAndGetFloat64(DCM_FlipAngle, val, 0);
                        final[val] = *f;
                    }


                }


                // save a new QStringList with ordered value
                it->second.clear();
                for (QMap<double, QString>::iterator fit = final.begin(); fit != final.end(); ++fit)
                    it->second << *fit;

            }

            if (sortdiff)
            {
                std::cout << "Sorting slices using diffusion properties" << std::endl;


                // first we will need to properly set the order, i.e. first all the b == 0 slices, followed by the gradients
                // The order of the gradients will be set to the "previously" found order according to files.front() order
                //                files.at(0)
                QStringList gradOrder;
                //files.at(0)
                for (QStringList::iterator f = (*files.begin()).second.begin(); f != (*files.begin()).second.end(); ++f)
                {
                    // load each file and set the proper order
                    DcmFileFormat *data = new DcmFileFormat();
                    OFCondition status = data->loadFile(f->toLatin1().data());
                    DcmDataset* obj = data->getDataset();
                    float b = 0;
                    QString pattern;
                    if (obj->findAndGetElement(DCM_DiffusionBValue, elt).good())
                        b = toText(elt).toFloat();
                    if (obj->findAndGetElement(DcmTagKey(0x2001, 0x1003), elt).good())
                        b = toText(elt).toFloat();

                    if ((data->getDataset()->findAndGetElement(DcmTagKey(0x0029,0x1010), elt)).good())
                    {
                        CSAHeader header(*elt);
                        if (header.hasGradients())
                        {
                            b = header.BValue();
                            pattern = QString("%1#%2#%3#%4").arg(b).arg(header.gradients()[0]).arg(header.gradients()[1]).arg(header.gradients()[2]);
                            gradients.push_back(header.gradients());
                        }
                    }

                    if (obj->findAndGetElement(DcmTagKey(0x2005, 0x1071), elt).good())
                    {

                        b = toText(elt).toFloat();
                        std::vector<float> g;g.resize(3);
                        // Philips AP angulation
                        g[0] = toText(elt).toFloat();
                        if (obj->findAndGetElement(DcmTagKey(0x2005, 0x1072), elt).good()) // Philips RL angulation
                            g[1] = toText(elt).toFloat();
                        if (obj->findAndGetElement(DcmTagKey(0x2005, 0x1073), elt).good()) // Philips FH angulation
                            g[2] = toText(elt).toFloat();
                        pattern = QString("%1#%2#%3#%4").arg(b).arg(g[0]).arg(g[1]).arg(g[2]);
                        gradients.push_back(g);

                        //  2005,1071. Philips AP angulation
                        //  2005,1072. Philips RL angulation
                        //  2005,1073. Philips FH angulation
                        //2001,100B Philips slice orientation (TRANSVERSAL, AXIAL, SAGITTAL)
                    }

                    if (pattern.isEmpty())
                        pattern=QString("%1").arg(b);

                    //     qDebug() << pattern;

                    if (b == 0)
                        gradOrder.push_front(pattern);
                    else
                        gradOrder.push_back(pattern);

                }

                for (std::map<double, QStringList>::iterator it = files.begin(); it != files.end(); ++it)
                {
                    QStringList ordered = gradOrder;
                    //ordered.it->second.size());

                    for (QStringList::iterator f = it->second.begin(); f != it->second.end(); ++f)
                    {
                        DcmFileFormat *data = new DcmFileFormat();
                        OFCondition status = data->loadFile(f->toLatin1().data());
                        DcmDataset* obj = data->getDataset();
                        float b = 0;
                        QString pattern;
                        if (obj->findAndGetElement(DCM_DiffusionBValue, elt).good()) b = toText(elt).toFloat();
                        if (obj->findAndGetElement(DcmTagKey(0x2001, 0x1003), elt).good()) b = toText(elt).toFloat();

                        if ((data->getDataset()->findAndGetElement(DcmTagKey(0x0029,0x1010), elt)).good())
                        {
                            CSAHeader header(*elt);
                            if (header.hasGradients())
                            {
                                b = header.BValue();
                                pattern = QString("%1#%2#%3#%4").arg(b).arg(header.gradients()[0]).arg(header.gradients()[1]).arg(header.gradients()[2]);

                            }
                        }
                        if (obj->findAndGetElement(DcmTagKey(0x2005, 0x1071), elt).good()) // Philips AP angulation
                        {

                            std::vector<float> g;g.resize(3);
                            g[0] = toText(elt).toFloat();
                            if (obj->findAndGetElement(DcmTagKey(0x2005, 0x1072), elt).good()) // Philips AP angulation
                                g[1] = toText(elt).toFloat();
                            if (obj->findAndGetElement(DcmTagKey(0x2005, 0x1073), elt).good()) // Philips AP angulation
                                g[2] = toText(elt).toFloat();
                            pattern = QString("%1#%2#%3#%4").arg(b).arg(g[0]).arg(g[1]).arg(g[2]);
                            //  2005,1071. Philips AP angulation
                            //  2005,1072. Philips RL angulation
                            //  2005,1073. Philips FH angulation
                            //2001,100B Philips slice orientation (TRANSVERSAL, AXIAL, SAGITTAL)
                        }
                        if (pattern.isEmpty())
                            pattern = QString("%1").arg(b);

                        // now find pattern in the "ordered" List
                        int p = ordered.indexOf(pattern);
                        if (p == -1)
                            std::cerr << "Error, the pattern was not found in the list... " << std::endl;
                        ordered.replace(p, *f);
                    }

                    it->second.clear();
                    for (QStringList::iterator fit = ordered.begin(); fit != ordered.end(); ++fit)
                        it->second << *fit;
                }
            }

        }
        //	float b0 = -100;
        //        qDebug() << "Data Sorted";

        for (int cur = 0; cur < (*files.begin()).second.size(); ++cur)
        {
            int z = 0;
            std::vector<float> t1(6), t2(3);
            std::vector<std::vector<float> > IOP(files.size(),t1);
            std::vector<std::vector<float> > IPP(files.size(),t2);


            for (std::map<double, QStringList>::iterator it = files.begin(); it != files.end(); ++it, ++z)
            {
                try {
                    //			QString file = base + *it + fin;
                    if (it->second.size() <= cur) continue;

                    QString file = it->second.at(cur);


                    // Now load dicom image, and display it!!!
                    //	  std::cout << file.toLatin1().data() << std::endl;
                    unsigned long       opt_compatibilityMode = CIF_MayDetachPixelData | CIF_TakeOverExternalDataset;

                    DcmFileFormat *data = new DcmFileFormat();
                    OFCondition status = data->loadFile(file.toLatin1().data());

                    E_TransferSyntax xfer = data->getDataset()->getOriginalXfer();
                    /*
                                 if (AcrNema)
                                 opt_compatibilityMode |= CIF_AcrNemaCompatibility;
                                 if (RAWdump)
                                 opt_compatibilityMode |= CIF_IgnoreModalityTransformation;
                                 */

                    DicomImage *dcimage = new DicomImage(data, xfer, opt_compatibilityMode, 0, 0);
                    if (!status.good())
                    {
                        std::cout << "Error Loading File" << std::endl;
                        abort();
                    }
                    if (!img)
                    {
                        img = new Image3D<float>(dcimage->getWidth(), dcimage->getHeight(), files.size(), 0);

                        OFString str;

                        data->getDataset()->findAndGetOFStringArray(DCM_SliceThickness, str);
                        img->dz = QString(str.c_str()).toFloat();

                        data->getDataset()->findAndGetOFStringArray(DCM_PixelSpacing, str);
                        dirs = QString(str.c_str()).split("\\");

                        img->dx = dirs.front().toFloat();
                        img->dy = dirs.back().toFloat();

                        if (data->getDataset()->findAndGetElement(DcmTagKey(0x2001, 0x1003), elt).good())
                        {
                            float lb0 = atof(toText(elt).toLatin1());
                            if (lb0 > b0) b0 = lb0;
                        }
                        if (data->getDataset()->findAndGetElement(DCM_DiffusionBValue, elt).good())
                        {
                            float lb0 = atof(toText(elt).toLatin1());
                            if (lb0 > b0) b0 = lb0;
                        }
                        if ((data->getDataset()->findAndGetElement(DcmTagKey(0x0029,0x1010), elt)).good())
                        {
                            //			  std::cout << "Found CSA Header (1)" << std::endl;
                            CSAHeader header(*elt);
                            if (header.hasGradients())
                            {
                                //			      gradients.clear();
                                //   gradients.push_back(header.gradients());
                                b0 = header.BValue();

                            }
                        }


                    }

                    {
                        DcmDataset* obj = data->getDataset();
                        double v;

                        if (obj->findAndGetFloat64(DCM_EchoTime, v, 0).good())
                        {
                            if (EchoTimes.size() != 0)
                            {
                                if (EchoTimes.back() != (float)v)
                                    EchoTimes.push_back(v);
                            }
                            else
                                EchoTimes.push_back((float)v);
                        }

                        if (obj->findAndGetFloat64(DCM_RepetitionTime, v, 0).good())
                        {
                            if (RepetitionTimes.size() != 0)
                            {
                                if (RepetitionTimes.back() != (float)v)
                                    RepetitionTimes.push_back(v);
                            }
                            else
                                RepetitionTimes.push_back((float)v);
                        }

                        if (obj->findAndGetFloat64(DCM_FlipAngle, v, 0).good())
                        {
                            if (FlipAngles.size() != 0)
                            {
                                if (FlipAngles.back() != (float)v)
                                    FlipAngles.push_back(v);
                            }
                            else
                                FlipAngles.push_back((float)v);
                        }

                        if (obj->findAndGetFloat64(DCM_InversionTime, v, 0).good())
                        {
                            if (InversionTimes.size() != 0)
                            {
                                if (InversionTimes.back() != (float)v)
                                    InversionTimes.push_back(v);
                            }
                            else
                                InversionTimes.push_back((float)v);
                        }

                        if (obj->findAndGetFloat64(DCM_SpacingBetweenSlices, v, 0).good())
                            SpacingBetweenSlice = v;

                        if (SequenceDetails.isEmpty())
                        {
                            OFString str;
                            obj->findAndGetOFStringArray(DCM_Modality, str);
                            SequenceDetails = QString("%1").arg(str.c_str());
                            obj->findAndGetOFStringArray(DCM_SequenceName, str);
                            SequenceDetails = QString("%1#%2").arg(SequenceDetails).arg(str.c_str());

                            obj->findAndGetOFStringArray(DCM_ScanningSequence, str);
                            SequenceDetails = QString("%1#%2").arg(SequenceDetails).arg(str.c_str());
                            obj->findAndGetOFStringArray(DCM_SequenceVariant, str);
                            SequenceDetails = QString("%1#%2").arg(SequenceDetails).arg(str.c_str());
                            obj->findAndGetOFStringArray(DCM_ScanOptions, str);
                            SequenceDetails = QString("%1#%2").arg(SequenceDetails).arg(str.c_str());
                            obj->findAndGetOFStringArray(DCM_MRAcquisitionType, str);
                            SequenceDetails = QString("%1#%2").arg(SequenceDetails).arg(str.c_str());
                            // qDebug() << SequenceDetails;

                            //                        SequenceDetails; // Modality, ScanningSequence, SequenceVariant, ScanOption # separated string
                        }
                        if (ContrastBolus.isEmpty())
                        {
                            OFString str;

                            bool r = obj->findAndGetOFStringArray(DCM_ContrastBolusAgent, str).good();
                            if (r)
                            {
                                ContrastBolus = QString("%1").arg(str.c_str());

                                obj->findAndGetOFStringArray(DCM_ContrastBolusVolume, str);
                                ContrastBolus = QString("%1#%2").arg(ContrastBolus).arg(str.c_str());
                                obj->findAndGetOFStringArray(DCM_ContrastBolusTotalDose, str);
                                ContrastBolus = QString("%1#%2").arg(ContrastBolus).arg(str.c_str());
                                obj->findAndGetOFStringArray(DCM_ContrastBolusIngredient, str);
                                ContrastBolus = QString("%1#%2").arg(ContrastBolus).arg(str.c_str());
                                obj->findAndGetOFStringArray(DCM_ContrastBolusIngredientConcentration, str);
                                ContrastBolus = QString("%1#%2").arg(ContrastBolus).arg(str.c_str());
                            }
                            else ContrastBolus = "None";

                        }
                        if (sessionUID.isEmpty())
                        {
                            OFString str;
                            obj->findAndGetOFStringArray(DCM_StudyInstanceUID, str);
                            sessionUID = QString("%1").arg(str.c_str());

                            obj->findAndGetOFStringArray(DCM_SeriesInstanceUID, str);
                            sessionUID = QString("%1#%2").arg(sessionUID).arg(str.c_str());
                        }

                        if (PatientInfo.isEmpty())
                        {
                            OFString str;
                            obj->findAndGetOFStringArray(DCM_PatientsName, str);
                            PatientInfo = QString("%1").arg(str.c_str());

                            obj->findAndGetOFStringArray(DCM_PatientID, str);
                            PatientInfo = QString("%1#%2").arg(PatientInfo).arg(str.c_str());

                            obj->findAndGetOFStringArray(DCM_PatientsSex, str);
                            PatientInfo = QString("%1#%2").arg(PatientInfo).arg(str.c_str());

                            obj->findAndGetOFStringArray(DCM_PatientsBirthDate, str);
                            PatientInfo = QString("%1#%2").arg(PatientInfo).arg(str.c_str());

                            obj->findAndGetOFStringArray(DCM_PatientsAge, str);
                            PatientInfo = QString("%1#%2").arg(PatientInfo).arg(str.c_str());

                            obj->findAndGetOFStringArray(DCM_PatientsSize, str);
                            PatientInfo = QString("%1#%2").arg(PatientInfo).arg(str.c_str());

                            obj->findAndGetOFStringArray(DCM_PatientsWeight, str);
                            PatientInfo = QString("%1#%2").arg(PatientInfo).arg(str.c_str());

                        }


                        if (acquisitionDate == "")
                        {
                            OFString str;
                            obj->findAndGetOFStringArray(DCM_AcquisitionDate, str);
                            acquisitionDate = std::string(str.c_str());
                        }
                        //			OFString str;
                        //			float z = it->first;//dirs.back().toFloat();
                        //			if (lastz != z) zcount = 1;
                        //			if (lastz == z) zcount++;
                        //			lastz = z;
                    }
                    if (dcimage != NULL)
                    {
                        if (dcimage->getStatus() == EIS_Normal)
                        {
                            // getOutputData is always unsigned
                            // We shall use '0' as bit per sample, so that it uses
                            // the internal 'Depth' value for the pixels.
                            // We shall convert based on that value

                            //						int depth = 16;
                            if (dcimage->getDepth() < depth)
                                depth = dcimage->getDepth();

                            if (depth <= 16)
                            {
                                if (depth <= 8) {

                                    unsigned char *pixelData = (unsigned char *)(dcimage->getOutputData(depth /* bits per sample */));
                                    if (pixelData != NULL)
                                    {
                                        int pos = 0;
                                        for (unsigned j = 0; j < dcimage->getHeight(); j++)
                                            for (unsigned i = 0; i < dcimage->getWidth(); ++i)
                                                (*img)(i,j,z) = pixelData[pos++];
                                    }


                                } else {

                                    unsigned short *pixelData = (unsigned short *)(dcimage->getOutputData(depth /* bits per sample */));
                                    if (pixelData != NULL)
                                    {
                                        int pos = 0;
                                        for (unsigned j = 0; j < dcimage->getHeight(); j++)
                                            for (unsigned i = 0; i < dcimage->getWidth(); ++i)
                                                (*img)(i,j,z) = pixelData[pos++];
                                    }


                                }
                            }
                            else {								// 32 bits

                                unsigned int *pixelData = (unsigned int *)(dcimage->getOutputData(depth /* bits per sample */));
                                if (pixelData != NULL)
                                {
                                    int pos = 0;
                                    for (unsigned j = 0; j < dcimage->getHeight(); j++)
                                        for (unsigned i = 0; i < dcimage->getWidth(); ++i)
                                            (*img)(i,j,z) = pixelData[pos++];
                                }
                            }


                        }
                        else
                        {
                            std::cerr << "Error: cannot load DICOM image (" << DicomImage::getString(dcimage->getStatus()) << ")" << std::endl;
                            std::cerr << qPrintable(file) <<  std::endl;
                        }
                    }

                    if (AcqName.isEmpty())
                        if (data->getDataset()->findAndGetElement(DCM_SeriesDescription, elt).good())
                            AcqName = toText(elt);
                    if (AcqNumber.isEmpty())
                        if (data->getDataset()->findAndGetElement(DCM_AcquisitionNumber, elt).good())
                            AcqNumber = toText(elt);
                    if (SeriesNumber.isEmpty())
                        if (data->getDataset()->findAndGetElement(DCM_SeriesNumber, elt).good())
                            SeriesNumber = toText(elt);
                    if (scanningseq.isEmpty())
                        if (data->getDataset()->findAndGetElement(DCM_ScanningSequence, elt).good())
                            scanningseq = toText(elt);


                    //if (z == 0)
                    {
                        // Get IOP / IPP
                        DcmDataset* obj = data->getDataset();
                        double value[6];

                        Vect3Df qoffset;
                        float qfac = 1;
                        Vect3Df quat;

                        if (obj->findAndGetFloat64(DCM_ImagePositionPatient, value[0], 0).good())
                        {
                            obj->findAndGetFloat64(DCM_ImagePositionPatient, value[1], 1);
                            obj->findAndGetFloat64(DCM_ImagePositionPatient, value[2], 2);
                            qoffset = Vect3Df(-value[0], -value[1], value[2]); // Negate according to nifti format

                        }
                        //	IPP[z] = new double[6];

                        // Warning !!!! we need to negate the x and y axis correponding to the patient's axis, Left-Right/ Antérior-Posterior

                        for (int i = 0; i < 3; ++i)
                            IPP[z][i] = value[i];
                        IPP[z][0] = -IPP[z][0]; IPP[z][1] = -IPP[z][1]; // Negate x,y according to the nifti format



                        if (obj->findAndGetFloat64(DCM_ImageOrientationPatient, value[0], 0).good())
                            for ( int i = 1; i < 6;++i)
                                obj->findAndGetFloat64(DCM_ImageOrientationPatient, value[i], i);

                        //IOP[z] = new double[3];
                        for (int i = 0; i < 6; ++i)
                            IOP[z][i] = value[i];

                        IOP[z][0] = -IOP[z][0]; IOP[z][1] = -IOP[z][1];
                        IOP[z][3] = -IOP[z][3]; IOP[z][4] = -IOP[z][4];



                        Nii::mat44 mat;
                        for (int i = 0; i < 4; ++i) for (int j = 0; j < 4; ++j) mat.m[i][j] = 0; mat.m[3][3] = 1;
                        //				qDebug() << "Build output";
                        //					qDebug() << value[0];

                        // Negate xa,ya, and xb,yb for orientation (if xa,ya,za,xb,yb,zb) is the IOP...
                        mat.m[0][0] = -value[0]; mat.m[0][1] = -value[3];
                        mat.m[1][0] = -value[1]; mat.m[1][1] = -value[4];
                        mat.m[2][0] = value[2]; mat.m[2][1] = value[5];


                        // last column should be cross product of the two first...
                        Vect3Df refNrm((value[1] * value[5] - value[2] * value[4]),
                                       (value[2] * value[3] - value[0] * value[5]),
                                       (value[0] * value[4] - value[1] * value[3]));
                        mat.m[0][2] = refNrm.x();
                        mat.m[1][2] = refNrm.y();
                        mat.m[2][2] = refNrm.z();




                        // According to the Nifti header (should negate, x and y axis)
                        mat.m[0][3] = qoffset.x();
                        mat.m[1][3] = qoffset.y();
                        mat.m[2][3] = qoffset.z();
                        /*
                                         for (int i = 0; i < 4; ++i)
                                         {
                                         for (int j = 0; j < 4; ++j)
                                         std::cout << mat.m[i][j] << " ";
                                         std::cout << std::endl;
                                         } */

                        Vect3Df dims;
                        nifti_mat44_to_quatern(mat,
                                               quat.x(), quat.y(), quat.z(),
                                               qoffset.x(), qoffset.y(), qoffset.z(),
                                               dims.x(), dims.y(), dims.z(), qfac );


                        img->setProperty("NiftiQform", (short)NIFTI_XFORM_SCANNER_ANAT);
                        img->setProperty("NiftiQOffset", qoffset);
                        img->setProperty("NiftiQfac", qfac);
                        img->setProperty("NiftiQuaternion", quat);

                        if (b0 > 0)
                        {
                            img->setProperty("b0", b0);
                            img->setProperty("DiffusionGradientOrientation", gradients);
                        }

                        //	std::cout << quat << std::endl;
                        std::vector<float> rowx(4), rowy(4), rowz(4);

                        Vect3Df d(img->dx, img->dy, img->dz);
                        for (int i = 0; i < 4; ++i)
                        {
                            float mult = 1;

                            if (i < 3)
                                mult = d(i);

                            rowx[i] = mat.m[0][i];
                            rowy[i] = mat.m[1][i];
                            rowz[i] = mat.m[2][i];
                        }

                        img->setProperty("Niftisrow_x", rowx);
                        img->setProperty("Niftisrow_y", rowy);
                        img->setProperty("Niftisrow_z", rowz);

                        img->setProperty("NiftiSform", (short)NIFTI_XFORM_SCANNER_ANAT);



                    }


                    delete dcimage; dcimage = 0;

                }

                catch (...)
                {}
            }
            // May require to properly restore the position of slices in the volume...
            // If any reordering is required let do it here ! slice position are in IPP and orientation in IOP,
            // First we can check that each slice has the proper orientation i.e. the same orientation

            for (unsigned z = 1; z < IOP.size();++z)
            {
                double sum = 0;
                for (int i = 0; i < 3; i++)
                    sum += pow(IOP[z][i]-IOP[z-1][i],2.f);
                if (sum > 1e-3) std::cerr << "Slice Orientation differs in the same volume..." << std::endl;
            }

            // then pick the 0 slice and check if we can project it on the last slice
            // if not reordering might be necessary !
            Matrix RefMat(4,4);

            Vect3Df refNrm((IOP[0][1] * IOP[0][5] - IOP[0][2] * IOP[0][4]) *  img->dz,
                           (IOP[0][2] * IOP[0][3] - IOP[0][0] * IOP[0][5]) *  img->dz,
                           (IOP[0][0] * IOP[0][4] - IOP[0][1] * IOP[0][3]) *  img->dz);



            Vect3Df refRow(IOP[0][0] * img->dx, IOP[0][1] * img->dx, IOP[0][2] * img->dx);

            Vect3Df refCol(IOP[0][3] * img->dy, IOP[0][4] * img->dy, IOP[0][5] * img->dy);

            Vect3Df ref_ipp(IPP[0][0], IPP[0][1] , IPP[0][2]);

            RefMat << refRow.x() << refCol.x() << refNrm.x() << ref_ipp.x()
                    << refRow.y() << refCol.y() << refNrm.y() << ref_ipp.y()
                    << refRow.z() << refCol.z() << refNrm.z() << ref_ipp.z()
                    << 0 << 0 << 0 << 1;


            ColumnVector p(4);

            // Last slice should project to the position of

            p << 0 << 0 << img->nbz-1 << 1;
            p = RefMat*p;
            // This should projet the point very close to the ref_IPPl;

            float dist = sqrt(pow(p(1) - IPP[IPP.size()-1][0],2 ) + pow(p(2) - IPP[IPP.size()-1][1],2) + pow(p(3) - IPP[IPP.size()-1][2],2));
            if (dist > (img->nbz-1)*SpacingBetweenSlice+2*img->dz)
            {
                //	std::cerr << "Warning slice order is probably wrong " << dist << std::endl;

                // Try a negative qfac
                Matrix T(4,4);
                T << 1 << 0 << 0 << 0
                        << 0 << 1 << 0 << 0
                        << 0 << 0 << -1 << 0
                        << 0 << 0 << 0 << 1;

                p << 0 << 0 << img->nbz-1 << 1;
                RefMat = RefMat * T;
                p = RefMat*p;


                float dist = sqrt(pow(p(1) - IPP[IPP.size()-1][0],2 ) + pow(p(2) - IPP[IPP.size()-1][1],2) + pow(p(3) - IPP[IPP.size()-1][2],2));				//      std::cout << "dist " << dist << std::endl;
                if (dist  >  (img->nbz-1)*SpacingBetweenSlice+2*img->dz)
                {
                    std::cerr
                            << "Error computing the transform between images, checking the correctness of the program required" << std::endl
                            << dist << std::endl;
                }

                // float qf = img->getProperty<float>("NiftiQfac");
                //if (qf > 0) std::cerr << "qfac was originally positive, corrected..." << std::endl;

                float qfac = -1;
                img->setProperty("NiftiQfac", qfac);


                std::vector<float> rowx(4), rowy(4), rowz(4);

                Vect3Df d(img->dx, img->dy, img->dz);
                for (int i = 0; i < 4; ++i)
                {
                    rowx[i] = RefMat(1,i+1);
                    rowy[i] = RefMat(2,i+1);
                    rowz[i] = RefMat(3,i+1);
                }

                img->setProperty("Niftisrow_x", rowx);
                img->setProperty("Niftisrow_y", rowy);
                img->setProperty("Niftisrow_z", rowz);

                img->setProperty("NiftiSform", (short)NIFTI_XFORM_SCANNER_ANAT);




            }

            // Insert the serialization of first and last data...
            img->setProperty("ImagePositionPatientFirst", IPP[0]);
            img->setProperty("ImagePositionPatientLast", IPP[img->nbz-1]);

            img->setProperty("ImageOrientationPatientFirst", IOP[0]);
            img->setProperty("ImageOrientationPatientLast", IOP[img->nbz-1]);

            // Set the other properties

            img->setProperty("FlipAngles", FlipAngles);
            img->setProperty("EchoTimes", EchoTimes);
            img->setProperty("RepetitionTimes", RepetitionTimes);
            img->setProperty("InversionTimes", InversionTimes);

            img->setProperty("SpacingBetweenSlice", SpacingBetweenSlice);
            img->setProperty("SequenceDetails", SequenceDetails.toStdString());
            img->setProperty("ContrastBolus", ContrastBolus.toStdString());

            img->setProperty("SessionUID", sessionUID.toStdString());
            img->setProperty("PatientInfo", PatientInfo.toStdString());
            img->setProperty("AcquisitionDate", acquisitionDate);


            if (img)
            {
                QString fname = QString("%1%2a%3_%4").arg(AcqName).arg(AcqNumber,3,'0').arg(SeriesNumber, 3, '0').arg(scanningseq);

                fname.replace(' ', '_').replace('/','_');
                fname = QString("%1/%2").arg(destdir.c_str()).arg(fname);
                // Save the image to a file

                if (depth <= 16) // Reduce file dimensionnality according to the size of the dataset
                {
                    if (depth <= 8)
                    {
                        Image3D< char> ima(*img, 0);
                        for (Image3D<float>::iterator it = img->begin(); it != img->end(); ++it)
                            ima(it.Position()) = ( char)std::min(127.0f, std::max(-127.f, *it));
                        gis::saveVolume(fname.replace("\\","_").toAscii(), ima, cur);


                    }
                    else {
                        Image3D< short> ima(*img, 0);
                        for (Image3D<float>::iterator it = img->begin(); it != img->end(); ++it)
                            ima(it.Position()) = ( short)std::min(std::numeric_limits< short>::max()+0.f, std::max(std::numeric_limits<short>::min()+0.f, *it));

                        gis::saveVolume(fname.replace("\\","_").toAscii(), ima, cur);
                    }

                } else {
                    gis::saveVolume(fname.replace("\\","_").toAscii(), *img, cur);
                }

            }
            delete img; img = 0;
        }
        //	exit(0);





    }


};


#endif 	    /* !MYDCMVIEW_H_ */
