import requests
import json
import getpass
import re
import sys
from pathlib import Path
Path.ls = lambda x: list(x.iterdir())
from http.client import responses
import requests
import argparse
import logging
import http.client as http_client

parser = argparse.ArgumentParser(prog=__file__, description="""Shanoir downloader script""", formatter_class=argparse.ArgumentDefaultsHelpFormatter)

parser.add_argument('-u', '--username', required=True, help='Your shanoir username.')
parser.add_argument('-d', '--domain', default='shanoir.irisa.fr', help='The shanoir domain to query.')
parser.add_argument('-f', '--format', default='nifti', choices=['nifti', 'dicom'], help='The format to download.')
parser.add_argument('-dt', '--datasetId', default='', help='The dataset id to download.')
parser.add_argument('-dts', '--datasetIds', default='', help='Path to a file containing the dataset ids to download (a .txt file containing one dataset id per line).')
parser.add_argument('-st', '--studyId', default='', help='The study id to download.')
parser.add_argument('-sb', '--subjectId', default='', help='The subject id to download.')
parser.add_argument('-dd', '--destDir', required=True, help='The destination directory where files will be downloaded.')
parser.add_argument('-c', '--configurationFolder', required=False, help='Path to the configuration folder containing proxy.properties (Tries to use ~/.su_vX.X.X/ by default). You can also use --proxy_url to configure the proxy (in which case the proxy.properties file will be ignored).')
parser.add_argument('-p', '--proxyUrl', required=False, help='The proxy url in the format "user@host:port". The proxy password will be asked in the terminal. See --configuration_folder.')
parser.add_argument('-v', '--verbose', default=False, action='store_true', help='Print log messages.')

args = parser.parse_args()

server_domain = args.domain
username = args.username

file_format = args.format
dataset_id = args.datasetId
dataset_ids = Path(args.datasetIds) if args.datasetIds else None
if args.datasetIds and not dataset_ids.exists():
    sys.exit('Error: given file does not exist: ' + str(dataset_ids))
study_id = args.studyId
subject_id = args.subjectId
dest_dir = Path(args.destDir)
dest_dir.mkdir(parents=True, exist_ok=True)

proxy_url = None # 'user:pass@host:port'

if args.proxyUrl:
    proxy_a = args.proxyUrl.split('@')
    proxy_user = proxy_a[0]
    proxy_host = proxy_a[1]
    proxy_password = getpass.getpass(prompt='Proxy password for user ' + proxy_user + ' and host ' + proxy_host + ': ', stream=None)
    proxy_url = proxy_user + ':' + proxy_password + '@' + proxy_host

else:
    
    configuration_folder = None
    
    if args.configurationFolder:
        configuration_folder = Path(args.configurationFolder)
    else:
        cfs = list(Path.home().glob('.su_v*'))
        cfs.sort()
        configuration_folder = cfs[-1]

    proxy_settings = configuration_folder / 'proxy.properties'
    
    proxy_config = {}

    if proxy_settings.exists():
        with open(proxy_settings) as file:
            for line in file:
                if line.startswith('proxy.'):
                    line_s = line.split('=')
                    proxy_key = line_s[0]
                    proxy_value = line_s[1].strip()
                    proxy_key = proxy_key.split('.')[-1]
                    proxy_config[proxy_key] = proxy_value
        
            if 'enabled' not in proxy_config or proxy_config['enabled'] == 'true':
                if 'user' in proxy_config and len(proxy_config['user']) > 0 and 'password' in proxy_config and len(proxy_config['password']) > 0:
                    proxy_url = proxy_config['user'] + ':' + proxy_config['password']
                proxy_url += '@' + proxy_config['host'] + ':' + proxy_config['port']
    else:
        print("Proxy configuration file not found. Proxy will be ignored.")

proxies = None

if proxy_url:

    proxies = {
        'http': 'http://' + proxy_url,
        'https': 'https://' + proxy_url,
    }

verbose = args.verbose

if verbose:
    http_client.HTTPConnection.debuglevel = 1

    # You must initialize logging, otherwise you'll not see debug output.
    logging.basicConfig()
    logging.getLogger().setLevel(logging.DEBUG)
    requests_log = logging.getLogger("requests.packages.urllib3")
    requests_log.setLevel(logging.DEBUG)
    requests_log.propagate = True

access_token = None
refresh_token = None

# using user's password, get the first access token and the refresh token
def ask_access_token():
    try:
        password = getpass.getpass(prompt='Password for Shanoir user ' + username + ': ', stream=None) 
    except:
        exit(0)
    url = 'https://' + server_domain + '/auth/realms/shanoir-ng/protocol/openid-connect/token'
    payload = {
        'client_id' : 'shanoir-uploader', 
        'grant_type' : 'password', 
        'username' : username, 
        'password' : password,
        'scope' : 'offline_access'
    }
    headers = {'content-type': 'application/x-www-form-urlencoded'}
    print('get keycloak token...', end=' ')
    response = requests.post(url, data=payload, headers=headers, proxies=proxies)
    print('response status :', response.status_code, responses[response.status_code])
    response_json = json.loads(response.text)
    if 'error_description' in response_json and response_json['error_description'] == 'Invalid user credentials':
        print('bad username or password')
        exit(1)
    global refresh_token 
    refresh_token = response_json['refresh_token']
    return response_json['access_token']

def get_filename_from_response(response):
    return re.findall('filename=(.+)', response.headers.get('Content-Disposition'))[0]

try:
    from tqdm import tqdm

    def download_file(response):
        filename = str(dest_dir / get_filename_from_response(response))
        total = int(response.headers.get('content-length', 0))
        with open(filename, 'wb') as file, tqdm(
            desc=filename,
            total=total,
            unit='iB',
            unit_scale=True,
            unit_divisor=1024,
        ) as bar:
            for data in response.iter_content(chunk_size=1024):
                size = file.write(data)
                bar.update(size)

except ImportError as e:

    def download_file(response):
        filename = str(dest_dir / get_filename_from_response(response))
        open(filename, 'wb').write(response.content)
        return

# get a new acess token using the refresh token
def refresh_access_token():
    url = 'https://' + server_domain + '/auth/realms/shanoir-ng/protocol/openid-connect/token'
    payload = {
        'grant_type' : 'refresh_token',
        'refresh_token' : refresh_token,
        'client_id' : 'shanoir-uploader'
    }
    headers = {'content-type': 'application/x-www-form-urlencoded'}
    print('refresh keycloak token...', end=' ')
    response = requests.post(url, data=payload, headers=headers, proxies=proxies)
    print('response status :', response.status_code, responses[response.status_code])
    response_json = json.loads(response.text)
    return response_json['access_token']

def perform_rest_request(rtype, url, headers=None, params=None, files=None, stream=None):
    response = None
    # if verbose:
    #     print(dict(url=url, headers=headers, params=params, files=files, stream=stream))
    if rtype == 'get':
        response = requests.get(url, headers=headers, params=params, stream=stream, proxies=proxies)
    elif rtype == 'post':
        response = requests.post(url, headers=headers, params=params, files=files, stream=stream, proxies=proxies)
    else:
        print('Error: unimplemented request type')

    return response

# perform a request on the given url, asks for a new access token if the current one is outdated
def rest_request(rtype, url, params=None, files=None, stream=None):
    global access_token
    if access_token is None:
        access_token = ask_access_token()
    headers = { 
        'Authorization' : 'Bearer ' + access_token,
        'content-type' : 'application/json'
    }
    response = perform_rest_request(rtype, url, headers, params, files, stream)
    # if token is outdated, refresh it and try again
    if response.status_code == 401:
        access_token = refresh_access_token()
        headers['Authorization'] = 'Bearer ' + access_token
        response = perform_rest_request(rtype, url, headers, params, files, stream)
    if 'Content-Type' in response.headers and 'application/json' in response.headers['Content-Type']:
        # if the response is json data we return the body parsed
        return json.loads(response.text)
    else:
        # else just return the whole response
        return response

    
# perform a GET request on the given url, asks for a new access token if the current one is outdated
def rest_get(url, params=None, stream=None):
    return rest_request('get', url, params=params, stream=stream)

# perform a POST request on the given url, asks for a new access token if the current one is outdated
def rest_post(url, params=None, files=None, stream=None):
    return rest_request('post', url, params=params, files=files, stream=stream)

# # get every acquisition equipment from shanoir
# url = 'https://' + server_domain + '/shanoir-ng/studies/acquisitionequipments'
# print(json.dumps(rest_get(url), indent=4, sort_keys=True))

# # get one acquisition equipment
# url = 'https://' + server_domain + '/shanoir-ng/studies/acquisitionequipments/244'
# print(json.dumps(rest_get(url), indent=4, sort_keys=True))

# # download a given dataset as nifti into the current folder
# # !!! You might not have the right to download this dataset, change 100 to a dataset id that you can download
# url = 'https://' + server_domain + '/shanoir-ng/datasets/datasets/download/100?format=nii' # ?format=dcm for dicom
# dl_request = rest_get(url)
# filename = re.findall('filename=(.+)', dl_request.headers.get('Content-Disposition'))[0]
# open(filename, 'wb').write(dl_request.content)

def download_dataset(dest_dir, dataset_id, file_format):
    print('Downloading dataset', dataset_id)
    file_format = 'nii' if file_format == 'nifti' else 'dcm'
    url = 'https://' + server_domain + '/shanoir-ng/datasets/datasets/download/' + dataset_id
    dl_request = rest_get(url, params={ 'format': file_format })
    download_file(dl_request)
    return

def download_datasets(dest_dir, dataset_ids, file_format):
    print('Downloading datasets', dataset_ids)
    file_format = 'nii' if file_format == 'nifti' else 'dcm'
    dataset_ids = ','.join([str(dataset_id) for dataset_id in dataset_ids])
    url = 'https://' + server_domain + '/shanoir-ng/datasets/datasets/massiveDownload'
    params = dict(datasetIds=dataset_ids, forma=file_format)
    dl_request = rest_post(url, params=params, files=params, stream=True)
    download_file(dl_request)
    return

def download_dataset_by_study(dest_dir, study_id, file_format):
    print('Downloading datasets from study', study_id)
    file_format = 'nii' if file_format == 'nifti' else 'dcm'
    url = 'https://' + server_domain + '/shanoir-ng/datasets/datasets/massiveDownloadByStudy'
    dl_request = rest_get(url, params={ 'studyId': study_id, 'format': file_format })
    download_file(dl_request)
    return

def find_dataset_ids_by_subject_id(subject_id):
    print('Getting datasets from subject', subject_id)
    url = 'https://' + server_domain + '/shanoir-ng/datasets/datasets/subject/' + subject_id
    dataset_ids = rest_get(url)
    return dataset_ids

def find_dataset_ids_by_subject_id_study_id(subject_id, study_id):
    print('Getting datasets from subject', subject_id, 'and study', study_id)
    url = 'https://' + server_domain + '/shanoir-ng/datasets/datasets/subject/' + subject_id + '/study/' + study_id
    dataset_ids = rest_get(url)
    return dataset_ids

def download_dataset_by_subject(dest_dir, subject_id, file_format):
    dataset_ids = find_dataset_ids_by_subject_id(subject_id)
    download_datasets(dest_dir, dataset_ids, file_format)
    return

def download_dataset_by_subject_id_study_id(dest_dir, subject_id, study_id, file_format):
    dataset_ids = find_dataset_ids_by_subject_id_study_id(subject_id, study_id)
    download_datasets(dest_dir, dataset_ids, file_format)
    return

if not dataset_ids and dataset_id == '' and study_id == '' and subject_id == '':
    print('Either datasetId, studyId or subjectId must be given to download a dataset')
    parser.print_help()
    sys.exit()

if dataset_ids:
    with open(dataset_ids) as file:
        for dataset_id in file:
            download_dataset(dest_dir, dataset_id.strip(), file_format)

if dataset_id != '':
    download_dataset(dest_dir, dataset_id, file_format)
else:

    if study_id != '' and subject_id == '':
        download_dataset_by_study(dest_dir, study_id, file_format)
    
    if study_id == '' and subject_id != '':
        download_dataset_by_subject(dest_dir, subject_id, file_format)
        
    if study_id != '' and subject_id != '':
        download_dataset_by_subject_id_study_id(dest_dir, subject_id, study_id, file_format)
