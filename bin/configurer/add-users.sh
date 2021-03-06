#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})

jq -r '.[] | .email + " " + .roles + " " +  .lastName' ${dir}/users.json | while read args; do
  ${dir}/utils/idam-create-caseworker.sh $args
done

echo "\nGenerating local user mappings"
${dir}/utils/generate-local-user-mappings.sh
