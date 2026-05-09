package com.mikey.auction.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.File;
import java.util.Map;

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
}