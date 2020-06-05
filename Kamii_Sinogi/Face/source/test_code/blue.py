#coding=utf-8
import serial
ser = serial.Serial("/dev/rfcomm0", 9600) 
ser.write("Successfully connected(output)!".encode()) 
while True:
	s=raw_input()
	ser.write(s.encode())
