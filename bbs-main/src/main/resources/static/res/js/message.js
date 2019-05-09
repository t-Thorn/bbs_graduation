var layer;
var socket;
layui.use('layer', function () { //独立版的layer无需执行这一句
    var $ = layui.jquery
    layer = layui.layer; //独立版的layer无需执行这一句
});

function getMessage(token, type) {
    var list = [];//创建数组
    var a1 = {};//创建对象
    a1.token = token;
    if (type == 1) {
        a1.type = 1;
    }
    list.push(a1);//添加对象
    var json = JSON.stringify(list);//将对象转换为json
    json = json.replace('[', '');
    json = json.replace(']', '');
    return json;
}

/**
 * 检查是否是请求更新token
 * @param msg
 */
function NeedRefreshToken(msg) {
    if (msg.refresh == undefined) {
        return false;
    }
    return true;
}

function sendMsg(msg) {
    if (socket && socket.readyState == WebSocket.OPEN) {
        socket.send(msg);
    }
}

/**
 * 建立websocket
 */
$(function () {
    if (getCookie("token") != null) {

        if (window.WebSocket) {
            //第一个ws是协议，第二个ws是我们在pipeline.addLast(new WebSocketServerProtocolHandler("/ws")); 配置的
            socket = new WebSocket("ws://localhost:8899/msg");
            //发送初始化请求消息
            //当socket得到数据的时候调用，相当于channelRead0
            socket.onmessage = function (event) {
                var msg = JSON.parse(event.data);
                if (NeedRefreshToken(msg)) {
                    sendMsg(getMessage(getCookie("token"), 1));
                } else {
                    //解析数据
                    if (msg.num !== undefined) {
                        num = getCookie("messageNum");
                        if (num === undefined) {
                            num = 0;
                        }
                        if (msg.type !== 0) {
                            num = Number(num) + Number(msg.num);
                        } else {
                            num = msg.num;
                        }
                        //最大显示数
                        num = num > 99 ? 99 : num;
                        if (num !== 0) {
                            //显示红点+显示消息数
                            if ($("#hongdian").length === 0) {
                                var hongdian = "<span class='layui-badge-dot' id='hongdian'></span>";
                                $("#nickname").after(hongdian)
                            }
                            if ($("#messageNum").length > 0) {
                                $("#messageNum").remove()
                            }
                            var messageNum = " <span class='layui-badge' id='messageNum'>" + num + "</span> ";
                            $("#message").append(messageNum);
                        }
                    }
                    if (msg.content !== "") {
                        //显示公告
                        show(msg.content);
                    }
                    document.cookie = "messageNum=" + num;
                }
            }
            socket.onopen = function (ev) {

            }
            socket.onclose = function (ev) {
            }
            setTimeout("sendMsg(getMessage(getCookie('token'), 0))", 1000);
        } else {
            alert("当前浏览器不支持websocket程序");
        }
    }
});

function getCookie(key) {
    var arr1 = document.cookie.split("; ");//由于cookie是通过一个分号+空格的形式串联起来的，所以这里需要先按分号空格截断,变成[name=Jack,pwd=123456,age=22]数组类型；
    for (var i = 0; i < arr1.length; i++) {
        var arr2 = arr1[i].split("=");//通过=截断，把name=Jack截断成[name,Jack]数组；
        if (arr2[0] == key) {
            return decodeURI(arr2[1]);
        }
    }
}

function show(msg) {
    layer.open({
        type: 1
        ,
        title: false //不显示标题栏
        ,
        closeBtn: false
        ,
        area: '300px;'
        ,
        shade: 0.8
        ,
        id: 'LAY_layuipro' //设定一个id，防止重复弹出
        ,
        btn: ['了解']
        ,
        btnAlign: 'c'
        ,
        moveType: 1 //拖拽模式，0或者1
        ,
        content: "<div style='padding: 50px; line-height: 22px; background-color: #393D49; color: #fff; font-weight: 300;'>"
            + msg + "</div>"
        ,
        success: function (layero) {
            /* var btn = layero.find('.layui-layer-btn');
             btn.find('.layui-layer-btn0').attr({
                 href: 'http://www.layui.com/'
                 , target: '_blank'
             });*/
        }
    });
}