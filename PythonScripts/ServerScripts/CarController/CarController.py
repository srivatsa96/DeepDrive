import socket
import os
import re


## TODO: Port network information on background thread.

"""
Interface for establishing connection with android device to receive control parameters.
Developed by Srivatsa Sinha, 31st August, 2017
"""

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

controller = CarController(14002)
speed,angle = 0,0
while(not (speed==-1 and angle==-1)):
    angle,speed = controller.getParameters()
    print "The angle is: %d and speed is: %d"%(angle,speed)
