package com.thorn.bbsmain.services;


import com.thorn.bbsmain.mapper.ReplyMapper;
import com.thorn.bbsmain.mapper.entity.Reply;
import com.thorn.bbsmain.utils.MsgBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReplyService {

    @Value("${system.path.replyImg}")
    String imgPath;
    private ReplyMapper replyMapper;

    public ReplyService(ReplyMapper replyMapper) {
        this.replyMapper = replyMapper;
    }

    /**
     * 回复列表框上传图片
     *
     * @param imgList 图片合集
     * @return 消息字符串
     */
    public String imgUpload(MultipartFile[] imgList) {
        MsgBuilder builder = new MsgBuilder();
        //返回的图片链接地址
        List<String> imgLinkList = new ArrayList<>();
        //状态码
        int errno = 0;

        imgLinkList =
                Arrays.stream(imgList).map(this::createImgFile).filter(imgFile -> imgFile != null)
                        .collect(Collectors.toList());
        builder.addData("data", imgLinkList);
        builder.addData("errno", errno);
        //出错的图片数量
        builder.addData("errorNum", imgList.length - imgLinkList.size());
        return builder.getMsg();
    }

    private String createImgFile(MultipartFile img) {
        if (img.getSize() == 0 || img.getSize() / 1024 >= 50) {
            return null;
        }
        String fileType;
        String[] suffixs = {".jpg", ".gif", ".jpeg", ".png", ".JPG", ".GIF", ".JPEG", ".PNG"};
        if (!Arrays.stream(suffixs).anyMatch(suffix -> img.getOriginalFilename().endsWith(suffix))) {
            return null;
        } else {
            fileType = img.getOriginalFilename().substring(img.getOriginalFilename().lastIndexOf(
                    "."));
        }

        File imgFile = new File(imgPath + UUID.randomUUID() + fileType);
        try {
            if (!imgFile.exists()) {
                if (!imgFile.createNewFile()) {
                    return null;
                }
            }
            img.transferTo(imgFile);
        } catch (IOException e) {
            return null;
        }
        return "/img/replyImg/" + imgFile.getName();
    }

    /**
     * 新建帖子时的一楼回复
     *
     * @param reply 回复信息
     */
    public void createPostTopReply(Reply reply) {
        replyMapper.createPostTopReply(reply);
    }
}
