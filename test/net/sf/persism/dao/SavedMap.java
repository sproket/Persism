package net.sf.persism.dao;

import net.sf.persism.annotations.NotColumn;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public final class SavedMap {

    private Integer id;
    private int gameId;
    private String mapName;
    private String backgroundResource;
    private byte[] imageData;
    private String longText;

    @NotColumn
    private BufferedImage image = null;

    public Integer getId() {
        return id;
    }

//    public void setId(Integer id) {
//        this.id = id;
//    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public String getBackgroundResource() {
        return backgroundResource;
    }

    public void setBackgroundResource(String backgroundResource) {
        this.backgroundResource = backgroundResource;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public String getLongText() {
        return longText;
    }

    public void setLongText(String longText) {
        this.longText = longText;
    }

    public BufferedImage getImage() {
        if (image == null && imageData != null) {
            ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
            try {
                image = ImageIO.read(bais);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return image;
    }

    @Override
    public String toString() {
        return "SavedMap{" +
                "id=" + id +
                ", gameId=" + gameId +
                ", mapName='" + mapName + '\'' +
                ", backgroundResource='" + backgroundResource + '\'' +
                ", image='" + getImage() + '\'' +
                '}';
    }
}
