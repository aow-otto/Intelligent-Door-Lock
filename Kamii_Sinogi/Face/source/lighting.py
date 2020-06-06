import RPi.GPIO
import time
import json
import threading
dictionary = {"red": [255, 0, 0], "green": [0, 255, 0], "qing": [0, 255, 255],
              "blue": [0, 0, 255], "white": [255, 255, 255], "black": [0, 0, 0]}
statu = {"still": 0, "sblink": 0.25, "blink": 0.5, "lblink": 1}
# def light_still(colour="green"):

R, G, B = 18, 4, 17
RPi.GPIO.setmode(RPi.GPIO.BCM)

RPi.GPIO.setup(R, RPi.GPIO.OUT)
RPi.GPIO.setup(G, RPi.GPIO.OUT)
RPi.GPIO.setup(B, RPi.GPIO.OUT)

pwmR = RPi.GPIO.PWM(R, 50)
pwmG = RPi.GPIO.PWM(G, 50)
pwmB = RPi.GPIO.PWM(B, 50)

pwmR.start(0)
pwmG.start(0)
pwmB.start(0)

while True:
	try:
		with open("/home/pi/Kamii_Sinogi/Face/source/light.json") as file_open:
			light_set=json.load(file_open)
		file_open.close()
		if type(light_set["status"]) == unicode:
			light_set["status"]=statu[light_set["status"]]
		for i in range(3):
			light_set["colour"][i] = light_set["colour"][i] / 255.0 * 100.0
		if light_set["status"] == 0:
			pwmR.ChangeDutyCycle(light_set["colour"][0])
			pwmG.ChangeDutyCycle(light_set["colour"][1])
			pwmB.ChangeDutyCycle(light_set["colour"][2])
			time.sleep(0.5)
		else:
			pwmR.ChangeDutyCycle(light_set["colour"][0])
			pwmG.ChangeDutyCycle(light_set["colour"][1])
			pwmB.ChangeDutyCycle(light_set["colour"][2])
			time.sleep(light_set["status"])
			if light_set["status"] > 0:
				pwmR.ChangeDutyCycle(0)
				pwmG.ChangeDutyCycle(0)
				pwmB.ChangeDutyCycle(0)
				time.sleep(light_set["status"])
	except KeyboardInterrupt:
		break
	except:
		print light_set
    # time.sleep(1)
# p = light("white", "still")
# p.start_still()
