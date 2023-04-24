# SolnyshkoLampControl
Alpha version. Timer is working but state tracking is not developed yet. Added preheating timer. App structure is well thought out for future modification.

Main switch outputs: <br>
  "relay:on#" - turn lamp on <br>
  "relay:off#" - turn lamp off<br>
  "timer:settimer:<seconds>#" - set timer<br>
  "timer:pause#" - pause timer<br>
  "timer:resume#" - resume paused timer<br>
  "timer:status#" - get relay status<br>
  "timer:gettime#" - get time in millis<br>
  
Interface state changing codes:<br>
  "1" = turn button state on and hide timer stop button<br>
  "2" - turn button state off and hide timer stop button<br>
  "3" = timer started or resumed - turn button to timer playing state and reveal timer stop button<br>
  "4" = timer finished - turn button back to off state and hide timer stop button<br>
  "5" = timer paused - turn button to paused state and reveal timer stop button<br>
  
  
