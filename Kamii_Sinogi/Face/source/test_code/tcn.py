#coding=utf-8
import serial
ser = serial.Serial("/dev/rfcomm0", 9600) 
ser.write("Successfully connected(input)!".encode()) 
while True:
	count = ser.inWaiting() 
	if count!=0:
		recv = ser.read(count)
		print recv.split(' ' , -1)
