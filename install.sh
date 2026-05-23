#!/usr/bin/env bash

set -euo pipefail

# Make sure script run with root privileges
[[ $(id -u) -eq 0 ]] || { printf 'root required!\n'; exit 1; }

source $(dirname $0)/info.sh
source $(dirname $0)/utils.sh

# Disable reflector
systemctl disable --now reflector.timer
systemctl disable --now reflector.service

# Check for internet
check_internet || connect_to_wifi "$wifi_ssid" "$wifi_password"
check_internet && printf "Connected to internet!\n"

# Waiting for datetime sync
waiting_for_datetime_sync

# Waiting for keyring sync
waiting_for_keyring_sync

# Configure mirrorlist
cp $(dirname $0)/mirrorlist /etc/pacman.d/mirrorlist

# Create partition layout
source $(dirname $0)/create_normal_ext4_disk_layout.sh

# Install essential packages via pacstrap
retry pacstrap /mnt base base-devel linux linux-headers \
      linux-lts linux-lts-headers linux-zen linux-zen-headers \
      linux-firmware man-pages man-db iptables bash-completion \
      usbutils systemd-ukify

#--------------------------------------------------------------------
# Configure zram
#--------------------------------------------------------------------
retry arch-chroot /mnt pacman -Syu --needed --noconfirm \
      zram-generator
cat > /mnt/etc/systemd/zram-generator.conf <<EOF
[zram0]
compression-algorithm = zstd
zram-size = ram / 2
EOF
#--------------------------------------------------------------------
# Configure zram
#--------------------------------------------------------------------

# Generate fstab
genfstab -U /mnt >> /mnt/etc/fstab

# Configure timezone, localization, keymap, hostname
systemd-firstboot --root=/mnt \
                  --locale=en_US.UTF-8 \
                  --keymap=us \
                  --timezone=Asia/Ho_Chi_Minh \
                  --hostname="$hostname"
arch-chroot /mnt hwclock --systohc
sed -i '/^#en_US.UTF-8/s/#//' /mnt/etc/locale.gen
arch-chroot /mnt locale-gen

# Enable multilib repository
sed -i '/^#\[multilib\]/,/^#Include/ s/^#//' /mnt/etc/pacman.conf

# Configurate NetworkManager
retry arch-chroot /mnt pacman -Syu --needed --noconfirm \
      networkmanager
arch-chroot /mnt systemctl enable NetworkManager

# # Set vconsole font size
# arch-chroot /mnt pacman -Syu --needed --noconfirm terminus-font
# grep -qxF 'FONT=ter-v32n' /mnt/etc/vconsole.conf \
    #     || printf 'FONT=ter-v32n\n' >> /mnt/etc/vconsole.conf
# arch-chroot /mnt mkinitcpio -P

# Setup account
arch-chroot /mnt usermod -p "$root_pwhash" root
arch-chroot /mnt id -u "$username" &>/dev/null \
    || arch-chroot /mnt useradd -m -s /bin/bash \
                   -G wheel \
                   -c "$realname" \
                   -p "$user_pwhash" \
                   "$username"

# Enable sudo for wheel
arch-chroot /mnt install -m 0440 -o root -g root \
            /dev/stdin /etc/sudoers.d/10-wheel \
            <<< '%wheel ALL=(ALL:ALL) ALL'

#  ------------------------------------------------------------------
# Configure systemd-boot
#  ------------------------------------------------------------------
arch-chroot /mnt pacman -Syu --needed --noconfirm \
            efibootmgr amd-ucode

# Configure kernel parameter
mkdir -p /mnt/etc/cmdline.d

cat > /mnt/etc/cmdline.d/root.conf <<EOF
root=UUID=$(arch-chroot /mnt findmnt -n -o UUID /)
rw
rootfstype=$(arch-chroot /mnt findmnt -n -o FSTYPE /)
EOF

cat > /mnt/etc/cmdline.d/zram.conf <<EOF
zswap.enabled=0
EOF

# Configure /etc/mkinitcpio.d/*.preset files
for file in /mnt/etc/mkinitcpio.d/*; do
    sed -i 's/^default_image/#&/' $file
    sed -i '/#default_uki/s/#//' $file
    sed -i '/#default_options/s/#//' $file
    sed -i '/#fallback_uki/s/#//' $file
done

# Create directory for the UKIs and regenerate the initramfs
mkdir -p /mnt/efi/EFI/Linux
arch-chroot /mnt mkinitcpio -P

# Install systemd-boot
arch-chroot -S /mnt bootctl --esp-path=/efi --boot-path=/boot install
cat > /mnt/efi/loader/loader.conf <<EOF
timeout 10
EOF
#  ------------------------------------------------------------------
# Configure systemd-boot
#  ------------------------------------------------------------------

# Install KVM
retry arch-chroot /mnt pacman -Syu --needed --noconfirm \
      - < "$(dirname "$0")/packages/kvm.txt"
arch-chroot /mnt systemctl enable libvirtd.socket virtlogd.socket
arch-chroot /mnt usermod -aG libvirt,kvm "$username"

# Install PipeWire
retry arch-chroot /mnt pacman -Syu --needed --noconfirm \
      - < "$(dirname "$0")/packages/pipewire.txt"

# Install GPU-agnostic packages
retry arch-chroot /mnt pacman -Syu --needed --noconfirm \
      - < "$(dirname "$0")/packages/gpu.txt"

# Install AMD GPU
retry arch-chroot /mnt pacman -Syu --needed --noconfirm \
      - < "$(dirname "$0")/packages/amd.txt"

# Install Docker
retry arch-chroot /mnt pacman -Syu --needed --noconfirm \
      - < "$(dirname "$0")/packages/docker.txt"
arch-chroot /mnt systemctl enable docker.socket
arch-chroot /mnt usermod -aG docker "$username"

# Install fonts
retry arch-chroot /mnt pacman -Syu --needed --noconfirm \
      - < "$(dirname "$0")/packages/font.txt"

# Install GNOME
retry arch-chroot /mnt pacman -Syu --needed --noconfirm \
      - < "$(dirname "$0")/packages/gnome.txt"
arch-chroot /mnt systemctl enable gdm.service
arch-chroot /mnt systemctl enable bluetooth.service

# Install core packages
retry arch-chroot /mnt pacman -Syu --needed --noconfirm \
      - < "$(dirname "$0")/packages/core.txt"

# -------------------------------------------------------------------
# yay AUR Helper and AUR installation
# -------------------------------------------------------------------
retry arch-chroot /mnt pacman -Syu --needed --noconfirm git

# Enable passwordless
arch-chroot /mnt install -m 0440 -o root -g root \
            /dev/stdin "/etc/sudoers.d/99-passwordless-$username" \
            <<< "$username ALL=(ALL:ALL) NOPASSWD: ALL"

# Cleanup on script exit
trap 'rm -f "/mnt/etc/sudoers.d/99-passwordless-$username"' EXIT

# Install yay
arch-chroot /mnt /usr/bin/su - "$username" -s /bin/bash \
            < "$(dirname "$0")/install_yay.sh"

# Install AUR packages
# Don't try to put $packages inside double quotes in the heredocs in
# this case, it cause error "package not found"
pkgfile="$(dirname "$0")/packages/aur.txt"
packages=$(grep -E '^.+$' "$pkgfile" | tr '\n' ' ')
arch-chroot /mnt /usr/bin/su - "$username" -s /bin/bash <<EOF
yay -Syu --needed --noconfirm $packages
EOF

# Disable passwordless (redundant action)
rm -f "/mnt/etc/sudoers.d/99-passwordless-$username"
trap - EXIT
# -------------------------------------------------------------------
# yay AUR Helper and AUR installation
# -------------------------------------------------------------------
