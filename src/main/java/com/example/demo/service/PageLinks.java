package com.example.demo.service;
import java.util.*;
public final class PageLinks {
 private PageLinks(){}
 public record Link(Integer index,boolean current){public boolean isGap(){return index==null;}}
 public static List<Link> of(int page,int total){
  if(total<1)return List.of();var indices=new TreeSet<Integer>();indices.add(0);indices.add(total-1);for(int i=Math.max(0,page-2);i<=Math.min(total-1,page+2);i++)indices.add(i);
  var links=new ArrayList<Link>();int prior=-1;for(int i:indices){if(prior>=0&&i-prior>1)links.add(new Link(null,false));links.add(new Link(i,i==page));prior=i;}return List.copyOf(links);
 }
}
