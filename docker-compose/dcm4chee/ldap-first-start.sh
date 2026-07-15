#!/bin/sh
set -eu

# Run only once for a given persisted LDAP data volume.
MARKER_FILE=/var/lib/openldap/openldap-data/.shanoir-ldap-first-start.done
if [ -f "${MARKER_FILE}" ]; then
  exit 0
fi

# If no custom URI is provided, keep default config and mark as initialized.
if [ -z "${LDAP_STORAGE_URI:-}" ]; then
  touch "${MARKER_FILE}"
  exit 0
fi

LDAP_STORAGE_ENTRY_DN=${LDAP_STORAGE_ENTRY_DN:-dcmStorageID=fs1,dicomDeviceName=dcm4chee-arc,cn=Devices,cn=DICOM Configuration,dc=dcm4che,dc=org}
LDAP_BASE_DN=${LDAP_BASE_DN:-dc=dcm4che,dc=org}
LDAP_BIND_DN="cn=admin,${LDAP_BASE_DN}"
LDAP_ROOTPASS=${LDAP_ROOTPASS:-secret}
LDAP_URLS=${LDAP_URLS:-ldap:///}

cat <<EOF | ldapmodify -xw "${LDAP_ROOTPASS}" -D "${LDAP_BIND_DN}" -H "${LDAP_URLS}"
dn: ${LDAP_STORAGE_ENTRY_DN}
changetype: modify
replace: dcmURI
dcmURI: ${LDAP_STORAGE_URI}
EOF

touch "${MARKER_FILE}"
