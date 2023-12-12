package com.hedgehog.client.board.model.service;

import com.hedgehog.client.board.model.dao.BoardMapper;
import com.hedgehog.client.board.model.dto.QuestionDTO;
import com.hedgehog.common.paging.SelectCriteria;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class BoardService {
    private final BoardMapper mapper;

    public int selectTotalCountQuestionList(Map<String, String> searchMap) {
        int result = mapper.selectTotalCountQuestionList(searchMap);
        log.info("");
        log.info("");
        log.info("BoardService : selectTotalCountQuestionList ... : " + result);

        return result;
    }

    public List<QuestionDTO> selectQuestionList(SelectCriteria selectCriteria) {
        List<QuestionDTO> result = mapper.selectQuestionList(selectCriteria);
        log.info("");
        log.info("");
        log.info("BoardService : selectQuestionList ... : " + result);

        return result;
    }
}
