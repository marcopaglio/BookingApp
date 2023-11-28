#/bin/bash
NEW_DISPLAY=0
DONE="no"

while [ "$DONE" == "no" ]
do
  out=$(xdpyinfo -display :${NEW_DISPLAY} 2>&1)
  if [[ "$out" == name* ]] || [[ "$out" == Invalid* ]]
  then
    # command succeeded; or failed with access error;  display exists
    DONE="yes"
  else
    # display doesn't exist
    (( NEW_DISPLAY+=1 ))
  fi
done

echo "Using first available display :${NEW_DISPLAY}"

OLD_DISPLAY=${DISPLAY}
x11vnc -display :${NEW_DISPLAY} -noxrecord -noxfixes -noxdamage -forever -passwd 123456
export DISPLAY=:${NEW_DISPLAY}