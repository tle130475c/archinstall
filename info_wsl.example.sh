#!/usr/bin/env bash

set -euo pipefail

username=
realname=

# Pre-hashed password (output of: openssl passwd -6)
# Single quotes are important - the hash contains $ characters.
user_pwhash=
root_pwhash=

# Make sure all necessary parameters are set
: ${username:?}
: ${realname:?}
: ${user_pwhash:?}
: ${root_pwhash:?}
