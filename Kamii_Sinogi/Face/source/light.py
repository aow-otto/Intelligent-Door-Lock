import RPi.GPIO
import time
import threading
dictionary = {"red": [255, 0, 0], "green": [0, 255, 0],
              "blue": [0, 0, 255], "white": [255, 255, 255], "black": [0, 0, 0]}
status = {"still": 0, "sblink": 0.25, "blink": 0.5, "lblink": 1}


class light(threading.Thread):
    def __init__(self, colour="red", stat="still", length=3):
        threading.Thread.__init__(self)
        if type(colour) == str:
            colour = dictionary[colour]
        for i in range(3):
            colour[i] = colour[i] / 255 * 100
        if type(stat) == str:
            stat = status[stat]
        self.colour = colour
        self.status = stat
        self.length = length

        R, G, B = 18, 4, 17
        RPi.GPIO.setmode(RPi.GPIO.BCM)

        RPi.GPIO.setup(R, RPi.GPIO.OUT)
        RPi.GPIO.setup(G, RPi.GPIO.OUT)
        RPi.GPIO.setup(B, RPi.GPIO.OUT)

        self.pwmR = RPi.GPIO.PWM(R, 50)
        self.pwmG = RPi.GPIO.PWM(G, 50)
        self.pwmB = RPi.GPIO.PWM(B, 50)

        self.pwmR.start(0)
        self.pwmG.start(0)
        self.pwmB.start(0)
    def run(self):
        if self.status == 0:
            self.pwmR.ChangeDutyCycle(self.colour[0])
            self.pwmG.ChangeDutyCycle(self.colour[1])
            self.pwmB.ChangeDutyCycle(self.colour[2])
            time.sleep(self.length)
        else:
            for i in range(int(self.length/(4*self.status))):
                self.pwmR.ChangeDutyCycle(self.colour[0])
                self.pwmG.ChangeDutyCycle(self.colour[1])
                self.pwmB.ChangeDutyCycle(self.colour[2])
                time.sleep(self.status)
                if self.status > 0:
                    self.pwmR.ChangeDutyCycle(0)
                    self.pwmG.ChangeDutyCycle(0)
                    self.pwmB.ChangeDutyCycle(0)
                    time.sleep(self.status)
class light_still(threading.Thread):
    def __init__(self, colour="red"):
        threading.Thread.__init__(self)
        if type(colour) == str:
            colour = dictionary[colour]
        for i in range(3):
            colour[i] = colour[i] / 255 * 100
        self.colour = colour
        self.status = 0

        R, G, B = 18, 4, 17
        RPi.GPIO.setmode(RPi.GPIO.BCM)

        RPi.GPIO.setup(R, RPi.GPIO.OUT)
        RPi.GPIO.setup(G, RPi.GPIO.OUT)
        RPi.GPIO.setup(B, RPi.GPIO.OUT)

        self.pwmR = RPi.GPIO.PWM(R, 50)
        self.pwmG = RPi.GPIO.PWM(G, 50)
        self.pwmB = RPi.GPIO.PWM(B, 50)

        self.pwmR.start(0)
        self.pwmG.start(0)
        self.pwmB.start(0)


    def start_still(self):
        self.pwmR.ChangeDutyCycle(self.colour[0])
        self.pwmG.ChangeDutyCycle(self.colour[1])
        self.pwmB.ChangeDutyCycle(self.colour[2])
        time.sleep(1)


# p = light("white", "still")
# p.start_still()
