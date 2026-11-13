#!/usr/bin/env python3
"""Query the local Neurobagel nodes, the way the public query tool would.

Each instance is found by its config, nb-<instance>.conf next to this script,
which gives the port of its node (API_PORT).

Usage:
  python3 query.py                                   # every instance, every dataset
  python3 query.py ofsep                             # one instance
  python3 query.py neurinfo ofsep --modality T1w     # several instances, filtered
  python3 query.py ofsep --min-age 35 --max-age 60
"""
import argparse
import json
import sys
import urllib.error
import urllib.request
from pathlib import Path

HERE = Path(__file__).resolve().parent
NIDM = "http://purl.org/nidash/nidm#"

# option value -> (term sent to the API, readable label)
MODALITIES = {
    "T1w": ("T1Weighted", "T1-weighted"),
    "T2w": ("T2Weighted", "T2-weighted"),
    "dwi": ("DiffusionWeighted", "diffusion-weighted"),
    "bold": ("FlowWeighted", "BOLD (functional)"),
    "asl": ("ArterialSpinLabeling", "arterial spin labeling"),
    "eeg": ("Electroencephalography", "EEG"),
    "meg": ("Magnetoencephalography", "MEG"),
    "pet": ("PositronEmissionTomography", "PET"),
}
LABELS = {term: label for term, label in MODALITIES.values()}


def instances():
    """Every instance configured next to this script, by name (nb-proxy.conf is the proxy's, not an instance)."""
    return sorted(name for name in (p.name[3:-5] for p in HERE.glob("nb-*.conf")) if name != "proxy")


def read_conf(name):
    """The KEY=VALUE lines of nb-<name>.conf (the shell syntax nb-node.sh sources)."""
    conf = {}
    for line in (HERE / f"nb-{name}.conf").read_text().splitlines():
        line = line.strip()
        if line and not line.startswith("#") and "=" in line:
            key, value = line.split("=", 1)
            conf[key.strip()] = value.strip().strip("'\"")
    return conf


def query_node(port, query):
    request = urllib.request.Request(
        f"http://127.0.0.1:{port}/datasets",
        data=json.dumps(query).encode(),
        headers={"Content-Type": "application/json"},
    )
    with urllib.request.urlopen(request, timeout=60) as response:
        return json.load(response)


def describe(args):
    parts = []
    if args.modality:
        parts.append(f"{MODALITIES[args.modality][1]} imaging")
    if args.min_age is not None and args.max_age is not None:
        parts.append(f"aged {args.min_age:g} to {args.max_age:g}")
    elif args.min_age is not None:
        parts.append(f"aged {args.min_age:g} or more")
    elif args.max_age is not None:
        parts.append(f"aged {args.max_age:g} or less")
    return "subjects with " + ", ".join(parts) if parts else "all subjects"


def show_instance(name, query):
    """Print one instance's results; return False if its node could not be queried."""
    conf = read_conf(name)
    port = conf.get("API_PORT", "")
    source = conf.get("SHANOIR_URL") or "Shanoir URL not set"
    print(f"\n== {name}  ({source})")
    if not port.isdigit():
        print(f"   API_PORT is not set in nb-{name}.conf")
        return False
    try:
        datasets = query_node(port, query)
    except (urllib.error.URLError, OSError) as error:
        reason = getattr(error, "reason", error)
        print(f"   The node does not answer on port {port} ({reason}).")
        print(f"   Check it with: bash nb-node.sh status {name}")
        return False

    if not datasets:
        print("   No matching dataset.")
        return True
    for d in sorted(datasets, key=lambda d: d["dataset_name"].lower()):
        terms = (m.replace(NIDM, "").split("#")[-1] for m in d.get("image_modals", []))
        modalities = ", ".join(sorted(LABELS.get(t, t) for t in terms)) or "none"
        print(f"\n   {d['dataset_name']}")
        print(f"     Matching subjects: {d['num_matching_subjects']} of {d['dataset_total_subjects']}")
        print(f"     Imaging:           {modalities}")
        if d.get("homepage"):
            print(f"     Homepage:          {d['homepage']}")
        if d.get("records_protected"):
            print("     Subject details:   hidden (counts only)")
    matching = sum(d["num_matching_subjects"] for d in datasets)
    print(f"\n   {len(datasets)} dataset(s), {matching} matching subject(s)")
    return True


def main():
    known = instances()
    parser = argparse.ArgumentParser(
        description="Query the local Neurobagel nodes by instance name.",
        epilog=f"Configured instances: {', '.join(known) or 'none'}",
    )
    parser.add_argument("instance", nargs="*",
                        help="instance(s) to query, as in nb-<instance>.conf (default: all of them)")
    parser.add_argument("--modality", choices=MODALITIES, help="only subjects with this imaging modality")
    parser.add_argument("--min-age", type=float, metavar="YEARS")
    parser.add_argument("--max-age", type=float, metavar="YEARS")
    args = parser.parse_args()

    if not known:
        parser.error(f"no instance configured: no nb-<instance>.conf in {HERE}")
    unknown = [name for name in args.instance if name not in known]
    if unknown:
        parser.error(f"unknown instance(s): {', '.join(unknown)} (configured: {', '.join(known)})")

    query = {}
    if args.modality:
        query["image_modal"] = [f"nidm:{MODALITIES[args.modality][0]}"]
    if args.min_age is not None:
        query["min_age"] = args.min_age
    if args.max_age is not None:
        query["max_age"] = args.max_age

    print(f"Query: {describe(args)}")
    ok = [show_instance(name, query) for name in (args.instance or known)]
    sys.exit(0 if all(ok) else 1)


if __name__ == "__main__":
    main()
