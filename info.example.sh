#!/usr/bin/env bash

set -euo pipefail

disk= # e.g. /dev/nvme0n1, /dev/sda,...
luks_password= # put the password between single quotes

username=
realname=

# Pre-hashed password (output of: openssl passwd -6)
# Single quotes are important - the hash contains $ characters.
user_pwhash=
root_pwhash=

hostname=

# Wifi information
wifi_ssid=
wifi_password=
wifi_hidden='false'

# Make sure all necessary parameters are set
: ${disk:?}
: ${luks_password:?}
: ${username:?}
: ${realname:?}
: ${user_pwhash:?}
: ${root_pwhash:?}
: ${hostname:?}
: ${wifi_ssid:?}
: ${wifi_password:?}

# Determine the partition prefix based on the disk type.
# NVMe, MMC, and Loop devices require a 'p' suffix before the
# partition number (e.g., nvme0n1p1).
# Standard SCSI and Virtual disks do not (e.g., sda1, vda1).
if [[ $disk =~ nvme|mmcblk|loop ]]; then
    part_prefix="${disk}p"
else
    part_prefix="$disk"
fi
