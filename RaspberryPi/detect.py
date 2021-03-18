from gpiozero import LightSensor
import datetime
import serial
import time

# LightSensor classes and methods
## Pause until no light is detected
	# wait_for_dark()
# ------------------------- 
## Pause until light is detected
	# wait_for_light()
# -------------------------
## Return True if light is detected
	# light_detected
# -------------------------
## Function to run when it is dark
	# when_dark = my_function
# -------------------------
## Function to run when it is light
	# when_light = my_function
# -------------------------
def try_send(timestamp):
	# https://towardsdatascience.com/sending-data-from-a-raspberry-pi-sensor-unit-over-serial-bluetooth-f9063f3447af
	# tty for bluetooth
	ser = serial.Serial('/dev/tty.raspberrypi-SerialPort', timeout=1, baudrate=115000)
	# flush
	serial.flushInput();serial.flushOutput()
        # https://www.arduino.cc/reference/en/language/functions/communication/serial/write/
	# Send data to device
	serial.write(F"Post received at: {timestamp}")
	# Sleep waiting for response
	time.sleep(2)
	# If we received reset message from device, reset warning
	if serial.readline().decode() == "Reset":
		return True
	# Else try again
	else:
		return False

def notification_set(value):
	timestamp = datetime.datetime.now().strftime("%H:%M:%S %m-%h-%Y")
	print(value)
	sent = False
	while True:
		# Try to send notification
		sent = try_send(timestamp)
		# If sent, reset notification value and break
		if sent:
			return True
		# else sleep 10 sec
		else:
			time.sleep(10)

# TODO
#  Debug
#  Create log
#  Test with app / device

# Should log
	# Time post arrived
	# Time phone was notified
if __name__=="__main__":	
	# Assumes connection on GPIO17
	ldr = LightSensor(17)
	# All values are given as float between 0 (dark) and 1 (light)
	print(ldr.value)
	# When it gets dark, send notification (beam broken)
	when_dark = notification_set(ldr.value)

