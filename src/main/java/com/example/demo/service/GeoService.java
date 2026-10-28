package com.example.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

@Service
public class GeoService {
 public record Place(String address,double latitude,double longitude){}
 private record Cached(Instant expires,List<Place> places){}
 private final Map<String,Cached> cache=new LinkedHashMap<>();
 private final HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();
 private final ObjectMapper mapper;
 @Value("${app.maps.geocoder-base-url:https://photon.komoot.io}") private String baseUrl;
 private Instant nextRequest=Instant.EPOCH;
 public GeoService(ObjectMapper mapper){this.mapper=mapper;}
 public static void validate(Double lat,Double lng){
  if((lat==null)!=(lng==null)||(lat!=null&&(!Double.isFinite(lat)||!Double.isFinite(lng)||lat< -90||lat>90||lng< -180||lng>180)))throw new BusinessException("Tọa độ chưa hợp lệ. Hãy chọn lại vị trí trên bản đồ.");
 }
 public List<Place> search(String q){if(q==null||q.strip().length()<3||q.length()>200)throw new BusinessException("Nhập địa chỉ tìm kiếm từ 3 đến 200 ký tự.");return lookup("/api/?limit=5&q="+URLEncoder.encode(q.strip(),StandardCharsets.UTF_8));}
 public List<Place> reverse(double lat,double lng){validate(lat,lng);return lookup(String.format(Locale.ROOT,"/reverse?limit=1&lat=%.6f&lon=%.6f",lat,lng));}
 private synchronized List<Place> lookup(String path){
  Instant now=Instant.now();cache.entrySet().removeIf(e->!e.getValue().expires().isAfter(now));var hit=cache.get(path);if(hit!=null)return hit.places();
  if(now.isBefore(nextRequest))throw new BusinessException("Vui lòng đợi một giây rồi tìm lại địa chỉ.");nextRequest=now.plusMillis(1100);
  try{
   URI base=URI.create(baseUrl);if(!"https".equals(base.getScheme())||base.getHost()==null||base.getQuery()!=null||base.getUserInfo()!=null)throw new IllegalArgumentException();
   var request=HttpRequest.newBuilder(URI.create(baseUrl.replaceAll("/+$","")+path)).timeout(Duration.ofSeconds(7)).header("User-Agent","BongBakery/1.0 (address selection)").header("Accept","application/json").GET().build();
   try(var response=client.send(request,HttpResponse.BodyHandlers.ofInputStream()).body()){
    byte[] bytes=response.readNBytes(524289);if(bytes.length>524288)throw new IllegalStateException();var data=mapper.readTree(bytes);if(!data.path("features").isArray())throw new IllegalStateException();var places=new ArrayList<Place>();
    for(var feature:data.path("features")){
     var xy=feature.path("geometry").path("coordinates");if(xy.size()!=2||!xy.get(0).isNumber()||!xy.get(1).isNumber())continue;double lat=xy.get(1).asDouble(),lng=xy.get(0).asDouble();validate(lat,lng);
     var p=feature.path("properties");var parts=new LinkedHashSet<String>();String street=(p.path("housenumber").asText("")+" "+p.path("street").asText("")).strip();
     if(!p.path("name").asText("").isBlank())parts.add(p.path("name").asText());if(!street.isBlank())parts.add(street);
     for(String key:List.of("district","city","county","state","country")){String text=p.path(key).asText("");if(!text.isBlank())parts.add(text);}
     String text=String.join(", ",parts);if(!text.isBlank())places.add(new Place(text.substring(0,Math.min(500,text.length())),lat,lng));if(places.size()==5)break;
    }
    if(cache.size()>=512)cache.remove(cache.keySet().iterator().next());var result=List.copyOf(places);cache.put(path,new Cached(now.plusSeconds(600),result));return result;
   }
  }catch(InterruptedException e){Thread.currentThread().interrupt();throw new BusinessException("Tra cứu bị gián đoạn. Bạn có thể nhập địa chỉ bằng tay.");}
  catch(Exception e){throw new BusinessException("Chưa tra được địa chỉ. Bạn vẫn có thể giữ ghim và nhập địa chỉ bằng tay.");}
 }
}
