<?php
$ip = '127.0.0.1';
$port = 8888;
$socket = socket_create(AF_INET,SOCK_STREAM,SOL_TCP);
if($socket < 0) {
    echo "socket_create() 失败的原因是:".socket_strerror($sock)."\n";
 }
$result = socket_connect($socket, $ip, $port);
if ($result < 0) {
    echo "socket_connect() failed.\nReason: ($result) " . socket_strerror($result) . "\n";
}else {
    echo "连接OK\n";
}
$msg ="1,5";
socket_write($socket, $msg, strlen($msg));
$out = socket_read($socket, 256);
$id = unpack("J", $out);
var_dump($id);
if($id[1]<=0){
echo "参数错误，根据errorNo查询对应错误原因\r\n";
}
socket_close($socket);