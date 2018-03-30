/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.acconsulting.sftp;

import it.acconsulting.conf.ConfigurationException;
import it.acconsulting.conf.ConfigurationProperty;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

/**
 *
 * @author Admin
 */
public class SFTPTransfer {
    
    public void transfer(ArrayList<String> eventsRecords, ArrayList<String> accFileList ) throws IOException, ConfigurationException {
        if (eventsRecords.size() > 0) {
                SimpleDateFormat FullDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSSS");
                // file Name
                String FileName="ISA_Event_"+FullDateFormat.format(new java.util.Date())+".txt";
                
                String longPath = ConfigurationProperty.ISTANCE.getProperty(ConfigurationProperty.LONG_PATH_DWH);
                String FullFileName=longPath+FileName;
                
                //apri file
                File FPT=new File(FullFileName);
                BufferedWriter writer = new BufferedWriter(new FileWriter(FPT));

                for (int i = 0; i < eventsRecords.size(); i++) {
                    // write file
                    writer.write (eventsRecords.get(i));
                }
                //close File
                writer.close();
                
                // FTP file Transfer
                boolean Ret =SFTP_FileTranfer(FullFileName, FileName);
                for (String accFileName : accFileList) {
                    File F= new File(accFileName);
                    
                    SFTP_FileTranfer(accFileName, F.getName());
                }
        }  
    }
    
    
    boolean SFTP_FileTranfer(String FileName,String RemoteFinalFile){
        StandardFileSystemManager manager = new StandardFileSystemManager();

        try {
              String serverAddress = "FTPServer";
              String userId = "FTPUser";
              String password = "FTPPass";
              String remoteDirectory = "FTPPath";

              //check if the file exists
              String filepath = FileName;
              File file = new File(filepath);
              if (!file.exists()) {  throw new RuntimeException("Error. Local file not found"); }

              //Initializes the file manager
              manager.init();

              //Setup our SFTP configuration
              FileSystemOptions opts = new FileSystemOptions();
              SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking( opts, "no");
              SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);
              SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, 10000);

              //Create the SFTP URI using the host name, userid, password,  remote path and file name
              String sftpUri ;
              sftpUri =createConnectionString(serverAddress,userId,password,remoteDirectory+ "/" + RemoteFinalFile);
              System.out.println(sftpUri);

              // Create local file object
              FileObject localFile = manager.resolveFile(file.getAbsolutePath());

              // Create remote file object
              FileObject remoteFile = manager.resolveFile(sftpUri, opts);

              // Copy local file to sftp server
              remoteFile.copyFrom(localFile, Selectors.SELECT_SELF);
              System.out.println("File upload successful");

       }
       catch (Exception ex) {
            ex.printStackTrace();
            return false;
       }
       finally {
           manager.close();
       }

       return true;
    }
    
    public static String createConnectionString(String hostName, String username, String password, String remoteFilePath) {
        return "sftp://" + username + ":" + password + "@" + hostName + "/" + remoteFilePath;
    }
}
