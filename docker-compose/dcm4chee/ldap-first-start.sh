#!/bin/sh
set -eu

# Run only once for a given persisted LDAP data volume.
MARKER_FILE=/var/lib/openldap/openldap-data/.shanoir-ldap-first-start.done
if [ -f "${MARKER_FILE}" ]; then
  exit 0
fi

LDAP_STORAGE_ENTRY_DN=${LDAP_STORAGE_ENTRY_DN:-dcmStorageID=fs1,dicomDeviceName=dcm4chee-arc,cn=Devices,cn=DICOM Configuration,dc=dcm4che,dc=org}
LDAP_BASE_DN=${LDAP_BASE_DN:-dc=dcm4che,dc=org}
LDAP_BIND_DN="cn=admin,${LDAP_BASE_DN}"
LDAP_ROOTPASS=${LDAP_ROOTPASS:-secret}
LDAP_URLS=${LDAP_URLS:-ldap:///}
LDAP_PROPERTY_TARGET_DN=${LDAP_PROPERTY_TARGET_DN:-${LDAP_STORAGE_ENTRY_DN}}

# Optional URI update and optional dcmProperty additions.
HAS_URI_UPDATE=0
if [ -n "${LDAP_STORAGE_URI:-}" ]; then
  HAS_URI_UPDATE=1
fi

HAS_PROPERTY_UPDATE=0
# Support indexed properties from env_file, e.g. LDAP_DCM_PROPERTY_1=key=value.
if env | grep -q '^LDAP_DCM_PROPERTY_[0-9][0-9]*='; then
  HAS_PROPERTY_UPDATE=1
fi

# If nothing is requested, keep default config and mark as initialized.
if [ "${HAS_URI_UPDATE}" -eq 0 ] && [ "${HAS_PROPERTY_UPDATE}" -eq 0 ]; then
  touch "${MARKER_FILE}"
  exit 0
fi

if [ "${HAS_URI_UPDATE}" -eq 1 ]; then
cat <<EOF | ldapmodify -xw "${LDAP_ROOTPASS}" -D "${LDAP_BIND_DN}" -H "${LDAP_URLS}"
dn: ${LDAP_STORAGE_ENTRY_DN}
changetype: modify
replace: dcmURI
dcmURI: ${LDAP_STORAGE_URI}
EOF
fi

if [ "${HAS_PROPERTY_UPDATE}" -eq 1 ]; then
  # One property per variable via LDAP_DCM_PROPERTY_N.
  LDAP_TMP_FILE=$(mktemp)
  {
    echo "dn: ${LDAP_PROPERTY_TARGET_DN}"
    echo "changetype: modify"
    echo "add: dcmProperty"

    env | grep '^LDAP_DCM_PROPERTY_[0-9][0-9]*=' | sort -t_ -k4,4n | cut -d= -f2- | while IFS= read -r line; do
      trimmed=$(printf '%s' "${line}" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//;s/^dcmProperty[[:space:]]*//')
      if [ -n "${trimmed}" ]; then
        echo "dcmProperty: ${trimmed}"
      fi
    done
  } > "${LDAP_TMP_FILE}"

  ldapmodify -xw "${LDAP_ROOTPASS}" -D "${LDAP_BIND_DN}" -H "${LDAP_URLS}" -f "${LDAP_TMP_FILE}"
  rm -f "${LDAP_TMP_FILE}"
fi

touch "${MARKER_FILE}"
