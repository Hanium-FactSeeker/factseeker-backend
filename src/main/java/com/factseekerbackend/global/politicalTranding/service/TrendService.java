package com.factseekerbackend.global.politicalTranding.service;

import com.factseekerbackend.global.exception.BusinessException;
import com.factseekerbackend.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class TrendService {

  private volatile List<String> latestTrends = new ArrayList<>();

  public List<String> getLatestTrends() {
    if (CollectionUtils.isEmpty(latestTrends)) {
      throw new BusinessException(ErrorCode.TRENDS_NOT_AVAILABLE);
    }
    return latestTrends;
  }

  public void updateTrends(List<String> newTrends) {
    this.latestTrends = newTrends;
    log.info("Updated Trends: {}", newTrends);
  }

}
