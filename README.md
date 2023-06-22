# SolnyshkoLampControl
Alpha version. Timer has state tracking. Supports setting multiple timers with pause in between.<br>

Main switch outputs: <br>
  "relay:on#" - turn lamp on <br>
  "relay:off#" - turn lamp off<br>
  "timer:settimer:<seconds>#" - set timer<br>
  "timer:pause#" - pause timer<br>
  "timer:resume#" - resume paused timer<br>
  "timer:status#" - get remaining time in millis and timer status<br>
  "timer:gettime#" - get remaining time in millis<br>
  
Interface state changing codes:<br>
  "1#" = turn button state on<br>
  "2#" - turn button state off<br>
  "3#" = timer started or resumed<br>
  "4#" = timer finished<br>
  "5#" = timer paused<br>
  "time:<millis>#" - remaining time. 0 if timer is not active.
  
  
