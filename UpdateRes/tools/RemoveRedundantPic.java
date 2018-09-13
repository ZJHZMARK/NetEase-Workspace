package com.netease.tools;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.netease.tools.model.ImgStatus;
import com.netease.tools.ui.select.SelectImgDialog;
import com.netease.tools.util.CommandUtil;
import com.netease.tools.util.ConfigUtil;
import com.netease.tools.util.Fio;
import operation.ImgOperation;
import org.apache.http.util.TextUtils;
import java.util.*;
import java.io.*;

/**
 * Created by maqicheng on 30/07/2018.
 */

public class RemoveRedundantPic extends AnAction{
    @Override
    public void actionPerformed(AnActionEvent AnActionEvent){
        Project project = AnActionEvent.getData(DataKeys.PROJECT);
        MipmapPathDialog mipmap_git_pathdlg = new MipmapPathDialog(project


    }

    private void startsearchprocess(Project project, String mipmapPath){
        if (TextUtils.isEmpty(mipmapPath)) {
            Messages.showMessageDialog("wrong mipmap_git path", "Error", Messages.getErrorIcon());
            return;
        }
        ConfigUtil.setMipmapGitPath(mipmapPath);
        Process process = doGitPullProcess(project, mipmapPath);
            if(process == null){
                return;
            }

            StringBuilder str = new StringBuilder(124 * 1024).append("===================\n")
                    .append(new Date(System.currentTimeMillis())).append("\n");

            boolean success = true;
            try{
                String outputResPath = getOutputResPath(project);
            List<ImgOperation> operations = new ArrayList<ImgOperation>();
            operations.addAll(getUpdateMipmapOperations(project, mipmapPath, "mipmap-mdpi"));
            operations.addAll(getUpdateMipmapOperations(project, mipmapPath, "mipmap-hdpi"));
            operations.addAll(getUpdateMipmapOperations(project, mipmapPath, "mipmap-xhdpi"));
            operations.addAll(getUpdateMipmapOperations(project, mipmapPath, "mipmap-xxhdpi"));
            operations.addAll(getUpdateMipmapOperations(project, mipmapPath, "mipmap-xxxhdpi"));
            operations.addAll(getRemoveUnusedImagesOperations(getInputMipmapPath(mipmapPath, "mipmap-mdpi"), outputResPath));
            operations.addAll(getRemoveUnusedImagesOperations(getInputMipmapPath(mipmapPath, "mipmap-hdpi"), outputResPath));
            operations.addAll(getRemoveUnusedImagesOperations(getInputMipmapPath(mipmapPath, "mipmap-xhdpi"), outputResPath));
            operations.addAll(getRemoveUnusedImagesOperations(getInputMipmapPath(mipmapPath, "mipmap-xxhdpi"), outputResPath));
            operations.addAll(getRemoveUnusedImagesOperations(getInputMipmapPath(mipmapPath, "mipmap-xxxhdpi"), outputResPath));
            SelectImgDialog selectDlg = new SelectImgDialog(project, operations);
            selectDlg.show();
            if (selectDlg.isOK()) {
                List<ImgOperation> selectOps = selectDlg.getSelectedOps();
                for (ImgOperation op : selectOps) {
                    str.append(op.run())
                            .append("\n");
                }
            }
        } catch (Exception e) {
            Messages.showMessageDialog(e.toString(), "Error", Messages.getErrorIcon());
            success = false;
        }
        str.append("\n");
        NEConsole.show(project, process, str.toString());

        if(success){
            writeToFile(project, str.toString());
        }
    }

    private String getOutputResPath(Project project) {
        return project.getBasePath() + File.separator +
                "app" + File.separator +
                "src" + File.separator +
                "main" + File.separator +
                "res";
    }

    private String getOutputMipmapPath(Project project, String mipmap) {
        return getOutputResPath(project) + File.separator + mipmap;
    }

    private List<ImgOperation> getUpdateMipmapOperations(Project project, String mipmapGitPath, String mipmap) throws Exception{
        List<ImgOperation> result = new ArrayList<ImgOperation>();
        String  inMipmapPath = getInputMipmapPath(mipmapGitPath, mipmap);
        String toMipmapPath = getOutputMipmapPath(project, mipmap);

        return result;
    }



    private List<ImgOperation> getRemoveUnusedImagesOperations(String inMipmapPath, String toResPath) throws Exception {
        List<ImgOperation> result = new ArrayList<ImgOperation>();

        File inMipmapFile = new File(inMipmapPath);
        if(!inMipmapFile.exists()){
            return result;
        }

        String toMipmapPath = toResPath + File.separator + inMipmapFile.getName();
        File toMipmapFile = new File(toMipmapPath);

        if(!toMipmapFile.exists()){
            return result;
        }

        File[] toFiles = toMipmapFile.listFiles();
        if(toFiles == null){
            return result;
        }

        for (File toFile : toFiles) {
            String toName = toFile.getName();
            if (!isPicture(toName)) {
                continue;
            }

            String[] splt = toName.split("[_]", 2);
            if (splt.length == 2) {
                String Module = splt[0];
                String Name = splt[1];
                String inPath = inMipmapPath + File.separator + Module + File.separator + Name;
                File inFile = new File(inPath);
                if (!inFile.exists()) {
                    result.add(new ImgOperation(inPath, inFile.getAbsolutePath()));
                }
            }


        }
        return result;

    }





    private Process doGitPullProcess(Project project, String mipmapGitPath) {


        if (TextUtils.isEmpty(mipmapGitPath)) {
            Messages.showMessageDialog("wrong mipmap_git path", "Error", Messages.getErrorIcon());
            return null;
        }


        String commandLine = "cd " + mipmapGitPath + "\n";


        String[] cmds = CommandUtil.getSystemCmds(commandLine);

        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmds, null, new File(project.getBasePath()));
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            String error = e.getMessage() + "   \n  " + e.getCause();

            for (StackTraceElement stackTrace : e.getStackTrace()) {
                error += stackTrace.toString() + "\n";
            }

            Messages.showMessageDialog(error, "Error", Messages.getErrorIcon());
            return null;
        }

        NEConsole.show(project, process, cmds.toString());
        return process;
    }


    private boolean isPicture(String str){
        if(str == null){
            return false;
        }
        return str.endsWith(".png") || str.endsWith(".jpg") || str.endsWith(".jpeg") || str.endsWith(".webp")
                || str.endsWith(".gif") || str.endsWith(".svg") || str.endsWith(".psd");
    }


    private String getInputMipmapPath(String mipmapGitPath, String mipmap) {
        return mipmapGitPath + File.separator + "android" + File.separator + mipmap;
    }

    private List<ImgOperation> getunusedImageOperation (String inMipmapPath, String toResPath) throws Exception {
        List<ImgOperation> result = new ArrayList<ImgOperation>();
        File inMipmapFile = new File(inMipmapPath);
        if (!inMipmapFile.exists()){
            return result;
        }

    }

    private List<String> comcaredresult(List<String> lst1, List<String> lst2){
        List<String> result = new ArrayList<String>();
        for(int i = 0; i < lst1.size() ; i++){
            String current = lst1.get(i);
            boolean hasmatch = false;
            for(int j = 0; j < lst2.size(); j++){
                String target = lst2.get(j);
                if( current.equalsIgnoreCase(target) ){
                    hasmatch = true;
                }
            }
            if(hasmatch = false){
                result.add(current);
            }

        }
        return result; //list of pic name that is unused in the android application.
    }

    private void writeToFile(Project project, String msg) {
        String filePath = project.getBaseDir().getParent().getPath() + File.separator + "res_update_record.txt";
        Fio.writeToFile(filePath, msg, true);
    }

    private List<String> resultImageOperation (List<String> targetlist){

    }


}
