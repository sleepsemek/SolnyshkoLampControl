# SolnyshkoLampControl
Alpha version. Can set multiple timers. Preheating is client-side for the latest release.

Main switch outputs: <br>
  "relay:on#" - turn lamp on <br>
  "relay:off#" - turn lamp off<br>
  "timer:settimer:<seconds>#" - set timer<br>
  "timer:pause#" - pause timer<br>
  "timer:resume#" - resume paused timer<br>
  "timer:status#" - get time in millis and timer status<br>
  "timer:gettime#" - get time in millis<br>
  
Interface state changing codes:<br>
  "1" = turn button state on<br>
  "2" - turn button state off<br>
  "3" = timer started or resumed<br>
  "4" = timer finished<br>
  "5" = timer paused<br>
  "time:XXXXXX" = leftower time<br>
  
  
