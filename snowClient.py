import socket,sys
HOST = '127.0.0.1'  # 标准的回环地址 (localhost)
PORT = 8888        # 监听的端口 (非系统级的端口: 大于 1023)

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    s.connect((HOST, PORT))
    s.sendall(b'54')
    data = s.recv(128)

print('Received', repr(data))
id=int.from_bytes(data, 'big')
print(id)
if id>=sys.maxsize :
   print('参数错误，根据errorNo查询对应错误原因\r\n') 
 
