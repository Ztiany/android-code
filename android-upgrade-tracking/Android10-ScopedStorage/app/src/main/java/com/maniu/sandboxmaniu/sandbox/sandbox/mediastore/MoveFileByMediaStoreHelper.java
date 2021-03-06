
/*********************************************************************
 ** Copyright (C), 2010-2020, OPPO Mobile Comm Corp., Ltd.
 ** VENDOR_EDIT
 ** File        :
 ** Description : MoveFileByMediaStoreHelper.
 ** Version     : V1.0
 ** Date        : 2020-03-10
 ** Author      : dingyong@Apps.Gallery3D
 **
 ** ---------------------Revision History: ----------------------------
 **  <author>                    <date>          <version>      <desc>
 **  dingyong@Apps.Gallery3D     2020-03-10       V1.0          MoveFileByMediaStoreHelper
 ***********************************************************************/
package com.maniu.sandboxmaniu.sandbox.sandbox.mediastore;

import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;


import com.maniu.sandboxmaniu.sandbox.sandbox.IOUtils;
import com.maniu.sandboxmaniu.sandbox.sandbox.OppoEnvironment;
import com.maniu.sandboxmaniu.sandbox.sandbox.constants.FileConstants;
import com.maniu.sandboxmaniu.sandbox.sandbox.request.DeleteFileRequest;
import com.maniu.sandboxmaniu.sandbox.sandbox.request.NewFileRequest;
import com.maniu.sandboxmaniu.sandbox.sandbox.request.OpenFileRequest;
import com.maniu.sandboxmaniu.sandbox.sandbox.request.RenameToFileRequest;
import com.maniu.sandboxmaniu.sandbox.sandbox.response.FileResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MoveFileByMediaStoreHelper {
    private static final String TAG = "MoveFileByMediaStoreHelper";
    private final static int RESULT_SUCCESS = 0;

    public static FileResponse renameTo(Context context, RenameToFileRequest renameRequest) {
        if ((renameRequest == null)
                || (renameRequest.getSrcFile() == null)
                || (renameRequest.getTargetFile() == null)) {
            return null;
        }
        // get file
        File srcFile = renameRequest.getSrcFile();
        File targetFile = renameRequest.getTargetFile();

        // get filePath
        String srcFilePath = srcFile.getAbsolutePath();
        String targetFilePath = targetFile.getAbsolutePath();

        // getRelative
        String srcRelative = OppoEnvironment.getRelativePath(srcFilePath);
        String targetRelative = OppoEnvironment.getRelativePath(targetFilePath);
        if (isPublicToPublic(srcRelative, targetRelative)) {
            // ????????????????????????????????????
            if (targetIsDownLoad(targetRelative)) {
                // ??????????????? Download ??????
                return publicToDownloadOrDocuments(context, renameRequest, true);
            } else if (targetIsDocuments(targetRelative)) {
                // ??????????????? Documents ??????
                return publicToDownloadOrDocuments(context, renameRequest, false);
            } else {
                // ??????????????? DCIM  Pictures Movies??????
                return publicToPublicMedia(context, renameRequest, targetRelative);
            }

        } else if (isPublicToPrivate(srcRelative, targetRelative)) {
            // ????????????????????????????????????
            return publicToPrivate(context, renameRequest);
        } else if (isPrivateToPublic(srcRelative, targetRelative)) {
            // ????????????????????????????????????
            return privateToPublic(context, renameRequest);
        }
        return null;
    }

    /**
     * ????????????????????????????????? Download ??????Documents ?????????
     * 1.????????????????????????????????????????????????????????????????????????
     * 2.?????? MediaStore.Downloads.getContentUri(volumeName)  ????????? download?????? ?????? MediaStore.Files.getContentUri(volumeName) ?????????Documents??????
     * 3.?????? contentResolver.openOutputStream(uri)  ????????????????????? OutputStream
     * 4.?????? contentResolver.openInputStream(uri)   ?????????????????? inputStream
     * 5.InputStream OutputStream ????????????
     * 6.????????????????????????????????????
     *
     * @param context       Context
     * @param renameRequest RenameToFileRequest
     * @param isDownload    ?????????Download??????  true???Download false???documents
     * @return FileResponse FileResponse
     */
    private static FileResponse publicToDownloadOrDocuments(Context context,
                                                            RenameToFileRequest renameRequest,
                                                            boolean isDownload) {
        long time = System.currentTimeMillis();
        FileResponse fileAccessResponse = null;

        File srcFile = renameRequest.getSrcFile();
        File targetFile = renameRequest.getTargetFile();

        if (targetFile.exists()) {
            //1.????????????????????????????????????????????????????????????????????????
            DeleteFileRequest deleteRequest = new DeleteFileRequest.Builder()
                    .setFile(targetFile)
                    .setImage(renameRequest.isImage())
                    .setMediaId(renameRequest.getMediaId())
                    .setUri(renameRequest.getUri())
                    .builder();
            DeleteFileByMediaStoreHelper.delete(context, deleteRequest);
        }
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {

            //???????????????
            Uri srcUri = MediaStoreUriHelper.getUri(context, renameRequest);
            OpenFileRequest srcOpenRequest = new OpenFileRequest.Builder()
                    .setModeType(FileConstants.FileModeType.MODE_READ)
                    .setFile(srcFile)
                    .setUri(srcUri)
                    .builder();

            fileInputStream = (FileInputStream) OpenFileByMediaStoreHelper
                    .getInputStream(context, srcOpenRequest);


            String targetFilePath = targetFile.getAbsolutePath();
            Uri contentUri;
            if (isDownload) {
                contentUri = MediaStoreUriHelper.getDownloadContentUri(targetFilePath);
            } else {
                contentUri = MediaStoreUriHelper.getFileContentUri(targetFilePath);
            }
            Boolean isImageFile = renameRequest.isImage();
            boolean isImage = false;
            if (isImageFile == null) {
                isImage = !MediaStoreUriHelper.isVideo(srcFile.getAbsolutePath());
            } else {
                isImage = isImageFile;
            }
            //???????????????????????????Request
            NewFileRequest createRequest = new NewFileRequest.Builder()
                    .setFile(targetFile)
                    .setImage(isImage)
                    .setContentValues(renameRequest.getContentValues())
                    .setContentUri(contentUri)
                    .builder();
            //??????????????????
            FileResponse response = CreateFileByMediaStoreHelper.newCreateFile(context, createRequest);
            if ((response != null) && (response.isSuccess())) {
                //??????????????????
                Uri uri = response.getUri();
                OpenFileRequest openRequest = new OpenFileRequest.Builder()
                        .setModeType(FileConstants.FileModeType.MODE_WRITE)
                        .setUri(uri)
                        .builder();
                fileOutputStream = (FileOutputStream) OpenFileByMediaStoreHelper
                        .getOutStream(context, openRequest);
                if ((fileOutputStream != null) && (fileInputStream != null)) {
                    byte[] buf = new byte[1024];
                    while (fileInputStream.read(buf) != -1) {
                        fileOutputStream.write(buf);
                    }
                    // step 4 ???????????????
                    DeleteFileRequest deleteRequest = new DeleteFileRequest.Builder()
                            .setUri(srcUri)
                            .builder();
                    boolean result = DeleteFileByMediaStoreHelper.delete(context, deleteRequest);
                    response.setSuccess(result);
                    fileAccessResponse = response;
                }
            }
        } catch (Exception e) {
        } finally {
        }
        return fileAccessResponse;

    }


    /**
     * ??????????????? ?????????  ???????????????????????????  -- DCIM Pictures Movies
     * ???????????? ?????????????????? update ????????????  MediaStore.MediaColumns.RELATIVE_PATH
     *
     * @param context        Context
     * @param renameRequest  RenameToFileRequest
     * @param relativeTarget relativeTarget
     * @return FileResponse
     */
    private static FileResponse publicToPublicMedia(Context context,
                                                    RenameToFileRequest renameRequest,
                                                    String relativeTarget) {
        long time = System.currentTimeMillis();
        int result = -1;
        Uri uri = MediaStoreUriHelper.getUri(context, renameRequest);
        if (uri != null) {
            try {
                ContentResolver contentResolver = context.getContentResolver();
                ContentValues contentValues = renameRequest.getContentValues();
                if (contentValues == null) {
                    contentValues = new ContentValues();
                }
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativeTarget);
                result = contentResolver.update(uri, contentValues, null, null);
            } catch (RecoverableSecurityException exception) {
                // access to permission
                MediaStorePermissionHelper.startIntentSenderForResult(context, exception, MediaStorePermissionHelper.REQUEST_CODE_UPDATE);
            }
        }
        FileResponse response = new FileResponse.Builder()
                .setSuccess(result == RESULT_SUCCESS)
                .setUri(uri)
                .setAccessType(FileConstants.FileType.TYPE_MEDIA_STORE)
                .builder();
        return response;
    }

    /**
     * ?????????????????????????????????????????????
     * 1.??????????????????????????????????????????????????????????????????
     * 2.??????????????????InputStream
     * 3.?????? ????????????????????????????????????????????????????????????uri??????????????????uri
     * 4.??????contentResolver.openOutputStream(uri)  ????????????????????? OutputStream
     * 5.?????????????????????
     * 6.file?????????????????????
     *
     * @param context Context
     * @param request RenameToFileRequest
     * @return FileResponse
     */
    private static FileResponse privateToPublic(Context context, RenameToFileRequest request) {
        long time = System.currentTimeMillis();
        File srcFile = request.getSrcFile();
        File targetFile = request.getTargetFile();

        FileResponse fileAccessResponse = null;
        // target file  exists ???delete target file
        if (targetFile.exists()) {
            DeleteFileRequest deleteRequest = new DeleteFileRequest.Builder()
                    .setFile(targetFile)
                    .setImage(request.isImage())
                    .setUri(request.getUri())
                    .setMediaId(request.getMediaId())
                    .builder();
            DeleteFileByMediaStoreHelper.delete(context, deleteRequest);
        }

        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            // step 1  read src file FileInputStream
            fileInputStream = new FileInputStream(srcFile );
            Boolean isImageFile = request.isImage();
            boolean isImage = false;
            if (isImageFile == null) {
                isImage = !MediaStoreUriHelper.isVideo(srcFile.getAbsolutePath());
            } else {
                isImage = isImageFile;
            }
            // step 2   create target file
            NewFileRequest createRequest = new NewFileRequest.Builder()
                    .setFile(targetFile)
                    .setContentValues(request.getContentValues())
                    .setImage(isImage)
                    .builder();

            FileResponse response = CreateFileByMediaStoreHelper
                    .newCreateFile(context, createRequest);

            if ((response != null) && (response.isSuccess())) {
                // step 3 write target file
                Uri uri = response.getUri();
                OpenFileRequest openRequest = new OpenFileRequest.Builder()
                        .setModeType(FileConstants.FileModeType.MODE_WRITE)
                        .setUri(uri).builder();
                fileOutputStream = (FileOutputStream) OpenFileByMediaStoreHelper
                        .getOutStream(context, openRequest);
                if (fileOutputStream != null) {
                    byte[] buf = new byte[1024];
                    while (fileInputStream.read(buf) != -1) {
                        fileOutputStream.write(buf);
                    }
                    // step 4 delete srcFile
                    boolean delete = srcFile.delete();
                    response.setSuccess(delete);
                    fileAccessResponse = response;
                }
            } else {

            }
        } catch (Exception e) {
        } finally {

            IOUtils.closeQuietly(fileInputStream, fileOutputStream);
        }
        return fileAccessResponse;

    }

    /**
     * ?????????????????????????????? ??????????????????
     * 1.?????????????????????????????????file????????????????????????
     * 2.?????????????????????????????????
     * 3.?????? contentResolver.openInputStream(uri)   ?????????????????? inputStream
     * 4.?????? OutPutStream ????????????
     * 5.????????????????????????????????????
     *
     * @param context Context
     * @param request RenameToFileRequest
     * @return FileResponse
     */
    private static FileResponse publicToPrivate(Context context, RenameToFileRequest request) {
        long time = System.currentTimeMillis();
        boolean result = false;

        File srcFile = request.getSrcFile();
        File targetFile = request.getTargetFile();

        FileInputStream fileInputStream = null;
        FileOutputStream outputStream = null;
        try {
            // step 1
            boolean mkdirResult = true;
            boolean createNewFile;
            if (targetFile.exists()) {
                boolean delete = targetFile.delete();
            }

            File parentFile = targetFile.getParentFile();
            if ((parentFile != null) && (!parentFile.exists())) {
                mkdirResult = parentFile.mkdir();
            }
            createNewFile = targetFile.createNewFile();


            // step 2
            OpenFileRequest openRequest = new OpenFileRequest.Builder()
                    .setModeType(FileConstants.FileModeType.MODE_READ)
                    .setFile(srcFile)
                    .setImage(request.isImage())
                    .setMediaId(request.getMediaId())
                    .setUri(request.getUri())
                    .builder();
            if (mkdirResult && createNewFile) {
                fileInputStream = (FileInputStream) OpenFileByMediaStoreHelper
                        .getInputStream(context, openRequest);
                outputStream = new FileOutputStream(targetFile );
                if (fileInputStream != null) {
                    byte[] buf = new byte[1024];
                    while (fileInputStream.read(buf) != -1) {
                        outputStream.write(buf);
                    }
                    // step 3
                    DeleteFileRequest deleteRequest = new DeleteFileRequest.Builder()
                            .setFile(srcFile)
                            .setUri(request.getUri())
                            .setImage(request.isImage())
                            .setMediaId(request.getMediaId())
                            .builder();
                    result = DeleteFileByMediaStoreHelper.delete(context, deleteRequest);
                }
            }
        } catch (Exception e) {
        } finally {
            IOUtils.closeQuietly(fileInputStream, outputStream);
        }
        return new FileResponse.Builder()
                .setAccessType(FileConstants.FileType.TYPE_MEDIA_STORE)
                .setSuccess(result)
                .builder();
    }


    /**
     * ????????????????????? Download??????
     *
     * @param targetRelative RelativePath
     * @return boolean
     */
    private static boolean targetIsDownLoad(String targetRelative) {
        return OppoEnvironment.isRelativeDownloadsDir(targetRelative);
    }


    /**
     * ????????????????????? Documents??????
     *
     * @param targetRelative RelativePath
     * @return boolean
     */
    private static boolean targetIsDocuments(String targetRelative) {
        return OppoEnvironment.isRelativeDocumentsDir(targetRelative);
    }

    /**
     * ?????????????????????????????? ????????????
     *
     * @param srcRelative    srcRelative
     * @param targetRelative targetRelative
     * @return boolean
     */
    private static boolean isPublicToPublic(String srcRelative, String targetRelative) {
        return (OppoEnvironment.isRelativePublicDir(srcRelative))
                && (OppoEnvironment.isRelativePublicDir(targetRelative));
    }

    /**
     * ?????????????????????????????? ???????????? Android/data
     *
     * @param srcRelative    srcRelative
     * @param targetRelative targetRelative
     * @return boolean
     */
    private static boolean isPublicToPrivate(String srcRelative, String targetRelative) {
        return (OppoEnvironment.isRelativePublicDir(srcRelative))
                && (OppoEnvironment.isRelativePrivateDir(targetRelative));
    }

    /**
     * ????????????????????????????????????
     *
     * @param srcRelative    srcRelative
     * @param targetRelative targetRelative
     * @return boolean
     */
    private static boolean isPrivateToPublic(String srcRelative, String targetRelative) {
        return (OppoEnvironment.isRelativePrivateDir(srcRelative))
                && (OppoEnvironment.isRelativePublicDir(targetRelative));
    }
}
