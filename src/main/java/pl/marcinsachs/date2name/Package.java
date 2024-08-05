package pl.marcinsachs.date2name;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
public class Package {
    private App.Mode mode;
    private List<MediaFile> mediaFiles;
    private String outputDirectory;
    private SimpleDateFormat dateFormat;
    private String separator;

    public Package(App.Mode mode, File[] fileList, boolean includeVideo, String outputDirectory, SimpleDateFormat dateFormat, String separator) {
        this.mode = mode;
        this.mediaFiles = createMediaFileList(fileList, includeVideo);
        this.outputDirectory = outputDirectory;
        this.dateFormat = dateFormat;
    }
    private List<MediaFile> createMediaFileList(File[] fileList, boolean includeVideo){
        List<MediaFile> mediaFileList = new ArrayList<>();
        for(File file:fileList){
            if(file.isFile()){
                if(MediaFile.IsSupportedMediaFile(file) == true)
                {
                    MediaFile mediaFile = new MediaFile(file.getPath());
                    if((mediaFile.getType() == MediaFile.MediaType.Image)
                            ||  (mediaFile.getType() == MediaFile.MediaType.Video && includeVideo==true))
                    {
                        mediaFileList.add(mediaFile);
                    }
                }
            }
        }
        return mediaFileList;
    }
}
