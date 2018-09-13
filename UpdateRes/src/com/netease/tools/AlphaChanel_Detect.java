package com.netease.tools;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.netease.tools.ui.select.SelectImgDialog;
import com.netease.tools.util.CommandUtil;
import com.netease.tools.util.ConfigUtil;
import com.netease.tools.util.Fio;
import operation.ImgOperation;
import org.apache.batik.svggen.ImageCacher;
import org.apache.http.util.TextUtils;
import sun.jvm.hotspot.oops.Array;
import sun.jvm.hotspot.utilities.BitMap;
import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by maqicheng on 14/08/2018.
 */
public class AlphaChanel_Detect extends AnAction {

    int depth = 0;
    String path = null;
    List<String> list_all = new ArrayList<String>();

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        // TODO: insert action logic here
        Project project = anActionEvent.getData(DataKeys.PROJECT);
        if (project != null) {
                    MipmapPathDialog mipmapGitPathDlg = new MipmapPathDialog(project);
                    mipmapGitPathDlg.show();
                    if (mipmapGitPathDlg.getExitCode() != DialogWrapper.OK_EXIT_CODE) {
                        return;
            }

            try {
                startsearchprocess(project, mipmapGitPathDlg.getMipmapGitPath());
            } catch (Exception e) {
                e.printStackTrace();
                String error = e.getMessage() + "   \n  " + e.getCause();

                for (StackTraceElement stackTrace : e.getStackTrace()) {
                    error += stackTrace.toString() + "\n";
                }
                Messages.showMessageDialog(error, "Error", Messages.getErrorIcon());
            }
        }
    }


    private List<String> printDirectory(File f, int depth){
        if (!f.isDirectory()) {
            System.out.println("not a Directory");
        } else {
            File[] fs = f.listFiles();
            depth++;
            for (int i = 0; i < fs.length; i++) {
                File file = fs[i];
                path = file.getPath();
                list_all.add(path);
                printDirectory(file, depth);
            }
        }
        return list_all;
    }


    public List<String> getPictures(final String strPath) {

        List<String> list_last = new ArrayList<String>();
        List<String> list = new ArrayList<String>();
        File file = new File(strPath);
        list = printDirectory(file, depth);
        list.size();

        for (int k = 0; k < list.size(); k++) {

            int idx = list.get(k).lastIndexOf(".");
           //Log.v("idx:", String.valueOf(idx));
            if (idx <= 0) {
                continue;
            }
            String suffix = list.get(k).substring(idx);

            if (suffix.toLowerCase(Locale.PRC).equals(".png")
                    ) {
                list_last.add(list.get(k));
            }
        }

        list_all.clear();
        return list_last;
    }

    private static void writeToFile(Project project, String msg) {
        String filePath = project.getBaseDir().getParent().getPath() + File.separator + "alpha_chanel_check_result.txt";
        Fio.writeToFile(filePath, msg, true);
    }


    private void startsearchprocess(Project project, String mipmapPath){
        if (TextUtils.isEmpty(mipmapPath)) {
            Messages.showMessageDialog("wrong mipmap path", "Error", Messages.getErrorIcon());
            return;
        }

        StringBuilder str = new StringBuilder(124 * 1024).append("===================\n")
                .append(new Date(System.currentTimeMillis())).append("\n");
        boolean success = true;
        ArrayList<String> target_list = new ArrayList<String>();

        try{
            List<String> whole_path = new ArrayList<String>();
            List<String> result = new ArrayList<String>();

            /*whole_path.addAll(addPath(project, "mipmap-mdpi", whole_path));
            whole_path.addAll(addPath(project, "mipmap-hdpi", whole_path));
            whole_path.addAll(addPath(project, "mipmap-xhdpi", whole_path));
            whole_path.addAll(addPath(project, "mipmap-xxhdpi", whole_path));
            whole_path.addAll(addPath(project, "mipmap-xxxhdpi", whole_path));*/
            whole_path.addAll(getPictures("mipmap-mdpi"));
            whole_path.addAll(getPictures("mipmap-hdpi"));
            whole_path.addAll(getPictures("mipmap-xhdpi"));
            whole_path.addAll(getPictures("mipmap-xxhdpi"));
            whole_path.addAll(getPictures("mipmap-xxxhdpi"));
            for( int j = 0; j < whole_path.size(); j++ ) {
                result(whole_path.get(j),project);
            }


        } catch (Exception e) {
            Messages.showMessageDialog(e.toString(), "Error", Messages.getErrorIcon());
            success = false;
        }

        if(success){
            writeToFile(project, str.toString());
        }
    }

    private String getOutputMipmapPath(Project project, String mipmap) {
        return project.getBasePath() + File.separator + mipmap;
    }


    private ArrayList<String> addPath(Project project, String mipmap, ArrayList<String> currentList) throws Exception{
        String currentPath = getPath(mipmap);
        currentList.add(currentPath);
        return currentList;

    }

//    private String toString(ArrayList<String> list){
//        String listString = "";
//        for(String s: list){
//            listString += s + "\t";
//        }
//        return listString;
//    }



    private boolean makeDirExist(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return file.mkdirs();
        }

        return file.isDirectory();
    }

    private String getPath(String mipmap) {
        return File.separator + "android" + File.separator + mipmap;
    }





    /*public static void setAlphaChanel(String str) {

        try {
            ImageIcon imageIcon = new ImageIcon(str);
            BufferedImage bufferedimage = new BufferedImage(imageIcon.getIconWidth(), imageIcon.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D graph = (Graphics2D) bufferedimage.getGraphics();
            graph.drawImage(imageIcon.getImage(), 0, 0, imageIcon.getImageObserver());
            for (int i = bufferedimage.getMinY(); i < bufferedimage.getHeight(); i++) {
                for (int j = bufferedimage.getMinX(); j < bufferedimage.getWidth(); j++) {
                    int pixelpoint = bufferedimage.getRGB(j, i);
                    int[] RGB = new int[3];
                    RGB[0] = (pixelpoint & 0x00ff0000) >> 16;
                    RGB[1] = (pixelpoint & 0x0000ff00) >> 8;
                    RGB[2] = (pixelpoint & 0x000000ff);
                    int a = (pixelpoint & 0xff000000) >>> 24;

                    if ((RGB[0] == 0 || RGB[1] == 0 || RGB[2] == 0) || a == 0) {
                        pixelpoint = pixelpoint | 0xffffffff;
                    } else {
                        pixelpoint = (pixelpoint & 0xff000000) | 0xff000000
                    }
                    bufferedimage.setRGB(j, i, pixelpoint);

                }
                System.out.println();
            }
            graph.drawImage(bufferedimage, 0, 0, imageIcon.getImageObserver());
            ImageIO.write(bufferedimage, "png", new File("Downlowds:\\2.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }*/

    private static boolean compare(int r, int g, int b){
        int i = 0;
        if (r > 200) {
            i++;
        }
        if (g > 200) {
            i++;
        }
        if (b > 200) {
            i++;
        }
        if (i >= 2) {
            return true;
        } else {
            return false;
        }
    }


    private static void checkTransparency(BufferedImage image){
        if (containsAlphaChannel(image)){
            System.out.println(image.toString() + "image contains alpha channel");
        } else {
            System.out.println(image.toString() + "image does NOT contain alpha channel");
        }

        if (containsTransparency(image)){
            System.out.println(image.toString() + "image contains transparency");
        } else {
            System.out.println(image.toString() + "Image does NOT contain transparency");
        }
    }

    private static boolean containsAlphaChannel(BufferedImage image){
        return image.getColorModel().hasAlpha();
    }


    private static boolean containsTransparency(BufferedImage image){
        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                if (isTransparent(image, j, i)){
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isTransparent(BufferedImage image, int x, int y ) {
        int pixel = image.getRGB(x,y);
        return (pixel>>24) == 0x00;
    }



    private static void result(String pathToExcelFile, Project project) throws IOException{

        File pngInput = new File(pathToExcelFile);
        BufferedImage pngImage = ImageIO.read(pngInput);
        checkTransparency(pngImage);
        if(containsAlphaChannel(pngImage)){
            writeToFile(project, pathToExcelFile);
        }
    }


}
