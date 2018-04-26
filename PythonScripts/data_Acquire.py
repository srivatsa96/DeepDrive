#! /usr/bin/env python


import time
import math as m
import pigpio # To use one hardware PWM
import RPi.GPIO as GPIO # To use 2 Software PWM
import socket
import os
import re
import time
import numpy as np

class CarController:

    """
    @param port: port for connectivity
    """
    def __init__(self,port):
        ## Intialize parameters of communication.
        self.command= os.popen('ifconfig wlp6s0 | grep "inet\ addr" | cut -d: -f2 | cut -d" " -f1') #Works for WiFi only.
        self.TCP_IP = self.command.read()[0:-1]
        self.TCP_PORT = port
        self.BUFFER_SIZE = 8
        self.pattern = re.compile('-*[0-9]+\|[0-9]+$')

        ##Intialize the connection
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.bind((self.TCP_IP, self.TCP_PORT))
        self.sock.listen(True)
        print 'Connect to: %s:%d'%(self.TCP_IP,self.TCP_PORT)
        #Blocking call.
        self.conn, self.addr = self.sock.accept()
        print 'Connected with: ',self.addr

    """
    @param
    @return speed,angle
    """
    def getParameters(self):
        data = self.conn.recv(self.BUFFER_SIZE)
        if(data == 'stop\n'):
            self.conn.close()
            return -1,-1
        if(self.pattern.match(data) != None):
            angle,speed=data.split('|')
            return int(angle),int(speed);
        else:
            print 'Frame dropped'
	    return self.getParameters()

controller = CarController(14001)


speed,angle,pw = 0,0,0
GPIO.setmode(GPIO.BOARD) # Setting up RPI in physical pin mode

GPIO.setup(15,GPIO.OUT) # Lmotor software PWM
GPIO.setup(13,GPIO.OUT) # Rmotor software PWM

Lmotor=GPIO.PWM(15,50) # set initial PWM frequency to 50 Hz
Rmotor=GPIO.PWM(13,50) # set initial PWM frequency to 50 Hz

servo= 18 # servo connected to BCM pin 18 for Hardware PWM.
H=30.10  # H= Distance between front and back wheel pair
k=(25.0/H) # Hardware prop const
L=37.0  # L= Distance between both the back wheels

# Set initial duty cycle to 50%
Lmotor.start(50) 
Rmotor.start(50)
NL=50 # Left motor RPM (basically Duty cycle)
NR=50 # Right motor RPM (basically Duty cycle)

# Some initial setup for pigpio library

pi = pigpio.pi()
pi.set_mode(servo, pigpio.OUTPUT)


while(not (speed==-1 and angle==-1)):
    angle,speed = controller.getParameters() # Get angle and speed from App. Can't use Speed variable due to hardware constrain. You can use it as a switch to start or stop data acquisition
    if (angle>=-80) and (angle<=80):
        pw=(2.5*(float)(angle))+700 # calculating pulsewidth (pw) for servo. Store this variable as image name Eg: "(S.No)_(pw).jgp"
        
        angleR=(angle/180.0)*(m.pi) # Converting angle to Radians     
  
        NL=k*((2.0*H)-(L* (m.tan(angleR)/1.5) )) # Left wheel rpm equation (basically Duty cycle)
        NR=k*((2.0*H)+(L* (m.tan(angleR)/1.5) )) # Right wheel rpm equation (basically Duty cycle)
        
        if (NR<0) or (NL>100):
            NL=0
            NR=100
        elif (NL<0) or (NR>100):
            NL=100
            NR=0
        
        Lmotor.ChangeDutyCycle(NL) # apply duty cycle to the left motor
        Rmotor.ChangeDutyCycle(NR) # apply duty cycle to the right motor
        
        pi.set_servo_pulsewidth(servo, pw) # Set servo angle 
        print "The angle is: %d , speed is: %d and DC: %d NL: %d NR: %d"%(angle,speed,pw,NL,NR)
    
# Although not necessary code below is used to stop the servo
pi.set_servo_pulsewidth(18, 0) # Switch servo pulses off. 

pi.stop()























