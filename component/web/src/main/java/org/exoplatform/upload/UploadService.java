/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.upload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PortalContainerInfo;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Dec 8, 2006  
 */
public class UploadService {

  private Map<String, UploadResource> uploadResources = new LinkedHashMap<String, UploadResource>() ;
  private String uploadLocation_ ;
  
  @SuppressWarnings("unused")
  public UploadService(PortalContainerInfo pinfo, InitParams params) throws Exception {
    String tmpDir = System.getProperty("java.io.tmpdir") ;
    uploadLocation_ = tmpDir + "/" + pinfo.getContainerName() + "/eXoUpload" ;
    File uploadDir = new File(uploadLocation_) ;
    if(!uploadDir.exists()) uploadDir.mkdirs() ;    
  }

  public void createUploadResource(HttpServletRequest request) throws IOException {
    String uploadId =  request.getParameter("uploadId") ;
    UploadResource upResource = new UploadResource(uploadId) ;
    RequestStreamReader reader = new RequestStreamReader(upResource);
    
    String headerEncoding =  request.getCharacterEncoding();    
    Map<String, String> headers = reader.parseHeaders(request.getInputStream(), headerEncoding);
   
    String fileName = reader.getFileName(headers);
    if(fileName == null) fileName = uploadId;
    fileName = new File(fileName).getName();
    upResource.setFileName(fileName);
    upResource.setMimeType(headers.get(RequestStreamReader.CONTENT_TYPE));
    upResource.setStoreLocation(uploadLocation_ + "/" + uploadId+"."+fileName) ;
    upResource.setEstimatedSize(request.getContentLength()) ;
    
    uploadResources.put(upResource.getUploadId(), upResource) ;
    

    FileOutputStream output = new FileOutputStream(upResource.getStoreLocation());
    reader.readBodyData(request, output);

    if(upResource.getStatus() == UploadResource.UPLOADING_STATUS){
      upResource.setStatus(UploadResource.UPLOADED_STATUS) ;
      return;
    }
    uploadResources.remove(uploadId) ;
    File file  = new File(upResource.getStoreLocation());
    file.delete();  
  }  
  
  public UploadResource getUploadResource(String uploadId) {//throws Exception 
    UploadResource upResource = uploadResources.get(uploadId) ;
    return upResource ;
  }

  public void removeUpload(String uploadId){
    if(uploadId == null) return;
    UploadResource upResource = uploadResources.get(uploadId);
    if(upResource == null) return;
    File file  = new File(upResource.getStoreLocation());
    file.delete();
    uploadResources.remove(uploadId) ;
  }
  
}