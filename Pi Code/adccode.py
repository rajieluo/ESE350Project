import spidev
import time
import math

spi = spidev.SpiDev()
spi.open(0,1)

#Read SPI Sensor input 0 to 7 for MCP3008
def ReadInput(Sensor):
    adc = spi.xfer2([1,(8+Sensor)<<4,0])
    data = ((adc[1]&3) << 8) + adc[2]
    return data

while True:
    RawValue0 = ReadInput(0)    #Read the raw input from the chip
    RawValue1 = ReadInput(1)    #Read the raw input from the chip
    RawValue2 = ReadInput(2)    #Read the raw input from the chip
    print "Sensor No "+str(0),"Raw="+str(RawValue0), RawValue1, RawValue2
    time.sleep(2) #Delay to slow down display for use humans