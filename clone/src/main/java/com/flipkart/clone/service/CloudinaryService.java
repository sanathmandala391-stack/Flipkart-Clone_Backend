//package com.flipkart.clone.service;
//
//import com.cloudinary.Cloudinary;
//import com.cloudinary.utils.ObjectUtils;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.util.Map;
//
//@Service
//public class CloudinaryService {
//
//    private final Cloudinary cloudinary;
//
//    public CloudinaryService(
//            @Value("${app.cloudinary.cloud-name}") String cloudName,
//            @Value("${app.cloudinary.api-key}")    String apiKey,
//            @Value("${app.cloudinary.api-secret}") String apiSecret) {
//
//        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
//                "cloud_name", cloudName,
//                "api_key",    apiKey,
//                "api_secret", apiSecret,
//                "secure",     true
//        ));
//    }
//
//    // ── Upload image ──────────────────────────────────────────────
//    public String uploadImage(MultipartFile file,
//                              String folder) throws IOException {
//
//        Map<?, ?> result = cloudinary.uploader().upload(
//                file.getBytes(),
//                ObjectUtils.asMap(
//                        "folder",          "flipkart/" + folder,
//                        "transformation",  ObjectUtils.asMap(
//                                "quality", "auto",
//                                "fetch_format", "auto"
//                        )
//                )
//        );
//        return result.get("secure_url").toString();
//    }
//
//    // ── Delete image ──────────────────────────────────────────────
//    public void deleteImage(String publicId) throws IOException {
//        cloudinary.uploader().destroy(publicId,
//                ObjectUtils.emptyMap());
//    }
//}






package com.flipkart.clone.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${app.cloudinary.cloud-name}") String cloudName,
            @Value("${app.cloudinary.api-key}") String apiKey,
            @Value("${app.cloudinary.api-secret}") String apiSecret
    ) {

        this.cloudinary = new Cloudinary(
                ObjectUtils.asMap(
                        "cloud_name", cloudName,
                        "api_key", apiKey,
                        "api_secret", apiSecret,
                        "secure", true
                )
        );
    }

    public String uploadImage(
            MultipartFile file,
            String folder
    ) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "flipkart/" + folder,
                        "resource_type", "auto"
                )
        );

        return result.get("secure_url").toString();
    }

    public void deleteImage(String publicId)
            throws IOException {

        cloudinary.uploader().destroy(
                publicId,
                ObjectUtils.emptyMap()
        );
    }
}