# Code for reading values from the SPI ADC. Code adapted from https://github.com/WorldFamousElectronics/PulseSensor_Amped_Arduino
# to work on SPI ADC instead of IC2 ADC. 


import spidev
import time
import math
from bluetooth import *

server_sock=BluetoothSocket( RFCOMM )
server_sock.bind(("",PORT_ANY))
server_sock.listen(1)

spi = spidev.SpiDev();
spi.open(0,0);

spi1 = spidev.SpiDev();
spi1.open(0, 1);

# base X, Y, Z values on non-moving accelerometer
oldX = 510;
oldY = 499;
oldZ = 629;

# Method to read SPI sensor value, for inputs 0 to 7
# Taken from http://robsraspberrypi.blogspot.com/2016/01/raspberry-pi-adding-analogue-inputs.html
def ReadInput0(sensor):
    adc = spi.xfer2([1,(8+sensor)<<4,0]);
    data = ((adc[1]&3) << 8) + adc[2];
    return data;

def ReadInput1(sensor):
    adc = spi1.xfer2([1,(8+sensor)<<4,0]);
    data = ((adc[1]&3) << 8) + adc[2];
    return data;

def CalcMovement(x, y, z):
  return math.sqrt(math.pow(x-oldX, 2) + math.pow(y-oldY, 2) + math.pow(z-oldZ, 2))

def calcAverage(datapoint, oldaverage, numdatapoints) :
  return (oldaverage * numdatapoints + datapoint) / (numdatapoints + 1);


# Adapted from pulse seonsor supplied arduino code
# https://github.com/WorldFamousElectronics/PulseSensor_Amped_Arduino/tree/master/PulseSensorAmped_Arduino_1.5.0

sampleCounter = 0;   # for determining pulse timing
lastBeatTime = 0;    #for finding IBI
P = 512;             #used for finding peak
T = 512;             #used for finding trough
thresh = 530;         # used to find instant moment of beat
Pulse = False;        #pulse flag
IBI = 600;            #Interval between beats
rate = [0]*10;        #rate array for tracking
amp = 100;            #amplitude of pulse wave
sampleCounter = 0;   
lastBeatTime = 0;     #time of last beat
firstBeat = True;     #detecting first beat? DUN-dun
secondBeat = False;   #detecting second beat? dun-DUN
lastTime = 0;
curTime = 0;
msCounter = 0;

averageHeartrate = 0;
numHeartrateDatapoints = 0;
averageMovement = 0;
numMovementDatapoints = 0;

port = server_sock.getsockname()[1]

uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"

advertise_service( server_sock, "raspberrypi",
                   service_id = uuid,
                   service_classes = [ uuid, SERIAL_PORT_CLASS ],
                   profiles = [ SERIAL_PORT_PROFILE ], 
#                   protocols = [ OBEX_UUID ] 
                    )
                   
print "Waiting for connection on RFCOMM channel %d" % port

client_sock, client_info = server_sock.accept()
print "Accepted connection from ", client_info

# Main loop
while True:
  time.sleep(0.005);                           # interrupt every 2 ms       
  # read from the ADC
  x = ReadInput1(0);
  y = ReadInput1(1);
  z = ReadInput1(2);
  Signal = ReadInput0(0);                       # Read from SPI port 0
  sampleCounter += 5;
  N = sampleCounter - lastBeatTime;           # monitor the time since the last beat to avoid noise
  #print N, Signal, curTime, sampleCounter, lastBeatTime

  #  find the peak and trough of the pulse wave
  if Signal < thresh and N > (IBI/5.0)*3.0 :  # avoid dichrotic noise by waiting 3/5 of last IBI
      if Signal < T :                         # T is the trough
        T = Signal;                           # keep track of lowest point in pulse wave 

  if Signal > thresh and Signal > P:          # thresh condition helps avoid noise
      P = Signal;                             # P is the peak

    #  NOW IT'S TIME TO LOOK FOR THE HEART BEAT
    # signal surges up in value every time there is a pulse
  if N > 250 :                                # avoid high frequency noise
      if  (Signal > thresh) and (Pulse == False) and (N > (IBI/5.0)*3.0)  :       
        Pulse = True;                         # set the Pulse flag when we think there is a pulse
        IBI = sampleCounter - lastBeatTime;   # measure time between beats in mS
        lastBeatTime = sampleCounter;         # keep track of time for next pulse

        if secondBeat :                       # if this is the second beat, if secondBeat == TRUE
          secondBeat = False;                 # clear secondBeat flag
          for i in range(0,9):                # seed the running total to get a realisitic BPM at startup
            rate[i] = IBI;                      

        if firstBeat :                        # if it's the first time we found a beat, if firstBeat == TRUE
          firstBeat = False;                  # clear firstBeat flag
          secondBeat = True;                  # set the second beat flag
          continue;                            # IBI value is unreliable so discard it

        # keep a running total of the last 10 IBI values
        runningTotal = 0;                     # clear the runningTotal variable    

        for i in range(0,8):                  # shift data in the rate array
          rate[i] = rate[i+1];                # and drop the oldest IBI value 
          runningTotal += rate[i];            # add up the 9 oldest IBI values

        rate[9] = IBI;                        # add the latest IBI to the rate array
        runningTotal += rate[9];              # add the latest IBI to runningTotal
        runningTotal /= 10;                   # average the last 10 IBI values 
        BPM = 60000/runningTotal;             # how many beats can fit into a minute? that's BPM!
        
        movement = CalcMovement(x,y,z)
        print "BPM: " + str(BPM);
        print "Sensor No "+str(1),"x="+str(x), ", y="+str(y), ", z="+str(z);
        print "movement=" + str(movement);
        averageMovement = calcAverage(movement, averageMovement, numMovementDatapoints);
        numMovementDatapoints += 1;
        oldX = x;
        oldY = y;
        oldZ = z;
        averageHeartrate = calcAverage(BPM, averageHeartrate, numHeartrateDatapoints);
        numHeartrateDatapoints += 1;


  if Signal < thresh and Pulse == True :      # when the values are going down, the beat is over
      Pulse = False;                          # reset the Pulse flag so we can do it again
      amp = P - T;                            # get amplitude of the pulse wave
      thresh = amp/2 + T;                     # set thresh at 50% of the amplitude
      P = thresh;                             # reset these for next time
      T = thresh;

  if N > 2500 :                               # if 2.5 seconds go by without a beat
      thresh = 512;                           # set thresh default
      P = 512;                                # set P default
      T = 512;                                # set T default
      lastBeatTime = sampleCounter;           # bring the lastBeatTime up to date        
      firstBeat = True;                       # set these to avoid noise
      secondBeat = False;                     # when we get the heartbeat back
      print "no beats";
      print "Sensor No "+ str(1),"x="+str(x), ", y="+str(y), ", z="+str(z);
      movement = CalcMovement(x,y,z);
      print "movement=" + str(movement);
      averageMovement = calcAverage(movement, averageMovement, numMovementDatapoints);
      numMovementDatapoints += 1;
      oldX = x;
      oldY = y;
      oldZ = z;

  msCounter += 5;
  if (msCounter >= 60000) :
    msCounter = 0;              #reset counter
    print time.strftime("%a, %d %b %Y %H:%M:%S +0000", time.gmtime());
    print "avg movement=" + str(averageMovement) + "avg heartrate=" + str(averageHeartrate);
    client_sock.send(averageMovement, averageHeartrate)

