#!/usr/bin/env python3
"""Query the local Neurobagel node API, the way the public query tool would.

Usage: python3 query.py                              # every dataset
       python3 query.py --modality T1w --min-age 35  # filtered
Modalities: T1w T2w dwi bold asl eeg meg pet
"""
import argparse
import json
import os
import urllib.request

MODALITIES = {
    "T1w": "nidm:T1Weighted", "T2w": "nidm:T2Weighted", "dwi": "nidm:DiffusionWeighted",
    "bold": "nidm:FlowWeighted", "asl": "nidm:ArterialSpinLabeling", "eeg": "nidm:Electroencephalography",
    "meg": "nidm:Magnetoencephalography", "pet": "nidm:PositronEmissionTomography",
}

parser = argparse.ArgumentParser()
parser.add_argument("--modality", choices=MODALITIES)
parser.add_argument("--min-age", type=float)
parser.add_argument("--max-age", type=float)
parser.add_argument("--port", default=os.environ.get("API_PORT", "8000"))
args = parser.parse_args()

query = {}
if args.modality:
    query["image_modal"] = [MODALITIES[args.modality]]
if args.min_age is not None:
    query["min_age"] = args.min_age
if args.max_age is not None:
    query["max_age"] = args.max_age

request = urllib.request.Request(
    f"http://127.0.0.1:{args.port}/datasets",
    data=json.dumps(query).encode(),
    headers={"Content-Type": "application/json"},
)
results = json.load(urllib.request.urlopen(request))

print("query:", query or "(all)")
if not results:
    print("no matching dataset")
for d in results:
    modalities = sorted(m.split("#")[-1] for m in d["image_modals"])
    print(f"- {d['dataset_name']}: {d['num_matching_subjects']}/{d['dataset_total_subjects']} subjects match"
          f" | modalities: {', '.join(modalities) or 'none'} | homepage: {d['homepage']}"
          f" | subject-level data hidden: {d['records_protected']}")
