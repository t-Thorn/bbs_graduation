function selectFile() {
    document.querySelector('#file').click();
}

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
            alert(data.errorMsg);
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
        alert("图片类型必须是.gif,jpeg,jpg,png中的一种");
        return false;
    }
    var reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = function (e) {
        var dx = e.total / 1024;
        if (dx >= 50) {
            alert("文件大小大于50k");
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
 * ajax关注用户
 */
function fan(uid) {
    $.ajax({
        url: '/user/fan',
        type: 'post',
        dataType: 'json',
        data: {"toUser": uid},
        success: function () {
            $("#isFan a").remove();
            var element = $("<a></a>").text("已关注")
                .addClass("layui-btn").addClass("layui-btn-primary").addClass("fly-imActive")
                .on("click", {uid: uid}, cancelFan);
            $("#isFan").append(element);
        },
        error: function () {
            alert("失败");
        }
    })
}

function fan_(event) {
    $.ajax({
        url: '/user/fan',
        type: 'post',
        dataType: 'json',
        data: {"toUser": event.data.uid},
        success: function () {
            $("#isFan a").remove();
            var element = $("<a></a>").text("已关注")
                .addClass("layui-btn").addClass("layui-btn-primary").addClass("fly-imActive")
                .on("click", {uid: event.data.uid}, cancelFan_);
            $("#isFan").append(element);
        },
        error: function () {
            alert("失败");
        }
    })
}

function cancelFan(uid) {
    $.ajax({
        url: '/user/cancelFan',
        type: 'delete',
        dataType: 'json',
        data: {"toUser": uid},
        success: function () {
            $("#isFan a").remove();
            var element = $("<a></a>").text("关注")
                .addClass("layui-btn").addClass("layui-btn-normal").addClass("fly-imActive")
                .on("click", {uid: uid}, fan_);
            $("#isFan").append(element);
        },
        error: function () {
            alert("失败");
        }
    })
}

function cancelFan_(event) {
    $.ajax({
        url: '/user/cancelFan',
        type: 'delete',
        dataType: 'json',
        data: {"toUser": event.data.uid},
        success: function () {
            $("#isFan a").remove();
            var element = $("<a></a>").text("关注")
                .addClass("layui-btn").addClass("layui-btn-normal").addClass("fly-imActive")
                .on("click", {uid: event.data.uid}, fan_);
            $("#isFan").append(element);
        },
        error: function () {
            alert("失败");
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
                alert("上传失败数：" + result.errorNum);
            }
            // 图片上传并返回结果，图片插入成功之后触发
            // xhr 是 XMLHttpRequst 对象，editor 是编辑器对象，result 是服务器端返回的结果
        }
    }

    editor.create();
    //$(".w-e-text").attr("lay-verify","required");
    return editor;
}

function processForm() {
    if ($("#type").val() == "") {
        alert("请选择帖子类型");
        return false;
    }
    if ($("#title").val() == "") {
        alert("请输入帖子标题");
        return false;
    }
    $("#content").val(editor.txt.text());
    $("#content_show").val(editor.txt.html());
    $("#newPost").submit();
}