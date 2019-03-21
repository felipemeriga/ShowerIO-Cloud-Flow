//BathLogic FUNCTIONS .INO FILE
void flow () // Interrupt function
{
  flow_frequency++;
  totalFlowFrequency++;

}

void computeBathStatistics(boolean bathReached) {
  double duration = 0;
  double litters = 0;

  if (bathReached) {
    duration = bathTime;
  } else {
    duration = ceil(((bathTime * 1000 * 60) - bathRemainingTime) / 60000);
  }

  litters = (duration / 60) * l_hour;
  updateBathStatistics(duration, litters);
  l_hour = 0;
}



void bathScanTimerReached(MillisTimer &mt) {
  if (flowLastValue == flow_frequency) {
    // Check if the flow is increasing, otherwise set shower state to stopped
    DBG_OUTPUT_PORT.println("flow frequency reseted");
    flow_frequency = 0;
  } else if (flow_frequency > 1000) {
    // Adding this validation to prevent overflow of the flow frequency
    flow_frequency = 1;
  }
  flowLastValue = flow_frequency;

  l_hour = (totalFlowFrequency * 60 / 7.5); // (Pulse frequency x 60 min) / 7.5Q = flowrate in L/hour
  bathScanTimmer.reset();
  bathScanTimmer.start();
  totalFlowFrequency = 0;
}

void bathWaitTimerReached(MillisTimer &mt) {

  waiting = false;
  digitalWrite(rele, LOW);

  // TODO - Compute the bath statistics
  DBG_OUTPUT_PORT.println("Bath waiting time reached! Now shower is enabled!");
  flow_frequency = 0;

  //Reseting the timers
  bathDurationTimer.reset();
  bathStopTimer.reset();
  bathWaitingTimer.reset();

  // Setting the interval again to ensure that it will be available for a new bath with the correct time
  bathDurationTimer.setInterval((bathTime * 1000 * 60));
  bathWaitingTimer.setInterval(bathWaitTime * 1000 * 60);
  bathStopTimer.setInterval(bathStoppedTime * 1000 * 60);
}

void bathTimeReached(MillisTimer &mt) {
  //computeBathStatistics(true);
  //The bathTime was reached, turnoff the shower
  bathRunning = false;
  waiting = true;
  digitalWrite(rele, HIGH);
  showerIsOn = false;

  bathWaitingTimer.setInterval(bathWaitTime * 1000 * 60);
  bathWaitingTimer.expiredHandler(bathWaitTimerReached);
  bathWaitingTimer.start();
  DBG_OUTPUT_PORT.println("Bath time reached! Triggering the wait time of: " + (String)bathWaitTime);

}

void bathStoppedTimerReached(MillisTimer &mt) {
  //computeBathStatistics(false);
  bathRunning = false;
  waiting = true;
  digitalWrite(rele, HIGH);
  showerIsOn = false;
  //TODO - Start wait time decreasing the stopped time reached
  bathWaitingTimer.setInterval(bathWaitTime * 1000 * 60);
  bathWaitingTimer.expiredHandler(bathWaitTimerReached);
  bathWaitingTimer.start();
  DBG_OUTPUT_PORT.println("Bath stop reached! Triggering the wait time of: " + (String)bathWaitTime);

}

void bathFalseAlarmReached(MillisTimer &mt) {
  DBG_OUTPUT_PORT.println("Bath False alarm remaining repeats: " + (String)mt.getRemainingRepeats());
  if (mt.getRemainingRepeats() == 0) {
    //The bath is really valid, starting it
    if (falseAlarmRunning) {
      DBG_OUTPUT_PORT.println("Initializing a new bath with max time: " + (String)bathTime);
      // TODO - Decrease with the false alarm time
      bathDurationTimer.setInterval((bathTime * 1000 * 60));
      bathDurationTimer.expiredHandler(bathTimeReached);
      bathDurationTimer.start();
      showerIsOn = true;

      bathScanTimmer.setInterval(1000);
      bathScanTimmer.expiredHandler(bathScanTimerReached);
      bathScanTimmer.start();

      DBG_OUTPUT_PORT.println("The bath stopped time is default set as 1 minutes");
      // TODO - CHANGE TO 60 * 1000 and to variable address_espera
      bathStopTimer.setInterval(bathStoppedTime * 1000 * 60);
      bathStopTimer.expiredHandler(bathStoppedTimerReached);
      // commented, because the time will go out even if the bath is running
      //bathStopTimer.start();
      bathRunning = true;

      showerFalseAlarmTesting = false;
      bathFalseAlarmTimmer.reset();
      falseAlarmRunning = false;
      flowLastValue = 0;
      flow_frequency = 0;
      totalFlowFrequency = 0;
    } else {
      DBG_OUTPUT_PORT.println("It may be a pipe leaking, triggering bath off");
      //It may be a pipe leaking, triggering bath off
      showerFalseAlarmTesting = false;
      bathFalseAlarmTimmer.reset();
      falseAlarmRunning = false;
      flow_frequency = 0;
      flowLastValue = 0;
      totalFlowFrequency = 0;
    }
  }

  if (flowLastValue == flow_frequency) {
    falseAlarmRunning = false;
  } else {
    falseAlarmRunning = true;
  }
  flowLastValue = flow_frequency;
}

void initBathConfiguration() {
  pinMode(FLOW_SENSOR_PIN, INPUT);
  //digitalWrite(FLOW_SENSOR_PIN, HIGH); // Optional Internal Pull-Up
  //attachInterrupt(FLOW_SENSOR_PIN, flow, RISING); // Setup Interrupt
  attachInterrupt(digitalPinToInterrupt(FLOW_SENSOR_PIN), flow, RISING);
  sei(); // Enable interrupts
  waiting = false;
  bathRunning = false;
  showerIsOn = false;
  flow_frequency = 0;
  l_hour = 0;
  flowLastValue = 0;
  totalFlowFrequency = 0;
}

//void computeBathWorking() {
//  if (currentTime >= (cloopTime + 1000)) {
//    bathTime++;
//    // Pulse frequency (Hz) = 7.5Q, Q is flow rate in L/min.
//    l_hour = (flow_frequency * 60 / 7.5); // (Pulse frequency x 60 min) / 7.5Q = flowrate in L/hour
//    flow_frequency = 0; // Reset Counter
//    cloopTime = currentTime;
//  }
//}
void bathProcess ()
{
  // TEST - Test if the variable currentTime will be good for all bathTime/waitingTime/stoppedTime
  if (!showerFalseAlarmTesting) {
    if (waiting != true) {
      // We might have to modify it because of the pipe drain
      if (flow_frequency > 0) {
        // Indicates that a new bath has started
        if (bathRunning == false && !bathDurationTimer.isRunning()) {
          //Before starting the balse the false alarm has to detect if it's really a bath or only a pipe leak
          bathFalseAlarmTimmer.setInterval(test_timer * 1000);
          bathFalseAlarmTimmer.expiredHandler(bathFalseAlarmReached);
          bathFalseAlarmTimmer.setRepeats(5);
          bathFalseAlarmTimmer.start();
          showerFalseAlarmTesting = true;
        }

        if (!showerFalseAlarmTesting) {
          if (!showerIsOn) {
            bathDurationTimer.start();
            DBG_OUTPUT_PORT.println("bath is running");
            stopRemainingTime = bathStopTimer.getRemainingTime();
            bathStopTimer.reset();
            bathStopTimer.setInterval(stopRemainingTime);
          }
          showerIsOn = true;
          bathDurationTimer.run();
        }

      } else if (bathRunning == true) {
        if (showerIsOn) {
          bathStopTimer.start();
          DBG_OUTPUT_PORT.println("bath is stopped");
          bathRemainingTime = bathDurationTimer.getRemainingTime();
          bathDurationTimer.reset();
          bathDurationTimer.setInterval(bathRemainingTime);
        }
        showerIsOn = false;
        bathStopTimer.run();
      }
      if (bathRunning == true) {
        bathScanTimmer.run();
      }
      // TODO - Implement the alert LED that the bath is finishing
    } else {
      bathWaitingTimer.run();
    }
  } else {
    bathFalseAlarmTimmer.run();
  }

}
