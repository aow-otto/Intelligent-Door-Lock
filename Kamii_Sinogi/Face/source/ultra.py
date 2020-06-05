import time,opt
import RPi.GPIO as GPIO

def DistanceMeasure(distance_limit=0.2,time_limit=1,time_delay=10):
	GPIO.setmode(GPIO.BCM)
	trig=27;GPIO.setup(trig,GPIO.OUT)
	echo=22;GPIO.setup(echo,GPIO.IN)
	count=0
	while 39:
		GPIO.output(trig,1)
		time.sleep(0.00001)
		GPIO.output(trig,0)
	#	while GPIO.input(echo)==0: pass
		pulse_start=time.time()
		while GPIO.input(echo)==1: pass
		pulse_end=time.time()
		pulse_time=pulse_end-pulse_start
		distance=pulse_time*171.5
		if distance>distance_limit: count=0
		else:
			count+=1
			if count>distance_limit*1000:
				stp=opt.CameraJudge()
				if stp==0: 
					# print "shibieshibai"
					time.sleep(time_delay)
				else: return 1
				count=0

# DistanceMeasure()