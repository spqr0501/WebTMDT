package com.example.demo.service;
public final class VietnamPhone {
 private VietnamPhone(){}
 public static String normalize(String value){
  if(value==null)return null;
  String s=value.strip().replaceAll("[\\s().-]", "");
  if(s.startsWith("+84"))s="0"+s.substring(3);
  else if(s.startsWith("84")&&s.length()==11)s="0"+s.substring(2);
  return s;
 }
}
