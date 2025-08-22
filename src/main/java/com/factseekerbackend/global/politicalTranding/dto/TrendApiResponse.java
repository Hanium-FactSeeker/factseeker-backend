////package com.factseekerbackend.global.politicalTranding.dto;
////
////import java.util.List;
////
////public class TrendApiResponse {
////
////  private List<String> trends;
////  public List<String> getTrends() { return trends; }
////  public void setTrends(List<String> trends) { this.trends = trends; }
////
////}
//package com.factseekerbackend.global.politicalTranding.dto;
//
//import lombok.Getter;
//import lombok.Setter;
//
//import java.util.List;
//
//@Getter
//@Setter
//public class TrendApiResponse {
//  private boolean success;
//  private String message;
//  private List<String> data;  // ← 여기서 trends가 아니라 data여야 함
//
//  public List<String> getTrends() {
//    return data; // fastapi가 주는 리스트가 data니까
//  }
//}