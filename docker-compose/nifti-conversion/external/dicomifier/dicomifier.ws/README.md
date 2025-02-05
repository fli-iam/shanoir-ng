# Dicomifier.ws

This project is a simple [Werkzeug](http://werkzeug.pocoo.org/)-based webservice around [Dicomifier](https://github.com/lamyj/dicomifier).

The webservice has the following routes:
- `POST /bruker2dicom`, with a JSON request containing
    - `source`: the path on the server to the Bruker directory to be converted
    - `destination`: the path on the server to the destination DICOM directory
    - `dicomdir`: whether a DICOMDIR should be created, default to _false_
    - `multiframe`: whether multiframe DICOM instances should be created, default to _false_
- `POST /dicom2nifti`, with a JSON request containing
    - `source`: the path on the server to the DICOM directory to be converted
    - `destination`: the path on the server to the destination NIfTI directory
    - `zip`: whether a gzipped NIfTI files should be created, default to _true_
    - `pretty-print`: whether the JSON files should be pretty-printed, default to _false_

The environment variables of the Werkzeug application 
