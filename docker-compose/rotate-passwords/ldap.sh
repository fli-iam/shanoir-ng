# Shanoir NG - Import, manage and share neuroimaging data
# Copyright (C) 2009-2019 Inria - https://www.inria.fr/
# Contact us on https://project.inria.fr/shanoir/
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# You should have received a copy of the GNU General Public License
# along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html

# Run by rotate-passwords.sh with sh inside the 'ldap' container: sets the passwords of the two
# LDAP admin accounts to the values the container was created with (.env):
#  - cn=admin,cn=config            LDAP_CONFIGPASS
#  - cn=admin,<base dn>            LDAP_ROOTPASS (also used by dcm4chee-arc)
# Both are the olcRootPW of their database in cn=config, changed while bound as cn=admin,cn=config.
# OLD_CONFIG_PASSWORD: current password of cn=admin,cn=config, when it is neither the new one nor
# 'secret' (the image default).

set -e
umask 077

tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT

# passwords go through files (-y, -T), not the command line
pwfile() { printf '%s' "$2" > "$tmp/$1"; echo "$tmp/$1"; }

for i in $(seq 60) ; do
	ldapsearch -x -H ldap://localhost -s base -b '' >/dev/null 2>&1 && break
	sleep 1
done

config_pw=
for pw in "$LDAP_CONFIGPASS" "${OLD_CONFIG_PASSWORD:-}" secret ; do
	if [ -n "$pw" ] && ldapwhoami -x -H ldap://localhost -D cn=admin,cn=config -y "$(pwfile try "$pw")" >/dev/null 2>&1 ; then
		config_pw="$pw"
		break
	fi
done
if [ -z "$config_pw" ] ; then
	echo "error: cannot bind as cn=admin,cn=config with the password of .env nor with 'secret':" \
		"set the current one in SHANOIR_OLD_PACS_LDAP_CONFIG_PASSWORD (see README)" >&2
	exit 1
fi
pwfile config "$config_pw" >/dev/null

# database of the base dn (the one whose root dn is not cn=admin,cn=config)
data_db=$(ldapsearch -LLL -x -H ldap://localhost -D cn=admin,cn=config -y "$tmp/config" \
		-b cn=config '(olcRootDN=*)' olcRootDN \
	| awk '/^dn: /{dn=substr($0,5)} /^olcRootDN: /{if ($2 != "cn=admin,cn=config") print dn}')
root_dn=$(ldapsearch -LLL -x -H ldap://localhost -D cn=admin,cn=config -y "$tmp/config" \
		-b "$data_db" -s base olcRootDN | awk '/^olcRootDN: /{print $2}')
if [ -z "$data_db" ] || [ -z "$root_dn" ] ; then
	echo "error: cannot find the LDAP database of the base dn" >&2
	exit 1
fi

root_hash=$(slappasswd -T "$(pwfile root "$LDAP_ROOTPASS")")
config_hash=$(slappasswd -T "$(pwfile newconfig "$LDAP_CONFIGPASS")")
ldapmodify -x -H ldap://localhost -D cn=admin,cn=config -y "$tmp/config" >/dev/null <<EOF
dn: $data_db
changetype: modify
replace: olcRootPW
olcRootPW: $root_hash

dn: olcDatabase={0}config,cn=config
changetype: modify
replace: olcRootPW
olcRootPW: $config_hash
EOF
echo "    $root_dn: password set"
echo "    cn=admin,cn=config: password set"

if ldapwhoami -x -H ldap://localhost -D "$root_dn" -y "$tmp/root" >/dev/null 2>&1 &&
   ldapwhoami -x -H ldap://localhost -D cn=admin,cn=config -y "$tmp/newconfig" >/dev/null 2>&1 ; then
	echo "    checked: new passwords accepted"
else
	echo "error: an LDAP admin account cannot bind with its new password" >&2
	exit 1
fi
