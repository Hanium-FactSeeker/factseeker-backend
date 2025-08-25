package com.factseekerbackend.domain.history.service;

import com.factseekerbackend.domain.history.dto.response.HistoryResponse;
import com.factseekerbackend.domain.history.repository.AnalysisHistoryRepository;
import com.factseekerbackend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class HistoryService {


}
