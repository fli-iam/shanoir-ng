#########################################################################
# Dicomifier.ws - Copyright (C) Universite de Strasbourg
# Distributed under the terms of the MIT license. Refer to the 
# LICENSE.txt file or to https://opensource.org/licenses/MIT for details.
#########################################################################

import json
import os
import subprocess

import werkzeug
import werkzeug.exceptions
import werkzeug.routing
import yaml

class Application(object):
    _instance = None
    
    @staticmethod
    def instance():
        if Application._instance is None:
            Application._instance = Application()
        return Application._instance
    
    def __init__(self):
        self.routes = werkzeug.routing.Map([
            werkzeug.routing.Rule(
                "/bruker2dicom", methods=["POST"], endpoint = self.bruker2dicom),
            werkzeug.routing.Rule(
                "/dicom2nifti", methods=["POST"], endpoint = self.dicom2nifti)
        ])
    
    def __call__(self, environ, start_response):
        routes = self.routes.bind_to_environ(environ)
        request = werkzeug.Request(environ)
        try:
            endpoint, arguments = routes.match()
            response = endpoint(request)
        except werkzeug.exceptions.HTTPException as e:
            response = e
        except Exception as e:
            response = werkzeug.exceptions.InternalServerError(str(e))
        return response(environ, start_response)
        
    def bruker2dicom(self, request):
        data = json.loads(request.get_data().decode())
        self._parse_request(
            data, {
                "source": {
                    "required": True, "type": str,
                    "validator": lambda x: (
                        None if os.path.isdir(x) else 
                        werkzeug.exceptions.NotFound(
                            "No such directory: {}".format(x))) },
                "destination": {
                    "required": True, "type": str },
                "dicomdir": {
                    "required": False, "type": bool, "default": False },
                "multiframe": {
                    "required": False, "type": bool, "default": False }
            })
        
        arguments = ["convert", data["source"], data["destination"]]
        if data["dicomdir"]:
            arguments.append("--dicomdir")
        if data["multiframe"]:
            arguments.append("--multiframe")
        
        return self._run(["bruker2dicom"]+arguments)
    
    def dicom2nifti(self, request):
        data = json.loads(request.get_data().decode())
        self._parse_request(
            data, {
                "source": {
                    "required": True, "type": str,
                    "validator": lambda x: (
                        None if os.path.isdir(x) else 
                        werkzeug.exceptions.NotFound(
                            "No such directory: {}".format(x))) },
                "destination": {
                    "required": True, "type": str },
                "zip": {
                    "required": False, "type": bool, "default": True },
                "pretty-print": {
                    "required": False, "type": bool, "default": False }
            })
        
        arguments = [data["source"], data["destination"]]
        if data["zip"]:
            arguments.append("--zip")
        if data["pretty-print"]:
            arguments.append("--pretty-print")
        
        return self._run(["dicom2nifti"]+arguments)
    
    def _parse_request(self, data, parser):
        """ Parse and validate the data passed to a request. The data must be
            a name-value dictionary, and the parser must be a dictionnary 
            mapping arguments name to:
            - "required": whether or not the argument is required. Required.
            - "type": type of the argument. Required.
            - "validator": validation function. Must return None if the result
              is valid and an exception object if not. Optional.
            - "default": default value if the argument is not provided. Required
              if argument is not required, ignored otherwise.
        """
        
        for name, items in parser.items():
            if items["required"] and name not in data:
                raise werkzeug.exceptions.BadRequest("Missing {}".format(name))
            elif not items["required"]:
                data.setdefault(name, items["default"])
            
            if not isinstance(data[name], items["type"]):
                raise werkzeug.exceptions.BadRequest(
                    "Wrong type for {}".format(name))
            
            if "validator" in items:
                errors = items["validator"](data[name])
                if errors is not None:
                    raise errors
    
    def _run(self, *args):
        try:
            stdout = subprocess.check_output(*args, stderr=subprocess.STDOUT)
        except subprocess.CalledProcessError as e:
            raise werkzeug.exceptions.InternalServerError(e.stdout)
        else:
            return werkzeug.Response(
                json.dumps({"output": stdout.decode()}), 200, 
                {"Content-Type": "application/json"})
