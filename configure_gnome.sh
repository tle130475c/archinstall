#!/usr/bin/env bash

set -euo pipefail

gnome-extensions enable appindicatorsupport@rgcjonas.gmail.com

gsettings set org.gnome.desktop.interface font-antialiasing rgba
gsettings set org.gnome.desktop.interface clock-show-weekday true
gsettings set org.gnome.desktop.interface \
          show-battery-percentage true
gsettings set org.gnome.desktop.interface clock-format 24h
gsettings set org.gnome.nautilus.preferences \
          default-folder-viewer list-view
gsettings set org.gnome.settings-daemon.plugins.power \
          sleep-inactive-battery-type nothing
gsettings set org.gnome.settings-daemon.plugins.power \
          sleep-inactive-ac-type nothing
