package pl.marcinsachs.date2name;

import javafx.scene.control.ListView;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Operations {
    public static void MakeChanges(App.Mode mode, String datePatern, String separator, String postfix)throws IOException {
        Package appPackage = new Package(mode, getMediaFileList("C:\\Users\\Marcin\\Pictures\\2024.06.16 Poznań\\Wszystkie\\RAW"),
                true,"C:\\Users\\Marcin\\Pictures\\2024.06.16 Poznań\\Wszystkie\\RAW\\out",
                Operations.getDateFormat(datePatern),separator);

        File theDir = new File(appPackage.getOutputDirectory());
        if (!theDir.exists()){
            theDir.mkdirs();
        }
        for(MediaFile mediaFile: appPackage.getMediaFiles()){
            System.out.println(mediaFile.getName());
            File newFile = new File(appPackage.getOutputDirectory()+"\\"+ mediaFile.getNewName()
                    + postfix +"." + FilenameUtils.getExtension(mediaFile.getPath()));
            System.out.println(newFile);
            if(appPackage.getMode() == App.Mode.Move){
                Files.move(mediaFile.getFile().toPath(), getAllowedFileName(newFile, separator).toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            else if(appPackage.getMode() == App.Mode.Copy){

                Files.copy(mediaFile.getFile().toPath(), getAllowedFileName(newFile, separator).toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            else if(appPackage.getMode() == App.Mode.Override){
                mediaFile.getFile().renameTo(newFile);
            }
        }
    }
    public static SimpleDateFormat getDateFormat(String patern){
        return new SimpleDateFormat(patern);
    }
    public static  File[] getMediaFileList(String path){
        File folder = new File(path);
        return folder.listFiles();

    }
    public static File getAllowedFileName(File file, String separator){
        File originalFile = file;
        int index = 0;
            while (file.exists()){
                index++;
                String path = originalFile.getAbsolutePath();
                String filePath= FilenameUtils.removeExtension(path)
                        +separator+index + "." + FilenameUtils.getExtension(path);
                file = new File(filePath);
            }
            return file;
    }
    public static String[] prepend(String[] input, String prepend) {
        String[] output = new String[input.length];
        for (int index = 0; index < input.length; index++) {
            output[index] = "" + prepend + input[index];
        }
        return output;
    }
    public static File[] getFileList(ListView<String> list){
        List<File> filelist = new ArrayList<File>();
        for(String str : list.getItems()){
            File file = new File(str);
            filelist.add(file);
        }
        File[] result = filelist.toArray(new File[filelist.size()]);
        return result;
    }
}
