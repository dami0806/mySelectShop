package com.sparta.myselectshop.naver.service;


import com.sparta.myselectshop.naver.dto.ItemDto;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j(topic = "NAVER API")
@Service
public class NaverApiService {

    private final RestTemplate restTemplate;

    @Value("${spring.api.naver.clientid}")
    private String clientId;

    @Value("${spring.api.naver.secreykey}")
    private String clientSecret;

    /**
     * 생성자에서 RestTemplateBuilder를 이용해서 restTemplate 초기화
     * @param builder
     */
    public NaverApiService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    /**
     * 네이버 API를 사용하여 아이템을 검색하는 메서드
     * <a href="https://developers.naver.com/docs/serviceapi/search/shopping/shopping.md#%EC%87%BC%ED%95%91-%EA%B2%80%EC%83%89-%EA%B2%B0%EA%B3%BC-%EC%A1%B0%ED%9A%8C">
     *     Naver API Documentation</a>
     * @param query 검색어
     * @return 검색 결과로 얻은 ItemDto 객체 리스트
     */
    public List<ItemDto> searchItems(String query) {
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("https://openapi.naver.com") // 기본경로
                .path("/v1/search/shop.json") // 경로 설정
                .queryParam("display", 15) // 쿼리 파라미터 설정: 한 페이지에 표시할 결과 수
                .queryParam("query", query) // 쿼리 파라미터 설정: 검색어
                .encode()// URI 인코딩
                .build()// URI 빌딩
                .toUri();// URI 객체로 변환
        log.info("uri = " + uri);

        // 요청 엔티티 생성 (GET 메서드, 헤더 포함)
        RequestEntity<Void> requestEntity = RequestEntity
                .get(uri)
                .header("X-Naver-Client-Id", clientId)
                .header("X-Naver-Client-Secret", clientSecret)
                .build();

        // 요청 보내고 응답 받기
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

        log.info("NAVER API Status Code : " + responseEntity.getStatusCode());

        // 응답 바디를 ItemDto 리스트로 변환해서 반환
        return fromJSONtoItems(responseEntity.getBody());
    }

    /**
     * JSON 응답을 ItemDto 객체 리스트로 변환하는 메서드
     * @param responseEntity: JSON 형식의 응답 문자열
     * @return ItemDto 객체 리스트
     */
    public List<ItemDto> fromJSONtoItems(String responseEntity) {
        // JSON 객체로 변환
        JSONObject jsonObject = new JSONObject(responseEntity);

        // // JSON 배열 추출
        JSONArray items  = jsonObject.getJSONArray("items");

        // ItemDto 객체 리스트 생성
        List<ItemDto> itemDtoList = new ArrayList<>();

        // JSON 배열을 순회하면서 ItemDto 객체 생성 및 리스트에 추가
        for (Object item : items) {
            ItemDto itemDto = new ItemDto((JSONObject) item);
            itemDtoList.add(itemDto);
        }
        // ItemDto 리스트 반환
        return itemDtoList;
    }
}