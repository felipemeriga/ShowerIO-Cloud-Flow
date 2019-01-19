//BathLogic FUNCTIONS .INO FILE
void flow () // Interrupt function
{
  flow_frequency++;
}

void initBathConfiguration() {
  pinMode(FLOW_SENSOR_PIN, INPUT);
  //digitalWrite(FLOW_SENSOR_PIN, HIGH); // Optional Internal Pull-Up
  attachInterrupt(FLOW_SENSOR_PIN, flow, RISING); // Setup Interrupt
  sei(); // Enable interrupts
  currentTime = millis();
  cloopTime = currentTime;
  waiting = false;
  bathRunning = false;
  showerIsOn = false;
  flow_frequency = 0;
  bathTime = 0;
  stoppedTime = 0;
  waitingTime = 0;
  l_hour = 0;
}

void computeBathWorking() {
  if (currentTime >= (cloopTime + 1000)) {
    bathTime++;
    // Pulse frequency (Hz) = 7.5Q, Q is flow rate in L/min.
    l_hour = (flow_frequency * 60 / 7.5); // (Pulse frequency x 60 min) / 7.5Q = flowrate in L/hour
    flow_frequency = 0; // Reset Counter
    cloopTime = currentTime;
  }
}

void computeBathStopped() {
  if (currentTime >= (cloopTime + 1000)) {
    cloopTime = currentTime;
    stoppedTime++;
  }
}

void computeWaitingTime() {
  if (currentTime >= (cloopTime + 1000)) {
    cloopTime = currentTime;
    waitingTime++;
  }
}

void bathProcess ()
{
  // TEST - Test if the variable currentTime will be good for all bathTime/waitingTime/stoppedTime
  currentTime = millis();

  if (waiting != true) {
    // We might have to modify it because of the pipe drain
    if (flow_frequency > 0) {
      showerIsOn = true;

      // Indicates that a new bath has started
      if (bathRunning == false && bathTime == 0) {
        bathRunning = true;
        bathTime = 1;
      }

      computeBathWorking();
    } else if (bathRunning == true) {
      showerIsOn = false;
      computeBathStopped();
    }


    //verify the bath time
    if (bathTime == EEPROM.read(address_tempo) * 60 && bathRunning == true) {
      //The bathTime was reached, turnoff the shower
      bathRunning = false;
      waiting = true;
      digitalWrite(rele, HIGH);
    }

    //Turn of the shower
    if (stoppedTime > 60) {
      bathRunning = false;
      waiting = true;
      digitalWrite(rele, HIGH);
    }

  } else {
    // TODO - Compute the bath statistics
    showerIsOn = false;
    waitingTime = waitingTime + stoppedTime;
    flow_frequency = 0;
    bathTime = 0;
    stoppedTime = 0;
    computeWaitingTime();

    if (waitingTime > EEPROM.read(address_pausa) * 60) {
      waiting = false;
      waitingTime = 0;
      digitalWrite(rele, LOW);
    }

  }
}
