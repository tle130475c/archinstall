#!/usr/bin/env bash

set -euo pipefail

# Make sure script run with root privileges
[[ $(id -u) -eq 0 ]] || { printf 'root required!\n'; exit 1; }

source $(dirname $0)/info_wsl.sh
source $(dirname $0)/utils.sh

# Configure mirrorlist
cp $(dirname $0)/mirrorlist /etc/pacman.d/mirrorlist

# Configure timezone, localization, keymap
ln -sf /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime
printf "en_US.UTF-8 UTF-8\n" > /etc/locale.gen
printf "LANG=en_US.UTF-8\n" > /etc/locale.conf
printf "KEYMAP=us\n" > /etc/vconsole.conf
locale-gen

# Setup account
usermod -p "$root_pwhash" root
id -u "$username" &>/dev/null || useradd -m -s /bin/bash \
                                         -G wheel \
                                         -c "$realname" \
                                         -p "$user_pwhash" \
                                         "$username"

# Enable sudo for wheel
retry pacman -Syu --needed --noconfirm sudo
install -m 0440 -o root -g root \
        /dev/stdin /etc/sudoers.d/10-wheel \
        <<< '%wheel ALL=(ALL:ALL) ALL'

# Install Docker
retry pacman -Syu --needed --noconfirm \
      - < "$(dirname "$0")/packages/docker.txt"
systemctl disable --now systemd-networkd-wait-online.service
systemctl enable docker.socket
usermod -aG docker "$username"

# Install core packages
retry pacman -Syu --needed --noconfirm base-devel bash-completion \
      git github-cli azure-cli tree postgresql-libs uv nvm maven \
      eslint prettier jq

# -------------------------------------------------------------------
# yay AUR Helper and AUR installation
# -------------------------------------------------------------------
retry pacman -Syu --needed --noconfirm git base-devel

# Enable passwordless
install -m 0440 -o root -g root \
        /dev/stdin "/etc/sudoers.d/99-passwordless-$username" \
        <<< "$username ALL=(ALL:ALL) NOPASSWD: ALL"

# Cleanup on script exit
trap 'rm -f "/etc/sudoers.d/99-passwordless-$username"' EXIT

# Install yay
/usr/bin/su - "$username" -s /bin/bash \
            < "$(dirname "$0")/install_yay.sh"

# Disable passwordless (redundant action)
rm -f "/etc/sudoers.d/99-passwordless-$username"
trap - EXIT
# -------------------------------------------------------------------
# yay AUR Helper and AUR installation
# -------------------------------------------------------------------
