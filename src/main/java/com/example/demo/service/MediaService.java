package com.example.demo.service;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.io.*;
import java.util.*;
@Service
public class MediaService {
 private final Path root;
 public MediaService(@Value("${app.upload-dir}") String root){this.root=Path.of(root).toAbsolutePath().normalize();}
 public String save(MultipartFile file,boolean video){
  if(file==null||file.isEmpty())return null;
  if(file.getSize()>(video?30:5)*1024L*1024L)throw new BusinessException(video?"Video tối đa 30 MB.":"Ảnh tối đa 5 MB.");
  try(InputStream in=file.getInputStream()){
   byte[] head=in.readNBytes(32);String ext;
   if(video){
    if(head.length<12||head[4]!='f'||head[5]!='t'||head[6]!='y'||head[7]!='p')throw new BusinessException("Chỉ nhận video MP4 hợp lệ.");ext="mp4";
   }else{
    if(head.length>=8&&head[0]==(byte)0x89&&head[1]=='P'&&head[2]=='N'&&head[3]=='G')ext="png";
    else if(head.length>=3&&head[0]==(byte)0xff&&head[1]==(byte)0xd8&&head[2]==(byte)0xff)ext="jpg";
    else if(head.length>=12&&new String(head,0,4,java.nio.charset.StandardCharsets.US_ASCII).equals("RIFF")&&new String(head,8,4,java.nio.charset.StandardCharsets.US_ASCII).equals("WEBP"))ext="webp";
    else throw new BusinessException("Chỉ nhận ảnh JPG, PNG hoặc WebP.");
   }
   Files.createDirectories(root);String name=UUID.randomUUID()+"."+ext;
   try(InputStream all=file.getInputStream()){Files.copy(all,root.resolve(name));}cleanupOnRollback(root.resolve(name));return "/media/"+name;
  }catch(IOException e){throw new BusinessException("Không thể lưu tệp. Kiểm tra thư mục uploads.");}
 }
 public String saveReviewPhoto(MultipartFile file){
  return saveDecodedPhoto(file,false);
 }
 public String saveEvidencePhoto(MultipartFile file){return saveDecodedPhoto(file,true);}
 private String saveDecodedPhoto(MultipartFile file,boolean privatePhoto){
  if(file.getSize()>5*1024L*1024L)throw new BusinessException("Mỗi ảnh đánh giá tối đa 5 MB.");
  try(var source=file.getInputStream();var stream=javax.imageio.ImageIO.createImageInputStream(source)){
   var readers=javax.imageio.ImageIO.getImageReaders(stream);
   if(!readers.hasNext())throw new BusinessException("Ảnh đánh giá phải là JPG hoặc PNG hợp lệ.");
   var reader=readers.next();
   try{
    String format=reader.getFormatName();if(!format.equalsIgnoreCase("png")&&!format.equalsIgnoreCase("jpeg"))throw new BusinessException("Ảnh đánh giá chỉ nhận JPG hoặc PNG.");
    reader.setInput(stream);long pixels=(long)reader.getWidth(0)*reader.getHeight(0);
    if(pixels>16000000||pixels==0)throw new BusinessException("Ảnh đánh giá tối đa 16 triệu điểm ảnh.");
    var decoded=reader.read(0);Path folder=privatePhoto?root.resolve("private"):root;Files.createDirectories(folder);String name=UUID.randomUUID()+".png";Path path=folder.resolve(name);cleanupOnRollback(path);
    if(!javax.imageio.ImageIO.write(decoded,"png",path.toFile()))throw new IOException("Không ghi được ảnh");
    return (privatePhoto?"/private-media/":"/media/")+name;
   }finally{reader.dispose();}
  }catch(IOException e){throw new BusinessException("Không đọc được ảnh. Vui lòng chọn ảnh JPG hoặc PNG khác.");}
 }
 private void cleanupOnRollback(Path path){
  if(org.springframework.transaction.support.TransactionSynchronizationManager.isSynchronizationActive())
   org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(new org.springframework.transaction.support.TransactionSynchronization(){
    @Override public void afterCompletion(int status){if(status!=STATUS_COMMITTED)try{Files.deleteIfExists(path);}catch(IOException e){org.slf4j.LoggerFactory.getLogger(MediaService.class).warn("Không dọn được tệp của giao dịch thất bại: {}",path.getFileName());}}
   });
 }
 public Path resolve(String name){
  return resolveUnder(root,name);
 }
 public Path resolvePrivate(String stored){if(stored==null||!stored.startsWith("/private-media/"))throw new BusinessException("Tệp không hợp lệ.");return resolveUnder(root.resolve("private"),stored.substring("/private-media/".length()));}
 private Path resolveUnder(Path folder,String name){
  if(!name.matches("[a-f0-9-]{36}\\.(jpg|png|webp|mp4)"))throw new BusinessException("Tệp không hợp lệ.");
  Path file=folder.resolve(name).normalize();if(!file.startsWith(folder)||!Files.isRegularFile(file))throw new BusinessException("Không tìm thấy tệp.");return file;
 }
}
