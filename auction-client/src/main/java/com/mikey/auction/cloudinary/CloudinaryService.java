package com.mikey.auction.cloudinary;

import java.io.File;
import java.util.Map;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

public class CloudinaryService {
    private static final Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "devnd8ndw",
            "api_key", "452683679762893",
            "api_secret", "dKoonrz4l4H0IGVtv1dpm3Duass",
            "secure", true
    ));

    public static String upload(File file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.asMap("folder", "auction_items"));
            return uploadResult.get("secure_url").toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

   public static String upload(File file, String username) {
    try {
        Map params = ObjectUtils.asMap(
            "folder", "auction_avatars",       
            "public_id", "avatar_" + username, 
            "overwrite", true,                 
            "invalidate", true,
            // Thêm transformation để ép ảnh về hình vuông 400x400 và tự động tập trung vào khuôn mặt
            "transformation", "w_400,h_400,c_fill,g_face" 
        );
        
        Map uploadResult = cloudinary.uploader().upload(file, params);
        return uploadResult.get("secure_url").toString();
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}
}