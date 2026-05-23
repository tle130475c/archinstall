#!/usr/bin/env bash

set -euo pipefail

builddir=$(mktemp -d)
trap 'rm -rf "$builddir"' EXIT

git clone --depth=1 https://aur.archlinux.org/yay.git "$builddir/yay"
cd "$builddir/yay"
makepkg -sri --noconfirm
