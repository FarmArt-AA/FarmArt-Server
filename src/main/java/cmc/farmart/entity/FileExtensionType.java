package cmc.farmart.entity;

import java.util.List;

public class FileExtensionType {

    public static final String PNG = "png";
    public static final String JPG = "jpg";
    public static final String JPEG = "jpeg";
    public static final String PDF = "pdf";
    public static final List<String> DRAWING = List.of(FileExtensionType.PDF);
    public static final List<String> IMAGE = List.of(FileExtensionType.PNG, FileExtensionType.JPG, FileExtensionType.JPEG);
}
