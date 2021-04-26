package net.sf.persism.dao.northwind;

import net.sf.persism.annotations.NotColumn;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Data object for the Northwind categories table.
 * Demonstrates reading binary data (picture) which is an OLE wrapping a JPEG file.
 * Demonstrates having a calculated field in the data object which converts the picture into a BufferedImage.
 *
 * @author Dan Howard
 * @since 5/3/12 8:50 PM
 */
public final class Category {

    private int categoryId;
    private String categoryName;
    private String description;
    private byte[] picture;
    private String data; // xml type in SQL

    @NotColumn // Fix for Breaking change in 1.1.0
    private BufferedImage image = null;

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getPicture() {
        return picture;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    /**
     * Convert picture data into a BufferedImage
     *
     * @return JPEG BufferedImage
     * @throws IOException
     */
    public BufferedImage getImage() throws IOException {
        // Fails but maybe we could use JavaFX for this
        // https://docs.oracle.com/javase/8/javafx/api/javafx/scene/image/Image.html
        if (image == null) {
            // OLE header is 1st 78 bytes so we strip it.
            byte[] imageData = new String(picture).substring(78).getBytes();

//            try (InputStream in = new ByteArrayInputStream(imageData)) {
//                Image image1 = new Image(in);
//                image = SwingFXUtils.fromFXImage(image1, null);
//            }
        }
        return image;
    }

    @Override
    public String toString() {
        return "Category{" +
                "categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", description='" + description + '\'' +
                ", picture=" + picture +
                "} ";
    }
}
