function selectFile() {
    document.querySelector('#file').click();
}

var layer;
layui.use(['element', 'layer'], function () {
    layer = layui.layer;
});

/* 上传头像 */
function upload() {
    var formData = new FormData($("#avatorForm")[0]);
    $.ajax({
        url: '/info/img',
        type: 'patch',
        dataType: 'json',
        data: formData,
        processData: false,
        contentType: false,
        cache: false,
        success: function (data) {
            $("#img").attr('src', data.msg);
            $("#avator").attr('src', data.msg);
        },
        error: function (data) {
            layer.msg(data.errorMsg);
        }
    })
}

/**
 * 检查头像是否合法
 * @param file 文件对象
 * @returns {boolean}
 */
function checkImg(file) {
    var fileType = document.getElementById("file").value;
    if (!/\.(gif|jpg|jpeg|png|GIF|JPG|JPEG|PNG)$/.test(fileType)) {
        layer.msg("图片类型必须是.gif,jpeg,jpg,png中的一种");
        return false;
    }
    var reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = function (e) {
        var dx = e.total / 1024;
        if (dx >= 50) {
            layer.msg("文件过大")
            return false;
        }
    }
    return true;
}

/**
 * 取文件真实路径
 * @param file 文件对象
 * @returns {*}
 */
function getObjectURL(file) {
    var url = null;
    if (window.createObjcectURL != undefined) {
        url = window.createOjcectURL(file);
    } else if (window.URL != undefined) {
        url = window.URL.createObjectURL(file);
    } else if (window.webkitURL != undefined) {
        url = window.webkitURL.createObjectURL(file);
    }
    return url;
}

/**
 * 关注
 * @param uid
 */
function fan(uid) {
    var fanBtn = $("#isFan a");
    if (!fanBtn.hasClass('layui-btn-primary')) {
        url = '/user/fan';
        type = 'post';
    } else {
        url = '/user/cancelFan';
        type = 'delete';
    }
    $.ajax({
        url: url,
        type: type,
        dataType: 'json',
        data: {"toUser": uid},
        success: function () {
            if (!fanBtn.hasClass('layui-btn-primary')) {
                fanBtn.removeClass('layui-btn-normal').text("已关注").addClass("layui-btn-primary");
                layer.msg("关注成功")
            } else {
                fanBtn.removeClass("layui-btn-primary").text("关注").addClass("layui-btn-normal");
                layer.msg("取关成功")
            }
        },
        error: function (data) {
            layer.msg("失败:" + JSON.parse(data.responseText).errorMsg);
        }
    })
}

/**
 * 收藏
 * @param pid
 */
function collect(pid) {
    var collectBtn = $("#collect button");
    if (collectBtn.hasClass('layui-btn-primary')) {
        url = '/info/collect';
        type = 'post';
    } else {
        url = '/info/cancelCollect';
        type = 'delete';
    }
    $.ajax({
        url: url,
        type: type,
        dataType: 'json',
        data: {"pid": pid},
        success: function () {
            if (!collectBtn.hasClass('layui-btn-primary')) {
                collectBtn.removeClass('layui-btn-normal').addClass("layui-btn-primary");
                layer.msg("取消收藏成功")
            } else {
                collectBtn.removeClass("layui-btn-primary").addClass("layui-btn-normal");
                layer.msg("收藏成功")
            }
        },
        error: function (data) {
            layer.msg("失败:" + JSON.parse(data.responseText).errorMsg);
        }
    })
}
/**
 * 消息框配置 todo 加入websocket
 * @type {NotificationFx}
 */

/*
var notification = new NotificationFx({
    message :
        '<div class="ns-thumb"><img src="/img/user1.jpg"/></div><div class="ns-content"><p><a href="#">Zoe Moulder</a> accepted your invitation.</p></div>',
    layout : 'other',
    ttl : 6000,
    effect : 'thumbslider',
    type : 'notice', // notice, warning, error or success
    onClose : function() {

    }
});

// show the notification
notification.show();*/
var E = window.wangEditor;
var editor = new E('#editor');

function createEditor() {
    //元素层数级别，越大越高，其他元素必须大于这个值才能触发鼠标事件
    editor.customConfig.zIndex = 100;
    //复制 时忽略图片
    editor.customConfig.pasteIgnoreImg = true;
    //插入网络图片回调
    editor.customConfig.linkImgCallback = function (url) {
        console.log(url) // url 即插入图片的地址
    };
    //限制图片大小为3M
    editor.customConfig.uploadImgMaxSize = 3 * 1024 * 1024;
    editor.customConfig.uploadImgHeaders = {
        'Accept': 'text/x-json'
    };
    editor.customConfig.uploadFileName = 'img';
    // 上传图片到服务器
    editor.customConfig.uploadImgServer = '/post/imgUpload';
    editor.customConfig.uploadImgHooks = {
        success: function (xhr, editor, result) {
            if (result.errorNum > 0) {
                layer.msg("上传失败数：" + result.errorNum);
            }
            // 图片上传并返回结果，图片插入成功之后触发
            // xhr 是 XMLHttpRequst 对象，editor 是编辑器对象，result 是服务器端返回的结果
        }
    }
    editor.customConfig.menus = [
        'head',  // 标题
        'bold',  // 粗体
        'fontSize',  // 字号
        'fontName',  // 字体
        'italic',  // 斜体
        'underline',  // 下划线
        'strikeThrough',  // 删除线
        'foreColor',  // 文字颜色
        'link',  // 插入链接
        'list',  // 列表
        'justify',  // 对齐方式
        'quote',  // 引用
        'emoticon',  // 表情
        'image',  // 插入图片
        'video',  // 插入视频
        'code',  // 插入代码
        'undo'  // 撤销
    ]
    editor.create();
    //$(".w-e-text").attr("lay-verify","required");
    return editor;
}

function processForm() {
    if ($("#type").val() == "") {
        layer.msg("请选择帖子类型");
        return false;
    }
    if ($("#title").val() == "") {
        layer.msg("请输入帖子标题");
        return false;
    }
    $("#content").val(editor.txt.text());
    $("#content_show").val(editor.txt.html());
    $("#newPost").submit();
}

/**
 * 添加引用到回复中
 */
function yinyong(floor) {
    var nickname = $("#jieda li:eq(" + floor + ") .source").data("nickname");
    var reply = $("#jieda li:eq(" + floor + ") .content");
    $("#replyTo").val(floor);
    $("#yinyongcontent").html(reply.html());
    $("#yinyonglink").attr("href", "/user/home/" + replyTo).text(nickname);
    $("#yinyong").show();
}

function dianzan(index) {
    var zan = $("#jieda li:eq(" + index + ") .jieda-zan");
    var uid = zan.data("uid");
    var pid = zan.data("pid");
    var floor = zan.data("floor");
    if ($("#loginAlready").data("login") == true) {
        //登录才允许执行
        $.ajax({
            url: '/reply/zan',
            type: 'post',
            dataType: 'json',
            data: {"toUser": uid, "floor": floor, "pid": pid},
            success: function (data) {
                $("#jieda li:eq(" + index + ") .jieda-zan em").text(data.no);
                if (zan.hasClass("zanok")) {
                    zan.removeClass("zanok");
                    zan.addClass("zan");
                } else {
                    zan.addClass("zanok");
                    zan.removeClass("zan");
                }
            },
            error: function (data) {
                layer.msg('点赞失败:' + JSON.parse(data.responseText).errorMsg);
            }
        })
    } else {
        layer.msg('请先登录');
    }
}

/**
 * 新增回复
 */
function submitReply() {
    if ("" == editor.txt.text()) {
        layer.msg("请输入回复内容");
        return false;
    }
    $("#content").val(editor.txt.text());
    $("#content_show").val(editor.txt.html());
    $("#addReply").submit();
}

/**
 * 删除回复
 * @param index
 */
function delReply(index) {
    var reply = $("#jieda .jieda-admin:eq(" + index + ") a");
    var uri = "/reply/del/" + reply.data("pid") + "/" + reply.data("floor");
    $.ajax({
        url: uri,
        type: 'delete',
        dataType: 'json',
        data: "",
        success: function () {
            layer.msg("删除成功");
            window.location.href = '/post/' + reply.data("pid")
        },
        error: function (data) {
            layer.msg('删除失败:' + JSON.parse(data.responseText).errorMsg);
        }
    })
}

/**
 * 弹出错误消息
 */
function msg(uri) {
    var errorMsg = document.getElementById("msg");
    if (errorMsg.value != "") {
        layer.msg(errorMsg.value);
        if (uri != null && uri != "")
            window.location.href = uri;
    }
}
