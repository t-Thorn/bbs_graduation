package com.thorn.bbsmain.utils;

import com.thorn.bbsmain.exceptions.PageException;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 分页
 */
@Slf4j
public class MyUtil {

    private static final float COMPRESS_THRESHOLD = 1024 * 768;

    /**
     * 分页
     *
     * @param list 待分页列表
     * @param page
     * @param step 每页显示多少
     * @return 一页的部分//可能不需要返回
     */
    public static List subList(List list, int page, int step) throws PageException {
        if (list == null || list.size() == 0) {
            return list;
        }

        if (page < 1) {
            throw new PageException("非法参数");
        }
        int beginIndex = --page * step;
        int endIndex = beginIndex + step;
        endIndex = endIndex > list.size() ? list.size() : endIndex;
        if (endIndex <= beginIndex) {
            throw new PageException("非法参数");
        }
        return list.subList(beginIndex, endIndex);
    }

    public static int getPage(int num, int step) {
        if (num % step == 0) {
            return num / step;
        }
        return num / step + 1;
    }

    public static String compressImg(MultipartFile img, String fileType, File imgFile,
                                     String imgPath) throws IOException {
        //文件大小小于50k，并且不是png 压缩图片
        if (img.getSize() / 1024 >= 50 && !"png".equalsIgnoreCase(fileType)) {
            int width = 0;
            int height = 0;
            BufferedImage image = ImageIO.read(img.getInputStream());
            //如果image=null 表示上传的不是图片格式
            if (image != null) {
                //获取图片宽度，单位px
                width = image.getWidth();
                //获取图片高度，单位px
                height = image.getHeight();
            }
            File imgFileOfCompress = new File(imgPath + UUID.randomUUID() + fileType);
            float size = width * height;
            if (size > COMPRESS_THRESHOLD) {
                float rate = COMPRESS_THRESHOLD / size;
                Thumbnails.of(imgFile)
                        .scale(rate)
                        .outputQuality(0.5f)
                        .toFile(imgFileOfCompress);
                if (!imgFile.delete()) {
                    log.error("删除压缩前的图片错误.");
                }
                return imgFileOfCompress.getName();
            }
        }
        return null;
    }


    /**
     * 获取访问页url
     *
     * @param request
     * @return
     */
    public static String getReferer(HttpServletRequest request) {
        String uri = request.getHeader("Referer");
        if (uri == null) {
            return "/";
        }
        uri = uri.substring(uri.indexOf("/", uri.indexOf("/") + 2));
        if (uri.indexOf("?") > 0) {
            uri = uri.substring(0, uri.indexOf("?"));
        }
        return "".equals(uri) ? "/" : uri;
    }
}
