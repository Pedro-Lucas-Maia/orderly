package bti.pds.dinner.product.infrastructure.persistence.repository;

import bti.pds.dinner.product.domain.ProductImageRepository;
import bti.pds.dinner.product.domain.exception.ImageStorageException;
import bti.pds.dinner.product.domain.exception.UrlNotValidException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.Map;

@Repository
public class CloudinaryImageRepository implements ProductImageRepository {
    private final Cloudinary cloudinary;

    public CloudinaryImageRepository(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String uploadImage(byte[] fileBytes, String fileName) {
        try {
            Map uploadResult = cloudinary.uploader().upload(fileBytes, ObjectUtils.emptyMap());

            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new ImageStorageException("Failed to send image to the cloud");
        }
    }

    @Override
    public void deleteImage(String imageUrl) {
        try {
            String publicId = extractPublicIdFromUrl(imageUrl);

            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new ImageStorageException("Failed to delete image from the cloud");
        }
    }

    @Override
    public String replace(byte[] fileBytes, String fileName, String oldImageUrl) {
        try {
            String publicId = extractPublicIdFromUrl(oldImageUrl);
            Map<String, Object> options = ObjectUtils.asMap(
                    "public_id", publicId,
                    "overwrite", true
            );

            Map uploadResult = cloudinary.uploader().upload(fileBytes, options);

            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new ImageStorageException("Failed to replace cloud image");
        }
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new UrlNotValidException("Image Url is required");
        }

        int uploadIndex = imageUrl.indexOf("upload/");
        if (uploadIndex == -1) {
            throw new UrlNotValidException("Invalid Url: it does not follow Cloudinary standards");
        }

        String path = imageUrl.substring(uploadIndex + 7);

        // Remove a versão (ex: v1612345678/) se existir
        if (path.matches("^v\\d+/.*")) {
            int firstSlash = path.indexOf('/');
            path = path.substring(firstSlash + 1);
        }

        // Remove a extensão (.jpg, .png, etc)
        int lastDot = path.lastIndexOf('.');
        if (lastDot != -1) {
            path = path.substring(0, lastDot);
        }

        return path;
    }
}
