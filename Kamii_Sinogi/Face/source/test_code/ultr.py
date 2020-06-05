import RPi.GPIO as GPIO
import datetime
import time

def gpio_init():
	GPIO.setmode(GPIO.BCM)
	pass

def ultrasound_init(echo, trig):
	gpio_init()

	GPIO.setup(echo, GPIO.IN)
	GPIO.setup(trig, GPIO.OUT)
	pass

def get_distance(echo, trig):
	ultrasound_init(echo, trig)

	send_time = 0
	rece_time = 0

	GPIO.output(trig, GPIO.LOW)
	time.sleep(0.002)
	GPIO.output(trig, GPIO.HIGH)
	time.sleep(0.000015)
	GPIO.output(trig, GPIO.LOW)

	while GPIO.input(echo) == 0:
		send_time = time.time()
		pass

	while GPIO.input(echo) == 1:
		rece_time = time.time()
		pass

	distance = (rece_time - send_time) * 340 / 2 * 100

	return distance

trig=27
echo=22

print get_distance(echo,trig)