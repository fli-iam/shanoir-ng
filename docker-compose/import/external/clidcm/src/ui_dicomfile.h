/********************************************************************************
** Form generated from reading UI file 'dicomfile.ui'
**
** Created: Thu Jan 14 11:24:29 2010
**      by: Qt User Interface Compiler version 4.7.0
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_DICOMFILE_H
#define UI_DICOMFILE_H

#include <QtCore/QVariant>
#include <QtGui/QAction>
#include <QtGui/QApplication>
#include <QtGui/QButtonGroup>
#include <QtGui/QCheckBox>
#include <QtGui/QDialog>
#include <QtGui/QFrame>
#include <QtGui/QGraphicsView>
#include <QtGui/QGridLayout>
#include <QtGui/QHBoxLayout>
#include <QtGui/QHeaderView>
#include <QtGui/QPushButton>
#include <QtGui/QSpacerItem>
#include <QtGui/QTreeWidget>
#include <QtGui/QVBoxLayout>
#include <QtGui/QWidget>

QT_BEGIN_NAMESPACE

class Ui_DcmWidget
{
public:
    QWidget *widget;
    QGridLayout *gridLayout;
    QHBoxLayout *hboxLayout;
    QSpacerItem *spacerItem;
    QCheckBox *rawdump;
    QSpacerItem *spacerItem1;
    QPushButton *okButton;
    QHBoxLayout *hboxLayout1;
    QTreeWidget *listWidget;
    QFrame *line;
    QVBoxLayout *vboxLayout;
    QGraphicsView *graphicsView;
    QHBoxLayout *hboxLayout2;
    QPushButton *prev;
    QSpacerItem *spacerItem2;
    QPushButton *convertAll;
    QSpacerItem *spacerItem3;
    QPushButton *toImlib;
    QSpacerItem *spacerItem4;
    QPushButton *next;

    void setupUi(QDialog *DcmWidget)
    {
        if (DcmWidget->objectName().isEmpty())
            DcmWidget->setObjectName(QString::fromUtf8("DcmWidget"));
        DcmWidget->resize(607, 451);
        widget = new QWidget(DcmWidget);
        widget->setObjectName(QString::fromUtf8("widget"));
        widget->setGeometry(QRect(9, 9, 588, 385));
        gridLayout = new QGridLayout(DcmWidget);
#ifndef Q_OS_MAC
        gridLayout->setSpacing(6);
#endif
        gridLayout->setContentsMargins(8, 8, 8, 8);
        gridLayout->setObjectName(QString::fromUtf8("gridLayout"));
        hboxLayout = new QHBoxLayout();
#ifndef Q_OS_MAC
        hboxLayout->setSpacing(6);
#endif
#ifndef Q_OS_MAC
        hboxLayout->setContentsMargins(0, 0, 0, 0);
#endif
        hboxLayout->setObjectName(QString::fromUtf8("hboxLayout"));
        spacerItem = new QSpacerItem(40, 20, QSizePolicy::Expanding, QSizePolicy::Minimum);

        hboxLayout->addItem(spacerItem);

        rawdump = new QCheckBox(DcmWidget);
        rawdump->setObjectName(QString::fromUtf8("rawdump"));

        hboxLayout->addWidget(rawdump);

        spacerItem1 = new QSpacerItem(131, 31, QSizePolicy::Expanding, QSizePolicy::Minimum);

        hboxLayout->addItem(spacerItem1);

        okButton = new QPushButton(DcmWidget);
        okButton->setObjectName(QString::fromUtf8("okButton"));

        hboxLayout->addWidget(okButton);


        gridLayout->addLayout(hboxLayout, 1, 0, 1, 1);

        hboxLayout1 = new QHBoxLayout();
#ifndef Q_OS_MAC
        hboxLayout1->setSpacing(6);
#endif
        hboxLayout1->setContentsMargins(0, 0, 0, 0);
        hboxLayout1->setObjectName(QString::fromUtf8("hboxLayout1"));
        listWidget = new QTreeWidget(DcmWidget);
        listWidget->setObjectName(QString::fromUtf8("listWidget"));

        hboxLayout1->addWidget(listWidget);

        line = new QFrame(DcmWidget);
        line->setObjectName(QString::fromUtf8("line"));
        line->setFrameShape(QFrame::VLine);
        line->setFrameShadow(QFrame::Sunken);

        hboxLayout1->addWidget(line);

        vboxLayout = new QVBoxLayout();
#ifndef Q_OS_MAC
        vboxLayout->setSpacing(6);
#endif
        vboxLayout->setContentsMargins(0, 0, 0, 0);
        vboxLayout->setObjectName(QString::fromUtf8("vboxLayout"));
        graphicsView = new QGraphicsView(DcmWidget);
        graphicsView->setObjectName(QString::fromUtf8("graphicsView"));

        vboxLayout->addWidget(graphicsView);

        hboxLayout2 = new QHBoxLayout();
#ifndef Q_OS_MAC
        hboxLayout2->setSpacing(6);
#endif
        hboxLayout2->setContentsMargins(0, 0, 0, 0);
        hboxLayout2->setObjectName(QString::fromUtf8("hboxLayout2"));
        prev = new QPushButton(DcmWidget);
        prev->setObjectName(QString::fromUtf8("prev"));

        hboxLayout2->addWidget(prev);

        spacerItem2 = new QSpacerItem(21, 20, QSizePolicy::Expanding, QSizePolicy::Minimum);

        hboxLayout2->addItem(spacerItem2);

        convertAll = new QPushButton(DcmWidget);
        convertAll->setObjectName(QString::fromUtf8("convertAll"));

        hboxLayout2->addWidget(convertAll);

        spacerItem3 = new QSpacerItem(40, 20, QSizePolicy::Expanding, QSizePolicy::Minimum);

        hboxLayout2->addItem(spacerItem3);

        toImlib = new QPushButton(DcmWidget);
        toImlib->setObjectName(QString::fromUtf8("toImlib"));

        hboxLayout2->addWidget(toImlib);

        spacerItem4 = new QSpacerItem(21, 20, QSizePolicy::Expanding, QSizePolicy::Minimum);

        hboxLayout2->addItem(spacerItem4);

        next = new QPushButton(DcmWidget);
        next->setObjectName(QString::fromUtf8("next"));

        hboxLayout2->addWidget(next);


        vboxLayout->addLayout(hboxLayout2);


        hboxLayout1->addLayout(vboxLayout);


        gridLayout->addLayout(hboxLayout1, 0, 0, 1, 1);


        retranslateUi(DcmWidget);
        QObject::connect(okButton, SIGNAL(clicked()), DcmWidget, SLOT(accept()));
        QObject::connect(prev, SIGNAL(clicked()), DcmWidget, SLOT(previousImage()));
        QObject::connect(next, SIGNAL(clicked()), DcmWidget, SLOT(nextImage()));
        QObject::connect(listWidget, SIGNAL(currentItemChanged(QTreeWidgetItem*,QTreeWidgetItem*)), DcmWidget, SLOT(itemChanged(QTreeWidgetItem*,QTreeWidgetItem*)));
        QObject::connect(toImlib, SIGNAL(clicked()), DcmWidget, SLOT(ToImlib()));
        QObject::connect(listWidget, SIGNAL(clicked(QModelIndex)), DcmWidget, SLOT(exportAs(QModelIndex)));
        QObject::connect(convertAll, SIGNAL(clicked()), DcmWidget, SLOT(ExportAll()));
        QObject::connect(rawdump, SIGNAL(toggled(bool)), DcmWidget, SLOT(RawChange(bool)));

        QMetaObject::connectSlotsByName(DcmWidget);
    } // setupUi

    void retranslateUi(QDialog *DcmWidget)
    {
        DcmWidget->setWindowTitle(QApplication::translate("DcmWidget", "Dialog", 0, QApplication::UnicodeUTF8));
        rawdump->setText(QApplication::translate("DcmWidget", "RAW Dump", 0, QApplication::UnicodeUTF8));
        okButton->setText(QApplication::translate("DcmWidget", "OK", 0, QApplication::UnicodeUTF8));
        prev->setText(QApplication::translate("DcmWidget", "Prev", 0, QApplication::UnicodeUTF8));
        convertAll->setText(QApplication::translate("DcmWidget", "Convert All", 0, QApplication::UnicodeUTF8));
        toImlib->setText(QApplication::translate("DcmWidget", "Conversion", 0, QApplication::UnicodeUTF8));
        next->setText(QApplication::translate("DcmWidget", "Next", 0, QApplication::UnicodeUTF8));
    } // retranslateUi

};

namespace Ui {
    class DcmWidget: public Ui_DcmWidget {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_DICOMFILE_H
