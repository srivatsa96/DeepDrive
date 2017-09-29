import cv2
import numpy as np
import socket
import sys
import pickle
import struct


class ImageStreamService(Object):

    def __init__(self,ip,port,fps):
        self.ip = ip
        self.host = host
        self.fps = fps
        self.client_socket = []

    def init_connection(self,ip,port):
        try:
            self.client_socket = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
            self.client_socket.connect((ip,port))
            return True
        except socket.erro, msg:
            ## Log socket error
            return False


    def send_frames(self,camera_id):
        status = self.init_connection(self.ip,self.port)
        if(status):
            cap = cv2.VideoCapture(camera_id)
            while True:
                ret, frame = cap.read()
                data = pickle.dumps(frame)
                clientsocket.sendall(struct.pack("L",len(data))+data)
        else:
             ## Log Error Message
