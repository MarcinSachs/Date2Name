package pl.marcinsachs.date2name;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.avi.AviDirectory;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.mov.QuickTimeDirectory;
import com.drew.metadata.mp4.Mp4Directory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Stream;
import java.text.SimpleDateFormat;


@Getter
@Setter
@NoArgsConstructor
public class MediaFile {
    private String name;
    private String newName;
    private String path;
    private MediaType type;
    private Metadata metadata;
    private File file;
    private Date originalDate;

    public MediaFile(String filePath){
        this.name = FilenameUtils.getBaseName(filePath);
        this.path = filePath;
        this.type = getMediaTypeFromFileExtension(FilenameUtils.getExtension(filePath));
        this.file=new File(filePath);
        if(type!=MediaType.Unsupported)
        {
            setMetadata(getMetadataFromFile(file));
            setOriginalDate(getOriginalDateFromMetadata());
        }
        this.newName=getNewName(getOriginalDate());
    }

    private String getNewName(Date originalDate ){
      return  Operations.getDateFormat(App.datePatern).format(originalDate);
    }

    private Metadata getMetadataFromFile(File file){
        Metadata metadata = null;
        try {
            metadata = ImageMetadataReader.readMetadata(file);
        } catch (ImageProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return metadata;
    }
    private Date getOriginalDateFromMetadata(){
        Date date = new Date();
            switch (getType()){
                case MediaType.Image:
                  date = getDateFromImage();
                  break;
                case MediaType.Video:
                  date = getDateFromVideo();
                  break;
            }
        return date;
    }

    private Date getDateFromImage(){
        Directory directory
                = getMetadata().getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        ExifIFD0Directory ifd0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        Date date = null;

        if (directory != null) {
            date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
        }

        if (date == null && ifd0Directory != null) {
            date = ifd0Directory.getDate(ExifIFD0Directory.TAG_DATETIME);
        }
                return date;
    }
    private Date getDateFromVideo(){
        String extension =  FilenameUtils.getExtension(path);

        Date date = new Date();
        switch (extension.toLowerCase()){
            case "avi":
            {
                Directory directory = getMetadata().getFirstDirectoryOfType(AviDirectory.class);
                date = directory.getDate(AviDirectory.TAG_DATETIME_ORIGINAL);
                break;
            }
            case "qt":
            case "mov":
            {
                Directory directory = getMetadata().getFirstDirectoryOfType(QuickTimeDirectory.class);
                date = directory.getDate(QuickTimeDirectory.TAG_CREATION_TIME);
                break;
            }
            case "mp4":
            case "m4a":
            case "m4p":
            case "m4b":
            case "m4r":
            case "m4v":
            {
                Directory directory = getMetadata().getFirstDirectoryOfType(Mp4Directory.class);
                date = directory.getDate(Mp4Directory.TAG_CREATION_TIME);
                break;
            }
        }
        return date;

    }

    public enum MediaType{Image,Video,Unsupported};

    public MediaType getMediaTypeFromFileExtension(String fileExtension){
        if(Arrays.stream(VideoExtensions).anyMatch(fileExtension.toLowerCase()::equals)){
            return MediaType.Video;
        }
        else if(Arrays.stream(ImageExtensions).anyMatch(fileExtension.toLowerCase()::equals)){
            return MediaType.Image;
        }
        return MediaType.Unsupported;
    }

    public static boolean IsSupportedMediaFile(File file) {
        boolean result = false;
        if(Arrays.stream(MediaFile.getSupportedMediaExtensions()).anyMatch(FilenameUtils.getExtension(file.getPath()).toLowerCase()::equals)){
            result =  true;
        }
        return result;
    }

    private String[] VideoExtensions = {"avi","mov","mp4","m4a","m4p","m4b","m4r","m4v","qt"};
    private String[] ImageExtensions={"jpg","jpeg","jpe","tif","tiff", "webp","png","bmp",
            "gif","heif","heic","nef","cr2","cr3","crm","crw","orf","arw","rw2","avif","raf","dng"};
    private String[] SupportedMedia = Stream.concat(Arrays.stream(getVideoExtensions()), Arrays.stream(getImageExtensions())).toArray(String[]::new);;
public static String[] getSupportedMediaExtensions(){
    MediaFile mediaFile= new MediaFile();
    return mediaFile.getSupportedMedia();
}

}

