package bti.pds.dinner.product.domain;

public interface ProductImageRepository {
    String uploadImage(byte[] fileBytes, String fileName);
    void deleteImage(String imageUrl);
    String replace(byte[] fileBytes, String fileName, String oldImageUrl);
}
