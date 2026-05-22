#!/usr/bin/env bash

set -euo pipefail

retry() {
    local -i attempt=1
    local -i max_attempts=5
    local -i delay=2

    until
        "$@" && return 0
    do
        [[ "$attempt" -ge "$max_attempts" ]] && return 1
        sleep "$delay"
        ((++attempt))
        ((delay*=2))
    done
}

check_internet() {
    local url='http://nmcheck.gnome.org/check_network_status.txt'
    local expected='NetworkManager is online'
    local response

    if response="$(curl --silent --fail --max-time 5 \
                          --user-agent 'unknown' "$url")"
       [[ "$response" == "$expected" ]]
    then
        return 0
    fi

    return 1
}

connect_to_wifi() {
    local ssid=$1
    local password=$2
    local station=${3:-wlan0}
    local hidden=${4:-false}

    local subcmd
    if [[ "$hidden" == "true" ]]; then
        subcmd="connect-hidden"
    else
        subcmd="connect"
    fi

    retry iwctl --passphrase="$password" \
          station "$station" "$subcmd" "$ssid"
}

waiting_for_datetime_sync() {
    local -i timeout=60
    local -i waited=0

    until
        status="$(timedatectl show -p NTPSynchronized --value)"
        [[ "$status" == "yes" ]] && return 0
    do
        [[ "$waited" -ge "$timeout" ]] && return 1
        sleep 1
        ((++waited))
    done
}

waiting_for_keyring_sync() {
    local -i timer_timeout=60
    local -i service_timeout=300
    local -i waited
    local ts state

    # Wait until timer has fired at least one
    waited=0
    until
        ts="$(systemctl show -p ActiveEnterTimestamp --value \
              archlinux-keyring-wkd-sync.timer 2>/dev/null)"
        [[ -n "$ts" ]]
    do
        [[ "$waited" -ge "$timer_timeout" ]] && return 1
        sleep 1
        ((++waited))
    done

    # Wait until the service has reached a terminal state
    waited=0
    until
        state="$(systemctl show -p SubState --value \
                 archlinux-keyring-wkd-sync.service 2>/dev/null)"
        [[ "$state" == "dead" ]] \
            || [[ "$state" == "failed" ]] \
            || [[ "$state" == "exited" ]]
    do
        [[ "$waited" -ge "$service_timeout" ]] && return 1
        sleep 1
        ((++waited))
    done
}
