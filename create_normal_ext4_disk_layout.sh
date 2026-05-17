#!/usr/bin/env bash

set -euo pipefail

# Make sure script run with root privileges
[[ $(id -u) -eq 0 ]] || { printf 'root required!\n'; exit 1; }

# Make sure all necessary parameters are set
: ${disk:?}
: ${part_prefix:?}

part_esp="${part_prefix}1"
part_xbootldr="${part_prefix}2"
part_root="${part_prefix}3"

# Clean up
umount -R /mnt 2>/dev/null || true
wipefs -a -f "$disk"
sgdisk -Z "$disk"
partprobe "$disk"

# Create ESP partition
sgdisk -n 0:0:+2GiB -t 0:ef00 -c 0:esp "$disk"
partprobe "$disk"

# Create XBOOTLDR partition
sgdisk -n 0:0:+2GiB -t 0:ea00 -c 0:XBOOTLDR "$disk"

# Create root partition
sgdisk -n 0:0:0 -t 0:8304 -c 0:root "$disk"
partprobe "$disk"

# Format partition
mkfs.fat -F32 "$part_esp"
mkfs.fat -F32 "$part_xbootldr"
mkfs.ext4 "$part_root"

# Mount partition
mount -o noatime "$part_root" /mnt
mount --mkdir "$part_esp" /mnt/efi
mount --mkdir "$part_xbootldr" /mnt/boot
