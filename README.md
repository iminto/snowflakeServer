# snowflakeServer
雪花算法服务，基于Netty，支持PHP和Python客户端。

#### 运行

```sh
mvn clean package
java -Xms128M -Xmx128M -XX:MetaspaceSize=32m -XX:MaxMetaspaceSize=48m -Xss256k -XX:+UseConcMarkSweepGC -XX:+HeapDumpOnOutOfMemoryError -XX:AutoBoxCacheMax=10000  -jar  target/snowflake-1.0-SNAPSHOT-jar-with-dependencies.jar
```
基于Netty，提供高性能无阻塞的服务，Java客户端例子参考SnowClientHandler和OldServer

#### PHP客户端

PHP客户端代码如下，详见socket.php：

```php
$ip = '127.0.0.1';
$port = 8888;
$socket = socket_create(AF_INET,SOCK_STREAM,SOL_TCP);
$result = socket_connect($socket, $ip, $port);
$msg ="1,5";//workid和dataCenterId，中间逗号隔开
socket_write($socket, $msg, strlen($msg));
$out = socket_read($socket, 256);
$id = unpack("J", $out);
var_dump($id);
if($id[1]<=0){
echo "参数错误，根据errorNo查询对应错误原因\r\n";
}
socket_close($socket);
```

#### Python客户端

python客户端详见snowClient.py，py3版本代码如下：

```python
import socket,sys
HOST = '127.0.0.1'  # 标准的回环地址 (localhost)
PORT = 8888        # 监听的端口 (非系统级的端口: 大于 1023)

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    s.connect((HOST, PORT))
    s.sendall(b'5,4')
    data = s.recv(128)

print('Received', repr(data))
id=int.from_bytes(data, 'big')
print(id)
if id>=sys.maxsize :
   print('参数错误，根据errorNo查询对应错误原因\r\n') 
```

#### 错误码

 当返回的id<0时不正确，错误原因如下：

| 服务器端错误码 | 错误原因                                      |
| -------------- | --------------------------------------------- |
| -1             | 参数长度不符合，不能超过10                    |
| -2             | 参数格式不符合，中间必须有逗号分隔            |
| -3             | workId参数不正确，不能大于31或小于等于0       |
| -4             | dataCenterId参数不正确，不能大于31或小于等于0 |