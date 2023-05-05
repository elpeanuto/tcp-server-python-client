import socket
import struct

HOST = 'localhost'  
PORT = 1234

def receive_data(s):
    data_len = s.recv(2)
    data = s.recv(int.from_bytes(data_len, byteorder = 'big'))
    print(data.decode('utf-8'))

def send_data(s, message):
    encoded_message = message.encode('utf-8')
    s.send(len(encoded_message).to_bytes(2, byteorder='big'))
    s.send(encoded_message)

def write_matrix(matrix, s):
    matrix_size = len(matrix)
    s.send(struct.pack('!i', matrix_size))
    for i in range(matrix_size):
        for j in range(matrix_size):
            s.send(struct.pack('!i', matrix[i][j]))

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect((HOST, PORT))

receive_data(s)

matrix_size = 3
matrix1 = [[1, 2, 3], [4, 5, 6], [7, 8, 9]]
matrix2 = [[9, 8, 7], [6, 5, 4], [3, 2, 1]]

write_matrix(matrix1, s)
write_matrix(matrix2, s)

receive_data(s)

send_data(s, 'Start')

receive_data(s)

while True:
    send_data(s, 'Get')
            
    len_bytes = s.recv(2)
    response = s.recv(int.from_bytes(len_bytes, byteorder='big')).decode('utf-8')

    if response == 'Running':
        print(response)
    elif response == "Done":
        matrix_diff = []
        len_bytes = s.recv(4)
        matrix_size = int.from_bytes(len_bytes, byteorder='big')
            
        for i in range(matrix_size):
            row = []
            for j in range(matrix_size):
                row.append(struct.unpack('!i', s.recv(4))[0])
            matrix_diff.append(row)
        print(matrix_diff)
        break

s.close()
