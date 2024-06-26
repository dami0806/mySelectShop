package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.entity.User;
import com.sparta.myselectshop.entity.UserRoleEnum;
import com.sparta.myselectshop.naver.dto.ItemDto;
import com.sparta.myselectshop.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public static final int MIN_MY_PRICE = 100;

    public ProductResponseDto createProduct(ProductRequestDto requestDto, User user) {
        Product product = productRepository.save(new Product(requestDto, user));
        return new ProductResponseDto(product);
    }

    @Transactional
    public ProductResponseDto updateProduct(long id, ProductMypriceRequestDto requestDto) {
        int myprice = requestDto.getMyprice();
        if (myprice < MIN_MY_PRICE) {
            throw new IllegalArgumentException("유효하지 않은 관심 가격입니다. 최소" + MIN_MY_PRICE + "원 이상으로 설정해 주세요.");
        }
        Product product = productRepository.findById(id).orElseThrow(
                () -> new NullPointerException("해당 상품을 찾을 수 없습니다.")
        );
        product.update(requestDto);
        return new ProductResponseDto(product);
    }

    // 상품 조회
    public Page<ProductResponseDto> getProducts(User user, int page, int size, String sortBy, boolean isAsc) {

        Sort.Direction direction = isAsc? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction,sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // admin이면
        UserRoleEnum userRoleEnum = user.getRole();
        Page<Product> productsList;

        if(userRoleEnum == UserRoleEnum.USER) {
            productsList = productRepository.findAllByUser(user, pageable);
        } else {
            productsList = productRepository.findAll(pageable);
        }

        return productsList.map(ProductResponseDto::new);
    }

    @Transactional
    public void updateBySearch(Long id, ItemDto itemDto) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new NullPointerException("해당 상품을 찾을 수 없습니다.")
        );
        product.updateByItemDto(itemDto);
    }

//    // 관리자의 모든 상품 보기
//    public List<ProductResponseDto> getAllProducts() {
//        List<Product> productList = productRepository.findAll();
//        List<ProductResponseDto> responseDtoList = new ArrayList<>(0);
//        for (Product product : productList) {
//            responseDtoList.add(new ProductResponseDto(product));
//        }
//        return responseDtoList;
//    }

}
